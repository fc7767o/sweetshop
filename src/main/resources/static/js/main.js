// Глобальные переменные
let currentCartId = null;
let currentUserId = null;

// Проверяем авторизацию при загрузке
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();
    checkAuth();
    setupCart();
});

// Проверка авторизации
function checkAuth() {
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (user) {
        currentUserId = user.id;
        // Показываем кнопку профиля вместо входа
        const authLinks = document.querySelectorAll('nav a[href="/login.html"]');
        authLinks.forEach(link => {
            link.innerHTML = '<i class="fas fa-user"></i> Профиль';
            link.href = '#';
            link.onclick = () => {
                alert(`Вы вошли как: ${user.name}`);
            };
        });
    }
}

// Настройка корзины
async function setupCart() {
    // Для неавторизованных - используем sessionId
    const sessionId = localStorage.getItem('sessionId') || generateSessionId();
    localStorage.setItem('sessionId', sessionId);

    try {
        const response = await fetch(`/api/cart/session/${sessionId}`);
        if (response.ok) {
            const cartData = await response.json();
            currentCartId = cartData.cartId;
            updateCartCounter(cartData.items);
        }
    } catch (error) {
        console.error('Ошибка загрузки корзины:', error);
    }
}

// Генерация ID сессии
function generateSessionId() {
    return 'session_' + Math.random().toString(36).substr(2, 9);
}

// Обновить счетчик товаров в корзине
function updateCartCounter(items) {
    const totalItems = items ? items.reduce((sum, item) => sum + item.quantity, 0) : 0;
    const cartLinks = document.querySelectorAll('nav a[href="/cart.html"]');
    cartLinks.forEach(link => {
        if (totalItems > 0) {
            link.innerHTML = `<i class="fas fa-shopping-cart"></i> Корзина (${totalItems})`;
        }
    });
}

// Загрузка товаров
async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        const products = await response.json();

        const container = document.getElementById('products-container');
        const catalogContainer = document.getElementById('catalog-products');

        if (container) {
            container.innerHTML = '';
            products.slice(0, 4).forEach(createProductCard);
        }

        if (catalogContainer) {
            catalogContainer.innerHTML = '';
            products.forEach(createProductCard);
        }
    } catch (error) {
        console.error('Ошибка загрузки товаров:', error);
    }
}

// Создание карточки товара
function createProductCard(product) {
    const container = document.getElementById('products-container') ||
                     document.getElementById('catalog-products');

    const productCard = document.createElement('div');
    productCard.className = 'product-card';
    productCard.innerHTML = `
        <div class="product-image" style="background: #${getRandomColor()}22">
            <i class="fas fa-shoe-prints" style="color: #${getRandomColor()}"></i>
        </div>
        <div class="product-info">
            <h3>${product.name}</h3>
            <p>${product.description || 'Стильная обувь'}</p>
            <p><small>${product.brand || 'Бренд не указан'} | ${product.category || 'Обувь'}</small></p>
            <div class="product-price">${product.price ? product.price + ' ₽' : 'Цена не указана'}</div>
            <button class="add-to-cart" onclick="addToCart(${product.id})">
                <i class="fas fa-cart-plus"></i> Добавить в корзину
            </button>
            <button class="btn-pink" style="margin-top: 10px; width: 100%;"
                    onclick="viewProduct(${product.id})">
                <i class="fas fa-eye"></i> Подробнее
            </button>
        </div>
    `;

    if (container) {
        container.appendChild(productCard);
    }
}

// Случайный цвет для карточек
function getRandomColor() {
    const colors = ['ff85a2', 'e75480', 'd6a4e8', 'ffb6c1', 'ff69b4', 'ff1493'];
    return colors[Math.floor(Math.random() * colors.length)];
}

// Добавление в корзину
async function addToCart(productId) {
    if (!currentCartId) {
        await setupCart();
    }

    try {
        // Для упрощения берем первый доступный размер
        const response = await fetch(`/api/products/${productId}/sizes`);
        const sizes = await response.json();

        if (sizes.length > 0) {
            const sizeId = sizes[0].id;

            const addResponse = await fetch('/api/cart/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    cartId: currentCartId,
                    productId: productId,
                    sizeId: sizeId,
                    quantity: 1
                })
            });

            if (addResponse.ok) {
                const item = await addResponse.json();
                alert('Товар добавлен в корзину! 🛍️');

                // Обновляем счетчик
                const cartResponse = await fetch(`/api/cart/session/${localStorage.getItem('sessionId')}`);
                if (cartResponse.ok) {
                    const cartData = await cartResponse.json();
                    updateCartCounter(cartData.items);
                }
            }
        } else {
            alert('Нет доступных размеров');
        }
    } catch (error) {
        console.error('Ошибка добавления в корзину:', error);
        alert('Ошибка добавления в корзину');
    }
}

// Просмотр товара
function viewProduct(productId) {
    // Временное решение - показываем информацию в alert
    fetch(`/api/products/${productId}`)
        .then(response => response.json())
        .then(product => {
            alert(`
                Название: ${product.name}
                Цена: ${product.price} ₽
                Категория: ${product.category}
                Бренд: ${product.brand}
                Описание: ${product.description || 'Нет описания'}
            `);
        });
}