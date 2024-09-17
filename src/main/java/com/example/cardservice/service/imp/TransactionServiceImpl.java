package com.example.cardservice.service.imp;

import com.example.cardservice.dto.response.Response;
import com.example.cardservice.entity.Transaction;
import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.TransactionType;
import com.example.cardservice.repository.TransactionRepository;
import com.example.cardservice.service.TransactionService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Response getTransactions(String cardId, TransactionType type, String transactionId, Currency currency, int page, int size) {
        UUID cardIdUUID = cardId != null ? UUID.fromString(cardId) : null;

        Specification<Transaction> specification = filterTransactions(
                cardIdUUID, type, transactionId, currency);

        Page<Transaction> transactions = transactionRepository.findAll(specification, PageRequest.of(page, size));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("page", transactions.getNumber());
        response.put("size", transactions.getSize());
        response.put("total_pages", transactions.getTotalPages());
        response.put("total_items", transactions.getTotalElements());
        response.put("content", transactions.getContent());

        return new Response("Transactions retrieved successfully", response, HttpStatus.OK);
    }

    private static Specification<Transaction> filterTransactions(
            UUID cardId,
            TransactionType type,
            String transactionId,
            Currency currency) {

        return (Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (cardId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cardId"), cardId));
            }
            if (type != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("type"), type));
            }
            if (transactionId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("id"), UUID.fromString(transactionId)));
            }
            if (currency != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("currency"), currency));
            }

            return predicate;
        };
    }
}
