<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Register | StudentHub</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"></head>
<body class="auth-page"><main class="container py-5"><div class="auth-card card border-0 shadow-lg mx-auto"><div class="card-body p-4 p-md-5">
<a class="text-decoration-none" href="${pageContext.request.contextPath}/home">StudentHub</a><h1 class="h2 mt-3">Create your student account</h1><p class="text-secondary">Use your UIT student ID and an email you can verify.</p>
<c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/register" novalidate>
<input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
<div class="mb-3"><label class="form-label" for="studentId">Student ID</label><input class="form-control" id="studentId" name="studentId" maxlength="8" pattern="TNT-[0-9]{4}" placeholder="TNT-0001" required></div>
<div class="mb-3"><label class="form-label" for="fullName">Full name</label><input class="form-control" id="fullName" name="fullName" maxlength="100" autocomplete="name" required></div>
<div class="mb-3"><label class="form-label" for="email">Email</label><input class="form-control" type="email" id="email" name="email" maxlength="120" autocomplete="email" required></div>
<div class="mb-3"><label class="form-label" for="password">Password</label><input class="form-control" type="password" id="password" name="password" minlength="8" maxlength="128" autocomplete="new-password" required><div class="form-text">At least 8 characters with uppercase, lowercase, and a number.</div></div>
<div class="mb-4"><label class="form-label" for="confirmPassword">Confirm password</label><input class="form-control" type="password" id="confirmPassword" name="confirmPassword" maxlength="128" autocomplete="new-password" required></div>
<button class="btn btn-primary w-100" type="submit">Create account</button></form><p class="mt-4 mb-0 text-center">Already registered? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
</div></div></main></body></html>
