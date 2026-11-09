// Use one auth page for login, registration, and forgot-password.
// Existing endpoints still handle submissions securely on the server.
(() => {
  const panels = [...document.querySelectorAll('[data-auth-panel]')];
  if (!panels.length) return;
  const allowed = ['login', 'register', 'forgot'];
  const activate = (view, updateUrl = false) => {
    const panel = panels.find(item => item.dataset.authPanel === view);
    if (!panel) return;
    panels.forEach(item => {
      const active = item === panel;
      item.classList.toggle('is-active', active);
      item.setAttribute('aria-hidden', String(!active));
      item.inert = !active;
    });
    document.querySelectorAll('[data-auth-target]').forEach(link => {
      const active = link.dataset.authTarget === view;
      link.classList.toggle('is-current', active);
      if (active && link.closest('.auth-switch')) link.setAttribute('aria-current', 'page');
      else link.removeAttribute('aria-current');
    });
    if (updateUrl && allowed.includes(view)) {
      const url = new URL('/login', window.location.origin);
      if (view !== 'login') url.searchParams.set('view', view);
      window.history.pushState({authView:view}, '', url.pathname + url.search);
    }
  };
  const initiallyActive = panels.find(panel => panel.classList.contains('is-active')) || panels[0];
  activate(initiallyActive.dataset.authPanel);
  document.querySelectorAll('[data-auth-target]').forEach(link => {
    link.addEventListener('click', (event) => {
      if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
      event.preventDefault();
      activate(link.dataset.authTarget, true);
    });
  });
  window.addEventListener('popstate', () => {
    const view = new URLSearchParams(window.location.search).get('view') || 'login';
    if (allowed.includes(view)) activate(view);
  });
})();

// Password visibility toggles use per-input IDs (all forms share one page).
document.querySelectorAll('[data-toggle]').forEach(button => {
  const input = document.getElementById(button.dataset.toggle);
  if (!input) return;
  button.addEventListener('click', () => {
    const reveal = input.type === 'password';
    input.type = reveal ? 'text' : 'password';
    button.setAttribute('aria-label', reveal ? 'Hide password' : 'Show password');
    button.setAttribute('aria-pressed', String(reveal));
    const icon = button.querySelector('use');
    if (icon) icon.setAttribute('href', reveal ? '/icons.svg#eye-off' : '/icons.svg#eye');
  });
});

// Check strength / matching for EACH form, not only the first form on the page.
document.querySelectorAll('[data-password-form]').forEach(form => {
  const pw = form.querySelector('[data-new-password]');
  const confirmInput = form.querySelector('[data-confirm-password]');
  const meter = form.querySelector('[data-strength-meter]');
  const meterText = form.querySelector('[data-strength-label]');
  const matchHint = form.querySelector('[data-match-hint]');
  if (!pw) return;
  function checkPasswords() {
    const val = pw.value;
    if (meter) {
      const score = [val.length >= 8, val.length >= 12, /[A-Z]/.test(val), /[0-9]/.test(val), /[^\w]/.test(val)].filter(Boolean).length;
      meter.style.width = val ? `${score * 20}%` : '0%';
      meter.style.background = score >= 4 ? '#1ab48b' : score >= 3 ? '#e6ae4c' : '#e6636d';
      if (meterText) meterText.textContent = !val ? 'Use 8–64 characters' : score >= 4 ? 'Good password variety' : score >= 3 ? 'Consider making it stronger' : 'Choose a stronger password';
    }
    if (confirmInput && matchHint) {
      matchHint.textContent = !confirmInput.value ? '' : confirmInput.value === val ? 'Passwords match' : 'Passwords do not match';
      matchHint.style.color = confirmInput.value === val ? '#148968' : '#b73549';
    }
  }
  pw.addEventListener('input', checkPasswords);
  if (confirmInput) confirmInput.addEventListener('input', checkPasswords);
  form.addEventListener('submit', event => {
    if (confirmInput && pw.value !== confirmInput.value) {
      event.preventDefault();
      checkPasswords();
      confirmInput.focus();
    }
  });
});
