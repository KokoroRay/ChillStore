package com.esms.repository;

import com.esms.model.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    List<Voucher> findByCodeContainingIgnoreCaseDescriptionContainingIgnoreCase(String codeKeyword, String descKeyword);
    Optional<Voucher> findByCode(String code);
}
