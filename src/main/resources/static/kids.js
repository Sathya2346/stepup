// JS for Kids Page

// Sorting dropdown (existing logic preservation assumed or new)
const sortToggle = document.getElementById('sortToggle');
const sortDropdown = document.getElementById('sortDropdown');

if (sortToggle) {
    sortToggle.addEventListener('click', () => {
        sortDropdown.classList.toggle('active');
    });
}

// Add to Cart / Buy redirection to details for size selection
document.querySelectorAll('.cart, .buy').forEach(button => {
    button.addEventListener('click', function () {
        const productId = this.getAttribute('data-id');
        window.location.href = '/product/' + productId;
    });
});
