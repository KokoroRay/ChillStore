package com.esms.controller;


import com.esms.model.dto.FeedbackDTO;
import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.service.FeedbackService;
import com.esms.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff/manageFeedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ReplyService replyService;

    @GetMapping
    public String viewFeedbackList(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedbacks());
        return "staff/feedback/manageFeedback";
    }

    @GetMapping("/{id}/reply")
    public String showReplyForm(@PathVariable("id") int feedbackId, Model model) {
        FeedbackDTO feedback = feedbackService.getAllFeedbacks()
                .stream()
                .filter(f -> f.getId() == feedbackId)
                .findFirst().orElse(null);

        ReplyFeedbackDTO replyDTO = new ReplyFeedbackDTO();
        replyDTO.setFeedbackId(feedbackId);

        model.addAttribute("feedback", feedback);
        model.addAttribute("reply", replyDTO);
        return "staff/feedback/replyForm";
    }

    @PostMapping("/reply")
    public String submitReply(@ModelAttribute("reply") ReplyFeedbackDTO dto) {
        dto.setStaffId(1);

        replyService.saveReply(dto);
        return "redirect:/staff/feedback/feedbackmanageFeedback";
    }

}
