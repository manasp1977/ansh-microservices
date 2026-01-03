package com.ansh.chat.repository;

import com.ansh.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * Find messages for a chat room ordered by creation time
     */
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(String chatRoomId, Pageable pageable);

    /**
     * Find all messages for a chat room
     */
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

    /**
     * Count unread messages for a user in a chat room
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoomId = :chatRoomId " +
           "AND cm.receiverId = :userId AND cm.isRead = false")
    Long countUnreadMessages(@Param("chatRoomId") String chatRoomId, @Param("userId") String userId);

    /**
     * Mark all messages as read in a chat room for a user
     */
    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chatRoomId = :chatRoomId " +
           "AND cm.receiverId = :userId AND cm.isRead = false")
    void markAllAsRead(@Param("chatRoomId") String chatRoomId, @Param("userId") String userId);

    /**
     * Count total unread messages for a user across all chat rooms
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.receiverId = :userId AND cm.isRead = false")
    Long countTotalUnreadMessages(@Param("userId") String userId);
}
