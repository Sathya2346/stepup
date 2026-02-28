// Main Script

// Swiper
if (typeof Swiper !== 'undefined' && document.querySelector('.heroSwiper')) {
  var swiper = new Swiper(".heroSwiper", {
    pagination: {
      el: ".swiper-pagination",
      clickable: true,
    },
    autoplay: {
      delay: 3000,
      disableOnInteraction: false,
    },
    loop: true,
  });
}

// Add to Cart Logic (for Details page)
document.querySelectorAll('.add-cart-btn-details').forEach(button => {
  button.addEventListener('click', function () {
    const productId = this.getAttribute('data-id');
    const sizeSelector = document.getElementById('sizeSelector');
    const size = sizeSelector ? sizeSelector.value : null;

    if (sizeSelector && !size) return alert('Please select a size');

    const params = new URLSearchParams({
      productId: productId,
      quantity: 1,
      size: size || ''
    });

    fetch('/cart/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params
    })
      .then(response => {
        if (response.redirected && response.url.includes('/login')) {
          window.location.href = '/login';
          return;
        }
        if (response.ok) {
          alert('Item added to cart!');
        } else if (response.status === 401) {
          window.location.href = '/login';
        } else {
          alert('Failed to add item to cart.');
        }
      })
      .catch(error => console.error('Error:', error));
  });
});

// Add to Cart Logic (for Index page products) - Redirect to details for size
document.querySelectorAll('.add-cart-btn').forEach(button => {
  button.addEventListener('click', function () {
    const productId = this.getAttribute('data-id');
    window.location.href = '/product/' + productId;
  });
});
