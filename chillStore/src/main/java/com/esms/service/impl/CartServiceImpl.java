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
        if (quantity > 99) {
            quantity = 99; // Tự động điều chỉnh về 99 thay vì ném lỗi
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra nếu hết hàng
        if (product.getStockQty() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        // Điều chỉnh số lượng theo tồn kho nếu vượt quá
        if (quantity > product.getStockQty()) {
            quantity = product.getStockQty();
        }
        
        // Kiểm tra giới hạn tổng tiền trước khi thêm
        double estimatedTotal = quantity * product.getPrice().doubleValue();
        if (estimatedTotal > 9999999999999.99) {
            // Tính số lượng tối đa có thể thêm
            int maxQuantityByPrice = (int) (9999999999999.99 / product.getPrice().doubleValue());
            quantity = Math.min(quantity, maxQuantityByPrice);
        }

        Optional<Cart> optionalCart = cartRepository.findByCustomerAndProduct(customer.getCustomerId(), product.getProductId());

        Cart cartItem;
        if (optionalCart.isPresent()) {
            // Nếu sản phẩm đã có trong giỏ
            cartItem = optionalCart.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            
            // Điều chỉnh số lượng tối đa theo tồn kho
            if (newQuantity > product.getStockQty()) {
                newQuantity = product.getStockQty();
            }
            
            // Điều chỉnh số lượng tối đa theo giới hạn 99
            if (newQuantity > 99) {
                newQuantity = 99;
            }
            
            // Kiểm tra giới hạn tổng tiền cho sản phẩm này
            double totalForThisProduct = newQuantity * product.getPrice().doubleValue();
            if (totalForThisProduct > 9999999999999.99) {
                int maxQuantityByPrice = (int) (9999999999999.99 / product.getPrice().doubleValue());
                newQuantity = Math.min(newQuantity, maxQuantityByPrice);
            }
            
            cartItem.setQuantity(newQuantity);
        } else {
            // Nếu chưa có, điều chỉnh số lượng theo giới hạn tối đa
            int finalQuantity = Math.min(Math.min(quantity, product.getStockQty()), 99);
            
            // Kiểm tra giới hạn tổng tiền
            double totalForThisProduct = finalQuantity * product.getPrice().doubleValue();
            if (totalForThisProduct > 9999999999999.99) {
                int maxQuantityByPrice = (int) (9999999999999.99 / product.getPrice().doubleValue());
                finalQuantity = Math.min(finalQuantity, maxQuantityByPrice);
            }
            
            cartItem = new Cart(customer, product, finalQuantity);
            cartItem.setCreatedAt(LocalDateTime.now());
        }

        cartRepository.save(cartItem);
    }

    @Override
    public List<CartItemDTO> getCartItems(int customerId) {
        List<CartItemDTO> items = cartRepository.findCartItemsByCustomerId(customerId);
        for (int i = 0; i < items.size(); i++) {
            CartItemDTO item = items.get(i);
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
                item.setStockQty(product.getStockQty());
            }
        }
        return items;
    }

    @Override
    public void updateQuantity(int cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null && quantity > 0) {
            Product product = cart.getProduct();
            
            // Điều chỉnh số lượng theo tồn kho và giới hạn tối đa
            int finalQuantity = Math.min(Math.min(quantity, product.getStockQty()), 99);
            
            // Nếu số lượng sau điều chỉnh là 0 hoặc âm, xóa item khỏi giỏ hàng
            if (finalQuantity <= 0) {
                cartRepository.deleteById(cartId);
            } else {
                cart.setQuantity(finalQuantity);
                cartRepository.save(cart);
            }
        }
    }

    @Override
    public void deleteCartItem(int cartId) {
        cartRepository.deleteById(cartId);
    }


    @Override
    public double calculateTotal(List<CartItemDTO> cartItems, Double voucherDiscountPct, Double voucherDiscountAmount) {
        double subtotal = 0.0;
        for (CartItemDTO item : cartItems) {
            // item.getPrice() đã bao gồm discount của sản phẩm rồi
            subtotal += item.getTotalPrice();
        }

        // Áp dụng voucher discount
        if (voucherDiscountAmount != null && voucherDiscountAmount > 0) {
            subtotal -= voucherDiscountAmount;
        } else if (voucherDiscountPct != null && voucherDiscountPct > 0) {
            // voucherDiscountPct đã được chia cho 100 rồi (0.1 = 10%)
            subtotal = subtotal * (1 - voucherDiscountPct);
        }

        return Math.max(subtotal, 0);
    }

    @Override
    public void clearCart(int customerId) {
        cartRepository.deleteByCustomerCustomerId(customerId);
    }
    
    /**
     * Tính toán số lượng tối đa có thể thêm vào giỏ hàng
     * Dựa trên tồn kho và giới hạn tối đa 99 sản phẩm
     */
    private int calculateMaxQuantity(int requestedQuantity, int stockQty, int currentCartQuantity) {
        // Tổng số lượng sau khi thêm
        int totalAfterAdd = currentCartQuantity + requestedQuantity;
        
        // Giới hạn theo tồn kho
        int maxByStock = Math.min(totalAfterAdd, stockQty);
        
        // Giới hạn theo quy định tối đa 99
        int maxByLimit = Math.min(maxByStock, 99);
        
        return Math.max(maxByLimit, 0);
    }

}
