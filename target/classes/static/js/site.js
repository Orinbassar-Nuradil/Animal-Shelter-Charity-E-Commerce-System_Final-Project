document.documentElement.classList.add("js");

const toggle = document.querySelector(".nav-toggle");
const menu = document.querySelector("#main-nav");
if (toggle && menu) {
    function closeMenu() {
        menu.classList.remove("open");
        toggle.setAttribute("aria-expanded", "false");
    }

    toggle.addEventListener("click", () => {
        toggle.setAttribute("aria-expanded", String(menu.classList.toggle("open")));
    });
    menu.addEventListener("click", closeMenu);
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && menu.classList.contains("open")) {
            closeMenu();
            toggle.focus();
        }
    });
}

const amount = document.querySelector("#donation-amount");
const presets = document.querySelector(".presets");
if (amount && presets) {
    const buttons = presets.querySelectorAll("[data-amount]");
    function markSelection() {
        buttons.forEach(button => button.setAttribute("aria-pressed", String(Number(button.dataset.amount) === Number(amount.value))));
    }
    presets.hidden = false;
    buttons.forEach(button => button.addEventListener("click", () => {
        amount.value = button.dataset.amount;
        markSelection();
    }));
    amount.addEventListener("input", markSelection);
    markSelection();
}

