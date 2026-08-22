<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Edit post | StudentHub</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script><link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet"></head><body class="auth-page"><main class="container py-5"><div class="auth-card card border-0 shadow-lg mx-auto"><div class="card-body p-4 p-md-5"><a href="${pageContext.request.contextPath}/announcements" class="text-decoration-none">← Back to announcements</a><h1 class="h2 mt-3">Edit announcement</h1><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/posts/edit">
    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
    <input type="hidden" name="id" value="${post.postId}">
    <div class="mb-3">
        <label class="form-label" for="categoryId">Category</label>
        <select class="form-select" id="categoryId" name="categoryId" required>
            <c:forEach var="category" items="${categories}">
                <option value="${category.categoryId}" data-name="<c:out value='${category.categoryName}' />" <c:if test="${post.categoryId eq category.categoryId}">selected</c:if>><c:out value="${category.categoryName}" /></option>
            </c:forEach>
        </select>
    </div>
    <div class="mb-3" id="deadlineContainer" style="display: none;">
        <label class="form-label" for="deadlineDate">Deadline <span class="text-danger" id="deadlineReqMark">*</span> <small class="text-secondary">(Required for Assignment / Tutorial / Exam)</small></label>
        <input type="datetime-local" class="form-control" id="deadlineDate" name="deadlineDate" value="<c:out value='${post.inputDueDate}' />">
    </div>
    <div class="mb-3">
        <label class="form-label" for="visibility">Visibility</label>
        <select class="form-select" id="visibility" name="visibility">
            <option value="ALL" ${post.visibility eq 'ALL'?'selected':''}>All students</option>
            <option value="SEMESTER" ${post.visibility eq 'SEMESTER'?'selected':''}>My semester</option>
            <option value="SECTION" ${post.visibility eq 'SECTION'?'selected':''}>My section</option>
        </select>
    </div>
    <div class="mb-3">
        <label class="form-label" for="title">Title</label>
        <input class="form-control" id="title" name="title" maxlength="200" value="<c:out value='${post.title}' />" required>
    </div>
    <div class="mb-4">
        <label class="form-label" for="content">Content</label>
        <textarea class="form-control" id="content" name="content" rows="8" maxlength="10000" required><c:out value="${post.content}" /></textarea>
    </div>
    <button class="btn btn-primary" type="submit">Save changes</button>
</form>
</div></div></main>
<script>
document.addEventListener('DOMContentLoaded', function() {
    var categorySelect = document.getElementById('categoryId');
    var deadlineContainer = document.getElementById('deadlineContainer');
    var deadlineInput = document.getElementById('deadlineDate');

    function updateDeadlineVisibility(isInitial) {
        var selectedOption = categorySelect.options[categorySelect.selectedIndex];
        var categoryName = selectedOption ? (selectedOption.getAttribute('data-name') || selectedOption.text || '').trim().toLowerCase() : '';
        var deadlineRequired = (categoryName === 'assignment' || categoryName === 'tutorial' || categoryName === 'exam');

        if (deadlineRequired) {
            deadlineContainer.style.display = 'block';
            deadlineInput.required = true;
        } else {
            deadlineContainer.style.display = 'none';
            deadlineInput.required = false;
            if (!isInitial) {
                deadlineInput.value = '';
            }
        }
    }

    categorySelect.addEventListener('change', function() { updateDeadlineVisibility(false); });
    updateDeadlineVisibility(true);
});
</script>
</body></html>
