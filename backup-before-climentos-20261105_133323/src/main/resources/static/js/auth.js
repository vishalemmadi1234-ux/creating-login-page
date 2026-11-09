
const form = document.getElementById("registerForm");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");
const strengthBar = document.getElementById("strengthBar");
const strengthLabel = document.getElementById("strengthLabel");
const matchMessage = document.getElementById("matchMessage");

document.querySelectorAll("[data-toggle]").forEach(button => {
    button.addEventListener("click", () => {
        const input = document.getElementById(button.dataset.toggle);

        const showing = input.type === "password";
        input.type = showing ? "text" : "password";
        button.textContent = showing ? "Hide" : "Show";
        button.setAttribute(
            "aria-label",
            showing ? "Hide password" : "Show password"
        );
    });
});

password.addEventListener("input", () => {
    const value = password.value;

    const checks = [
        value.length >= 8,
        /[A-Z]/.test(value),
        /[a-z]/.test(value),
        /[0-9]/.test(value),
        /[^A-Za-z0-9]/.test(value)
    ];

    const score = checks.filter(Boolean).length;

    if (!value) {
        strengthBar.style.width = "0%";
        strengthLabel.textContent = "Use at least 8 characters.";
    } else {
        strengthBar.style.width = `${score * 20}%`;
        strengthBar.style.background =
            score >= 4 ? "#16a34a" :
            score >= 3 ? "#f59e0b" : "#ef4444";
        strengthLabel.textContent =
            score >= 4 ? "Stronger password" :
            score >= 3 ? "Moderate password" : "Weak password";
    }

    checkMatch();
});

confirmPassword.addEventListener("input", checkMatch);

function checkMatch() {
    if (!confirmPassword.value) {
        matchMessage.textContent = "";
        return;
    }

    const matches = password.value === confirmPassword.value;

    matchMessage.textContent = matches
        ? "Passwords match"
        : "Passwords do not match";

    matchMessage.style.color = matches
        ? "#16a34a" : "#dc2626";
}

form.addEventListener("submit", event => {
    event.preventDefault();

    if (!form.reportValidity()) return;

    if (password.value !== confirmPassword.value) {
        matchMessage.textContent = "Passwords do not match";
        matchMessage.style.color = "#dc2626";
        confirmPassword.focus();
        return;
    }

    const notice = document.getElementById("demoNotice");
    notice.hidden = false;
    notice.textContent =
        "UI preview successful. Registration is not connected to the backend yet.";
});

