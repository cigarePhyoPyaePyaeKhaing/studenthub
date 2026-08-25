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
        window.StudentHubMessageComposer?.initialize(form);
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

