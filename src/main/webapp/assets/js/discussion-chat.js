"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".discussions-shell .chat-composer");
    const textarea = form?.querySelector("textarea");
    const messageList = document.querySelector(".discussions-shell .message-list");

    function scrollToBottom() {
        if (messageList) {
            messageList.scrollTop = messageList.scrollHeight;
        }
    }

    if (form && textarea) {
        const composer = window.StudentHubMessageComposer?.initialize(form);
        let submitting = false;
        form.addEventListener("submit", async event => {
            event.preventDefault();
            if (submitting) return;
            const file = form.querySelector('input[type="file"][name="attachment"]')?.files?.[0];
            if (!textarea.value.trim() && !file) return;
            submitting = true;
            const button = form.querySelector('button[type="submit"]');
            if (button) button.disabled = true;
            try {
                const response = await fetch(form.action, {
                    method: "POST",
                    credentials: "same-origin",
                    redirect: "error",
                    headers: {"Accept": "application/json"},
                    body: new FormData(form)
                });
                const contentType = response.headers.get("Content-Type") || "";
                const payload = contentType.includes("application/json") ? await response.json() : null;
                if (!response.ok || payload?.success !== true) {
                    throw new Error(payload?.message || "The message could not be sent right now.");
                }
                textarea.value = "";
                composer?.resetHeight();
                composer?.clearAttachment();
                window.location.assign(payload.redirectUrl || `${form.action.replace(/\/messages$/, "")}?scope=${encodeURIComponent(form.querySelector('[name="scope"]').value)}`);
                return;
            } catch (error) {
                let errorMessage = form.querySelector(".send-error");
                if (!errorMessage) {
                    errorMessage = document.createElement("p");
                    errorMessage.className = "send-error";
                    errorMessage.setAttribute("role", "alert");
                    form.append(errorMessage);
                }
                errorMessage.textContent = error.message || "The message could not be sent right now.";
                errorMessage.hidden = false;
            } finally {
                submitting = false;
                if (button) button.disabled = false;
            }
        });
        textarea.addEventListener("keydown", event => {
            if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
                event.preventDefault();
                form.requestSubmit();
            }
        });
        textarea.addEventListener("input", () => {
            scrollToBottom();
        });
    }

    const activeRoomTab = document.querySelector(".discussions-shell .room-tabs a.active");
    if (activeRoomTab && typeof activeRoomTab.scrollIntoView === "function") {
        activeRoomTab.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
    }

    scrollToBottom();
    window.addEventListener("load", scrollToBottom);
    window.addEventListener("resize", scrollToBottom);
});

