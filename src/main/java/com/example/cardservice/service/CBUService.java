package com.example.cardservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CBUService {

    private final RestTemplate restTemplate;

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String url = "https://cbu.uz/oz/exchange-rates/json/";

        var rates = restTemplate.getForObject(url, Map.class);

        BigDecimal fromRate = new BigDecimal((char[]) rates.get(fromCurrency));
        BigDecimal toRate = new BigDecimal((char[]) rates.get(toCurrency));

        return fromRate.divide(toRate, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal exchangeRate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(exchangeRate);
    }
}
