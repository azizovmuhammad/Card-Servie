package com.example.cardservice.dto;

import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardRequest {
    private Long userId;
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    private Long balance = 0L;
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.UZS;
}
