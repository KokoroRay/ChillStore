document.addEventListener('DOMContentLoaded', function() {
    const customerSelect = document.getElementById('customerId');
    const orderSelect = document.getElementById('orderId');
    const productSelect = document.getElementById('productId');

    if (!customerSelect || !orderSelect || !productSelect) return;

    customerSelect.addEventListener('change', function() {
        const customerId = this.value;
        console.log('Customer changed:', customerId);
        orderSelect.innerHTML = '<option value="">Select an order</option>';
        orderSelect.disabled = true;
        productSelect.innerHTML = '<option value="">Select a product</option>';
        productSelect.disabled = true;
        if (!customerId) return;
        fetch(`/admin/maintenance/orders/by-customer/${customerId}`)
            .then(res => res.json())
            .then(orders => {
                console.log('Orders for customer:', orders);
                orders.forEach(order => {
                    orderSelect.innerHTML += `<option value="${order.orderId}">Order #${order.orderId} - ${order.customer.name} (${order.orderDate} - ${order.status})</option>`;
                });
                orderSelect.disabled = false;
            });
    });

    orderSelect.addEventListener('change', function() {
        const orderId = this.value;
        productSelect.innerHTML = '<option value="">Select a product</option>';
        productSelect.disabled = true;
        if (!orderId) return;
        fetch(`/admin/maintenance/products/by-order/${orderId}`)
            .then(res => res.json())
            .then(products => {
                console.log('Products for order:', products);
                products.forEach(product => {
                    productSelect.innerHTML += `<option value="${product.productId}">${product.name} - ${(product.category ? product.category.name : '')} (${(product.brand ? product.brand.name : '')})</option>`;
                });
                productSelect.disabled = false;
            });
    });

    if (!customerSelect.value) {
        orderSelect.disabled = true;
        productSelect.disabled = true;
    }
}); 