package com.esms.service;

import com.esms.config.VNPayConfig;
import com.esms.dto.VNPayRequestDTO;
import com.esms.dto.VNPayResponseDTO;
import com.esms.service.impl.VNPayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private VNPayConfig vnPayConfig;

    @InjectMocks
    private VNPayServiceImpl vnPayService;

    @BeforeEach
    void setUp() {
        // Setup default VNPay config
        when(vnPayConfig.getTmnCode()).thenReturn("TEST123");
        when(vnPayConfig.getHashSecret()).thenReturn("TEST_SECRET");
        when(vnPayConfig.getUrl()).thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/payment/vnpay/callback");
        when(vnPayConfig.getIpnUrl()).thenReturn("http://localhost:8080/payment/vnpay/ipn");
        when(vnPayConfig.getCommand()).thenReturn("pay");
        when(vnPayConfig.getCurrCode()).thenReturn("VND");
        when(vnPayConfig.getLocale()).thenReturn("vn");
        when(vnPayConfig.getVersion()).thenReturn("2.1.0");
    }

    @Test
    void testCreatePaymentUrl() {
        // Given
        VNPayRequestDTO request = new VNPayRequestDTO();
        request.setOrderId("12345");
        request.setAmount(new BigDecimal("100000"));
        request.setOrderInfo("Test payment");
        request.setIpAddress("127.0.0.1");

        // When
        String paymentUrl = vnPayService.createPaymentUrl(request);

        // Then
        assertNotNull(paymentUrl);
        assertTrue(paymentUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        assertTrue(paymentUrl.contains("vnp_TxnRef=12345"));
        assertTrue(paymentUrl.contains("vnp_Amount=10000000")); // Amount * 100
        assertTrue(paymentUrl.contains("vnp_SecureHash="));
    }

    @Test
    void testProcessPaymentCallback_Success() {
        // Given
        String responseCode = "00";
        String orderId = "12345";
        String amount = "10000000"; // 100,000 VND * 100
        String secureHash = "test_hash";
        String bankTranNo = "123456789";
        String transactionNo = "987654321";
        String responseMessage = "Success";

        // When
        VNPayResponseDTO response = vnPayService.processPaymentCallback(
                responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
        );

        // Then
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(responseCode, response.getResponseCode());
        assertEquals(responseMessage, response.getResponseMessage());
        assertEquals(bankTranNo, response.getBankTranNo());
        assertEquals(transactionNo, response.getTransactionNo());
        assertEquals(new BigDecimal("100000"), response.getAmount());
        assertFalse(response.isSuccess()); // Should be false because hash validation will fail in test
    }

    @Test
    void testProcessPaymentCallback_Failure() {
        // Given
        String responseCode = "07";
        String orderId = "12345";
        String amount = "10000000";
        String secureHash = "test_hash";
        String bankTranNo = "";
        String transactionNo = "";
        String responseMessage = "Invalid amount";

        // When
        VNPayResponseDTO response = vnPayService.processPaymentCallback(
                responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
        );

        // Then
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(responseCode, response.getResponseCode());
        assertEquals(responseMessage, response.getResponseMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void testCreateSecureHash() {
        // Given
        String params = "vnp_Amount=10000000&vnp_Command=pay&vnp_CurrCode=VND&vnp_TxnRef=12345";

        // When
        String hash = vnPayService.createSecureHash(params);

        // Then
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertEquals(128, hash.length()); // SHA-512 hash length
    }

    @Test
    void testValidateSecureHash() {
        // Given
        String params = "vnp_Amount=10000000&vnp_Command=pay&vnp_CurrCode=VND&vnp_TxnRef=12345";
        String expectedHash = vnPayService.createSecureHash(params);

        // When
        boolean isValid = vnPayService.validateSecureHash(params, expectedHash);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateSecureHash_InvalidHash() {
        // Given
        String params = "vnp_Amount=10000000&vnp_Command=pay&vnp_CurrCode=VND&vnp_TxnRef=12345";
        String invalidHash = "invalid_hash";

        // When
        boolean isValid = vnPayService.validateSecureHash(params, invalidHash);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateCallback() {
        // Given
        String responseCode = "00";
        String orderId = "12345";
        String amount = "10000000";
        String secureHash = "test_hash";
        String bankTranNo = "123456789";
        String transactionNo = "987654321";
        String responseMessage = "Success";

        // When
        boolean isValid = vnPayService.validateCallback(
                responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
        );

        // Then
        assertFalse(isValid); // Should be false because hash validation will fail in test
    }

    @Test
    void testCreatePaymentUrl_WithCustomerInfo() {
        // Given
        VNPayRequestDTO request = new VNPayRequestDTO();
        request.setOrderId("12345");
        request.setAmount(new BigDecimal("50000"));
        request.setOrderInfo("Test payment with customer info");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setCustomerPhone("0123456789");
        request.setCustomerAddress("123 Test Street");
        request.setIpAddress("192.168.1.1");

        // When
        String paymentUrl = vnPayService.createPaymentUrl(request);

        // Then
        assertNotNull(paymentUrl);
        assertTrue(paymentUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        assertTrue(paymentUrl.contains("vnp_TxnRef=12345"));
        assertTrue(paymentUrl.contains("vnp_Amount=5000000")); // Amount * 100
        assertTrue(paymentUrl.contains("vnp_SecureHash="));
    }

    @Test
    void testProcessPaymentCallback_WithNullValues() {
        // Given
        String responseCode = "00";
        String orderId = "12345";
        String amount = null;
        String secureHash = "test_hash";
        String bankTranNo = null;
        String transactionNo = null;
        String responseMessage = null;

        // When
        VNPayResponseDTO response = vnPayService.processPaymentCallback(
                responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
        );

        // Then
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(responseCode, response.getResponseCode());
        assertNull(response.getAmount());
        assertFalse(response.isSuccess());
    }
} 