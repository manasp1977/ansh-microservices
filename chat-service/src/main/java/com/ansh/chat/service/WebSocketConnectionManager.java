package com.ansh.chat.service;

import com.ansh.chat.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages WebSocket connections for all users
 * Tracks active sessions and provides methods to send messages to specific users
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketConnectionManager {

    private final ObjectMapper objectMapper;

    // Map of userId -> WebSocketSession
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    // Map of sessionId -> userId
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    /**
     * Register a new WebSocket connection for a user
     */
    public void addConnection(String userId, WebSocketSession session) {
        // Remove old session if exists
        WebSocketSession oldSession = activeSessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
            } catch (IOException e) {
                log.error("Error closing old session for user {}: {}", userId, e.getMessage());
            }
        }

        activeSessions.put(userId, session);
        sessionToUser.put(session.getId(), userId);
        log.info("User {} connected. Total active connections: {}", userId, activeSessions.size());
    }

    /**
     * Remove a WebSocket connection
     */
    public void removeConnection(WebSocketSession session) {
        String userId = sessionToUser.remove(session.getId());
        if (userId != null) {
            activeSessions.remove(userId);
            log.info("User {} disconnected. Total active connections: {}", userId, activeSessions.size());
        }
    }

    /**
     * Remove a connection by userId
     */
    public void removeConnectionByUserId(String userId) {
        WebSocketSession session = activeSessions.remove(userId);
        if (session != null) {
            sessionToUser.remove(session.getId());
            log.info("User {} connection removed. Total active connections: {}", userId, activeSessions.size());
        }
    }

    /**
     * Get userId from WebSocket session
     */
    public String getUserId(WebSocketSession session) {
        return sessionToUser.get(session.getId());
    }

    /**
     * Check if a user is currently connected
     */
    public boolean isUserConnected(String userId) {
        WebSocketSession session = activeSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * Send a message to a specific user
     */
    public void sendMessageToUser(String userId, WebSocketMessage message) {
        WebSocketSession session = activeSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("Message sent to user {}: {}", userId, message.getType());
            } catch (IOException e) {
                log.error("Error sending message to user {}: {}", userId, e.getMessage());
                // Remove the problematic session
                removeConnectionByUserId(userId);
            }
        } else {
            log.debug("User {} is not connected. Message not sent.", userId);
        }
    }

    /**
     * Send a text message to a specific user
     */
    public void sendTextToUser(String userId, String text) {
        WebSocketSession session = activeSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(text));
            } catch (IOException e) {
                log.error("Error sending text to user {}: {}", userId, e.getMessage());
                removeConnectionByUserId(userId);
            }
        }
    }

    /**
     * Broadcast a message to all connected users
     */
    public void broadcastMessage(WebSocketMessage message) {
        activeSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                sendMessageToUser(userId, message);
            }
        });
    }

    /**
     * Get the count of active connections
     */
    public int getActiveConnectionCount() {
        return activeSessions.size();
    }

    /**
     * Get all connected user IDs
     */
    public java.util.Set<String> getConnectedUserIds() {
        return activeSessions.keySet();
    }
}
