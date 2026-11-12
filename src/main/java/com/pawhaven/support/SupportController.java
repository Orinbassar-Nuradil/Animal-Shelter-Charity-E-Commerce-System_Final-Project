package com.pawhaven.support;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

@Controller
@RequestMapping("/support")
public class SupportController {
    private static final String SESSION_TOKEN = "supportFormToken";
    private final DonationRepository donations;

    public SupportController(DonationRepository donations) {
        this.donations = donations;
    }

    @GetMapping
    public String form(Model model, HttpSession session) {
        synchronized (WebUtils.getSessionMutex(session)) {
            if (session.getAttribute(SESSION_TOKEN) == null) {
                session.setAttribute(SESSION_TOKEN, UUID.randomUUID().toString());
            }
            model.addAttribute("token", session.getAttribute(SESSION_TOKEN));
        }
        DonationForm form = new DonationForm();
        form.setAmount(new BigDecimal("2500"));
        model.addAttribute("donationForm", form);
        return "support";
    }

    @PostMapping
    public String donate(@Valid @ModelAttribute DonationForm donationForm, BindingResult errors,
                         @RequestParam String token, HttpSession session, Model model, RedirectAttributes flash) {
        synchronized (WebUtils.getSessionMutex(session)) {
            if (!token.equals(session.getAttribute(SESSION_TOKEN))) {
                flash.addFlashAttribute("notice", "This donation form has expired. Use the new form below.");
                return "redirect:/support";
            }
            if (errors.hasErrors()) {
                model.addAttribute("token", token);
                return "support";
            }
            Donation saved = donations.save(new Donation(donationForm.getAmount()));
            session.removeAttribute(SESSION_TOKEN);
            flash.addFlashAttribute("donatedAmount", saved.getAmount());
        }
        return "redirect:/support";
    }
}
