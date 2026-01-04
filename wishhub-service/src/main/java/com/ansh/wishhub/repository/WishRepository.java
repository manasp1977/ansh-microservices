package com.ansh.wishhub.repository;

import com.ansh.wishhub.entity.Wish;
import com.ansh.wishhub.enums.WishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishRepository extends JpaRepository<Wish, String> {

    List<Wish> findByStatus(WishStatus status);

    List<Wish> findByUserId(String userId);

    List<Wish> findByClaimerId(String claimerId);

    List<Wish> findByStatusOrderByCreatedAtDesc(WishStatus status);

    List<Wish> findAllByOrderByCreatedAtDesc();
}
