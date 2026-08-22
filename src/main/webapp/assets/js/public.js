(function () {
    "use strict";
    var root = document.documentElement;
    var themeButton = document.querySelector("[data-theme-toggle]");
    var menuButton = document.querySelector("[data-menu-toggle]");
    var navigation = document.querySelector("[data-public-nav]");

    function updateThemeButton() {
        if (!themeButton) return;
        var dark = root.dataset.theme === "dark";
        themeButton.setAttribute("aria-label", dark ? "Switch to light theme" : "Switch to dark theme");
        themeButton.setAttribute("title", dark ? "Switch to light theme" : "Switch to dark theme");
        themeButton.querySelector("svg").innerHTML = dark
            ? '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"/>'
            : '<path d="M21 15.2A9 9 0 1 1 8.8 3a7 7 0 0 0 12.2 12.2Z"/>';
    }

    if (themeButton) {
        updateThemeButton();
        themeButton.addEventListener("click", function () {
            root.dataset.theme = root.dataset.theme === "dark" ? "light" : "dark";
            root.style.colorScheme = root.dataset.theme;
            localStorage.setItem("studenthub-theme", root.dataset.theme);
            updateThemeButton();
        });
    }

    if (menuButton && navigation) {
        menuButton.addEventListener("click", function () {
            var open = navigation.classList.toggle("is-open");
            menuButton.setAttribute("aria-expanded", String(open));
        });
        navigation.addEventListener("click", function (event) {
            if (event.target.closest("a")) {
                navigation.classList.remove("is-open");
                menuButton.setAttribute("aria-expanded", "false");
            }
        });
    }
}());
