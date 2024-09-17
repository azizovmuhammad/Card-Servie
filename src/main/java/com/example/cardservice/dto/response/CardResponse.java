package com.example.cardservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private UUID cardId;
    private Long userId;
    private String status;
    private Long balance;
    private String currency;
}
