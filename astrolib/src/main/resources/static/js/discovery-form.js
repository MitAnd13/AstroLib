document.addEventListener("DOMContentLoaded", () => {
    const discoveryToggle = document.querySelector("[data-discovery-toggle]");
    const objectToggle = document.querySelector("[data-object-toggle]");
    const discoveryKindInput = document.querySelector("[data-discovery-kind-input]");
    const discoverySections = document.querySelectorAll("[data-discovery-section]");
    const objectKindSections = document.querySelectorAll("[data-object-kind]");
    const objectGroupSections = document.querySelectorAll("[data-object-group]");
    const parentSections = document.querySelectorAll("[data-parent-for]");
    const linkedObjectList = document.querySelector("[data-linked-object-list]");
    const addLinkedObjectButton = document.querySelector("[data-add-linked-object]");
    const staticKinds = new Set(["star", "nebula", "galaxy"]);
    let linkedObjectIndex = linkedObjectList ? linkedObjectList.querySelectorAll(".linked-object-row").length : 0;

    // Добавление объекта в явлении
    const linkedObjectRowTemplate = (index) => `
        <div class="linked-object-row">
            <label class="field">
                <span>Объект из базы</span>
                <input type="text" name="linkedObjectNames[${index}]" placeholder="Введите название объекта из базы">
            </label>
            <label class="field">
                <span>Роль</span>
                <select name="linkedObjectRoles[${index}]">
                    <option value="">Выберите роль</option>
                    <option value="Источник явления">Источник явления</option>
                    <option value="Наблюдаемый объект">Наблюдаемый объект</option>
                    <option value="Закрывающий объект">Закрывающий объект</option>
                    <option value="Закрываемый объект">Закрываемый объект</option>
                    <option value="Участник столкновения">Участник столкновения</option>
                    <option value="Центральное тело">Центральное тело</option>
                    <option value="Другое">Другое</option>
                </select>
            </label>
            <button class="button-inline button-inline-danger" type="button" data-remove-linked-object>Удалить</button>
        </div>
    `;

    const getCheckedValue = (container, name, fallback = null) => {
        if (!container) {
            return fallback;
        }

        const checked = container.querySelector(`input[name="${name}"]:checked`);
        return checked ? checked.value : fallback;
    };

    // Отображение нужных параметров объектов при заполнении формы
    const syncObjectSections = () => {
        const objectKind = getCheckedValue(objectToggle, "objectKind");
        if (!objectKind) {
            objectKindSections.forEach((section) => section.classList.add("is-hidden"));
            objectGroupSections.forEach((section) => section.classList.add("is-hidden"));
            parentSections.forEach((section) => section.classList.add("is-hidden"));
            return;
        }

        const isStatic = staticKinds.has(objectKind);

        objectKindSections.forEach((section) => {
            const matches = section.getAttribute("data-object-kind") === objectKind;
            section.classList.toggle("is-hidden", !matches);
        });

        objectGroupSections.forEach((section) => {
            const group = section.getAttribute("data-object-group");
            const shouldShow = (group === "static" && isStatic) || (group === "moving" && !isStatic);
            section.classList.toggle("is-hidden", !shouldShow);
        });

        parentSections.forEach((section) => {
            const parentFor = section.getAttribute("data-parent-for");
            section.classList.toggle("is-hidden", parentFor !== objectKind);
        });
    };

    // Отображение параметров объектов/явлений
    const syncDiscoverySections = () => {
        const discoveryKind = getCheckedValue(discoveryToggle, "discoveryKind", "object");
        if (discoveryKindInput) {
            discoveryKindInput.value = discoveryKind;
        }

        discoverySections.forEach((section) => {
            const sectionKind = section.getAttribute("data-discovery-section");
            section.classList.toggle("is-hidden", sectionKind !== discoveryKind);
        });

        if (discoveryKind === "object") {
            syncObjectSections();
        }
    };

    if (discoveryToggle) {
        discoveryToggle.addEventListener("change", syncDiscoverySections);
    }

    if (objectToggle) {
        objectToggle.addEventListener("change", syncObjectSections);
    }

    if (linkedObjectList && addLinkedObjectButton) {
        addLinkedObjectButton.addEventListener("click", () => {
            linkedObjectList.insertAdjacentHTML("beforeend", linkedObjectRowTemplate(linkedObjectIndex));
            linkedObjectIndex += 1;
        });

        linkedObjectList.addEventListener("click", (event) => {
            const removeButton = event.target.closest("[data-remove-linked-object]");
            if (!removeButton) {
                return;
            }

            const rows = linkedObjectList.querySelectorAll(".linked-object-row");
            if (rows.length <= 1) {
                return;
            }

            const row = removeButton.closest(".linked-object-row");
            if (row) {
                row.remove();
            }
        });
    }

    syncDiscoverySections();
});
