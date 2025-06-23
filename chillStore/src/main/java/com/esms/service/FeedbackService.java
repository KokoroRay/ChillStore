package com.esms.service;

import com.esms.model.dto.FeedbackDTO;

import java.util.List;

public interface FeedbackService {
    List<FeedbackDTO> getAllFeedbacks();
}
