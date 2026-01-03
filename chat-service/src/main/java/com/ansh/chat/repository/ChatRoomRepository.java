package com.ansh.chat.repository;

import com.ansh.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    /**
     * Find chat room between two users
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.user1Id = :userId1 AND cr.user2Id = :userId2) OR " +
           "(cr.user1Id = :userId2 AND cr.user2Id = :userId1)")
    Optional<ChatRoom> findByUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);

    /**
     * Find all chat rooms for a user
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1Id = :userId OR cr.user2Id = :userId " +
           "ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findByUserId(@Param("userId") String userId);
}
