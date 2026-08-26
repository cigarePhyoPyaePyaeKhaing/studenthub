"use strict";

window.StudentHubMessageComposer = (() => {
    const initialized = new WeakSet();

    function readableSize(bytes) {
        return bytes < 1048576
            ? `${Math.max(1, Math.round(bytes / 1024))} KB`
            : `${(bytes / 1048576).toFixed(1)} MB`;
    }

    function initialize(form) {
        if (!form || initialized.has(form)) return null;
        const input = form.querySelector('input[type="file"][name="attachment"]');
        const textarea = form.querySelector("textarea");
        const preview = form.querySelector(":scope > .attachment-preview-area");
        if (!input || !textarea || !preview) return null;
        initialized.add(form);
        form.dataset.composerInitialized = "true";
        let objectUrl;
        let selectedAttachment = null;

        function resize() {
            textarea.style.height = "auto";
            textarea.style.height = `${Math.min(textarea.scrollHeight, 140)}px`;
        }

        function releasePreview() {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
            objectUrl = undefined;
            preview.replaceChildren();
            preview.hidden = true;
        }

        function clearAttachment() {
            releasePreview();
            selectedAttachment = null;
            input.value = "";
        }

        function render(file) {
            releasePreview();
            selectedAttachment = file || null;
            if (!file) return;
            const media = document.createElement(file.type.startsWith("image/") ? "img"
                : file.type.startsWith("video/") ? "video" : "span");
            if (media instanceof HTMLImageElement || media instanceof HTMLVideoElement) {
                objectUrl = URL.createObjectURL(file);
                media.src = objectUrl;
                if (media instanceof HTMLImageElement) media.alt = "Selected attachment preview";
                else { media.muted = true; media.preload = "metadata"; }
            } else {
                media.className = "attachment-preview-file";
                media.setAttribute("aria-hidden", "true");
                media.textContent = file.type.startsWith("audio/") ? "♪" : "▤";
            }
            const details = document.createElement("span");
            details.className = "attachment-preview-details";
            const name = document.createElement("strong");
            name.textContent = file.name;
            const size = document.createElement("small");
            size.textContent = readableSize(file.size);
            details.append(name, size);
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "attachment-preview-remove";
            remove.setAttribute("aria-label", "Remove attachment");
            remove.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18"/></svg>';
            remove.addEventListener("click", clearAttachment);
            preview.append(media, details, remove);
            preview.hidden = false;
        }

        input.addEventListener("change", () => render(input.files?.[0]));
        textarea.addEventListener("input", resize);
        window.addEventListener("pagehide", () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        }, {once: true});
        return {
            clearAttachment,
            selectedAttachment: () => selectedAttachment,
            resetHeight: () => { textarea.style.height = "auto"; }
        };
    }

    return {initialize};
})();
