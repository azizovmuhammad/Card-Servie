package com.example.cardservice.utils;

import com.example.cardservice.dto.response.CardResponse;
import com.example.cardservice.entity.Card;

public class Utils {
    public static CardResponse fromEntity(Card card) {
        return new CardResponse(
                card.getId(),
                card.getUserId(),
                card.getStatus().toString(),
                card.getBalance(),
                card.getCurrency().toString()
        );
    }
}
