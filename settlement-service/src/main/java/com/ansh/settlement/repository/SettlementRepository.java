package com.ansh.settlement.repository;

import com.ansh.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Settlement entity.
 * In microservices: Updated queries to use payerId and payeeId (String fields).
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {

    /**
     * Find all settlements where user is payer or payee
     * Updated: Uses s.payerId and s.payeeId instead of s.payer.id and s.payee.id
     */
    @Query("SELECT s FROM Settlement s WHERE s.payerId = :userId OR s.payeeId = :userId ORDER BY s.settledDate DESC")
    List<Settlement> findByUserIdAsPayerOrPayee(@Param("userId") String userId);

    /**
     * Find settlements by payer ID
     */
    List<Settlement> findByPayerIdOrderBySettledDateDesc(String payerId);

    /**
     * Find settlements by payee ID
     */
    List<Settlement> findByPayeeIdOrderBySettledDateDesc(String payeeId);

    /**
     * Find settlements between two specific users
     * Updated: Uses s.payerId and s.payeeId instead of s.payer.id and s.payee.id
     */
    @Query("SELECT s FROM Settlement s WHERE " +
            "(s.payerId = :userId1 AND s.payeeId = :userId2) OR " +
            "(s.payerId = :userId2 AND s.payeeId = :userId1) " +
            "ORDER BY s.settledDate DESC")
    List<Settlement> findBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);

    /**
     * Count settlements involving a user
     * Updated: Uses s.payerId and s.payeeId
     */
    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.payerId = :userId OR s.payeeId = :userId")
    long countByUserId(@Param("userId") String userId);
}
