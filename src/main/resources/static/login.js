// login.js

const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

if (loginTab && registerTab && loginForm && registerForm) {
  loginTab.onclick = () => {
    loginTab.classList.add("active");
    registerTab.classList.remove("active");
    loginForm.classList.add("show");
    registerForm.classList.remove("show");
  };

  registerTab.onclick = () => {
    registerTab.classList.add("active");
    loginTab.classList.remove("active");
    registerForm.classList.add("show");
    loginForm.classList.remove("show");
  };
}

function openRegister() {
  if (registerTab) registerTab.click();
}

function togglePassword(id, icon) {
  const input = document.getElementById(id);
  if (input) {
    if (input.type === "password") {
      input.type = "text";
      icon.classList.replace("bi-eye-slash", "bi-eye");
    } else {
      input.type = "password";
      icon.classList.replace("bi-eye", "bi-eye-slash");
    }
  }
}

const forgotSection = document.getElementById("forgotSection");
const forgotLink = document.querySelector(".forgot");

if (forgotLink && forgotSection && loginForm && registerForm) {
  forgotLink.onclick = () => {
    loginForm.classList.remove("show");
    registerForm.classList.remove("show");
    forgotSection.classList.add("show");
  };
}

function backToLogin() {
  if (forgotSection && loginForm) {
    forgotSection.classList.remove("show");
    loginForm.classList.add("show");
  }
}

function sendOtp() {
  const emailInput = document.getElementById("forgotEmail");
  if (!emailInput) return;

  const email = emailInput.value;
  if (!email) return alert("Email is required");

  fetch('/api/auth/forgot-password?email=' + email, { method: 'POST' })
    .then(res => res.text())
    .then(msg => {
      alert(msg);
      const step1 = document.getElementById("step1");
      const step2 = document.getElementById("step2");
      const otpMsg = document.getElementById("otpSentMsg");

      if (step1 && step2 && otpMsg) {
        step1.style.display = "none";
        step2.style.display = "block";
        otpMsg.innerText = "OTP sent to " + email;
      }
    });
}

function verifyOtp() {
  const emailInput = document.getElementById("forgotEmail");
  const otpInput = document.getElementById("otpCode");
  if (!emailInput || !otpInput) return;

  const email = emailInput.value;
  const otp = otpInput.value;

  fetch('/api/auth/verify-otp?email=' + email + '&otp=' + otp, { method: 'POST' })
    .then(res => {
      if (res.ok) {
        const step2 = document.getElementById("step2");
        const step3 = document.getElementById("step3");
        if (step2 && step3) {
          step2.style.display = "none";
          step3.style.display = "block";
        }
      } else {
        alert("Invalid OTP");
      }
    });
}

function resetPassword() {
  const emailInput = document.getElementById("forgotEmail");
  const otpInput = document.getElementById("otpCode");
  const newPassInput = document.getElementById("newPass");

  if (!emailInput || !otpInput || !newPassInput) return;

  const email = emailInput.value;
  const otp = otpInput.value;
  const newPass = newPassInput.value;

  fetch('/api/auth/reset-password?email=' + email + '&otp=' + otp + '&newPassword=' + newPass, { method: 'POST' })
    .then(res => {
      if (res.ok) {
        alert("Password reset successfully!");
        location.reload();
      } else {
        alert("Failed to reset password");
      }
    });
}

// Auto-dismiss alerts after 1 minute (60000ms) and clean URL
document.addEventListener('DOMContentLoaded', function () {
  const alerts = document.querySelectorAll('.alert');
  if (alerts.length > 0) {
    // Clear URL parameters so refresh doesn't show the message again
    const url = new URL(window.location.href);
    url.searchParams.delete('success');
    url.searchParams.delete('error');
    url.searchParams.delete('logout');
    url.searchParams.delete('error_register');
    window.history.replaceState({}, document.title, url.toString());

    // Auto-dismiss logic
    setTimeout(function () {
      alerts.forEach(function (alert) {
        // Check if bootstrap is available for smooth dismissal, otherwise just remove
        if (typeof bootstrap !== 'undefined' && bootstrap.Alert) {
          try {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
          } catch (e) {
            alert.remove();
          }
        } else {
          alert.remove();
        }
      });
    }, 60000);
  }

  // Real-time Validation for Contact Form
  const contactName = document.getElementById('contactName');
  const contactPhone = document.getElementById('contactPhone');
  const forms = document.querySelectorAll('.needs-validation');

  if (contactName) {
    contactName.addEventListener('input', function (e) {
      // Remove any character that is not a letter or space
      this.value = this.value.replace(/[^A-Za-z\s]/g, '');
    });
  }

  if (contactPhone) {
    contactPhone.addEventListener('input', function (e) {
      // Remove any non-digit character
      this.value = this.value.replace(/\D/g, '');
      // Limit to 10 digits
      if (this.value.length > 10) {
        this.value = this.value.slice(0, 10);
      }
    });
  }

  // Bootstrap custom validation
  Array.prototype.slice.call(forms).forEach(function (form) {
    form.addEventListener('submit', function (event) {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add('was-validated');
    }, false);
  });
});
