package com.esms.service.impl;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Cart;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Product;
import com.esms.repository.CartRepository;
import com.esms.repository.CustomerRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.CartService;
import com.esms.service.ProductService;
import com.esms.model.entity.Discount;
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

    @Autowired
    private ProductService productService;

    @Override
    public void addToCart(int customerId, int productId, int quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra số lượng tồn kho
        if (product.getStockQty() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQty() + ", Requested: " + quantity);
        }

        Optional<Cart> optionalCart = cartRepository.findByCustomerAndProduct(customer.getCustomerId(), product.getProductId());

        Cart cartItem;
        if (optionalCart.isPresent()) {
            // Nếu sản phẩm đã có trong giỏ
            cartItem = optionalCart.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            
            // Kiểm tra lại tổng số lượng sau khi cộng thêm
            if (product.getStockQty() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQty() + ", Total requested: " + newQuantity);
            }
            
            cartItem.setQuantity(newQuantity);
        } else {
            // Nếu chưa có
            cartItem = new Cart(customer, product, quantity);
            cartItem.setCreatedAt(LocalDateTime.now());
        }

        cartRepository.save(cartItem);
    }

    @Override
    public List<CartItemDTO> getCartItems(int customerId) {
        List<CartItemDTO> items = cartRepository.findCartItemsByCustomerId(customerId);
        for (CartItemDTO item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                Discount discount = productService.getActiveDiscountForProduct(product);
                if (discount != null && discount.getDiscountPct() != null) {
                    double discountPct = discount.getDiscountPct().doubleValue() / 100.0;
                    double discountedPrice = product.getPrice().doubleValue() * (1 - discountPct);
                    item.setDiscount(discountPct);
                    item.setPrice(discountedPrice);
                    item.setTotalPrice(discountedPrice * item.getQuantity());
                } else {
                    item.setDiscount(0.0);
                    item.setPrice(product.getPrice().doubleValue());
                    item.setTotalPrice(product.getPrice().doubleValue() * item.getQuantity());
                }
            }
        }
        return items;
    }

    @Override
    public void updateQuantity(int cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null && quantity > 0) {
            // Kiểm tra số lượng tồn kho
            Product product = cart.getProduct();
            if (product.getStockQty() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQty() + ", Requested: " + quantity);
            }
            
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
