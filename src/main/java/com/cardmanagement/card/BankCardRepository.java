package com.cardmanagement.card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
//Repository for BankCard
public interface BankCardRepository extends CrudRepository<BankCard,Long>, PagingAndSortingRepository<BankCard,Long> {
    BankCard findByIdAndOwner(Long id, String owner);
    Page<BankCard> findByOwner(String owner, PageRequest pageRequest);
    boolean existsByIdAndOwner(Long id, String owner);
}
