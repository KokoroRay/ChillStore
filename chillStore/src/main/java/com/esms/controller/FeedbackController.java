package com.esms.controller;


import com.esms.model.dto.FeedbackDTO;
import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.service.FeedbackService;
import com.esms.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping({"/admin/manageFeedback","/staff/manageFeedback"})
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ReplyService replyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String viewFeedbackList(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedbacks());
        return "staff/feedback/manageFeedback";
    }

    @GetMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String submitReply(@ModelAttribute("reply") ReplyFeedbackDTO dto) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            com.esms.model.entity.Admin admin = ((com.esms.service.impl.ReplyServiceImpl)replyService).getAdminRepository().findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Admin account not found!"));
            dto.setAdminId(admin.getAdminId());
            dto.setStaffId(0);
        } else {
            com.esms.model.entity.Staff staff = ((com.esms.service.impl.ReplyServiceImpl)replyService).getStaffRepository().findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Staff account not found!"));
            dto.setStaffId(staff.getId());
            dto.setAdminId(0);
        }
        replyService.saveReply(dto);
        return "redirect:/staff/manageFeedback";
    }
}
