document.addEventListener("DOMContentLoaded", () => {
    const typeInput = document.querySelector("[data-search-object-kind]");
    const objectKindSections = document.querySelectorAll("[data-object-kind]");
    const objectGroupSections = document.querySelectorAll("[data-object-group]");
    const parentSections = document.querySelectorAll("[data-parent-for]");
    const staticKinds = new Set(["star", "nebula", "galaxy"]);

    if (!typeInput) {
        return;
    }

    const syncSections = () => {
        const objectKind = typeInput.value;
        if (!objectKind) {
            objectKindSections.forEach((section) => section.classList.add("is-hidden"));
            objectGroupSections.forEach((section) => section.classList.add("is-hidden"));
            parentSections.forEach((section) => section.classList.add("is-hidden"));
            return;
        }

        const isStatic = staticKinds.has(objectKind);

        objectKindSections.forEach((section) => {
            section.classList.toggle("is-hidden", section.getAttribute("data-object-kind") !== objectKind);
        });

        objectGroupSections.forEach((section) => {
            const group = section.getAttribute("data-object-group");
            const shouldShow = (group === "static" && isStatic) || (group === "moving" && !isStatic);
            section.classList.toggle("is-hidden", !shouldShow);
        });

        parentSections.forEach((section) => {
            section.classList.toggle("is-hidden", section.getAttribute("data-parent-for") !== objectKind);
        });
    };

    typeInput.addEventListener("change", syncSections);
    syncSections();
});
