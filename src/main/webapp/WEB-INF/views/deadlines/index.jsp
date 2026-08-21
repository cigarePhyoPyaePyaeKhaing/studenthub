<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Deadlines | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css" rel="stylesheet">
</head>
<body class="dashboard-body">
<header class="mobile-header d-lg-none">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation">
    <div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div>
    <div class="offcanvas-body"><jsp:include page="../partials/sidebar.jsp" /></div>
</div>

<div class="dashboard-shell deadlines-shell">
    <aside class="dashboard-sidebar d-none d-lg-flex"><jsp:include page="../partials/sidebar.jsp" /></aside>
    <main class="feed-column">
        <section class="welcome-panel">
            <div>
                <p class="eyebrow mb-2">Academic planning</p>
                <h1>Deadlines & Schedule</h1>
                <p class="mb-0 text-secondary">Assignments, exams, and important academic due dates for your semester.</p>
            </div>
            <div class="d-flex gap-2 align-items-center">
                <c:if test="${canCreateDeadline}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/deadlines/create">Create Deadline</a>
                </c:if>
            </div>
        </section>

        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="view-toggle-wrap">
                <button type="button" class="view-toggle-btn active" id="btnCalendarView" onclick="switchView('calendar')">📅 Calendar View</button>
                <button type="button" class="view-toggle-btn" id="btnListView" onclick="switchView('list')">📋 List View</button>
            </div>
        </div>

        <!-- CALENDAR VIEW -->
        <div id="calendarViewSection" class="calendar-wrap">
            <div class="calendar-toolbar">
                <div class="calendar-nav">
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="prevMonth()">‹</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="todayMonth()">Today</button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="nextMonth()">›</button>
                </div>
                <h2 class="calendar-month-title" id="calendarMonthTitle"></h2>
                <div class="d-none d-md-block text-secondary small">Click an event to view full details</div>
            </div>
            <div class="calendar-grid">
                <div class="calendar-day-head">Sun</div>
                <div class="calendar-day-head">Mon</div>
                <div class="calendar-day-head">Tue</div>
                <div class="calendar-day-head">Wed</div>
                <div class="calendar-day-head">Thu</div>
                <div class="calendar-day-head">Fri</div>
                <div class="calendar-day-head">Sat</div>
            </div>
            <div id="calendarDaysGrid" class="calendar-grid"></div>
        </div>

        <!-- LIST VIEW -->
        <div id="listViewSection" style="display:none;">
            <section>
                <div class="feed-heading">
                    <div>
                        <p class="eyebrow mb-1">Next up</p>
                        <h2 class="h3 mb-0">Upcoming deadlines</h2>
                    </div>
                </div>
                <div class="deadline-card-grid">
                    <c:choose>
                        <c:when test="${empty upcomingDeadlines}">
                            <div class="empty-state"><div class="empty-icon">D</div><h3>No upcoming deadlines.</h3><p>New academic deadlines will appear here.</p></div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="deadline" items="${upcomingDeadlines}">
                                <article class="deadline-card">
                                    <div class="deadline-card-top">
                                        <span class="deadline-status status-${deadline.status eq 'Due soon'?'soon':'upcoming'}"><c:out value="${deadline.status}" /></span>
                                        <span class="deadline-scope"><c:out value="${deadline.scopeLabel}" /></span>
                                    </div>
                                    <h3><c:out value="${deadline.title}" /></h3>
                                    <p class="deadline-subject"><c:out value="${deadline.subjectName}" /></p>
                                    <time>Due <c:out value="${deadline.dueLabel}" /></time>
                                    <p class="deadline-creator">Created by <c:out value="${deadline.creatorName}" /></p>
                                    <c:if test="${not empty deadline.relatedPostTitle}">
                                        <p class="related-post"><a href="${pageContext.request.contextPath}/posts/comments?postId=${deadline.postId}">Announcement: <c:out value="${deadline.relatedPostTitle}" /></a></p>
                                    </c:if>
                                    <c:if test="${sessionScope.role eq 'ADMIN' or (sessionScope.role eq 'CR' and sessionScope.userId eq deadline.createdBy)}">
                                        <div class="deadline-actions">
                                            <a href="${pageContext.request.contextPath}/deadlines/edit?id=${deadline.deadlineId}">Edit</a>
                                            <form method="post" action="${pageContext.request.contextPath}/deadlines/delete" onsubmit="return confirm('Are you sure you want to delete this deadline?');">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                                <input type="hidden" name="id" value="${deadline.deadlineId}">
                                                <button type="submit">Delete</button>
                                            </form>
                                        </div>
                                    </c:if>
                                </article>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <c:if test="${not empty pastDeadlines}">
                <section class="past-section">
                    <div class="feed-heading">
                        <div>
                            <p class="eyebrow mb-1">Archive</p>
                            <h2 class="h3 mb-0">Past deadlines</h2>
                        </div>
                    </div>
                    <div class="deadline-card-grid">
                        <c:forEach var="deadline" items="${pastDeadlines}">
                            <article class="deadline-card expired">
                                <div class="deadline-card-top">
                                    <span class="deadline-status status-expired">Expired</span>
                                    <span class="deadline-scope"><c:out value="${deadline.scopeLabel}" /></span>
                                </div>
                                <h3><c:out value="${deadline.title}" /></h3>
                                <p class="deadline-subject"><c:out value="${deadline.subjectName}" /></p>
                                <time>Was due <c:out value="${deadline.dueLabel}" /></time>
                                <c:if test="${not empty deadline.relatedPostTitle}">
                                    <p class="related-post"><a href="${pageContext.request.contextPath}/posts/comments?postId=${deadline.postId}">Announcement: <c:out value="${deadline.relatedPostTitle}" /></a></p>
                                </c:if>
                                <c:if test="${sessionScope.role eq 'ADMIN' or (sessionScope.role eq 'CR' and sessionScope.userId eq deadline.createdBy)}">
                                    <div class="deadline-actions">
                                        <a href="${pageContext.request.contextPath}/deadlines/edit?id=${deadline.deadlineId}">Edit</a>
                                        <form method="post" action="${pageContext.request.contextPath}/deadlines/delete" onsubmit="return confirm('Are you sure you want to delete this deadline?');">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                            <input type="hidden" name="id" value="${deadline.deadlineId}">
                                            <button type="submit">Delete</button>
                                        </form>
                                    </div>
                                </c:if>
                            </article>
                        </c:forEach>
                    </div>
                </section>
            </c:if>
        </div>
    </main>
</div>

<!-- Modal for Deadline Details -->
<div class="modal fade" id="deadlineDetailModal" tabindex="-1" aria-labelledby="modalDeadlineTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg" style="background:var(--surface-glass); backdrop-filter:blur(24px);">
            <div class="modal-header border-bottom-0 pb-0">
                <h2 class="modal-title h5" id="modalDeadlineTitle">Deadline Details</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <span id="modalDeadlineStatus" class="deadline-status"></span>
                    <span id="modalDeadlineScope" class="deadline-scope ms-1"></span>
                </div>
                <h3 class="h4 mb-2" id="modalDeadlineName"></h3>
                <p class="text-secondary mb-3" id="modalDeadlineSubject"></p>
                <div class="p-3 rounded-3 bg-body-tertiary mb-3">
                    <div><strong>Due Date:</strong> <span id="modalDeadlineDue"></span></div>
                    <div class="mt-1"><strong>Created By:</strong> <span id="modalDeadlineCreator"></span></div>
                </div>
                <div id="modalAnnouncementLinkWrap" class="mb-3 d-none">
                    <a id="modalAnnouncementLink" href="#" class="btn btn-sm btn-outline-primary w-100">📢 View Linked Announcement</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
const deadlineEvents = [
<c:forEach var="dl" items="${allDeadlines}" varStatus="status">
    {
        id: ${dl.deadlineId},
        title: "<c:out value='${dl.title}' />",
        subject: "<c:out value='${dl.subjectName}' />",
        due: "${dl.inputDueDate}",
        dueLabel: "<c:out value='${dl.dueLabel}' />",
        status: "<c:out value='${dl.status}' />",
        scope: "<c:out value='${dl.scopeLabel}' />",
        creator: "<c:out value='${dl.creatorName}' />",
        postId: "${dl.postId != null ? dl.postId : ''}",
        postTitle: "<c:out value='${dl.relatedPostTitle != null ? dl.relatedPostTitle : ""}' />"
    }<c:if test="${!status.last}">,</c:if>
</c:forEach>
];

let currentDate = new Date();
let currentYear = currentDate.getFullYear();
let currentMonth = currentDate.getMonth();

function switchView(view) {
    const calSec = document.getElementById('calendarViewSection');
    const listSec = document.getElementById('listViewSection');
    const calBtn = document.getElementById('btnCalendarView');
    const listBtn = document.getElementById('btnListView');

    if (view === 'calendar') {
        calSec.style.display = 'block';
        listSec.style.display = 'none';
        calBtn.classList.add('active');
        listBtn.classList.remove('active');
    } else {
        calSec.style.display = 'none';
        listSec.style.display = 'block';
        calBtn.classList.remove('active');
        listBtn.classList.add('active');
    }
}

function prevMonth() {
    currentMonth--;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    renderCalendar();
}

function nextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    renderCalendar();
}

function todayMonth() {
    const today = new Date();
    currentYear = today.getFullYear();
    currentMonth = today.getMonth();
    renderCalendar();
}

function renderCalendar() {
    const monthNames = ["January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"];
    document.getElementById('calendarMonthTitle').textContent = monthNames[currentMonth] + " " + currentYear;

    const daysGrid = document.getElementById('calendarDaysGrid');
    daysGrid.innerHTML = '';

    const firstDayIndex = new Date(currentYear, currentMonth, 1).getDay();
    const lastDayOfMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const prevMonthLastDay = new Date(currentYear, currentMonth, 0).getDate();

    const today = new Date();
    const isCurrentMonth = today.getFullYear() === currentYear && today.getMonth() === currentMonth;

    // Previous month filler days
    for (let i = firstDayIndex; i > 0; i--) {
        const dayNum = prevMonthLastDay - i + 1;
        const cell = document.createElement('div');
        cell.className = 'calendar-cell other-month';
        cell.innerHTML = '<div class="day-header"><span class="day-number">' + dayNum + '</span></div>';
        daysGrid.appendChild(cell);
    }

    // Days of current month
    for (let day = 1; day <= lastDayOfMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-cell';
        if (isCurrentMonth && today.getDate() === day) {
            cell.classList.add('today');
        }

        const dateStr = currentYear + '-' + String(currentMonth + 1).padStart(2, '0') + '-' + String(day).padStart(2, '0');
        const dayDeadlines = deadlineEvents.filter(ev => ev.due.startsWith(dateStr));

        let eventsHtml = '<div class="day-events">';
        dayDeadlines.forEach(ev => {
            const isUrgent = ev.status === 'Due soon';
            eventsHtml += '<span class="event-chip ' + (isUrgent ? 'chip-urgent' : '') + '" onclick="showDeadlineModal(' + ev.id + ')" title="' + ev.subject + ': ' + ev.title + '">' + ev.title + '</span>';
        });
        eventsHtml += '</div>';

        cell.innerHTML = '<div class="day-header"><span class="day-number">' + day + '</span></div>' + eventsHtml;
        daysGrid.appendChild(cell);
    }

    // Next month filler days to complete grid
    const totalCells = firstDayIndex + lastDayOfMonth;
    const remaining = (7 - (totalCells % 7)) % 7;
    for (let j = 1; j <= remaining; j++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-cell other-month';
        cell.innerHTML = '<div class="day-header"><span class="day-number">' + j + '</span></div>';
        daysGrid.appendChild(cell);
    }
}

function showDeadlineModal(deadlineId) {
    const ev = deadlineEvents.find(e => e.id === deadlineId);
    if (!ev) return;

    document.getElementById('modalDeadlineName').textContent = ev.title;
    document.getElementById('modalDeadlineSubject').textContent = ev.subject;
    document.getElementById('modalDeadlineDue').textContent = ev.dueLabel;
    document.getElementById('modalDeadlineCreator').textContent = ev.creator;
    document.getElementById('modalDeadlineScope').textContent = ev.scope;

    const statusEl = document.getElementById('modalDeadlineStatus');
    statusEl.textContent = ev.status;
    statusEl.className = 'deadline-status ' + (ev.status === 'Due soon' ? 'status-soon' : (ev.status === 'Expired' ? 'status-expired' : 'status-upcoming'));

    const linkWrap = document.getElementById('modalAnnouncementLinkWrap');
    const linkEl = document.getElementById('modalAnnouncementLink');
    if (ev.postId && ev.postId.trim() !== '') {
        linkWrap.classList.remove('d-none');
        linkEl.href = '${pageContext.request.contextPath}/posts/comments?postId=' + ev.postId;
    } else {
        linkWrap.classList.add('d-none');
    }

    const modal = new bootstrap.Modal(document.getElementById('deadlineDetailModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', renderCalendar);
</script>
</body>
</html>
