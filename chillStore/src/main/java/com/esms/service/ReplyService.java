package com.esms.service;

import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.model.entity.Reply;

public interface ReplyService {
    void saveReply(ReplyFeedbackDTO replyFeedbackDTO);
    Reply getReplyByFeedbackId(int feedbackId);
    Reply updateReply(int feedbackId, ReplyFeedbackDTO dto, int staffId);
}

