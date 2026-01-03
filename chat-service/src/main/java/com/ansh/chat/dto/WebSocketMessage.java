package com.ansh.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type; // MESSAGE, TYPING, READ_RECEIPT, CONNECT, DISCONNECT
    private String chatRoomId;
    private String senderId;
    private String receiverId;
    private String content;
    private ChatMessageDTO message;
    private Long timestamp;

    public static WebSocketMessage newMessage(ChatMessageDTO message) {
        return WebSocketMessage.builder()
                .type("MESSAGE")
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static WebSocketMessage typing(String chatRoomId, String senderId, String receiverId) {
        return WebSocketMessage.builder()
                .type("TYPING")
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .receiverId(receiverId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static WebSocketMessage readReceipt(String chatRoomId, String userId) {
        return WebSocketMessage.builder()
                .type("READ_RECEIPT")
                .chatRoomId(chatRoomId)
                .senderId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
