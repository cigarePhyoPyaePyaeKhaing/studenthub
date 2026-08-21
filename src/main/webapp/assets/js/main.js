"use strict";
(function(){
    const key="studenthub-theme",media=window.matchMedia("(prefers-color-scheme: dark)"),saved=localStorage.getItem(key);
    let preference=["light","dark","system"].includes(saved)?saved:"system";
    const resolved=()=>preference==="system"?(media.matches?"dark":"light"):preference;
    const apply=()=>{
        const theme=resolved();
        document.documentElement.dataset.theme=theme;
        document.documentElement.style.colorScheme=theme;
        document.querySelectorAll("[data-theme-choice]").forEach(button=>{
            const active=button.dataset.themeChoice===preference;
            button.classList.toggle("active",active);
            button.setAttribute("aria-pressed",String(active));
        });
    };
    apply();
    media.addEventListener?.("change",()=>{if(preference==="system")apply()});

    const eyeOpenSvg='<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
    const eyeClosedSvg='<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path><line x1="2" y1="2" x2="22" y2="22"></line></svg>';

    function initPasswordToggles(){
        document.querySelectorAll("[data-password-toggle]").forEach(button=>{
            const targetId=button.dataset.passwordToggle;
            const input=document.getElementById(targetId)||button.closest(".password-field")?.querySelector("input");
            if(!input) return;
            const updateIcon=()=>{
                const isText=input.type==="text";
                button.innerHTML=isText?eyeClosedSvg:eyeOpenSvg;
                button.setAttribute("aria-label",isText?"Hide password":"Show password");
            };
            updateIcon();
            button.onclick=(e)=>{
                e.preventDefault();
                input.type=input.type==="password"?"text":"password";
                updateIcon();
            };
        });
    }

    document.addEventListener("DOMContentLoaded",()=>{
        document.documentElement.classList.add("js-enabled");
        initPasswordToggles();
        if(!document.querySelector(".theme-control")){
            const control=document.createElement("div");
            control.className=document.body.classList.contains("error-page")?"theme-control error-theme-control":"theme-control auth-theme-control";
            control.setAttribute("role","group");
            control.setAttribute("aria-label","Color theme");
            control.innerHTML='<button type="button" data-theme-choice="light" aria-label="Use light theme">☀</button><button type="button" data-theme-choice="system" aria-label="Use system theme">◐</button><button type="button" data-theme-choice="dark" aria-label="Use dark theme">☾</button>';
            document.body.append(control);
        }
        if(document.body.classList.contains("dashboard-body")&&!document.querySelector(".mobile-bottom-nav")){
            const home=document.querySelector(".dashboard-brand")?.getAttribute("href")||"/home",context=home.slice(0,-5),path=location.pathname,items=[["⌂","Home","/home"],["◎","Announcements","/announcements"],["◌","Discussions","/discussions"],["◷","Deadlines","/deadlines"],["○","Profile","/profile"]],nav=document.createElement("nav");
            nav.className="mobile-bottom-nav";
            nav.setAttribute("aria-label","Mobile navigation");
            nav.innerHTML=items.map(([icon,label,url])=>`<a href="${context+url}" class="${path===context+url?'active':''}" aria-label="${label}"><span aria-hidden="true">${icon}</span>${label}</a>`).join("");
            document.body.append(nav);
        }
        document.addEventListener("click",event=>{
            const button=event.target.closest("[data-theme-choice]");
            if(!button)return;
            preference=button.dataset.themeChoice;
            localStorage.setItem(key,preference);
            apply();
        });
        apply();
    });
})();

