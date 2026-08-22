"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("[data-profile-photo-form]");
    if (!form) return;
    const input = form.querySelector("[data-photo-input]");
    const preview = form.querySelector("[data-photo-preview]");
    const error = form.querySelector("[data-photo-error]");
    const remove = form.querySelector("[data-remove-photo]");
    const accepted = new Set(["image/jpeg", "image/png", "image/webp"]);
    const maximum = 2 * 1024 * 1024;
    let objectUrl;

    const showFallback = () => {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = undefined;
        preview.replaceChildren(Object.assign(document.createElement("span"), {textContent: preview.dataset.fallback || "S"}));
    };

    input.addEventListener("change", () => {
        error.textContent = "";
        const file = input.files && input.files[0];
        if (!file) return;
        if (!accepted.has(file.type) || file.size > maximum) {
            input.value = "";
            error.textContent = file.size > maximum ? "Image must be 2 MB or smaller." : "Choose a JPG, PNG, or WEBP image.";
            return;
        }
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = URL.createObjectURL(file);
        const image = document.createElement("img");
        image.src = objectUrl;
        image.alt = "Selected profile photo preview";
        preview.replaceChildren(image);
        if (remove) remove.checked = false;
    });

    remove?.addEventListener("change", () => {
        if (remove.checked) {
            input.value = "";
            showFallback();
        }
    });

    window.addEventListener("pagehide", () => { if (objectUrl) URL.revokeObjectURL(objectUrl); });
});
