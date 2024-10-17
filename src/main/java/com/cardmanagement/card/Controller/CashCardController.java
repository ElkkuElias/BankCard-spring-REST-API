package com.cardmanagement.card.Controller;

import com.cardmanagement.card.CashCard;
import com.cardmanagement.card.CashCardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
@RestController
@CrossOrigin(origins = "*")
public class CashCardController {
    private final CashCardRepository cashCardRepository;
    private CashCardController(CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }
    @GetMapping("cashcards/{id}")
    public ResponseEntity<CashCard> findById(@PathVariable Long id){
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(id);
        if (cashCardOptional.isPresent()){
        return ResponseEntity.ok(cashCardOptional.get());
    }else {
        return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("cashcards")
    public ResponseEntity<Void> createCashCard(@RequestBody CashCard cashCard, UriComponentsBuilder ucb){
        CashCard savedCashCard = cashCardRepository.save(cashCard);
        URI location = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
