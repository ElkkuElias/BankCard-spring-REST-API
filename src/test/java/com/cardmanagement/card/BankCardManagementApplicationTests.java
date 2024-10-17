package com.cardmanagement.card;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BankCardManagementApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCashCardWhenDataIsSaved() throws Exception {
        mockMvc.perform(get("/cashcards/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.amount").value(22.3));
    }
    @Test
    void shouldNotReturnCashCardWithUnknownId() throws Exception{
        mockMvc.perform(get("/cashcards/1000")).andExpect(status().isNotFound());
    }
}
