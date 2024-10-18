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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

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
    //Autowire a mockmvc for use in every test to mock http requests and responses
    @Autowired
    private MockMvc mockMvc;
    //Autowire an object mapper to convert objects to json and vice versa when needed
    @Autowired
    private ObjectMapper objectMapper;

    //Test for returning a Card when it is saved in the database

    //Endpoints are secured so we use a user with the role CARD-OWNER to access the endpoints
    // httpBasic("sarah1","abc123")) is used to authenticate the user
    @Test
    void shouldReturnCashCardWhenDataIsSaved() throws Exception {

        mockMvc.perform(get("/cashcards/99").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L));
    }


    @Test
    void shouldNotReturnCashCardWithUnknownId() throws Exception{
        //Test for returning notFound when searching for an id that doesn't exist in the database
        mockMvc.perform(get("/cashcards/1000").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewBankCard() throws Exception {
        // Test for creating a new bank card and returning it
        BankCard cashCard = new BankCard(null, 250.00,"sarah1");
        String cashCardJSON = objectMapper.writeValueAsString(cashCard);

        MvcResult result = mockMvc.perform(post("/cashcards").with(user("sarah1").password("abc123").roles("CARD-OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashCardJSON))
                .andExpect(status().isCreated())
                .andReturn();
        //Extract the location header from the response and assert that is equal to the expected location
        String locationHeader = result.getResponse().getHeader("Location");
        mockMvc.perform(get(locationHeader).with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() throws Exception {
        //Test for returning all cash cards when list is requested
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();
        //Extract the content from the response
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
        //Test for returning a page of cash cards
        //Page size is set to 3 and page number is set to 0. So we return one page of 3 cards as seen in the urlTemplate
        MvcResult result = mockMvc.perform(get("/cashcards?page=0&size=3").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();

        //Extract the content from the response and assert that the size of the page is 3
        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        List<BankCard> page = documentContext.read("[*]");
        assertThat(page.size()).isEqualTo(3);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() throws Exception{
        //Test for returning a sorted page of cash cards
       MvcResult result = mockMvc.perform(get("/cashcards?page=0&size=3&sort=amount,desc").with(httpBasic("sarah1","abc123")))
               .andExpect(status().isOk())
               .andReturn();
        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        JSONArray read = documentContext.read("$[*]");
        //Assert that the size of the page is 3
        assertThat(read.size()).isEqualTo(3);
        //Assert that the amounts are sorted in descending order Sarah1 has 3 cards with amounts 1.00, 123.45 and 150.00
        //So the amounts should be in the order 150.00, 123.45, 1.00
        //We can just check the first one to confirm that the sorting is working

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() throws Exception{
        //Test for returning a sorted page of cash cards with no parameters
        //Default parameters are 20 and 0 per the conrtoller
        //The default sort is by amount in ascending order
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isOk())
                .andReturn();

        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);
        //Sarah1 owns 3 cards with amounts 1.00, 123.45 and 150.00
        //The default sort is by amount in ascending order
        //So the amounts should be in the order 1.00, 123.45, 150.00
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }

    @Test
    void shouldNotReturnBankCardWhenUsingBadCredentials() throws Exception{
        //Test for assessing proper authentication
        //We use the wrong credentials to access the endpoint and expect a 401 unauthorized status
        MvcResult result = mockMvc.perform(get("/cashcards").with(httpBasic("Elias","Badboy")))
                .andExpect(status().isUnauthorized())
                .andReturn();

    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() throws Exception{
        // Test for rejecting users based on their roles
        //Hank-owns-no-cards has the role NON-OWNER and should not be able to access the endpoint
        //roles are defined in the SecurityConfig class
        MvcResult result = mockMvc.perform(get("/cashcards/99").with(httpBasic("hank-owns-no-cards","qrs456")))
                .andExpect(status().isForbidden())
                .andReturn();
    }
    @Test
    void shouldNotAllowAccessToBankCardsTheyDoNotOwn() throws Exception{
        //Test for accessing cards that the user does not own
        //Kumar2 owns the card with id 102
        MvcResult result = mockMvc.perform(get("/cashcards/102").with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound())
                .andReturn();
    }
    @Test
    void shouldUpdateAnExistingBankCard() throws Exception{
        //Test for updating an existing bank card
        BankCard cashCardUpdate = new BankCard(null,19.99,null);
        String content = objectMapper.writeValueAsString(cashCardUpdate);
        //We update the card with id 99 to have an amount of 19.99 using the put method
         mockMvc.perform(MockMvcRequestBuilders.put("/cashcards/99")
                .with(httpBasic("sarah1","abc123"))
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNoContent());
        //We then retrieve the card with id 99
         MvcResult result = mockMvc.perform(get("/cashcards/99")
                 .with(httpBasic("sarah1","abc123")))
                 .andExpect(status().isOk())
                 .andReturn();
        DocumentContext documentContext = JsonPath.parse(result.getResponse().getContentAsString());

        //Assert that the id and amount have been updated
        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);


        double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo( 19.99);
    }
    @Test
    void ShouldNotUpdateBankCardThatDoesntExist() throws Exception{
        //Test for updating a bank card that does not exist
        //We use the put method to update a card with id 9999 that does not exist and return a 404 not found status
        BankCard nonExistentCard = new BankCard(null,19.99,null);
        String content = objectMapper.writeValueAsString(nonExistentCard);
        MvcResult result = mockMvc.perform(put("/cashcards/9999")
                .with(httpBasic("sarah1","abc123")).contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isNotFound())
                .andReturn();

    }
    @Test
    void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() throws Exception{
        //Test for updating a card that is owned by someone else
        //Kumar2 owns the card with id 102
        //We try to update the card with id 102 using the put method with wrong credentials and return a 404 not found status
        //The 404 protects the existance of the card from being known to the trying to access it
        BankCard kumarsCard = new BankCard(null,333.33,null);
        String content = objectMapper.writeValueAsString(kumarsCard);
        MvcResult result = mockMvc.perform(put("/cashcards/102")
                        .with(httpBasic("sarah1","abc123")).contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andReturn();
    }
    @Test
    void shouldDeleteAnExistingBankCard() throws Exception{
        //Test for deleting an existing bank card and returning a 204 no content status
        mockMvc.perform(delete("/cashcards/99")
                .with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNoContent());
        //We then try to retrieve the card with id 99 and expect a 404 not found status
        mockMvc.perform(get("/cashcards/99")
                        .with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldNotDeleteACashCardThatDoesNotExist() throws Exception{
        //Test for deleting a card that does not exist
        //We try to delete a card with id 9999 that does not exist and expect a 404 not found status
        mockMvc.perform(delete("/cashcards/9999")
                        .with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound());


    }
    @Test
    void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() throws Exception{
        //Test for deleting a card that the user does not own
        //Expected a 404 not found status
        mockMvc.perform(delete("/cashcards/102")
                .with(httpBasic("sarah1","abc123")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/cashcards/102")
                        .with(httpBasic("kumar2","xyz789")))
                .andExpect(status().isOk());
    }
    @Test
    void shouldCreateUserCreateCardAndRetrieveIt() throws Exception {
        // Test for creating a user, creating a card and retrieving it
        // Creating a user
        UserCreationRequest user = new UserCreationRequest(null, "Elias", "pogpog21", "CARD-OWNER");
        String content = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/createuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // Creating a card
        BankCard cashCard = new BankCard(null, 50.00, "Elias");
        String cashCardJSON = objectMapper.writeValueAsString(cashCard);
        MvcResult createResult = mockMvc.perform(post("/cashcards")
                        .with(user("Elias").password("pogpog21").roles("CARD-OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cashCardJSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Extracting the card ID from the location header
        String locationHeader = createResult.getResponse().getHeader("Location");
        String[] pathParts = locationHeader.split("/");
        Number createdCardId = Integer.parseInt(pathParts[pathParts.length - 1]);

        // Retrieving the card
        MvcResult retrieveResult = mockMvc.perform(get(locationHeader)
                        .with(httpBasic("Elias", "pogpog21")))
                .andExpect(status().isOk())
                .andReturn();

        String retrievedContent = retrieveResult.getResponse().getContentAsString();
        DocumentContext documentContext = JsonPath.parse(retrievedContent);

        //Assert that the id of the retrieved card is the same as the created card
        Number retrievedCardId = documentContext.read("$.id");
        assertThat(retrievedCardId.longValue()).isEqualTo(createdCardId.longValue());

        //Assert that the amount and owner of the retrieved card are the same as the created card
        double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(50.0);

        String owner = documentContext.read("$.owner");
        assertThat(owner).isEqualTo("Elias");
    }

}
