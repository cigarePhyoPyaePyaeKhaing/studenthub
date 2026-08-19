"use strict";
document.addEventListener("DOMContentLoaded",()=>{
  const form=document.querySelector("[data-otp-form]");
  if(form){const fields=[...form.querySelectorAll(".otp-digit")],hidden=form.querySelector("[data-otp-value]");
    const fill=value=>{const digits=value.replace(/\D/g,"").slice(0,6);digits.split("").forEach((digit,index)=>fields[index].value=digit);fields[Math.min(digits.length,5)]?.focus()};
    fields.forEach((field,index)=>{field.addEventListener("input",()=>{field.value=field.value.replace(/\D/g,"").slice(-1);if(field.value)fields[index+1]?.focus()});field.addEventListener("keydown",event=>{if(event.key==="Backspace"&&!field.value)fields[index-1]?.focus()});field.addEventListener("paste",event=>{event.preventDefault();fill(event.clipboardData.getData("text"))})});
    form.addEventListener("submit",event=>{hidden.value=fields.map(field=>field.value).join("");if(!/^\d{6}$/.test(hidden.value)){event.preventDefault();fields.find(field=>!field.value)?.focus()}})}
  document.querySelectorAll("[data-password-toggle]").forEach(button=>button.addEventListener("click",()=>{const input=document.getElementById(button.dataset.passwordToggle),show=input.type==="password";input.type=show?"text":"password";button.textContent=show?"Hide":"Show";button.setAttribute("aria-label",`${show?"Hide":"Show"} ${input.id==="password"?"new":"confirmed"} password`)}));
  const resend=document.querySelector("[data-resend-button]"),countdown=document.querySelector("[data-resend-countdown]");if(resend&&countdown){let seconds=60;const timer=setInterval(()=>{seconds--;countdown.textContent=`00:${String(seconds).padStart(2,"0")}`;if(seconds<=0){clearInterval(timer);resend.disabled=false;resend.textContent="Resend code"}},1000)}
});
