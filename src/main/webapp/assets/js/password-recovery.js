"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const otpForm = document.querySelector("[data-otp-form]");
    if (otpForm) initializeOtpForm(otpForm);

    const resendForm = document.querySelector("[data-resend-form]");
    if (resendForm) initializeResend(resendForm);
});

function initializeOtpForm(form) {
    const inputs = Array.from(form.querySelectorAll(".otp-code-input"));
    const completeCode = form.querySelector("[data-otp-value]");
    const submitButton = form.querySelector("[data-verify-button]");

    const updateState = () => {
        const code = inputs.map((input) => input.value).join("");
        completeCode.value = code;
        submitButton.disabled = !/^\d{6}$/.test(code);
    };

    const distribute = (value) => {
        const digits = value.replace(/\D/g, "").slice(0, 6);
        inputs.forEach((input, index) => { input.value = digits[index] || ""; });
        updateState();
        inputs[Math.min(digits.length, 5)].focus();
    };

    inputs.forEach((input, index) => {
        input.addEventListener("input", () => {
            const digits = input.value.replace(/\D/g, "");
            if (digits.length > 1) {
                distribute(digits);
                return;
            }
            input.value = digits;
            updateState();
            if (digits && index < inputs.length - 1) inputs[index + 1].focus();
        });

        input.addEventListener("keydown", (event) => {
            if (event.key === "Backspace" && !input.value && index > 0) {
                event.preventDefault();
                inputs[index - 1].focus();
                inputs[index - 1].select();
            } else if (event.key === "ArrowLeft" && index > 0) {
                event.preventDefault();
                inputs[index - 1].focus();
            } else if (event.key === "ArrowRight" && index < inputs.length - 1) {
                event.preventDefault();
                inputs[index + 1].focus();
            }
        });

        input.addEventListener("paste", (event) => {
            event.preventDefault();
            distribute(event.clipboardData.getData("text"));
        });
    });

    form.addEventListener("submit", (event) => {
        updateState();
        if (submitButton.disabled) {
            event.preventDefault();
            inputs.find((input) => !input.value)?.focus();
            return;
        }
        submitButton.disabled = true;
        submitButton.textContent = "Verifying…";
    });

    updateState();
}

function initializeResend(form) {
    const button = form.querySelector("[data-resend-button]");
    const countdown = form.querySelector("[data-resend-countdown]");
    let seconds = Math.max(0, Number.parseInt(button.dataset.resendSeconds, 10) || 0);

    const render = () => {
        if (seconds <= 0) {
            button.disabled = false;
            button.textContent = "Resend code";
            return false;
        }
        button.disabled = true;
        countdown.textContent = `00:${String(seconds).padStart(2, "0")}`;
        return true;
    };

    if (render()) {
        const timer = window.setInterval(() => {
            seconds -= 1;
            if (!render()) window.clearInterval(timer);
        }, 1000);
    }

    form.addEventListener("submit", () => {
        button.disabled = true;
        button.textContent = "Sending…";
    });
}
