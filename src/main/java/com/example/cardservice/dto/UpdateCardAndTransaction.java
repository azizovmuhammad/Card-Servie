package com.example.cardservice.dto;

import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.Purpose;
import com.example.cardservice.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCardAndTransaction {
    private String cardId;
    private String transactionId;
    private String externalId;
    private Long afterBalance;
    private Long amount;
    private Currency currency;
    private Purpose purpose;
    private Long exchangeRate;
    private TransactionType type;
}
