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

import java.util.List;

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
    public void addToCart(int customerId, int productId, int quantity) {
        Cart existingCart = cartRepository.findByCustomerAndProduct(customerId, productId);
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cartRepository.save(existingCart);
        } else {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            Product product = productRepository.findById(productId).orElse(null);
            if (customer != null && product != null) {
                Cart cart = new Cart();
                cart.setCustomer(customer);
                cart.setProduct(product);
                cart.setQuantity(quantity);
                cartRepository.save(cart);
            }
        }
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

}
