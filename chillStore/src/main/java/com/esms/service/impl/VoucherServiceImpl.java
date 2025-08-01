package com.esms.service.impl;

import com.esms.model.dto.VoucherDTO;
import com.esms.model.entity.Admin;
import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Voucher;
import com.esms.repository.AdminRepository;
import com.esms.repository.BrandRepository;
import com.esms.repository.CategoryRepository;
import com.esms.repository.VoucherRepository;
import com.esms.service.CustomerService;
import com.esms.service.VoucherService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Transient;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomerService customerService;

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
    public Voucher createVoucher(VoucherDTO voucherDto, String createdByEmail) {
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
        voucher.setSpecial(voucherDto.isSpecial());

        if (voucherDto.getCategoryIds() != null && !voucherDto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(voucherDto.getCategoryIds());
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
    public Voucher updateVoucher(Integer id, VoucherDTO voucherDto) {
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
        Optional <Voucher> voucherOptional = voucherRepository.findById(id);
        if (voucherOptional.isPresent()){
            voucherRepository.delete(voucherOptional.get());
            Integer maxId = voucherRepository.findMaxId();
            reseedIdentityTo(maxId);
        } else {
            throw new RuntimeException("Voucher khong ton tai" + id);
        }
    }

    private void reseedIdentityTo(Integer newSeed) {
        String table = "voucher";
        int seed = (newSeed != null) ? newSeed : 0;
        String sql = String.format("DBCC CHECKIDENT('%s', RESEED, %d)", table, seed);
        jdbcTemplate.execute(sql);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code).orElse(null);
    }

    @Override
    public Voucher updateVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public List<Voucher> getVoucherByCustomerId(Integer customerId) {
        List<Voucher> customerVouchers = new ArrayList<>();
        customerVouchers.addAll(voucherRepository.findAutoApplyVouchers());

        List<Voucher> specialVouchers = voucherRepository.findCustomerSpecialVoucher(customerId);
        customerVouchers.addAll(specialVouchers);
        LocalDate today = LocalDate.now();
        return customerVouchers.stream().filter(v -> v.getEnd_date().isAfter(today) || v.getEnd_date().isEqual(today) ).collect(Collectors.toList());

    }

    @Override
    public void applySpecialVoucher(Integer customerId, String code) {
        Voucher voucher = voucherRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Voucher not found"));
        if (!voucher.isSpecial())  {
            throw new RuntimeException("This is not special voucher");
        }
        if (voucher.getQuantity_available() <= 0) {
            throw new RuntimeException("Voucher is out of stock");
        }

        customerService.addVoucherToCustomer(customerId, voucher.getVoucher_id());
        voucher.setQuantity_available(voucher.getQuantity_available() - 1);
        voucherRepository.save(voucher);
    }
}
