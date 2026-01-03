package com.ansh.chat.service;

import com.ansh.chat.dto.*;
import com.ansh.chat.entity.ChatMessage;
import com.ansh.chat.entity.ChatRoom;
import com.ansh.chat.repository.ChatMessageRepository;
import com.ansh.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebSocketConnectionManager connectionManager;

    /**
     * Get or create a chat room between two users
     */
    @Transactional
    public ChatRoomDTO getOrCreateChatRoom(String userId1, String userId2) {
        ChatRoom chatRoom = chatRoomRepository.findByUsers(userId1, userId2)
                .orElseGet(() -> createChatRoom(userId1, userId2));

        ChatRoomDTO dto = ChatRoomDTO.fromEntity(chatRoom);
        // Populate otherUserId field (userId1 is the current user, userId2 is the other user)
        dto.setOtherUserId(userId2);
        // TODO: Fetch other user's name from user service
        dto.setOtherUserName("User " + userId2);
        return dto;
    }

    /**
     * Create a new chat room
     */
    private ChatRoom createChatRoom(String userId1, String userId2) {
        String roomId = ChatRoom.generateChatRoomId(userId1, userId2);

        ChatRoom chatRoom = ChatRoom.builder()
                .id(roomId)
                .user1Id(userId1)
                .user2Id(userId2)
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Send a message
     */
    @Transactional
    public ChatMessageDTO sendMessage(String senderId, SendMessageRequest request) {
        // Get or create chat room
        ChatRoomDTO chatRoom = getOrCreateChatRoom(senderId, request.getReceiverId());

        // Create message
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(chatRoom.getId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .messageType(ChatMessage.MessageType.valueOf(request.getMessageType()))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        message = chatMessageRepository.save(message);

        // Update chat room's last message time
        updateChatRoomLastMessage(chatRoom.getId());

        // Convert to DTO
        ChatMessageDTO messageDTO = ChatMessageDTO.fromEntity(message);

        // Send via WebSocket if receiver is online
        if (connectionManager.isUserConnected(request.getReceiverId())) {
            WebSocketMessage wsMessage = WebSocketMessage.newMessage(messageDTO);
            connectionManager.sendMessageToUser(request.getReceiverId(), wsMessage);
            log.debug("Message sent via WebSocket to user {}", request.getReceiverId());
        } else {
            log.debug("User {} is offline. Message saved but not sent via WebSocket", request.getReceiverId());
        }

        return messageDTO;
    }

    /**
     * Get messages for a chat room
     */
    public List<ChatMessageDTO> getChatRoomMessages(String chatRoomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageRequest);

        return messages.stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all messages for a chat room
     */
    public List<ChatMessageDTO> getAllChatRoomMessages(String chatRoomId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        return messages.stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get chat rooms for a user
     */
    public List<ChatRoomDTO> getUserChatRooms(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(userId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(chatRoom);

                    // Set other user ID
                    String otherUserId = chatRoom.getUser1Id().equals(userId)
                            ? chatRoom.getUser2Id()
                            : chatRoom.getUser1Id();
                    dto.setOtherUserId(otherUserId);

                    // Get unread count
                    Long unreadCount = chatMessageRepository.countUnreadMessages(chatRoom.getId(), userId);
                    dto.setUnreadCount(unreadCount);

                    // Get last message (optional enhancement)
                    List<ChatMessageDTO> lastMessage = getChatRoomMessages(chatRoom.getId(), 0, 1);
                    if (!lastMessage.isEmpty()) {
                        dto.setLastMessage(lastMessage.get(0));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(String chatRoomId, String userId) {
        chatMessageRepository.markAllAsRead(chatRoomId, userId);
        log.debug("Marked all messages as read in chat room {} for user {}", chatRoomId, userId);
    }

    /**
     * Get unread message count for a user
     */
    public Long getUnreadMessageCount(String userId) {
        return chatMessageRepository.countTotalUnreadMessages(userId);
    }

    /**
     * Get the other user ID in a chat room
     */
    public String getOtherUserId(String chatRoomId, String userId) {
        return chatRoomRepository.findById(chatRoomId)
                .map(chatRoom -> chatRoom.getUser1Id().equals(userId)
                        ? chatRoom.getUser2Id()
                        : chatRoom.getUser1Id())
                .orElse(null);
    }

    /**
     * Update chat room's last message timestamp
     */
    private void updateChatRoomLastMessage(String chatRoomId) {
        chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
            chatRoom.setLastMessageAt(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);
        });
    }

    /**
     * Delete a chat room
     */
    @Transactional
    public void deleteChatRoom(String chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
        log.info("Deleted chat room: {}", chatRoomId);
    }

    /**
     * Get chat room by ID
     */
    public ChatRoomDTO getChatRoom(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .map(ChatRoomDTO::fromEntity)
                .orElse(null);
    }
}
