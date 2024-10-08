package com.example.cardservice.entity;

import com.example.cardservice.enums.Currency;
import com.example.cardservice.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cards")
public class Card implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Max(value = 10000,message = "max balance - 10000")
    @Min(value = 0,message = "min balance - 0")
    private Long balance;
    @Enumerated(EnumType.STRING)
    private Currency currency;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Card card = (Card) o;
        return getId() != null && Objects.equals(getId(), card.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
