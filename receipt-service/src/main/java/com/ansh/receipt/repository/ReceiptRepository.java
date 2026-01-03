package com.ansh.receipt.repository;

import com.ansh.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Receipt entity.
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {

    /**
     * Find all receipts by user ID, ordered by date descending
     */
    List<Receipt> findByUserIdOrderByDateDesc(String userId);

    /**
     * Find all receipts with items loaded (eager fetch)
     */
    @Query("SELECT DISTINCT r FROM Receipt r LEFT JOIN FETCH r.items ORDER BY r.date DESC")
    List<Receipt> findAllWithItems();

    /**
     * Find receipts by user with items loaded
     */
    @Query("SELECT DISTINCT r FROM Receipt r LEFT JOIN FETCH r.items WHERE r.userId = :userId ORDER BY r.date DESC")
    List<Receipt> findByUserIdWithItems(@Param("userId") String userId);

    /**
     * Count receipts by user
     */
    long countByUserId(String userId);
}
