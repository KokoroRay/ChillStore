package com.esms.repository;

import com.esms.model.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    Optional<Reply> findByFeedbackId(int feedbackId);
}
