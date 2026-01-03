package com.ansh.chat.handler;

import com.ansh.chat.dto.SendMessageRequest;
import com.ansh.chat.dto.WebSocketMessage;
import com.ansh.chat.service.ChatService;
import com.ansh.chat.service.WebSocketConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket handler for chat messages
 * Handles incoming WebSocket messages and routes them appropriately
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final WebSocketConnectionManager connectionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract userId from query params or headers
        String userId = extractUserId(session);

        if (userId == null) {
            log.warn("Connection rejected: No userId provided");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        connectionManager.addConnection(userId, session);

        // Send connection confirmation
        WebSocketMessage confirmMessage = WebSocketMessage.builder()
                .type("CONNECT")
                .content("Connected successfully")
                .timestamp(System.currentTimeMillis())
                .build();
        connectionManager.sendMessageToUser(userId, confirmMessage);

        log.info("WebSocket connection established for user: {}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = connectionManager.getUserId(session);
        if (userId == null) {
            log.warn("Message from unknown session");
            return;
        }

        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            log.debug("Received message from user {}: type={}", userId, wsMessage.getType());

            switch (wsMessage.getType()) {
                case "MESSAGE":
                    handleChatMessage(userId, wsMessage);
                    break;
                case "TYPING":
                    handleTypingIndicator(userId, wsMessage);
                    break;
                case "READ_RECEIPT":
                    handleReadReceipt(userId, wsMessage);
                    break;
                default:
                    log.warn("Unknown message type: {}", wsMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing message from user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectionManager.removeConnection(session);
        log.info("WebSocket connection closed: {}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", exception.getMessage());
        connectionManager.removeConnection(session);
    }

    /**
     * Handle incoming chat message
     */
    private void handleChatMessage(String senderId, WebSocketMessage wsMessage) {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .receiverId(wsMessage.getReceiverId())
                    .content(wsMessage.getContent())
                    .messageType(wsMessage.getType())
                    .build();

            // Save message and send to receiver via ChatService
            chatService.sendMessage(senderId, request);
        } catch (Exception e) {
            log.error("Error handling chat message: {}", e.getMessage());
        }
    }

    /**
     * Handle typing indicator
     */
    private void handleTypingIndicator(String senderId, WebSocketMessage wsMessage) {
        String receiverId = wsMessage.getReceiverId();
        if (receiverId != null && connectionManager.isUserConnected(receiverId)) {
            WebSocketMessage typingMsg = WebSocketMessage.typing(
                wsMessage.getChatRoomId(),
                senderId,
                receiverId
            );
            connectionManager.sendMessageToUser(receiverId, typingMsg);
        }
    }

    /**
     * Handle read receipt
     */
    private void handleReadReceipt(String userId, WebSocketMessage wsMessage) {
        try {
            chatService.markMessagesAsRead(wsMessage.getChatRoomId(), userId);

            // Notify the sender
            String senderId = chatService.getOtherUserId(wsMessage.getChatRoomId(), userId);
            if (senderId != null && connectionManager.isUserConnected(senderId)) {
                WebSocketMessage receiptMsg = WebSocketMessage.readReceipt(
                    wsMessage.getChatRoomId(),
                    userId
                );
                connectionManager.sendMessageToUser(senderId, receiptMsg);
            }
        } catch (Exception e) {
            log.error("Error handling read receipt: {}", e.getMessage());
        }
    }

    /**
     * Extract userId from WebSocket session
     * Looks for userId in query parameters
     */
    private String extractUserId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null && uri.getQuery() != null) {
                String query = uri.getQuery();
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting userId: {}", e.getMessage());
        }
        return null;
    }
}
