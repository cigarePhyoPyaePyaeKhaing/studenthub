document.addEventListener("DOMContentLoaded", () => {
    const panel = document.querySelector(".conversation-list");
    if (panel) initializePeopleSearch(panel);
    const form = document.querySelector(".private-composer");
    const list = document.querySelector(".private-message-list");
    if (!form || !list) return;
    initializeChat(form, list);
});

function initializePeopleSearch(panel) {
    const input = panel.querySelector(".people-search input");
    const results = panel.querySelector(".people-results");
    if (!input || !results) return;
    let timer;
    let request;
    input.addEventListener("input", () => {
        clearTimeout(timer);
        request?.abort();
        const query = input.value.trim();
        panel.classList.toggle("searching", query.length > 0);
        if (query.length < 2) {
            results.hidden = true;
            results.replaceChildren();
            return;
        }
        timer = setTimeout(async () => {
            request = new AbortController();
            try {
                const response = await fetch(`${panel.dataset.searchUrl}?q=${encodeURIComponent(query)}`, {signal: request.signal});
                const users = response.ok ? await response.json() : [];
                results.replaceChildren();
                if (!users.length) {
                    const empty = document.createElement("p");
                    empty.className = "search-empty";
                    empty.textContent = "No users found.";
                    results.append(empty);
                }
                users.forEach(user => results.append(createSearchResult(panel, user)));
                results.hidden = false;
            } catch (error) {
                if (error.name !== "AbortError") results.hidden = true;
            }
        }, 320);
    });
}

function createSearchResult(panel, user) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "people-result";
    button.innerHTML = '<span class="search-avatar"><span class="avatar-fallback"></span></span><span><strong></strong><small></small></span>';
    button.querySelector(".avatar-fallback").textContent = user.fullName.charAt(0).toUpperCase();
    if (user.avatarUrl) {
        const image = document.createElement("img");
        image.src = user.avatarUrl;
        image.alt = "";
        image.addEventListener("error", () => image.remove());
        button.querySelector(".search-avatar").append(image);
    }
    button.querySelector("strong").textContent = user.fullName;
    button.querySelector("small").textContent = [user.studentId, user.role, user.presenceLabel].filter(Boolean).join(" · ");
    button.addEventListener("click", async () => {
        button.disabled = true;
        try {
            const response = await fetch(panel.dataset.startUrl, {
                method: "POST",
                headers: {"Content-Type": "application/x-www-form-urlencoded"},
                body: new URLSearchParams({csrfToken: panel.dataset.csrf, targetUserId: user.userId})
            });
            if (response.ok) location.href = response.url;
            else button.disabled = false;
        } catch (_ignored) {
            button.disabled = false;
        }
    });
    return button;
}

function initializeChat(form, list) {
    const field = form.querySelector("textarea");
    const fileInput = form.querySelector("[type=file]");
    const send = form.querySelector("button[type=submit]");
    const composer = window.StudentHubMessageComposer?.initialize(form);
    const csrf = list.dataset.csrf;
    let last = Math.max(0, ...[...list.querySelectorAll("[data-message-id]")].map(element => +element.dataset.messageId));
    let pending;
    let xhr;
    const size = bytes => bytes < 1048576 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1048576).toFixed(1)} MB`;

    function mark(element, status) {
        let receipt = element.querySelector(".message-receipt");
        if (!receipt) {
            receipt = document.createElement("span");
            receipt.className = "message-receipt";
            element.querySelector(".message-meta").append(receipt);
        }
        receipt.dataset.status = status;
        receipt.textContent = status === "SENDING" ? "◷" : status === "SENT" ? "✓" : status === "DELIVERED" || status === "SEEN" ? "✓✓" : "!";
        receipt.title = status;
    }

    function media(element, data) {
        if (!data.attachmentId) return;
        let box = element.querySelector(".private-file");
        if (!box) {
            box = document.createElement("div");
            box.className = "private-file";
            element.querySelector(".message-meta").before(box);
        }
        box.replaceChildren();
        let view;
        if (data.attachmentType === "IMAGE") {
            view = document.createElement("img"); view.src = data.previewUrl; view.alt = data.originalFilename || "Image attachment";
        } else if (data.attachmentType === "VIDEO") {
            view = document.createElement("video"); view.src = data.previewUrl; view.controls = true; view.preload = "metadata";
        } else if (data.attachmentType === "AUDIO") {
            view = document.createElement("audio"); view.src = data.previewUrl; view.controls = true; view.preload = "metadata";
        } else {
            view = document.createElement("strong"); view.textContent = data.originalFilename;
        }
        const details = document.createElement("small");
        details.textContent = `${data.originalFilename} · ${size(data.fileSize)}`;
        const link = document.createElement("a");
        link.href = data.downloadUrl; link.textContent = "Download";
        box.append(view, details, link);
    }

    function bubble(data, own, temporary = false) {
        const old = !temporary && list.querySelector(`[data-message-id="${data.messageId}"]`);
        if (old) { mark(old, data.status || "SENT"); return old; }
        const nearBottom = list.scrollHeight - list.scrollTop - list.clientHeight < 100;
        const element = document.createElement("article");
        element.className = `private-bubble ${own ? "outgoing" : "incoming"}`;
        if (!temporary) element.dataset.messageId = data.messageId;
        element.innerHTML = '<p></p><div class="message-meta"><time></time></div>';
        element.querySelector("p").textContent = data.message || "";
        element.querySelector("time").textContent = data.createdLabel || "";
        media(element, data);
        if (own) mark(element, data.status || "SENDING");
        list.append(element);
        if (!temporary) last = Math.max(last, +data.messageId);
        if (own || temporary || nearBottom) list.scrollTop = list.scrollHeight;
        else if (!document.querySelector(".new-message-indicator")) {
            const indicator = document.createElement("button");
            indicator.className = "new-message-indicator"; indicator.textContent = "New messages";
            indicator.addEventListener("click", () => { list.scrollTop = list.scrollHeight; indicator.remove(); });
            list.after(indicator);
        }
        return element;
    }

    function uploadCard(element, file) {
        const box = document.createElement("div");
        box.className = "upload-state";
        box.innerHTML = '<strong></strong><small></small><span>Uploading… 0%</span><progress max="100" value="0"></progress><div><button type="button">Cancel</button></div>';
        box.querySelector("strong").textContent = file.name;
        box.querySelector("small").textContent = size(file.size);
        box.querySelector("button").addEventListener("click", () => xhr?.abort());
        element.querySelector(".message-meta").before(box);
        return box;
    }

    function resetComposer() {
        field.value = "";
        composer?.resetHeight();
        composer?.clearAttachment();
    }

    function fail(message) {
        mark(pending.element, "FAILED");
        let actions;
        if (pending.upload) {
            pending.upload.querySelector("span").textContent = message || "Upload failed";
            pending.upload.querySelector("progress")?.remove();
            actions = pending.upload.querySelector("div"); actions.replaceChildren();
        } else {
            actions = document.createElement("div"); actions.className = "failed-message-actions"; pending.element.append(actions);
        }
        for (const [label, action] of [["Retry", sendPending], ["Remove", () => { pending.element.remove(); pending = null; }]]) {
            const button = document.createElement("button");
            button.type = "button"; button.textContent = label; button.addEventListener("click", action); actions.append(button);
        }
        xhr = null; send.disabled = false;
    }

    function sendPending() {
        if (!pending || xhr) return;
        mark(pending.element, "SENDING");
        pending.element.querySelector(".failed-message-actions")?.remove();
        if (pending.upload && !pending.upload.querySelector("progress")) {
            pending.upload.querySelector("span").textContent = "Uploading… 0%";
            const progress = document.createElement("progress");
            progress.max = 100;
            progress.value = 0;
            pending.upload.querySelector("div").before(progress);
            const actions = pending.upload.querySelector("div");
            actions.replaceChildren();
            const cancel = document.createElement("button");
            cancel.type = "button";
            cancel.textContent = "Cancel";
            cancel.addEventListener("click", () => xhr?.abort());
            actions.append(cancel);
        }
        send.disabled = true;
        xhr = new XMLHttpRequest(); xhr.open("POST", form.action); xhr.responseType = "json";
        if (pending.file) xhr.upload.onprogress = event => {
            if (event.lengthComputable) {
                const progress = Math.round(event.loaded / event.total * 100);
                pending.upload.querySelector("progress").value = progress;
                pending.upload.querySelector("span").textContent = `Uploading… ${progress}%`;
            }
        };
        xhr.onload = () => {
            const data = xhr.response || {};
            if (xhr.status < 200 || xhr.status >= 300) { xhr = null; fail(data.error); return; }
            pending.element.dataset.messageId = data.messageId;
            pending.element.querySelector("time").textContent = data.createdLabel;
            pending.upload?.remove(); media(pending.element, data); mark(pending.element, data.status);
            last = Math.max(last, +data.messageId); resetComposer(); pending = null; xhr = null; send.disabled = false;
        };
        xhr.onerror = () => { xhr = null; fail("Upload failed"); };
        xhr.onabort = () => { xhr = null; fail("Upload cancelled"); };
        xhr.send(pending.body);
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        if (pending) return;
        const text = field.value.trim();
        const file = fileInput.files[0];
        if (!text && !file) return;
        form.querySelector("[name=clientMessageId]").value = crypto.randomUUID();
        const body = new FormData(form);
        const element = bubble({message: text, status: "SENDING"}, true, true);
        pending = {file, body, element, upload: file ? uploadCard(element, file) : null};
        sendPending();
    });
    field.addEventListener("keydown", event => {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing) { event.preventDefault(); form.requestSubmit(); }
    });

    const seen = () => {
        if (document.hidden) return;
        const message = [...list.querySelectorAll(".incoming[data-message-id]")].at(-1);
        if (message) fetch(list.dataset.seenUrl, {method: "POST", headers: {"Content-Type": "application/x-www-form-urlencoded"}, body: new URLSearchParams({csrfToken, conversationId: list.dataset.conversation, lastSeenMessageId: message.dataset.messageId})}).catch(() => {});
    };
    const poll = async () => {
        if (document.hidden) return;
        try {
            const response = await fetch(`${form.action.replace(/\/send$/, "/poll")}?conversationId=${list.dataset.conversation}&afterMessageId=${last}`);
            if (!response.ok) return;
            const data = await response.json();
            data.messages.forEach(message => bubble(message, String(message.senderId) === list.dataset.currentUser));
            data.receipts.forEach(receipt => { const element = list.querySelector(`[data-message-id="${receipt.messageId}"]`); if (element) mark(element, receipt.status); });
            if (data.messages.some(message => String(message.senderId) !== list.dataset.currentUser)) seen();
        } catch (_ignored) {}
    };
    setInterval(poll, 1500);
    document.addEventListener("visibilitychange", () => { if (!document.hidden) { poll(); seen(); } });
    list.scrollTop = list.scrollHeight; seen(); initializeConversationMenu(form, list, csrf);
}

function initializeConversationMenu(form, list, csrf) {
    const threadHeader = document.querySelector(".private-thread-header");
    if (!threadHeader) return;
    const menu = document.createElement("button");
    menu.className = "conversation-menu"; menu.type = "button"; menu.textContent = "⋯"; menu.setAttribute("aria-label", "Conversation options");
    threadHeader.append(menu);
    menu.addEventListener("click", () => {
        const dialog = document.createElement("dialog");
        dialog.className = "delete-conversation-dialog";
        dialog.innerHTML = '<form method="dialog"><h2>Delete this conversation?</h2><p>This will remove the conversation from your chat list. The other participant keeps their history.</p><p class="delete-error" role="alert" hidden>Could not delete this conversation. Please try again.</p><div><button value="cancel">Cancel</button><button value="delete" class="danger">Delete</button></div></form>';
        document.body.append(dialog);
        const deleteButton = dialog.querySelector(".danger");
        dialog.addEventListener("close", async () => {
            if (dialog.returnValue !== "delete") { dialog.remove(); return; }
            deleteButton.disabled = true; deleteButton.textContent = "Deleting...";
            let deleteUrl;
            let requestBody;
            try {
                deleteUrl = form.action.replace(/\/send(?:\?.*)?$/, "/delete");
                const conversationId = form.querySelector('[name="conversationId"]')?.value;
                const csrfToken = form.querySelector('[name="csrfToken"]')?.value;
                if (!deleteUrl || !conversationId || !csrfToken) throw new Error("Delete request state is incomplete.");
                requestBody = new window.URLSearchParams({csrfToken, conversationId});
                console.debug("Private conversation delete", {stage: "BEFORE_FETCH"});
            } catch (error) {
                console.error("Private conversation delete client failure", {
                    name: error.name,
                    message: error.message,
                    stack: error.stack
                });
                showDeleteConversationError(dialog, "DELETE_CLIENT_ERROR");
                deleteButton.disabled = false; deleteButton.textContent = "Delete";
                dialog.showModal();
                return;
            }
            let response;
            try {
                response = await window.fetch(deleteUrl, {
                    method: "POST",
                    credentials: "same-origin",
                    redirect: "error",
                    headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
                    body: requestBody
                });
                console.debug("Private conversation delete", {stage: "AFTER_FETCH", status: response.status});
            } catch (error) {
                const code = error instanceof ReferenceError ? "DELETE_CLIENT_ERROR" : "DELETE_NETWORK_FAILED";
                console.error("Private conversation delete request failure", {
                    name: error.name,
                    message: error.message,
                    stack: error.stack,
                    code
                });
                showDeleteConversationError(dialog, code);
                deleteButton.disabled = false; deleteButton.textContent = "Delete";
                dialog.showModal();
                return;
            }

            console.debug("Private conversation delete", {stage: "BEFORE_PARSE"});
            const result = await parseDeleteConversationResponse(response);
            console.debug("Private conversation delete", {stage: "AFTER_PARSE", code: result.code || result.payload?.code});
            if (response.ok && result.payload?.success === true && result.payload.code === "DELETE_OK") {
                    document.querySelector(`.conversation-item[href*="conversationId=${list.dataset.conversation}"]`)?.remove();
                    dialog.remove();
                    history.replaceState(null, "", new URL("../messages", form.action));
                    const thread = document.querySelector(".private-thread");
                    thread.replaceChildren();
                    const empty = document.createElement("div");
                    empty.className = "private-empty thread-empty";
                    empty.innerHTML = "<h2>Your private messages</h2><p>Select a conversation to start chatting.</p>";
                    thread.append(empty);
                    document.querySelector(".conversation-list")?.classList.remove("has-selection");
                    return;
            }
            const code = result.code || (response.ok ? "DELETE_RESPONSE_INVALID" : "DELETE_HTTP_ERROR");
            console.error("Private conversation delete failed", {
                status: response.status,
                contentType: response.headers.get("Content-Type") || "missing",
                code
            });
            showDeleteConversationError(dialog, code);
            deleteButton.disabled = false; deleteButton.textContent = "Delete";
            dialog.showModal();
        });
        dialog.showModal();
    });
}

async function parseDeleteConversationResponse(response) {
    const contentType = response.headers.get("Content-Type") || "";
    if (!contentType.toLowerCase().includes("application/json")) {
        return {payload: null, code: "DELETE_RESPONSE_INVALID"};
    }
    const body = await response.text();
    if (!body.trim()) return {payload: null, code: "DELETE_RESPONSE_INVALID"};
    try {
        const payload = JSON.parse(body);
        if (!payload || typeof payload !== "object") return {payload: null, code: "DELETE_RESPONSE_INVALID"};
        if (!response.ok && typeof payload.code === "string") return {payload, code: payload.code};
        return {payload, code: null};
    } catch (_ignored) {
        return {payload: null, code: "DELETE_RESPONSE_INVALID"};
    }
}

function showDeleteConversationError(dialog, code) {
    const errorMessage = dialog.querySelector(".delete-error");
    errorMessage.dataset.errorCode = code;
    errorMessage.textContent = deleteConversationErrorMessage(code);
    errorMessage.hidden = false;
}

function deleteConversationErrorMessage(code) {
    const messages = {
        DELETE_CSRF_INVALID: "Your security token expired. Refresh the page and try again. (DELETE_CSRF_INVALID)",
        DELETE_UNAUTHENTICATED: "Your sign-in expired. Sign in again and retry. (DELETE_UNAUTHENTICATED)",
        DELETE_INVALID_ID: "This conversation could not be identified. Refresh the page and try again. (DELETE_INVALID_ID)",
        DELETE_FORBIDDEN: "You no longer have access to this conversation. (DELETE_FORBIDDEN)",
        DELETE_NOT_FOUND: "This conversation is no longer available. (DELETE_NOT_FOUND)",
        DELETE_DB_ERROR: "The conversation could not be removed right now. Please try again shortly. (DELETE_DB_ERROR)",
        DELETE_SERVER_ERROR: "StudentHub could not complete the delete request right now. Please retry. (DELETE_SERVER_ERROR)",
        DELETE_CLIENT_ERROR: "StudentHub could not prepare the delete request. Refresh the page and retry. (DELETE_CLIENT_ERROR)",
        DELETE_NETWORK_FAILED: "The delete request could not reach StudentHub. Check your connection and retry. (DELETE_NETWORK_FAILED)",
        DELETE_RESPONSE_INVALID: "StudentHub returned an unexpected response. Refresh the page and retry. (DELETE_RESPONSE_INVALID)",
        DELETE_HTTP_ERROR: "StudentHub could not complete the delete request. Please retry. (DELETE_HTTP_ERROR)"
    };
    return messages[code] || messages.DELETE_HTTP_ERROR;
}
