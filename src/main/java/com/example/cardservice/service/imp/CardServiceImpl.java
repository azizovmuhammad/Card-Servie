package com.example.cardservice.service.imp;

import com.example.cardservice.dto.CardRequest;
import com.example.cardservice.dto.Mapper.CardMapper;
import com.example.cardservice.dto.Mapper.ResponseMapper;
import com.example.cardservice.dto.TransactionDto;
import com.example.cardservice.dto.response.CardResponse;
import com.example.cardservice.dto.response.Response;
import com.example.cardservice.entity.Card;
import com.example.cardservice.entity.IdempotencyKey;
import com.example.cardservice.entity.Transaction;
import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.Status;
import com.example.cardservice.enums.TransactionType;
import com.example.cardservice.repository.CardRepository;
import com.example.cardservice.repository.IdempotencyKeyRepository;
import com.example.cardservice.repository.TransactionRepository;
import com.example.cardservice.service.CBUService;
import com.example.cardservice.service.CardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CBUService cbuService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final CardMapper cardMapper;
    private final ResponseMapper responseMapper;

    @Override
    public Response createCard(CardRequest request, String idempotencyKey) {
        logger.info("Creating card with idempotency key: {}", idempotencyKey);

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKey(idempotencyKey);
        if (existingKey.isPresent()) {
            Card card = existingKey.get().getCard();
            CardResponse cardResponse = cardMapper.toDto(card);
            logger.info("Card with idempotency key {} already created", idempotencyKey);
            return responseMapper.toResponse("Card with this Idempotency-Key already created", cardResponse, HttpStatus.OK);
        }

        Card card = new Card();
        card.setUserId(request.getUserId());
        card.setStatus(Optional.ofNullable(request.getStatus()).orElse(Status.ACTIVE));
        card.setBalance(Optional.ofNullable(request.getBalance()).orElse(0L));
        card.setCurrency(Optional.ofNullable(request.getCurrency()).orElse(Currency.UZS));

        Card savedCard = cardRepository.save(card);

        IdempotencyKey newKey = new IdempotencyKey();
        newKey.setKey(idempotencyKey);
        newKey.setCard(savedCard);
        idempotencyKeyRepository.save(newKey);

        CardResponse cardResponse = cardMapper.toDto(savedCard);
        logger.info("Card created successfully with id: {}", savedCard.getId());
        return responseMapper.toResponse("Card created successfully", cardResponse, HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public Response getCard(UUID cardId) {
        logger.info("Fetching card with id: {}", cardId);

        return cardRepository.findById(cardId)
                .map(card -> {
                    CardResponse cardDto = cardMapper.toDto(card);
                    logger.info("Card with id {} found", cardId);
                    return responseMapper.toResponse("API returns current card data.", cardDto, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("Card with id {} not found", cardId);
                    return responseMapper.toResponse("Card with such id not exists in processing.", HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Response blockCard(UUID cardId) {
        logger.info("Blocking card with id: {}", cardId);

        return cardRepository.findById(cardId)
                .map(card -> {
                    if (Status.ACTIVE.equals(card.getStatus())) {
                        card.setStatus(Status.BLOCKED);
                        cardRepository.save(card);
                        logger.info("Card with id {} has been blocked", cardId);
                        return responseMapper.toResponse("Response confirming the card has been blocked.", HttpStatus.OK);
                    }
                    logger.warn("Card with id {} is not active", cardId);
                    return responseMapper.toResponse("Card is not active", HttpStatus.BAD_REQUEST);
                })
                .orElseGet(() -> {
                    logger.warn("Card with id {} not found", cardId);
                    return responseMapper.toResponse("Card with such id not exists in processing.", HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Response unblockCard(UUID cardId) {
        logger.info("Unblocking card with id: {}", cardId);

        return cardRepository.findById(cardId)
                .map(card -> {
                    if (Status.BLOCKED.equals(card.getStatus())) {
                        card.setStatus(Status.ACTIVE);
                        cardRepository.save(card);
                        logger.info("Card with id {} has been unblocked", cardId);
                        return responseMapper.toResponse("Response confirming the card has been unblocked", HttpStatus.OK);
                    }
                    logger.warn("Card with id {} is not blocked", cardId);
                    return responseMapper.toResponse("Card is not blocked", HttpStatus.BAD_REQUEST);
                })
                .orElseGet(() -> {
                    logger.warn("Card with id {} not found", cardId);
                    return responseMapper.toResponse("Card with such id not exists in processing.", HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Response debit(UUID cardId, TransactionDto dto) {
        logger.info("Processing debit for card id: {} with amount: {}", cardId, dto.getAmount());

        Optional<Card> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isEmpty()) {
            logger.error("Card with id {} not found", cardId);
            return new Response("Card with such id not exists in processing.", HttpStatus.NOT_FOUND);
        }

        Card card = optionalCard.get();
        validateTransactionAmount(dto.getAmount());

        BigDecimal transactionAmount = BigDecimal.valueOf(dto.getAmount());

        if (!dto.getCurrency().equals(card.getCurrency())) {
            transactionAmount = cbuService.convertAmount(transactionAmount, dto.getCurrency().name(), card.getCurrency().name());
        }

        BigDecimal balance = BigDecimal.valueOf(card.getBalance());
        if (balance.compareTo(transactionAmount) < 0) {
            logger.error("Insufficient funds for card id: {}. Balance: {}, Requested amount: {}", cardId, balance, transactionAmount);
            throw new IllegalArgumentException("Insufficient funds");
        }

        card.setBalance(balance.subtract(transactionAmount).longValue());
        cardRepository.save(card);

        Transaction transaction = buildTransaction(cardId, dto, TransactionType.DEBIT, transactionAmount.longValue());
        transactionRepository.save(transaction);

        logger.info("Debit processed for card id: {}. Amount: {}", cardId, transactionAmount);
        return responseMapper.toResponse("Should withdraw only once with given idempotency key. Return existing transaction details if already withdrawn", HttpStatus.OK);
    }

    @Override
    public Response credit(UUID cardId, TransactionDto dto) {
        logger.info("Processing credit for card id: {} with amount: {}", cardId, dto.getAmount());

        Optional<Card> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isEmpty()) {
            logger.error("Card with id {} not found", cardId);
            return new Response("Card with such id not exists in processing.", HttpStatus.NOT_FOUND);
        }

        Card card = optionalCard.get();
        validateTransactionAmount(dto.getAmount());

        BigDecimal transactionAmount = BigDecimal.valueOf(dto.getAmount());

        if (!dto.getCurrency().equals(card.getCurrency())) {
            transactionAmount = cbuService.convertAmount(transactionAmount, dto.getCurrency().name(), card.getCurrency().name());
        }

        BigDecimal newBalance = BigDecimal.valueOf(card.getBalance()).add(transactionAmount);
        card.setBalance(newBalance.longValue());
        cardRepository.save(card);

        Transaction transaction = buildTransaction(cardId, dto, TransactionType.CREDIT, transactionAmount.longValue());
        transactionRepository.save(transaction);

        logger.info("Credit processed for card id: {}. Amount: {}", cardId, transactionAmount);
        return responseMapper.toResponse("Should top up only once with given idempotency key. Return existing transaction details if already topped up.", HttpStatus.OK);
    }

    private void validateTransactionAmount(Long amount) {
        if (amount == null || amount <= 0) {
            logger.error("Invalid transaction amount: {}", amount);
            throw new IllegalArgumentException("Invalid transaction amount");
        }
    }

    private Transaction buildTransaction(UUID cardId, TransactionDto dto, TransactionType type, Long amount) {
        return Transaction.builder()
                .cardId(cardId)
                .amount(amount)
                .currency(dto.getCurrency())
                .purpose(dto.getPurpose())
                .type(type)
                .build();
    }
}
