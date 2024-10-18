package com.cardmanagement.card.Controller;

import com.cardmanagement.card.BankCard;
import com.cardmanagement.card.BankCardRepository;
import com.cardmanagement.card.SecurityConfig;
import com.cardmanagement.card.UserCreationRequest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping
public class CashCardController {
    private final BankCardRepository cashCardRepository;
    private final SecurityConfig securityConfig;

    public CashCardController(BankCardRepository cashCardRepository, SecurityConfig securityConfig) {
        this.cashCardRepository = cashCardRepository;
        this.securityConfig = securityConfig;
    }
    private BankCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    @GetMapping("/cashcards/{id}")
    //Returns a bank card with the given id
    public ResponseEntity<BankCard> findById(@PathVariable Long id, Principal principal) {
      BankCard cashCard = findCashCard(id, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cashcards")
    //Creates a bank card with the given amount and owner
    public ResponseEntity<Void> createCashCard(@RequestBody BankCard cashCard, UriComponentsBuilder ucb, Principal principal) {
        BankCard savedCashCard = cashCardRepository.save(cashCard);
        URI location = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/cashcards")
    //Returns all bank cards owned by the user
    private ResponseEntity<List<BankCard>> findAll(Pageable pageable, Principal principal) {
        Page<BankCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
    @PutMapping("/cashcards/{id}")
    //Updates a bank card with the given id
    private ResponseEntity<Void> putBankCard(@PathVariable Long id, @RequestBody BankCard cashCardUpdate, Principal principal){
        BankCard cashCard = findCashCard(id, principal);
        if (cashCard == null) {
            System.out.println("null");
            return ResponseEntity.notFound().build();
        }
        BankCard cashcardUpdated = new BankCard(cashCard.id(),
                cashCardUpdate.amount(),
                principal.getName());
        cashCardRepository.save(cashcardUpdated);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/cashcards/{id}")
    //Deletes a bank card with the given id
    private ResponseEntity<Void> deleteBankCard(@PathVariable Long id,Principal principal) {
        if (!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }
        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/createuser")
    //Creates a user with the given username, password, and role using the SecurityConfig class
    public ResponseEntity<Void> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        UserDetails user = securityConfig.createUser(
                userCreationRequest.userName(),
                userCreationRequest.password(),
                userCreationRequest.role()
        );
        return ResponseEntity.ok().build();
    }

}