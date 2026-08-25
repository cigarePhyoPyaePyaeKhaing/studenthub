document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".discussions-shell .chat-composer");
    const textarea = form?.querySelector("textarea");
    if (!form || !textarea) return;
    window.StudentHubMessageComposer?.initialize(form);
    textarea.addEventListener("keydown", event => {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            form.requestSubmit();
        }
    });
});
