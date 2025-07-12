package com.esms.service.impl;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Cart;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Product;
import com.esms.repository.CartRepository;
import com.esms.repository.CustomerRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void addToCart(int customerId, int productId, int quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Cart> optionalCart = cartRepository.findByCustomerAndProduct(customer.getCustomerId(), product.getProductId());

        Cart cartItem;
        if (optionalCart.isPresent()) {
            // Nếu sản phẩm đã có trong giỏ
            cartItem = optionalCart.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Nếu chưa có
            cartItem = new Cart(customer, product, quantity);
            cartItem.setCreatedAt(LocalDateTime.now());
        }

        cartRepository.save(cartItem);
    }

    @Override
    public List<CartItemDTO> getCartItems(int customerId) {
        return cartRepository.findCartItemsByCustomerId(customerId);
    }

    @Override
    public void updateQuantity(int cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null && quantity > 0) {
            cart.setQuantity(quantity);
            cartRepository.save(cart);
        }
    }

    @Override
    public void deleteCartItem(int cartId) {
        cartRepository.deleteById(cartId);
    }


    @Override
    public double calculateTotal(List<CartItemDTO> cartItems, Double voucherDiscountPct, Double voucherDiscountAmount) {
        double total = 0.0;
        for (CartItemDTO item : cartItems) {
            double priceAfterDiscount = item.getPrice() * (1 - item.getDiscount());
            total += priceAfterDiscount * item.getQuantity();
        }

        if (voucherDiscountAmount != null && voucherDiscountAmount > 0) {
            total -= voucherDiscountAmount;
        } else if (voucherDiscountPct != null && voucherDiscountPct > 0) {
            total = total * (1 - voucherDiscountPct);
        }

        return Math.max(total, 0);
    }

    @Override
    public void clearCart(int customerId) {
        cartRepository.deleteByCustomerCustomerId(customerId);
    }

}
