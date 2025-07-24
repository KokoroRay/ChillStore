package com.esms.controller;

import com.esms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/contact")
    public String showContactForm() {
        return "customer/contact";
    }

    @PostMapping("/contact")
    public String submitContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message, Model model) {

        try {
            String contact = "Name: " + name + "\nEmail: " + email + "\nSubject: " + subject + "\nMessage: " + message;
            emailService.sendSimpleMessage("kokororay356@gmail.com", subject, contact);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", true);
        }
        return "customer/contact";

    }
}
