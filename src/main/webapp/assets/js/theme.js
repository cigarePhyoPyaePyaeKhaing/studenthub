(function () {
    "use strict";
    var key = "studenthub-theme";
    var saved = localStorage.getItem(key);
    var theme = saved === "light" || saved === "dark"
        ? saved
        : (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
}());
