package com.esms.repository;

import com.esms.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByCustomerCustomerId(Integer customerId);
    boolean existsByCustomerCustomerIdAndProductProductId(Integer customerId, Integer productId);
    void deleteByCustomerCustomerIdAndProductProductId(Integer customerId, Integer productId);
}