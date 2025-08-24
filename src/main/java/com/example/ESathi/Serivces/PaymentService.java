package com.example.ESathi.Serivces;

import com.example.ESathi.model.Bill;
import com.example.ESathi.model.Payments;
import com.example.ESathi.model.User;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.PaymentRepository;
import com.example.ESathi.repositories.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@Service
public class PaymentService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;


    public PaymentService(BillRepository billRepository,
                          UserRepository userRepository,
                          PaymentRepository paymentRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }
    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;


    public Map<String, Object> createPaymentOrder(Long billId, String userEmail) throws RazorpayException {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));

        // Get bill
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        // Check if the bill belongs to the user
        if (!bill.getUser().getUserID().equals(user.getUserID())) {
            throw new RuntimeException("Unauthorized access to bill");
        }

        // Initialize Razorpay client
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Prepare Razorpay order
        double actualAmount = bill.getAmountDue();
        int amountInPaisa = (int) Math.round(actualAmount * 100 );
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaisa); // in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "bill_" + bill.getBillId());
        orderRequest.put("payment_capture", 1);

        System.out.println("Bill Amount: " + bill.getAmountDue());
        System.out.println("Amount in paisa (sent to Razorpay): " + amountInPaisa);


        Order order = razorpay.orders.create(orderRequest);

        // Return response payload
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", bill.getAmountDue());
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());
        response.put("unitsUsed", bill.getUnitConsume());
        response.put("billingMonth", bill.getBillingMonth().toString());
        response.put("billId", bill.getBillId());

        return response;
    }


    //payment verification and add into payment table
    public void verifyAndSavePayment(String razorpayOrderId,
                                     String razorpayPaymentId,
                                     String razorpaySignature,
                                     Long billId,
                                     String method,
                                     String transactionRef,
                                     String email) throws Exception {

        // Step 1: Verify signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String generatedSignature = hmacSHA256(payload, razorpayKeySecret);

        if (!generatedSignature.equals(razorpaySignature)) {
            throw new Exception("Invalid payment signature!");
        }

        // Step 2: Fetch user and bill
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found when payment verify"));
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        // Step 3: Update bill
        bill.setStatus(Bill.Status.PAID);
        bill.setPaymentDate(YearMonth.now());
        billRepository.save(bill);

        // Step 4: Save payment record
        Payments payment = new Payments();
        payment.setUser(user);
        payment.setBill(bill);
        payment.setAmountPaid(bill.getAmountDue());
        payment.setPaidDate(LocalDateTime.now());
        payment.setMethod(Payments.Methods.valueOf(method.toUpperCase()));
        payment.setTransationalRef(transactionRef);

        paymentRepository.save(payment);
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
