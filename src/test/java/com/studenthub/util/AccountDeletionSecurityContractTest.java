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
  assertTrue(source.contains("FOR UPDATE"));assertTrue(source.contains("rollback()"));assertTrue(source.contains("commit()"));
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
}
