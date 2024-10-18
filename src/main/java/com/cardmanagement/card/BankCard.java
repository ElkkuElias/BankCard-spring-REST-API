package com.cardmanagement.card;


import org.springframework.data.annotation.Id;
//record class for BankCard
public record BankCard(@Id Long id, Double amount, String owner) {

    }

