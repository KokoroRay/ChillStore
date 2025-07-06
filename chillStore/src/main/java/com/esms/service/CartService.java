package com.esms.service;

import com.esms.model.dto.CartItemDTO;

import java.util.List;

public interface CartService {
    void addItemToCart(int customerId, int productId, int quantity);
    List<CartItemDTO> getCartItems(int customerId);

    void updateQuantity(int cartId, int quantity);

    void deleteCartItem(int cartId);

    void addToCart(int customerId, int productId, int quantity);

    double calculateTotal(List<CartItemDTO> cartItems, Double voucherDiscountPct, Double voucherDiscountAmount);

}
