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
        themeButton.querySelector(".theme-sun").hidden = !dark;
        themeButton.querySelector(".theme-moon").hidden = dark;
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
