package com.cardmanagement.card.Controller;

import com.cardmanagement.card.CashCard;
import com.cardmanagement.card.CashCardRepository;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }
    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashCard> findById(@PathVariable Long id, Principal principal) {
      CashCard cashCard = findCashCard(id, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createCashCard(@RequestBody CashCard cashCard, UriComponentsBuilder ucb,Principal principal) {
        CashCard savedCashCard = cashCardRepository.save(cashCard);
        URI location = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable,Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
    @PutMapping("/{id}")
    private ResponseEntity<Void> putBankCard(@PathVariable Long id,@RequestBody CashCard cashCardUpdate,Principal principal){
        CashCard cashCard = findCashCard(id, principal);
        if (cashCard == null) {
            System.out.println("null");
            return ResponseEntity.notFound().build();
        }
        CashCard cashcardUpdated = new CashCard(cashCard.id(),
                cashCardUpdate.amount(),
                principal.getName());
        cashCardRepository.save(cashcardUpdated);
        return ResponseEntity.noContent().build();
    }
}