package com.esms.repository;

import com.esms.model.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    List<Voucher> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String codeKeyword, String descKeyword);
    Optional<Voucher> findByCode(String code);

    @Query("SELECT MAX(v.voucher_id) FROM Voucher v")
    Integer findMaxId();
}
