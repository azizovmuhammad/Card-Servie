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
import com.example.cardservice.enums.Purpose;
import com.example.cardservice.enums.Status;
import com.example.cardservice.repository.CardRepository;
import com.example.cardservice.repository.IdempotencyKeyRepository;
import com.example.cardservice.repository.TransactionRepository;
import com.example.cardservice.service.CBUService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CBUService cbuService;

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private ResponseMapper responseMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardResponse cardResponse;
    private TransactionDto transactionDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        card = new Card();
        card.setId(UUID.randomUUID());
        card.setUserId(1L);
        card.setStatus(Status.ACTIVE);
        card.setBalance(100L);
        card.setCurrency(Currency.UZS);

        cardResponse = new CardResponse();
        cardResponse.setCardId(card.getId());
        cardResponse.setUserId(card.getUserId());
        cardResponse.setStatus(card.getStatus().name());
        cardResponse.setBalance(card.getBalance());
        cardResponse.setCurrency(card.getCurrency().name());

        transactionDto = new TransactionDto();
        transactionDto.setAmount(10L);
        transactionDto.setCurrency(Currency.UZS);
        transactionDto.setPurpose(Purpose.PAYMENT);
    }

    @Test
    public void testCreateCard_Success() {
        String idempotencyKey = "unique-key";

        when(idempotencyKeyRepository.findByKey(idempotencyKey)).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(idempotencyKeyRepository.save(any(IdempotencyKey.class))).thenReturn(new IdempotencyKey());
        when(cardMapper.toDto(card)).thenReturn(cardResponse);
        when(responseMapper.toResponse(anyString(), any(CardResponse.class), any(HttpStatus.class)))
                .thenReturn(new Response("Card created successfully", HttpStatus.CREATED));

        Response response = cardService.createCard(new CardRequest(1L, Status.ACTIVE, 100L, Currency.UZS), idempotencyKey);

        System.out.println("Actual Response: " + response);

        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals("Card created successfully", response.getMessage());
    }


    @Test
    public void testGetCard_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponse);
        when(responseMapper.toResponse(anyString(), any(CardResponse.class), any(HttpStatus.class)))
                .thenReturn(new Response("API returns current card data.", cardResponse, HttpStatus.OK));

        Response response = cardService.getCard(card.getId());

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("API returns current card data.", response.getMessage());
        assertEquals(cardResponse, response.getData());
    }

    @Test
    public void testBlockCard_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(responseMapper.toResponse(anyString(), any(HttpStatus.class)))
                .thenReturn(new Response("Response confirming the card has been blocked.", HttpStatus.OK));

        Response response = cardService.blockCard(card.getId());

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("Response confirming the card has been blocked.", response.getMessage());
        assertEquals(Status.BLOCKED, card.getStatus());
    }

    @Test
    public void testUnblockCard_Success() {
        card.setStatus(Status.BLOCKED);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(responseMapper.toResponse(anyString(), any(HttpStatus.class)))
                .thenReturn(new Response("Response confirming the card has been unblocked", HttpStatus.OK));

        Response response = cardService.unblockCard(card.getId());

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("Response confirming the card has been unblocked", response.getMessage());
        assertEquals(Status.ACTIVE, card.getStatus());
    }

    @Test
    public void testDebit_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cbuService.convertAmount(any(BigDecimal.class), anyString(), anyString())).thenReturn(BigDecimal.valueOf(10L));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(responseMapper.toResponse(anyString(), any(HttpStatus.class)))
                .thenReturn(new Response("Should withdraw only once with given idempotency key. Return existing transaction details if already withdrawn", HttpStatus.OK));

        Response response = cardService.debit(card.getId(), transactionDto);

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("Should withdraw only once with given idempotency key. Return existing transaction details if already withdrawn", response.getMessage());
    }

    @Test
    public void testCredit_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cbuService.convertAmount(any(BigDecimal.class), anyString(), anyString())).thenReturn(BigDecimal.valueOf(10L));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(responseMapper.toResponse(anyString(), any(HttpStatus.class)))
                .thenReturn(new Response("Should top up only once with given idempotency key. Return existing transaction details if already topped up.", HttpStatus.OK));

        Response response = cardService.credit(card.getId(), transactionDto);

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("Should top up only once with given idempotency key. Return existing transaction details if already topped up.", response.getMessage());
    }
}
