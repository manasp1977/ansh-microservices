package com.ansh.chat.controller;

import com.ansh.chat.dto.*;
import com.ansh.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for chat operations
 * Provides HTTP endpoints for chat functionality
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * Get or create a chat room between current user and another user
     */
    @GetMapping("/rooms/{otherUserId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String otherUserId) {
        log.info("==== CHAT SERVICE: Getting chat room for users {} and {}", userId, otherUserId);
        if (userId == null || userId.isEmpty()) {
            log.error("==== CHAT SERVICE: Missing X-User-Id header");
            return ResponseEntity.badRequest().build();
        }
        ChatRoomDTO chatRoom = chatService.getOrCreateChatRoom(userId, otherUserId);
        log.info("==== CHAT SERVICE: Successfully created/retrieved chat room: {}", chatRoom.getId());
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * Get all chat rooms for current user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Getting chat rooms for user {}", userId);
        List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * Send a message via REST API (alternative to WebSocket)
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SendMessageRequest request) {
        log.info("User {} sending message to {}", userId, request.getReceiverId());
        ChatMessageDTO message = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(message);
    }

    /**
     * Get messages for a chat room
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatRoomMessages(
            @PathVariable String chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Getting messages for chat room {}", chatRoomId);
        List<ChatMessageDTO> messages = chatService.getChatRoomMessages(chatRoomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get all messages for a chat room
     */
    @GetMapping("/rooms/{chatRoomId}/messages/all")
    public ResponseEntity<List<ChatMessageDTO>> getAllChatRoomMessages(
            @PathVariable String chatRoomId) {
        log.info("==== CHAT SERVICE: Getting all messages for chat room {}", chatRoomId);
        List<ChatMessageDTO> messages = chatService.getAllChatRoomMessages(chatRoomId);
        log.info("==== CHAT SERVICE: Found {} messages for chat room {}", messages.size(), chatRoomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/rooms/{chatRoomId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String chatRoomId) {
        log.info("Marking messages as read in room {} for user {}", chatRoomId, userId);
        chatService.markMessagesAsRead(chatRoomId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Messages marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {
        Long count = chatService.getUnreadMessageCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a chat room
     */
    @DeleteMapping("/rooms/{chatRoomId}")
    public ResponseEntity<Map<String, String>> deleteChatRoom(
            @PathVariable String chatRoomId) {
        log.info("Deleting chat room {}", chatRoomId);
        chatService.deleteChatRoom(chatRoomId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Chat room deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "chat-service");
        return ResponseEntity.ok(response);
    }
}
