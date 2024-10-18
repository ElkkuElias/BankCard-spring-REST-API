package com.cardmanagement.card;

import org.springframework.data.annotation.Id;
//record class for UserCreationRequest that will be handled by user creation in SecurityConfig
public record UserCreationRequest(@Id Long id, String userName, String password,String role) {

}