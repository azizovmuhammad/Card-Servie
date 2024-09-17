package com.example.cardservice.controller;

import com.example.cardservice.dto.CardRequest;
import com.example.cardservice.dto.TransactionDto;
import com.example.cardservice.dto.response.Response;
import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.TransactionType;
import com.example.cardservice.service.CardService;
import com.example.cardservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService cardService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardRequest request,
                                        @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        Response response = cardService.createCard(request, idempotencyKey);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable("cardId") UUID cardId) {
        Response response = cardService.getCard(cardId);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable UUID cardId) {
        Response response = cardService.blockCard(cardId);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<?> unblockCard(@PathVariable UUID cardId) {
        Response response = cardService.unblockCard(cardId);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping("/{cardId}/debit")
    public ResponseEntity<?> debit(@PathVariable("cardId") UUID cardId,
                                   @RequestBody TransactionDto dto) {
        Response response = cardService.debit(cardId, dto);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping("/{cardId}/credit")
    public ResponseEntity<?> credit(@PathVariable("cardId") UUID cardId,
                                    @RequestBody TransactionDto dto) {
        Response response = cardService.credit(cardId, dto);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable String cardId,
                                             @RequestParam(value = "type", required = false) TransactionType type,
                                             @RequestParam(value = "transaction_id", required = false) String transactionId,
                                             @RequestParam(value = "currency", required = false) Currency currency,
                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size) {

        Response response = transactionService.getTransactions(cardId, type, transactionId, currency, page, size);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}
