package com.studenthub.service;

import com.studenthub.util.DBConnection;
import com.studenthub.util.PasswordUtil;
import java.sql.*;
import java.util.UUID;

/** Atomically removes personal account data while retaining anonymized shared history. */
public class AccountDeletionService {
    @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
    @FunctionalInterface public interface PasswordVerifier { boolean matches(String password, String hash); }
    private final Connections connections;
    private final PasswordVerifier passwords;

    public AccountDeletionService() { this(DBConnection::getConnection, PasswordUtil::matches); }
    public AccountDeletionService(Connections connections, PasswordVerifier passwords) {
        this.connections = connections; this.passwords = passwords;
    }

    public Result deleteOwnAccount(long userId, String currentPassword) throws SQLException {
        if (userId <= 0 || currentPassword == null || currentPassword.isBlank()) return Result.passwordInvalid();
        try (Connection connection = connections.open()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Account account = lockAccount(connection, userId);
                if (account == null) { connection.rollback(); return new Result(false, "ACCOUNT_DELETE_CONFLICT", null); }
                if (!passwords.matches(currentPassword, account.passwordHash())) {
                    connection.rollback(); return Result.passwordInvalid();
                }
                if ("ADMIN".equals(account.role()) && countAdmins(connection) <= 1) {
                    connection.rollback(); return new Result(false, "ACCOUNT_DELETE_LAST_ADMIN", null);
                }

                // Owned personal state. Shared authored content and private conversations remain intact.
                executeRequired(connection, "verification_codes", "DELETE FROM verification_codes WHERE user_id=?", userId);
                executeOptional(connection, "notification_reads", "DELETE FROM notification_reads WHERE user_id=?", userId);
                executeOptional(connection, "private_message_reads", "DELETE FROM private_message_reads WHERE user_id=?", userId);
                executeOptional(connection, "private_conversation_visibility", "DELETE FROM private_conversation_visibility WHERE user_id=?", userId);
                executeOptional(connection, "reactions", "DELETE FROM reactions WHERE user_id=?", userId);
                executeOptional(connection, "academic_change_requests", "DELETE FROM academic_change_requests WHERE user_id=?", userId);
                executeOptional(connection, "academic_change_requests", "UPDATE academic_change_requests SET reviewed_by=NULL WHERE reviewed_by=?", userId);
                executeOptional(connection, "universities", "UPDATE universities SET requested_by=NULL WHERE requested_by=?", userId);
                executeOptional(connection, "universities", "UPDATE universities SET approved_by=NULL WHERE approved_by=?", userId);
                executeOptional(connection, "notifications", "DELETE FROM notifications WHERE target_user_id=?", userId);

                String token = UUID.randomUUID().toString().replace("-", "");
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE users SET username=NULL,student_id=NULL,email=?,password_hash=?,full_name='Deleted User',
                        role='STUDENT',email_verified=FALSE,google_sub=NULL,university_id=NULL,
                        university_locked=FALSE,academic_info_locked=FALSE,semester=NULL,section_name=NULL,
                        last_active_at=NULL,profile_image=NULL WHERE user_id=?""")) {
                    statement.setString(1, "deleted-" + token + "@invalid.studenthub");
                    statement.setString(2, PasswordUtil.hash(UUID.randomUUID().toString()));
                    statement.setLong(3, userId);
                    if (statement.executeUpdate() != 1) throw new SQLException("Account changed during deletion", "40001");
                } catch (SQLException exception) { throw databaseFailure("users_anonymize", "users", exception); }
                connection.commit();
                return new Result(true, "ACCOUNT_DELETE_OK", account.profileImage());
            } catch (SQLException | RuntimeException exception) {
                try { connection.rollback(); } catch (SQLException rollback) { exception.addSuppressed(rollback); }
                throw exception;
            } finally {
                try { connection.setAutoCommit(oldAutoCommit); } catch (SQLException ignored) { }
            }
        }
    }

    private Account lockAccount(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT password_hash,role,profile_image FROM users WHERE user_id=? FOR UPDATE")) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? new Account(results.getString(1), results.getString(2), results.getString(3)) : null;
            }
        } catch(SQLException exception){throw databaseFailure("account_lock","users",exception);}
    }
    private int countAdmins(Connection connection) throws SQLException {
        int count=0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM users WHERE role='ADMIN' FOR UPDATE");
             ResultSet results = statement.executeQuery()) { while(results.next()) count++; }
        catch(SQLException exception){throw databaseFailure("admin_lock","users",exception);}
        return count;
    }
    private void executeOptional(Connection connection,String table,String sql,long userId)throws SQLException {
        if(tableExists(connection,table))executeRequired(connection,table,sql,userId);
    }
    private boolean tableExists(Connection connection,String table)throws SQLException {
        try(PreparedStatement statement=connection.prepareStatement("SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")){
            statement.setString(1,table);try(ResultSet results=statement.executeQuery()){return results.next();}
        }catch(SQLException exception){throw databaseFailure("schema_check",table,exception);}
    }
    private void executeRequired(Connection connection,String table,String sql,long userId)throws SQLException {
        try(PreparedStatement statement=connection.prepareStatement(sql)){statement.setLong(1,userId);statement.executeUpdate();}
        catch(SQLException exception){throw databaseFailure("dependent_cleanup",table,exception);}
    }
    private AccountDeletionDatabaseException databaseFailure(String stage,String table,SQLException cause){
        return new AccountDeletionDatabaseException(stage,table,cause);
    }
    private record Account(String passwordHash, String role, String profileImage) { }
    public record Result(boolean success, String code, String profileImage) {
        static Result passwordInvalid() { return new Result(false, "ACCOUNT_DELETE_PASSWORD_INVALID", null); }
    }
    public static final class AccountDeletionDatabaseException extends SQLException {
        private final String stage;private final String table;
        public AccountDeletionDatabaseException(String stage,String table,SQLException cause){super("Account deletion database failure",cause.getSQLState(),cause.getErrorCode(),cause);this.stage=stage;this.table=table;}
        public String stage(){return stage;}public String table(){return table;}
    }
}
