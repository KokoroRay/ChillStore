package com.esms.service;

import com.esms.model.dto.VoucherDto;
import com.esms.model.entity.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherService {

    List<Voucher> getAllVouchers();
    List<Voucher> searchVouchers(String keyword);
    Optional<Voucher> getVoucherById(Integer id);
    Voucher createVoucher(VoucherDto voucherDto, String createdByEmail);
    Voucher updateVoucher(Integer id, VoucherDto voucherDto);
    void deleteVoucher(Integer id);

}
