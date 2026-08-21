<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Create Announcement | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="auth-page">
<main class="container py-5">
    <div class="auth-card create-post-card card border-0 shadow-lg mx-auto">
        <div class="card-body p-4 p-md-5">
            <a href="${pageContext.request.contextPath}/home" class="text-decoration-none">← Back to home</a>
            <h1 class="h2 mt-3">Create an announcement</h1>
            <p class="text-secondary">Share an official update with StudentHub students.</p>

            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}" /></div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/posts/create" enctype="multipart/form-data" novalidate>
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">

                <div class="mb-3">
                    <label class="form-label" for="categoryId">Category</label>
                    <select class="form-select" id="categoryId" name="categoryId" required>
                        <option value="">Select category</option>
                        <c:forEach var="category" items="${categories}">
                            <option value="${category.categoryId}" data-name="${category.categoryName}"><c:out value="${category.categoryName}" /></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="visibility">Visibility</label>
                    <select class="form-select" id="visibility" name="visibility" required>
                        <option value="ALL">All students</option>
                        <option value="SEMESTER">My semester</option>
                        <option value="SECTION">My section</option>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="title">Title</label>
                    <input class="form-control" id="title" name="title" maxlength="200" required>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="content">Content</label>
                    <textarea class="form-control" id="content" name="content" rows="6" maxlength="10000" required></textarea>
                </div>

                <div class="card p-3 mb-3 bg-body-tertiary border-0 rounded-3" id="deadlineContainer">
                    <h2 class="h6 mb-2">Announcement Deadline / Due Date</h2>
                    <div class="row g-2">
                        <div class="col-md-7">
                            <label class="form-label" for="dueDate">Due Date & Time <span id="deadlineReqBadge" class="badge bg-danger ms-1 d-none">Mandatory</span></label>
                            <input class="form-control" type="datetime-local" id="dueDate" name="dueDate">
                            <div class="form-text" id="deadlineHelp">Required for Assignment, Tutorial, and Exam announcements. Optional for General.</div>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label" for="subjectName">Subject / Course</label>
                            <input class="form-control" id="subjectName" name="subjectName" maxlength="100" placeholder="e.g. Data Structures">
                        </div>
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="attachment">Attachment (Image, Document, Video)</label>
                    <input class="form-control" type="file" id="attachment" name="attachment" accept="image/*,video/*,.pdf,.doc,.docx,.txt,.xls,.xlsx,.ppt,.pptx,.zip,.csv">
                    <div class="form-text">Supported: Images (PNG, JPG, GIF), Documents (PDF, DOCX, XLS, PPTX, ZIP), Videos (MP4, WEBM). Max 50MB.</div>
                </div>

                <button class="btn btn-primary w-100 py-2" type="submit">Publish Announcement</button>
            </form>
        </div>
    </div>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const categorySelect = document.getElementById('categoryId');
    const dueDateInput = document.getElementById('dueDate');
    const badge = document.getElementById('deadlineReqBadge');
    const mandatoryCategories = ['ASSIGNMENT', 'TUTORIAL', 'EXAM'];

    function checkDeadlineRequirement() {
        const selected = categorySelect.options[categorySelect.selectedIndex];
        const categoryName = (selected?.getAttribute('data-name') || selected?.text || '').trim().toUpperCase();
        if (mandatoryCategories.includes(categoryName)) {
            dueDateInput.required = true;
            badge.classList.remove('d-none');
        } else {
            dueDateInput.required = false;
            badge.classList.add('d-none');
        }
    }

    categorySelect.addEventListener('change', checkDeadlineRequirement);
    checkDeadlineRequirement();
});
</script>
</body>
</html>
