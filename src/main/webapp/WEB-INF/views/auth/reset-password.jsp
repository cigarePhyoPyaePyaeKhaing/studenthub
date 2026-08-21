<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Create new password | StudentHub</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><script src="${pageContext.request.contextPath}/assets/js/main.js?v=2" defer></script><script src="${pageContext.request.contextPath}/assets/js/password-recovery.js?v=2" defer></script><link href="${pageContext.request.contextPath}/assets/css/main.css?v=2" rel="stylesheet"></head><body class="auth-page"><main class="container py-5"><div class="auth-card card border-0 shadow-lg mx-auto"><div class="card-body p-4 p-md-5"><h1 class="h2">Create new password</h1><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if><form method="post"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><div class="mb-3"><label class="form-label" for="password">New password</label><div class="password-wrapper"><input class="form-control" type="password" id="password" name="password" minlength="8" maxlength="128" autocomplete="new-password" required><button type="button" class="toggle-password" data-input="password" aria-label="Toggle password visibility">👁</button></div></div><div class="mb-3"><label class="form-label" for="confirmPassword">Confirm password</label><div class="password-wrapper"><input class="form-control" type="password" id="confirmPassword" name="confirmPassword" maxlength="128" autocomplete="new-password" required><button type="button" class="toggle-password" data-input="confirmPassword" aria-label="Toggle password visibility">👁</button></div></div><ul class="password-requirements text-secondary"><li>At least 8 characters</li><li>Uppercase and lowercase</li><li>At least one number</li></ul><button class="btn btn-primary w-100" type="submit">Reset password</button></form></div></div></main>
<script>
document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".toggle-password").forEach(function(button) {
        button.addEventListener("click", function(e) {
            e.preventDefault();
            var input = document.getElementById(button.dataset.input);
            if (!input) return;
            if (input.type === "password") {
                input.type = "text";
                button.textContent = "🙈";
                button.setAttribute("aria-label", "Hide password");
            } else {
                input.type = "password";
                button.textContent = "👁";
                button.setAttribute("aria-label", "Show password");
            }
        });
    });
});
</script>
</body></html>
