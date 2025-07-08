package com.esms.repository;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Cart;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends CrudRepository<Cart, Integer> {
    @Query("SELECT c FROM Cart c WHERE c.customer.customerId = :customerId AND c.product.productId = :productId")
    Optional<Cart> findByCustomerAndProduct(Customer customer, Product product);

    @Query("SELECT new com.esms.model.dto.CartItemDTO(c.id, p.name, " +
            "CAST(p.price AS double), 0.0, c.quantity, " +
            "CAST(p.price * c.quantity AS double)) " +
            "FROM Cart c JOIN c.product p WHERE c.customer.customerId = :customerId")
    List<CartItemDTO> findCartItemsByCustomerId(@Param("customerId") int customerId);

}
