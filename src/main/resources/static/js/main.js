// Загрузка товаров на главной странице
async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        const products = await response.json();

        const container = document.getElementById('products-container');
        if (!container) return;

        container.innerHTML = '';

        // Показываем первые 4 товара
        products.slice(0, 4).forEach(product => {
            const productCard = `
                <div class="product-card">
                    <div class="product-image">
                        <i class="fas fa-shoe-prints"></i>
                    </div>
                    <div class="product-info">
                        <h3>${product.name}</h3>
                        <p>${product.description || 'Стильная обувь'}</p>
                        <div class="product-price">${product.price ? product.price + ' ₽' : 'Цена не указана'}</div>
                        <button class="add-to-cart" onclick="addToCart(${product.id})">
                            <i class="fas fa-cart-plus"></i> Добавить в корзину
                        </button>
                    </div>
                </div>
            `;
            container.innerHTML += productCard;
        });
    } catch (error) {
        console.error('Ошибка загрузки товаров:', error);
    }
}

// Добавление в корзину (упрощенная версия)
function addToCart(productId) {
    alert(`Товар ${productId} добавлен в корзину! 🛍️`);
    // Здесь будет логика добавления в корзину через API
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();

    // Анимация сердечек
    const hearts = document.querySelectorAll('.fa-heart');
    hearts.forEach(heart => {
        heart.addEventListener('click', function() {
            this.classList.toggle('fas');
            this.classList.toggle('far');
            this.style.color = this.classList.contains('fas') ? '#e75480' : '';
        });
    });
});