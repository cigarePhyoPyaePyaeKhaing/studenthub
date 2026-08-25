document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".discussions-shell .chat-composer");
    const textarea = form?.querySelector("textarea");
    if (form && textarea) {
        window.StudentHubMessageComposer?.initialize(form);
        textarea.addEventListener("keydown", event => {
            if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
                event.preventDefault();
                form.requestSubmit();
            }
        });
    }

    const activeRoomTab = document.querySelector(".discussions-shell .room-tabs a.active");
    if (activeRoomTab && typeof activeRoomTab.scrollIntoView === "function") {
        activeRoomTab.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
    }
});

