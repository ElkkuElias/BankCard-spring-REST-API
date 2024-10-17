package com.cardmanagement.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(SecurityConfig.class)
class BankCardManagementApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldReturnCashCardWhenDataIsSaved() throws Exception {
        mockMvc.perform(get("/cashcards/99").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L));
    }
    @Test
    void shouldNotReturnCashCardWithUnknownId() throws Exception{
        mockMvc.perform(get("/cashcards/1000").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldCreateNewBankCard() throws Exception {
        CashCard cashCard = new CashCard(null, 250.00,"sarah1");
        String cashCardJSON = objectMapper.writeValueAsString(cashCard);
        MvcResult result = mockMvc.perform(post("/cashcards").with(user("sarah1").password("abc123").roles("CARD-OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashCardJSON))
                .andExpect(status().isCreated())
                .andReturn();
        String locationHeader = result.getResponse().getHeader("Location");
        mockMvc.perform(get(locationHeader).with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk());

    }
    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() throws Exception {
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        DocumentContext documentContext = JsonPath.parse(content);

        // Check the number of items
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        // Check ids
        List<Integer> ids = documentContext.read("$[*].id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        // Check amounts
        List<Double> amounts = documentContext.read("$[*].amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.0);
    }

    @Test
    void shouldReturnAPageOfCashCards() throws Exception{
        MvcResult result = mockMvc.perform(get("/cashcards?page=0&size=3").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();


        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        List<CashCard> page = documentContext.read("[*]");
        assertThat(page.size()).isEqualTo(3);
    }
    @Test
    void shouldReturnASortedPageOfCashCards() throws Exception{
       MvcResult result = mockMvc.perform(get("/cashcards?page=0&size=1&sort=amount,desc").with(httpBasic("sarah1","abc123")))
               .andExpect(status().isOk())
               .andReturn();
        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        JSONArray read = documentContext.read("$[*]");

        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }
    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() throws Exception{
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }
    @Test
    void shouldNotReturnBankCardWhenUsingBadCredentials() throws Exception{
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("Elias","Badboy")))
                .andExpect(status().isUnauthorized())
                .andReturn();

    }
    @Test
    void shouldRejectUsersWhoAreNotCardOwners() throws Exception{
        MvcResult result = mockMvc.perform(get("/cashcards/99").with(httpBasic("hank-owns-no-cards","qrs456")))
                .andExpect(status().isForbidden())
                .andReturn();
    }
    @Test
    void shouldNotAllowAccessToBankCardsTheyDoNotOwn() throws Exception{
        MvcResult result = mockMvc.perform(get("/cashcards/102").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound())
                .andReturn();
    }
    @Test
    void shouldUpdateAnExistingBankCard() throws Exception{
        CashCard cashCardUpdate = new CashCard(null,19.99,null);
        String content = objectMapper.writeValueAsString(cashCardUpdate);

         mockMvc.perform(MockMvcRequestBuilders.put("/cashcards/99")
                .with(httpBasic("sarah1","abc123"))
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNoContent());

         MvcResult result = mockMvc.perform(get("/cashcards/99")
                 .with(httpBasic("sarah1","abc123")))
                 .andExpect(status().isOk())
                 .andReturn();
        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());



        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);


        double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo( 19.99);
    }
    @Test
    void ShouldNotUpdateBankCardThatDoesntExist() throws Exception{
        CashCard nonExistentCard = new CashCard(null,19.99,null);
        String content = objectMapper.writeValueAsString(nonExistentCard);
        MvcResult result = mockMvc.perform(put("/cashcards/9999")
                .with(httpBasic("sarah1","abc123")).contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isNotFound())
                .andReturn();

    }
    @Test
    void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() throws Exception{
        CashCard kumarsCard = new CashCard(null,333.33,null);
        String content = objectMapper.writeValueAsString(kumarsCard);
        MvcResult result = mockMvc.perform(put("/cashcards/102")
                        .with(httpBasic("sarah1","abc123")).contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
