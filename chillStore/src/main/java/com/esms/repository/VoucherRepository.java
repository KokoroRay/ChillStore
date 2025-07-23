package com.esms.repository;

import com.esms.model.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    List<Voucher> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String codeKeyword, String descKeyword);
    Optional<Voucher> findByCode(String code);

    @Query("SELECT MAX(v.voucher_id) FROM Voucher v")
    Integer findMaxId();

    @Query("SELECT v FROM Voucher v WHERE v.isSpecial = false  AND v.active = true AND CURRENT_DATE BETWEEN  v.start_date AND v.end_date")
    List<Voucher> findAutoApplyVouchers();

    @Query("SELECT v FROM Voucher v WHERE v.isSpecial = true AND v.active = true AND v.start_date <= current_date AND v.end_date >= current_date " +
            "AND v.voucher_id IN (SELECT o.voucher.voucher_id FROM Order o WHERE o.customer.customerId = :customerId AND o.isVoucherAcquisition = true)")
    List<Voucher> findCustomerSpecialVoucher(@Param("customerId") Integer customerId);

}
