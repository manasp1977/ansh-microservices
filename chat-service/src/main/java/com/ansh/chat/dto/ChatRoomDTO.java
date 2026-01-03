package com.ansh.chat.dto;

import com.ansh.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private String id;
    private String user1Id;
    private String user2Id;
    private String otherUserId;
    private String otherUserName;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private ChatMessageDTO lastMessage;

    public static ChatRoomDTO fromEntity(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .user1Id(chatRoom.getUser1Id())
                .user2Id(chatRoom.getUser2Id())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }
}
