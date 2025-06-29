package com.esms.service;

import com.esms.model.dto.FeedbackDto;

import java.util.List;

public interface FeedbackService {
    List<FeedbackDto> getAllFeedbacks();
}
