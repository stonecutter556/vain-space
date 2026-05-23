package com.vainspace.repository;

import com.vainspace.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(Long senderId, Long receiverId);
    List<Message> findByReceiverIdAndSenderIdOrderByCreatedAtAsc(Long receiverId, Long senderId);

    @Query("SELECT m FROM Message m WHERE m.senderId = ?1 OR m.receiverId = ?1 ORDER BY m.createdAt DESC")
    List<Message> findConversationsByUserId(Long userId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
