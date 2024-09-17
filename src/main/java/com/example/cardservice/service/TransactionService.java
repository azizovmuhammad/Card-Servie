package com.example.cardservice.service;

import com.example.cardservice.dto.response.Response;
import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.TransactionType;

public interface TransactionService {

    Response getTransactions(String cardId, TransactionType type, String transactionId, Currency currency, int page, int size);
}
