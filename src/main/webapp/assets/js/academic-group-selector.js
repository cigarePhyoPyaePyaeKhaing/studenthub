(function () {
    'use strict';

    function initializePicker(picker) {
        var semester = picker.querySelector('[data-group-semester]');
        var group = picker.querySelector('[data-group-name]');
        var label = picker.querySelector('[data-group-label]');
        if (!semester || !group) return;

        function update(preserveSelection) {
            var semesterValue = semester.value;
            var major = Number(semesterValue) >= 7;
            var previous = preserveSelection ? group.dataset.currentValue || group.value : '';
            var placeholder = group.options[0];
            if (label) label.textContent = (picker.dataset.groupLabelPrefix || '') + (major ? 'Major' : 'Section');
            placeholder.textContent = semesterValue ? (major ? 'Select major' : 'Select section') : 'Choose a semester first';
            Array.prototype.forEach.call(group.options, function (option, index) {
                if (index === 0) return;
                var matches = semesterValue !== '' && option.dataset.semester === semesterValue;
                option.hidden = !matches;
                option.disabled = !matches;
                option.selected = matches && option.value === previous;
            });
            group.disabled = semesterValue === '';
            if (!previous || !Array.prototype.some.call(group.options, function (option) {
                return !option.disabled && option.value === previous;
            })) group.value = '';
            group.dataset.currentValue = '';
        }

        semester.addEventListener('change', function () {
            update(false);
            var base = picker.dataset.navigationBase;
            if (base && semester.value) {
                window.location.assign(base + '?moderationScope=semester:' + encodeURIComponent(semester.value));
            }
        });
        group.addEventListener('change', function () {
            var base = picker.dataset.navigationBase;
            if (base && semester.value && group.value) {
                window.location.assign(base + '?moderationScope=section:'
                    + encodeURIComponent(semester.value) + ':' + encodeURIComponent(group.value));
            }
        });
        update(true);
    }

    function initialize() {
        document.querySelectorAll('[data-academic-group-picker]').forEach(initializePicker);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
    else initialize();
})();
