package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

class AccountDeletionSecurityContractTest {
 @Test void endpointIsPostOnlySessionBoundAndCsrfProtected()throws Exception{
  String source=Files.readString(Path.of("src/main/java/com/studenthub/controller/DeleteAccountServlet.java"));
  assertTrue(source.contains("urlPatterns=\"/profile/delete-account\""));
  assertTrue(source.contains("CsrfToken.isValid(request)"));
  assertTrue(source.contains("session.getAttribute(\"userId\")"));
  assertFalse(source.contains("getParameter(\"userId\")"));
  assertTrue(source.contains("session.invalidate()"));
  assertTrue(source.contains("ACCOUNT_DELETE_UNAUTHENTICATED"));
 }
 @Test void profileRequiresPasswordAndHasNoGetDeletionLink()throws Exception{
  String jsp=Files.readString(Path.of("src/main/webapp/WEB-INF/views/profile.jsp"));
  assertTrue(jsp.contains("type=\"password\""));assertTrue(jsp.contains("name=\"currentPassword\""));
  assertTrue(jsp.contains("name=\"csrfToken\""));assertTrue(jsp.contains("method=\"post\""));
  assertFalse(jsp.contains("href=\"${pageContext.request.contextPath}/profile/delete-account"));
 }
 @Test void sharedContentIsAnonymizedNotCascadedByService()throws Exception{
  String source=Files.readString(Path.of("src/main/java/com/studenthub/service/AccountDeletionService.java"));
  assertTrue(source.contains("full_name='Deleted User'"));
  assertFalse(source.contains("DELETE FROM users"));
  assertFalse(source.contains("DELETE FROM private_conversations"));
  assertFalse(source.contains("DELETE FROM private_messages"));
  assertFalse(source.contains("UPDATE universities"));
  assertFalse(source.contains("DELETE FROM universities"));
  assertTrue(source.contains("FOR UPDATE"));assertTrue(source.contains("rollback()"));assertTrue(source.contains("commit()"));
 }
 @Test void everyCleanupTableAndColumnMatchesAnActiveSchemaSource()throws Exception{
  String service=Files.readString(Path.of("src/main/java/com/studenthub/service/AccountDeletionService.java"));
  String schema=Files.readString(Path.of("database/schema.sql"));
  String runtime=Files.readString(Path.of("src/main/java/com/studenthub/util/ApplicationConfigurationListener.java"));
  String privateMessaging=Files.readString(Path.of("database/migrations/V9__private_messaging.sql"));
  String visibility=Files.readString(Path.of("database/migrations/V11__private_conversation_visibility.sql"));
  assertCleanupContract(service,schema,"verification_codes","user_id");
  assertCleanupContract(service,schema,"notification_reads","user_id");
  assertCleanupContract(service,privateMessaging,"private_message_reads","user_id");
  assertCleanupContract(service,visibility+runtime,"private_conversation_visibility","user_id");
  assertCleanupContract(service,schema,"reactions","user_id");
  assertCleanupContract(service,schema+runtime,"academic_change_requests","user_id");
  assertCleanupContract(service,schema+runtime,"academic_change_requests","reviewed_by");
  assertCleanupContract(service,schema,"notifications","target_user_id");
  assertFalse(service.contains("universities\", \"UPDATE"));
 }
 @Test void clientUsesExplicitUrlEncodedCsrfAndClassifiesEveryFailureStage()throws Exception{
  String js=Files.readString(Path.of("src/main/webapp/assets/js/account-deletion.js"));
  assertTrue(js.contains("new URLSearchParams()"));
  assertTrue(js.contains("body.set(\"currentPassword\",password.value)"));
  assertTrue(js.contains("body.set(\"csrfToken\",csrf.value)"));
  assertFalse(js.contains("new FormData(form)"));
  assertTrue(js.contains("application/x-www-form-urlencoded"));
  assertTrue(js.contains("ACCOUNT_DELETE_BEFORE_FETCH"));assertTrue(js.contains("ACCOUNT_DELETE_AFTER_FETCH"));
  assertTrue(js.contains("ACCOUNT_DELETE_BEFORE_PARSE"));assertTrue(js.contains("ACCOUNT_DELETE_AFTER_PARSE"));
  assertTrue(js.contains("ACCOUNT_DELETE_NETWORK_FAILED"));assertTrue(js.contains("ACCOUNT_DELETE_RESPONSE_INVALID"));assertTrue(js.contains("ACCOUNT_DELETE_CLIENT_ERROR"));
 }
 @Test void deletedTombstonesAreExcludedFromActiveAdminUsers()throws Exception{
  String dao=Files.readString(Path.of("src/main/java/com/studenthub/dao/AdminDAO.java"));
  assertTrue(dao.contains("email NOT LIKE 'deleted-%@invalid.studenthub'"));
  assertTrue(dao.contains("baseUserSelect() + \" AND user_id=?\""));
 }
 @Test void deletionGuaranteesSchemaDriftProtectionAndFineGrainedDiagnostics()throws Exception{
  String service=Files.readString(Path.of("src/main/java/com/studenthub/service/AccountDeletionService.java"));
  assertTrue(service.contains("columnExists"));
  assertTrue(service.contains("tableExists"));
  assertTrue(service.contains("users_anonymize"));
  assertTrue(service.contains("account_lock"));
  assertTrue(service.contains("admin_lock"));
  assertTrue(service.contains("dependent_cleanup"));
  assertFalse(service.contains("requested_by"));
  assertFalse(service.contains("approved_by"));
 }
 private void assertCleanupContract(String service,String schemaSource,String table,String column){
  assertTrue(service.contains(table),"Deletion plan must name "+table);
  assertTrue(service.contains(column+"=?"),"Deletion plan must scope "+table+" by "+column);
  assertTrue(schemaSource.contains(table),"Schema source must define "+table);
  assertTrue(schemaSource.contains(column),"Schema source must define "+table+"."+column);
 }
}
