document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".discussions-shell .chat-composer");
    if (!form) return;
    const textarea = form.querySelector("textarea");
    const input = form.querySelector("input[type=file]");
    if (!textarea || !input) return;

    const preview = document.createElement("div");
    preview.className = "discussion-attachment-preview";
    preview.hidden = true;
    form.prepend(preview);
    let objectUrl;

    function readableSize(bytes) {
        return bytes < 1048576 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1048576).toFixed(1)} MB`;
    }

    function clearPreview() {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = undefined;
        input.value = "";
        preview.replaceChildren();
        preview.hidden = true;
    }

    function showPreview(file) {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = undefined;
        preview.replaceChildren();
        preview.hidden = true;
        if (!file) return;
        let media;
        if (file.type.startsWith("image/")) {
            media = document.createElement("img");
            media.alt = "Selected image preview";
            objectUrl = URL.createObjectURL(file);
            media.src = objectUrl;
        } else if (file.type.startsWith("video/")) {
            media = document.createElement("video");
            media.muted = true;
            objectUrl = URL.createObjectURL(file);
            media.src = objectUrl;
        } else {
            media = document.createElement("span");
            media.className = "attachment-preview-icon";
            media.textContent = file.type.startsWith("audio/") ? "AUD" : "FILE";
        }
        const copy = document.createElement("span");
        copy.className = "attachment-preview-copy";
        const name = document.createElement("strong");
        name.textContent = file.name;
        const details = document.createElement("small");
        details.textContent = `${file.type || "File"} · ${readableSize(file.size)}`;
        copy.append(name, details);
        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "attachment-preview-remove";
        remove.setAttribute("aria-label", "Remove attachment");
        remove.textContent = "×";
        remove.addEventListener("click", clearPreview);
        preview.append(media, copy, remove);
        preview.hidden = false;
    }

    textarea.addEventListener("input", () => {
        textarea.style.height = "auto";
        textarea.style.height = `${Math.min(textarea.scrollHeight, 120)}px`;
    });
    textarea.addEventListener("keydown", event => {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            form.requestSubmit();
        }
    });
    input.addEventListener("change", () => showPreview(input.files[0]));
});
