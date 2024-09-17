package com.example.cardservice.service;

import com.example.cardservice.dto.CardRequest;
import com.example.cardservice.dto.TransactionDto;
import com.example.cardservice.dto.response.Response;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CardService {

    Response createCard(CardRequest request, String idempotencyKey);

    Response getCard(UUID cardId);

    Response debit(UUID cardId, TransactionDto dto);

    Response credit(UUID cardId, TransactionDto dto);

    Response blockCard(UUID cardId);

    Response unblockCard(UUID cardId);
}
