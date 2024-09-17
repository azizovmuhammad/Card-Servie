package com.example.cardservice.dto.Mapper;

import com.example.cardservice.dto.CardRequest;
import com.example.cardservice.dto.response.CardResponse;
import com.example.cardservice.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardResponse toDto(Card card);
    Card toEntity(CardRequest cardDto);
}
