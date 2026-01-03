# Chat Service

Real-time chat microservice with WebSocket support for one-on-one messaging between users in the AnshShare platform.

## Features

- **Real-time Messaging**: WebSocket-based instant messaging
- **Connection Management**: Tracks active user connections
- **Message Persistence**: All messages stored in PostgreSQL
- **Read Receipts**: Track message read status
- **Typing Indicators**: Real-time typing notifications
- **Unread Message Count**: Track unread messages per user
- **Chat History**: Retrieve conversation history with pagination
- **RESTful API**: Alternative HTTP endpoints for chat operations

## Technology Stack

- **Spring Boot 3.2.0**: Core framework
- **Spring WebSocket**: WebSocket support with SockJS fallback
- **PostgreSQL**: Message and chat room persistence
- **Spring Data JPA**: Data access layer
- **Lombok**: Reduce boilerplate code
- **Jackson**: JSON serialization

## Database Schema

### chat_rooms
```sql
CREATE TABLE chat_rooms (
    id VARCHAR(50) PRIMARY KEY,
    user1_id VARCHAR(50) NOT NULL,
    user2_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    last_message_at TIMESTAMP
);
```

### chat_messages
```sql
CREATE TABLE chat_messages (
    id VARCHAR(50) PRIMARY KEY,
    chat_room_id VARCHAR(50) NOT NULL,
    sender_id VARCHAR(50) NOT NULL,
    receiver_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP,
    message_type VARCHAR(20),
    INDEX idx_chat_room_id (chat_room_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at)
);
```

## Architecture

### Components

1. **WebSocketChatHandler**: Handles WebSocket connections and messages
2. **WebSocketConnectionManager**: Manages active WebSocket sessions
3. **ChatService**: Business logic for chat operations
4. **ChatController**: REST API endpoints
5. **Repositories**: Data access layer

### Connection Manager

The `WebSocketConnectionManager` maintains:
- Map of `userId` → `WebSocketSession`
- Map of `sessionId` → `userId`
- Methods to send messages to specific users
- Connection tracking and cleanup

## WebSocket API

### Connection

Connect to WebSocket endpoint with userId as query parameter:

```
ws://localhost:8089/ws/chat?userId={userId}
```

Or using SockJS fallback:
```
http://localhost:8089/ws/chat?userId={userId}
```

### Message Types

#### 1. Send Message
```json
{
  "type": "MESSAGE",
  "receiverId": "user_456",
  "content": "Hello!",
  "chatRoomId": "user_123_user_456"
}
```

#### 2. Typing Indicator
```json
{
  "type": "TYPING",
  "chatRoomId": "user_123_user_456",
  "receiverId": "user_456"
}
```

#### 3. Read Receipt
```json
{
  "type": "READ_RECEIPT",
  "chatRoomId": "user_123_user_456"
}
```

### Received Messages

#### New Message
```json
{
  "type": "MESSAGE",
  "chatRoomId": "user_123_user_456",
  "senderId": "user_123",
  "receiverId": "user_456",
  "message": {
    "id": "msg_abc123",
    "chatRoomId": "user_123_user_456",
    "senderId": "user_123",
    "receiverId": "user_456",
    "content": "Hello!",
    "isRead": false,
    "createdAt": "2025-12-29T10:30:00",
    "messageType": "TEXT"
  },
  "timestamp": 1735469400000
}
```

#### Typing Indicator
```json
{
  "type": "TYPING",
  "chatRoomId": "user_123_user_456",
  "senderId": "user_123",
  "receiverId": "user_456",
  "timestamp": 1735469400000
}
```

#### Read Receipt
```json
{
  "type": "READ_RECEIPT",
  "chatRoomId": "user_123_user_456",
  "senderId": "user_456",
  "timestamp": 1735469400000
}
```

## REST API

All endpoints require `X-User-Id` header for authentication (set by API Gateway).

### Endpoints

#### Get or Create Chat Room
```
GET /api/chat/rooms/{otherUserId}
```

**Response**:
```json
{
  "id": "user_123_user_456",
  "user1Id": "user_123",
  "user2Id": "user_456",
  "createdAt": "2025-12-29T10:00:00",
  "lastMessageAt": "2025-12-29T10:30:00"
}
```

#### Get All Chat Rooms
```
GET /api/chat/rooms
```

**Response**:
```json
[
  {
    "id": "user_123_user_456",
    "user1Id": "user_123",
    "user2Id": "user_456",
    "otherUserId": "user_456",
    "otherUserName": "Jane Doe",
    "createdAt": "2025-12-29T10:00:00",
    "lastMessageAt": "2025-12-29T10:30:00",
    "unreadCount": 3,
    "lastMessage": {
      "id": "msg_abc123",
      "content": "Hello!",
      "createdAt": "2025-12-29T10:30:00"
    }
  }
]
```

#### Send Message (REST)
```
POST /api/chat/messages
Content-Type: application/json

{
  "receiverId": "user_456",
  "content": "Hello!",
  "messageType": "TEXT"
}
```

**Response**:
```json
{
  "id": "msg_abc123",
  "chatRoomId": "user_123_user_456",
  "senderId": "user_123",
  "receiverId": "user_456",
  "content": "Hello!",
  "isRead": false,
  "createdAt": "2025-12-29T10:30:00",
  "messageType": "TEXT"
}
```

#### Get Chat Room Messages
```
GET /api/chat/rooms/{chatRoomId}/messages?page=0&size=50
```

**Response**:
```json
[
  {
    "id": "msg_abc123",
    "chatRoomId": "user_123_user_456",
    "senderId": "user_123",
    "receiverId": "user_456",
    "content": "Hello!",
    "isRead": true,
    "createdAt": "2025-12-29T10:30:00",
    "messageType": "TEXT"
  }
]
```

#### Mark Messages as Read
```
POST /api/chat/rooms/{chatRoomId}/read
```

**Response**:
```json
{
  "message": "Messages marked as read"
}
```

#### Get Unread Count
```
GET /api/chat/unread-count
```

**Response**:
```json
{
  "unreadCount": 5
}
```

#### Delete Chat Room
```
DELETE /api/chat/rooms/{chatRoomId}
```

**Response**:
```json
{
  "message": "Chat room deleted successfully"
}
```

## Integration with Frontend

### Example WebSocket Connection (JavaScript)

```javascript
// Using native WebSocket
const userId = 'user_123';
const ws = new WebSocket(`ws://localhost:8089/ws/chat?userId=${userId}`);

ws.onopen = () => {
  console.log('Connected to chat server');
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('Received:', message);

  switch(message.type) {
    case 'MESSAGE':
      displayNewMessage(message.message);
      break;
    case 'TYPING':
      showTypingIndicator(message.senderId);
      break;
    case 'READ_RECEIPT':
      markMessagesAsRead(message.chatRoomId);
      break;
  }
};

ws.onclose = () => {
  console.log('Disconnected from chat server');
};

// Send message
const sendMessage = (receiverId, content) => {
  const message = {
    type: 'MESSAGE',
    receiverId: receiverId,
    content: content
  };
  ws.send(JSON.stringify(message));
};
```

### Using SockJS (for older browser support)

```javascript
import SockJS from 'sockjs-client';

const userId = 'user_123';
const socket = new SockJS(`http://localhost:8089/ws/chat?userId=${userId}`);

socket.onopen = () => {
  console.log('Connected via SockJS');
};

socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  handleMessage(message);
};
```

## Configuration

### application.yml

```yaml
spring:
  application:
    name: CHAT-SERVICE
  datasource:
    url: jdbc:postgresql://localhost:5432/chat_db
    username: postgres
    password: ${DB_PASSWORD:manasp1977}

server:
  port: 8089

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

websocket:
  max-text-message-buffer-size: 16384
  max-binary-message-buffer-size: 16384
  max-session-idle-timeout: 300000
```

## Running the Service

### Prerequisites
1. PostgreSQL running on port 5432
2. chat_db database created
3. Eureka Server running on port 8761

### Start the Service

```bash
cd ansh-microservices/chat-service
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/chat-service-1.0.0.jar
```

## Testing

### Test WebSocket Connection

Using `wscat` (WebSocket command-line client):

```bash
npm install -g wscat
wscat -c "ws://localhost:8089/ws/chat?userId=user_123"
```

### Test REST API

```bash
# Get chat rooms
curl -H "X-User-Id: user_123" http://localhost:8089/api/chat/rooms

# Send message
curl -X POST http://localhost:8089/api/chat/messages \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user_123" \
  -d '{"receiverId":"user_456","content":"Hello!"}'

# Get messages
curl -H "X-User-Id: user_123" \
  http://localhost:8089/api/chat/rooms/user_123_user_456/messages
```

## Monitoring

### Active Connections

The `WebSocketConnectionManager` tracks:
- Number of active connections
- Connected user IDs
- Session health

### Logs

Debug logging enabled for:
- WebSocket connections/disconnections
- Message sending/receiving
- Connection manager operations

## Future Enhancements

- [ ] Group chat support
- [ ] File/image sharing
- [ ] Message encryption
- [ ] Message delivery confirmations
- [ ] Push notifications for offline users
- [ ] Message search functionality
- [ ] Chat room archiving
- [ ] User blocking/muting
- [ ] Message reactions (emojis)
- [ ] Voice/video call integration

## Port

**8089**

## Dependencies

- ansh-common (for JWT utilities if needed)
- PostgreSQL
- Eureka Server

## Notes

- Messages are automatically saved to database even if receiver is offline
- WebSocket connection automatically reconnects on disconnect
- SockJS provides fallback for browsers that don't support WebSocket
- All timestamps are in UTC
- Chat room IDs are deterministic based on user IDs (always sorted)
