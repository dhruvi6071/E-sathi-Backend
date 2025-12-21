package com.example.ESathi.controllers;

import com.example.ESathi.DTO.BillReceiptResponseDTO;
import com.example.ESathi.DTO.RazorpayVerifyDTO;
import com.example.ESathi.Serivces.PaymentService;
import com.example.ESathi.model.Bill;
import com.example.ESathi.model.Payments;
import com.example.ESathi.model.User;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.PaymentRepository;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.utils.ApiResponse;
import com.example.ESathi.utils.customeExeptions.ResourceNotFoundException;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService,
                             BillRepository billRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/payment/create/{billId}")
    public ApiResponse<Map<String, Object>> createPaymentOrder(
            @PathVariable Long billId,
            Principal principal
    ) throws RazorpayException {
        String email = principal.getName();
        Map<String, Object> response = paymentService.createPaymentOrder(billId, email);
        return ApiResponse.success(response);
    }

    //verifying payment
    @PostMapping("/payment/verify/{billId}")
    public ApiResponse<?> verifyPayment(
            @PathVariable Long billId,
            @RequestBody RazorpayVerifyDTO verifyDTO,
            Principal principal
    ) {
        try {
            paymentService.verifyAndSavePayment(
                    verifyDTO.getRazorpayOrderId(),
                    verifyDTO.getRazorpayPaymentId(),
                    verifyDTO.getRazorpaySignature(),
                    billId,
                    verifyDTO.getMethod(),
                    verifyDTO.getTransactionRef(),
                    principal.getName()
            );

            return ApiResponse.success("Payment verified and recorded.");
        } catch (Exception e) {
            return ApiResponse.error(
                    "Payment Verification faild" + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    //genearate bill-Receipt for successPayment
    @GetMapping("/bill-receipt/{billId}")
    public ApiResponse<BillReceiptResponseDTO> getBillReceiptData(@PathVariable Long billId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + principal.getName()));

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found" + billId));

        Payments payment = paymentRepository.findByUserAndBill(user, bill)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));


        BillReceiptResponseDTO billReceiptResponseDTO = BillReceiptResponseDTO.builder()
                .status("SUCCESS")
                .billPaymentDate(payment.getPaidDate())
                .billMoth(bill.getBillingMonth())
                .method(payment.getMethod())
                .amountPaid(payment.getAmountPaid())
                .userName(user.getName())
                .transacrtionalRef(payment.getTransationalRef())
                .unitConsume(bill.getUnitConsume())
                .unitConsume(bill.getUnitConsume())
                .userId(payment.getUser().getUserID())
                .build();

        return ApiResponse.success(billReceiptResponseDTO);
    }
}
