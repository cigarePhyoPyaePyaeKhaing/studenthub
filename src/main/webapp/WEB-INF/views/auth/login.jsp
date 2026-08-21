<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Sign in | StudentHub</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><script src="${pageContext.request.contextPath}/assets/js/main.js"></script><link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"></head>
<body class="auth-page"><main class="container py-5"><div class="auth-card card border-0 shadow-lg mx-auto"><div class="card-body p-4 p-md-5">
<a class="text-decoration-none" href="${pageContext.request.contextPath}/home">StudentHub</a><h1 class="h2 mt-3">Welcome back</h1><p class="text-secondary">Sign in using your student ID or email.</p>
<c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}"/></div></c:if><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/login"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
<div class="mb-3"><label class="form-label" for="login">Student ID or email</label><input class="form-control" id="login" name="login" maxlength="120" autocomplete="username" required></div>
<div class="mb-2"><label class="form-label" for="password">Password</label>
<div class="password-field"><input class="form-control" type="password" id="password" name="password" maxlength="128" autocomplete="current-password" required>
<button type="button" class="password-toggle" data-password-toggle="password" aria-label="Show password"></button></div></div>
<div class="text-end mb-4"><a href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a></div>
<button class="btn btn-primary w-100" type="submit">Sign in</button></form><p class="mt-4 mb-0 text-center">New to StudentHub? <a href="${pageContext.request.contextPath}/register">Register</a></p>
</div></div></main></body></html>
