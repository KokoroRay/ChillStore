package com.esms.repository;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Cart;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends CrudRepository<Cart, Integer> {
    @Query("SELECT c FROM Cart c WHERE c.customer.customerId = :customerId AND c.product.productId = :productId")
    Cart findByCustomerAndProduct(@Param("customerId") int customerId, @Param("productId") int productId);

    @Query("SELECT new com.esms.model.dto.CartItemDTO(c.id, p.name, " +
            "CAST(p.price AS double), 0.0, c.quantity, " +
            "CAST(p.price * c.quantity AS double)) " +
            "FROM Cart c JOIN c.product p WHERE c.customer.customerId = :customerId")
    List<CartItemDTO> findCartItemsByCustomerId(@Param("customerId") int customerId);

}
