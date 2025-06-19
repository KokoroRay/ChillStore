package com.esms.service.impl;

import com.esms.model.dto.VoucherDto;
import com.esms.model.entity.Admin;
import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Voucher;
import com.esms.repository.AdminRepository;
import com.esms.repository.BrandRepository;
import com.esms.repository.CategoryRepository;
import com.esms.repository.VoucherRepository;
import com.esms.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Transient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transient
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository  voucherRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public List<Voucher> searchVouchers(String keyword) {
        if(keyword == null || keyword.isEmpty()) {
            return voucherRepository.findAll();
        }
        String kw = keyword.trim();
        return voucherRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(kw, kw);
    }

    @Override
    public Optional<Voucher> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    @Override
    public Voucher createVoucher(VoucherDto voucherDto, String createdByEmail) {
        if (voucherRepository.findByCode(voucherDto.getCode()).isPresent()) {
            throw new RuntimeException("Voucher da ton tai");
        }
        if (voucherDto.getEnd_date().isBefore(voucherDto.getStart_date())) {
            throw new RuntimeException("ngay bat dau phai truoc ngay ket thuc");
        }
        Voucher voucher = new Voucher();
        voucher.setCode(voucherDto.getCode());
        voucher.setDescription(voucherDto.getDescription());
        voucher.setDiscount_amount(voucherDto.getDiscount_amount());
        voucher.setDiscount_pct(voucherDto.getDiscount_pct());
        voucher.setMin_order_amount(voucherDto.getMin_order_amount());
        voucher.setQuantity_available(voucherDto.getQuantity_available());
        voucher.setStart_date(voucherDto.getStart_date());
        voucher.setEnd_date(voucherDto.getEnd_date());
        voucher.setActive(voucherDto.isActive());

        if (voucherDto.getCategoryIds() != null && !voucherDto.getCategoryIds().isEmpty()) {
            List<Category> categories  = categoryRepository.findAllById(voucherDto.getCategoryIds());
            voucher.setCategories(new HashSet<>(categories));
        }

        if (voucherDto.getBrandIds() != null && !voucherDto.getBrandIds().isEmpty()) {
            List<Brand> brands = brandRepository.findAllById(voucherDto.getBrandIds());
            voucher.setBrands(new HashSet<>(brands));
        }

        Admin admin = adminRepository.findByEmail(createdByEmail).orElseThrow(() -> new RuntimeException("Admin not found"));
        voucher.setCreated_by(admin);
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher updateVoucher(Integer id, VoucherDto voucherDto) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("Voucher khong ton tai"));
        if (!voucher.getCode().equals(voucherDto.getCode())) {
            if (voucherRepository.findByCode(voucherDto.getCode()).isPresent()) {
                throw new RuntimeException("Voucher da ton tai");
            }
            voucher.setCode(voucherDto.getCode());
        }
        if (voucherDto.getEnd_date().isBefore(voucherDto.getStart_date())) {
            throw new RuntimeException("ngay bat dau phai truoc ngay ket thuc");
        }

        voucher.setDescription(voucherDto.getDescription());
        voucher.setDiscount_amount(voucherDto.getDiscount_amount());
        voucher.setDiscount_pct(voucherDto.getDiscount_pct());
        voucher.setMin_order_amount(voucherDto.getMin_order_amount());
        voucher.setQuantity_available(voucherDto.getQuantity_available());
        voucher.setStart_date(voucherDto.getStart_date());
        voucher.setEnd_date(voucherDto.getEnd_date());
        voucher.setActive(voucherDto.isActive());
        voucher.getCategories().clear();
        if (voucherDto.getCategoryIds() != null && !voucherDto.getCategoryIds().isEmpty()) {
            List<Category> categories  = categoryRepository.findAllById(voucherDto.getCategoryIds());
            voucher.setCategories(new HashSet<>(categories));
        }
        voucher.getBrands().clear();
        if (voucherDto.getBrandIds() != null && !voucherDto.getBrandIds().isEmpty()) {
            List<Brand> brands = brandRepository.findAllById(voucherDto.getBrandIds());
            voucher.setBrands(new HashSet<>(brands));
        }
        return voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher khong ton tai"));
        voucherRepository.delete(voucher);
    }
}
