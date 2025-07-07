package com.esms.service.impl;

import com.esms.model.dto.FeedbackDTO;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Feedback;
import com.esms.repository.FeedbackRepository;
import com.esms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public List<FeedbackDTO> getAllFeedbacks() {
       /* return feedbackRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()); */
        return fakeData();

    }
    private List<FeedbackDTO> fakeData() {
        List<FeedbackDTO> list = new ArrayList<>();
        Customer customer = new Customer();
        customer.setName("Nguyễn Văn A");
        list.add(new FeedbackDTO(1, customer, "Máy lạnh", (byte) 5, "Sản phẩm rất tốt", "New", LocalDateTime.now()));
        list.add(new FeedbackDTO(2, customer, "Máy giặt", (byte) 4, "Tốt nhưng hơi ồn", "Replied", LocalDateTime.now().minusDays(1)));
        return list;
    }

    private FeedbackDTO convertToDTO(Feedback fb) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(fb.getId());
        dto.setCustomer(fb.getCustomer());
        dto.setProduct(fb.getProduct().getName());
        dto.setRating(fb.getRating());
        dto.setComment(fb.getComment());
        dto.setStatus(fb.getStatus());
        dto.setCreatedAt(fb.getCreatedAt());
        return dto;
    }
}

