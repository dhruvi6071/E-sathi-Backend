package com.example.ESathi.DTO.UserNeedDTO;

import com.example.ESathi.model.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllBillResponseDTO {

    private Page<PendingBillsResponseDTO> pendingBills ;
    private Page<PaidBillResponseDTO> paidBill ;
}
