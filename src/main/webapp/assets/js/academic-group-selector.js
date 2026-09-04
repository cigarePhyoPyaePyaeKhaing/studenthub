(function () {
    'use strict';

    function initializePicker(picker) {
        var semester = picker.querySelector('[data-group-semester]');
        var group = picker.querySelector('[data-group-name]');
        var label = picker.querySelector('[data-group-label]');
        if (!semester || !group) return;
        var controls = picker.closest('.admin-discussion-controls') || picker.parentElement;
        var cards = controls ? controls.querySelectorAll('.admin-scope-card') : [];
        var semesterItem = semester.closest('.admin-selector-item');
        var groupItem = group.closest('.admin-selector-item');

        function syncActiveScope(isAcademic) {
            if (!picker.dataset.navigationBase) return;
            if (isAcademic) {
                Array.prototype.forEach.call(cards, function (card) {
                    card.classList.remove('active');
                    card.removeAttribute('aria-current');
                });
                if (semesterItem) {
                    semesterItem.classList.toggle('active', Boolean(semester.value));
                }
                if (groupItem) {
                    groupItem.classList.toggle('active', Boolean(semester.value && group.value));
                }
            } else {
                if (semesterItem) semesterItem.classList.remove('active');
                if (groupItem) groupItem.classList.remove('active');
            }
        }

        if (picker.dataset.navigationBase && cards.length) {
            Array.prototype.forEach.call(cards, function (card) {
                card.addEventListener('click', function () {
                    Array.prototype.forEach.call(cards, function (other) {
                        other.classList.remove('active');
                        other.removeAttribute('aria-current');
                    });
                    card.classList.add('active');
                    card.setAttribute('aria-current', 'page');
                    syncActiveScope(false);
                });
            });
        }

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
            syncActiveScope(Boolean(semester.value));
            var base = picker.dataset.navigationBase;
            if (base && semester.value) {
                window.location.assign(base + '?moderationScope=semester:' + encodeURIComponent(semester.value));
            }
        });
        group.addEventListener('change', function () {
            syncActiveScope(Boolean(semester.value && group.value));
            var base = picker.dataset.navigationBase;
            if (base && semester.value && group.value) {
                window.location.assign(base + '?moderationScope=section:'
                    + encodeURIComponent(semester.value) + ':' + encodeURIComponent(group.value));
            }
        });
        update(true);
        if (picker.dataset.navigationBase) {
            var anyCardActive = Array.prototype.some.call(cards, function (card) {
                return card.classList.contains('active');
            });
            if (!anyCardActive && (semester.value || group.value)) {
                syncActiveScope(true);
            } else if (anyCardActive) {
                syncActiveScope(false);
            }
        }
    }

    function initialize() {
        document.querySelectorAll('[data-academic-group-picker]').forEach(initializePicker);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
    else initialize();
})();
