package com.cardmanagement.card;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BankCardJsonTest {
    //Test Class for Serializing and Deserializing json

    @Autowired
    private JacksonTester<BankCard> json;

    @Autowired
    private JacksonTester<BankCard[]> jsonList;

    private BankCard[] bankCards;
    //Sets up the test data
    @BeforeEach
    void setUp() {
        bankCards = Arrays.array(
                new BankCard(99L, 123.45,"sarah1"),
                new BankCard(100L, 1.00,"sarah1"),
                new BankCard(101L, 150.00,"sarah1"));
    }
    //Tests the serialization of a list of bank cards
    //Compares the serialized list to the expected json
    @Test
    void bankCardListSerializationTest() throws IOException{
        assertThat(jsonList.write(bankCards)).isStrictlyEqualToJson("/list.json");
    }
    //Tests the deserialization of a list of bank cards from json
    @Test
    void bankCardListDeserializationTest() throws IOException {
        String expected="""
         [
            { "id": 99, "amount": 123.45,"owner": "sarah1" },
            { "id": 100, "amount": 1.00,"owner": "sarah1" },
            { "id": 101, "amount": 150.00,"owner": "sarah1" }
         ]
         """;
        assertThat(jsonList.parse(expected)).isEqualTo(bankCards);
    }
    //Tests the serialization of a single bank card
    @Test
    void bankCardSerializationTest() throws IOException {
        BankCard bankCard = bankCards[0];
        assertThat(json.write(bankCard)).isStrictlyEqualToJson("/single.json");
        assertThat(json.write(bankCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(bankCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(bankCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(bankCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }
    //Tests the deserialization of a single bank card from json
    @Test
    void bankCardDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 99,
                    "amount": 123.45,
                    "owner": "sarah1"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new BankCard(99L, 123.45,"sarah1"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

}



