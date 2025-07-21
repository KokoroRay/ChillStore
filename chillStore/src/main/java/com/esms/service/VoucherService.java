package com.esms.service;

import com.esms.model.dto.VoucherDTO;
import com.esms.model.entity.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherService {

    List<Voucher> getAllVouchers();
    List<Voucher> searchVouchers(String keyword);
    Optional<Voucher> getVoucherById(Integer id);
    Voucher createVoucher(VoucherDTO voucherDto, String createdByEmail);
    Voucher updateVoucher(Integer id, VoucherDTO voucherDto);
    void deleteVoucher(Integer id);
    Voucher getVoucherByCode(String code);
    Voucher updateVoucher(Voucher voucher);

}
