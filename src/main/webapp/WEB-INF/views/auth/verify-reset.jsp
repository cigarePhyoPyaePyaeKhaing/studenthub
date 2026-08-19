<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Verify your identity | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css?v=otp2" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/password-recovery.js?v=otp2" defer></script>
</head>
<body class="auth-page recovery-page">
<main class="container">
    <section class="auth-card recovery-card card border-0 shadow-lg mx-auto" aria-labelledby="verification-title">
        <div class="card-body p-4 p-md-5 text-center">
            <h1 class="h2 mb-2" id="verification-title">Verify your identity</h1>
            <p class="recovery-copy text-secondary mb-4">
                We sent a 6-digit verification code
                <c:choose>
                    <c:when test="${not empty maskedEmail}">to <strong><c:out value="${maskedEmail}"/></strong></c:when>
                    <c:otherwise>to your email address</c:otherwise>
                </c:choose>.
            </p>

            <c:if test="${not empty message}"><div class="alert alert-info text-start"><c:out value="${message}"/></div></c:if>

            <form method="post" data-otp-form novalidate>
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="code" data-otp-value>
                <fieldset class="otp-fieldset">
                    <legend class="visually-hidden">Six-digit verification code</legend>
                    <div class="otp-code-grid" role="group" aria-label="Six-digit verification code">
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="one-time-code" aria-label="Digit 1" autofocus>
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="off" aria-label="Digit 2">
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="off" aria-label="Digit 3">
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="off" aria-label="Digit 4">
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="off" aria-label="Digit 5">
                        <input class="otp-code-input" type="text" inputmode="numeric" pattern="[0-9]" maxlength="1" autocomplete="off" aria-label="Digit 6">
                    </div>
                    <c:if test="${not empty error}"><p class="otp-error" role="alert"><c:out value="${error}"/></p></c:if>
                </fieldset>
                <button class="btn btn-primary verify-code-button" type="submit" data-verify-button disabled>Verify code</button>
            </form>

            <form method="post" class="resend-form" data-resend-form>
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="resend">
                <span>Didn't receive the code?</span>
                <button class="resend-button" type="submit" data-resend-button data-resend-seconds="<c:out value='${resendSeconds}'/>" disabled>
                    Resend in <span data-resend-countdown></span>
                </button>
            </form>

            <form method="post" class="different-account-form">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="cancel">
                <button class="btn btn-link" type="submit">← Use a different account</button>
            </form>
        </div>
    </section>
</main>
</body>
</html>
