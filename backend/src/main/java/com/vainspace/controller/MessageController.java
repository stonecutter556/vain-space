package com.vainspace.controller;

import com.vainspace.entity.Message;
import com.vainspace.entity.User;
import com.vainspace.repository.MessageRepository;
import com.vainspace.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Message> messages = messageRepository.findConversationsByUserId(userId);
        Map<Long, Message> latestPerUser = new LinkedHashMap<>();
        Set<Long> partnerIds = new HashSet<>();

        for (Message msg : messages) {
            Long partnerId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (!latestPerUser.containsKey(partnerId)) {
                latestPerUser.put(partnerId, msg);
                partnerIds.add(partnerId);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestPerUser.entrySet()) {
            Long partnerId = entry.getKey();
            Message msg = entry.getValue();
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("partnerId", partnerId);
            item.put("partnerUsername", partner.getUsername());
            item.put("partnerNickname", partner.getNickname() != null ? partner.getNickname() : partner.getUsername());
            item.put("partnerAvatar", partner.getAvatar() != null ? partner.getAvatar() : "");
            item.put("lastMessage", msg.getContent());
            item.put("lastMessageTime", msg.getCreatedAt());
            item.put("unreadCount", messageRepository.countByReceiverIdAndIsReadFalse(partnerId));
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getMessages(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Message> sent = messageRepository.findBySenderIdAndReceiverIdOrderByCreatedAtAsc(currentUserId, userId);
        List<Message> received = messageRepository.findByReceiverIdAndSenderIdOrderByCreatedAtAsc(currentUserId, userId);

        Set<Message> merged = new TreeSet<>(Comparator.comparing(Message::getId));
        merged.addAll(sent);
        merged.addAll(received);

        List<Message> sorted = new ArrayList<>(merged);
        sorted.sort(Comparator.comparing(Message::getCreatedAt));

        for (Message msg : sorted) {
            if (msg.getReceiverId().equals(currentUserId) && !msg.getIsRead()) {
                msg.setIsRead(true);
                messageRepository.save(msg);
            }
        }

        return ResponseEntity.ok(sorted);
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long senderId = (Long) request.getAttribute("userId");
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = (String) body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "消息内容不能为空"));
        }

        if (!userRepository.existsById(receiverId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "接收用户不存在"));
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = messageRepository.countByReceiverIdAndIsReadFalse(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
