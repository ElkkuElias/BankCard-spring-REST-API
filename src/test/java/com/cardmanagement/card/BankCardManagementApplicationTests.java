package com.cardmanagement.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    @Test
    void shouldCreateNewBankCard() throws Exception {
        CashCard cashCard = new CashCard(null, 250.00);
        String cashCardJSON = new ObjectMapper().writeValueAsString(cashCard);
        MvcResult result = mockMvc.perform(post("/cashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashCardJSON))
                .andExpect(status().isCreated())
                .andReturn();
        String locationHeader = result.getResponse().getHeader("Location");
        mockMvc.perform(get(locationHeader))
                .andExpect(status().isOk());

    }
}
