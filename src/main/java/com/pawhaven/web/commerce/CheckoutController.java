package com.pawhaven.web.commerce;

import com.pawhaven.commerce.*;
import com.pawhaven.patterns.facade.CheckoutFacade;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private static final String REVIEW = CheckoutController.class.getName() + ".review";
    private static final String RECEIPT = CheckoutController.class.getName() + ".receipt.";
    private final CartService carts;
    private final CheckoutFacade checkout;
    private final ShelterOrderRepository orders;

    public CheckoutController(CartService carts, CheckoutFacade checkout, ShelterOrderRepository orders) {
        this.carts = carts;
        this.checkout = checkout;
        this.orders = orders;
    }

    @GetMapping
    public String details(HttpSession session, Model model) {
        SessionCart cart = SessionCart.from(session);
        synchronized (cart) {
            if (cart.getItemCount() == 0) {
                return "redirect:/cart";
            }
            Review review = (Review) session.getAttribute(REVIEW);
            CheckoutRequest details;
            if (review != null && cart.getToken().equals(review.details().getToken())) {
                details = review.details();
            } else {
                details = new CheckoutRequest();
                details.setToken(cart.getToken());
            }
            model.addAttribute("details", details);
            model.addAttribute("cart", carts.view(cart));
            return "checkout/details";
        }
    }

    @PostMapping("/review")
    public String review(@Valid @ModelAttribute("details") CheckoutRequest details, BindingResult errors,
                         HttpSession session, Model model, RedirectAttributes flash) {
        SessionCart cart = SessionCart.from(session);
        synchronized (cart) {
            if (!cart.getToken().equals(details.getToken())) {
                flash.addFlashAttribute("shopError", "Your cart changed. Please check the items before continuing.");
                return "redirect:/cart";
            }
            if (!errors.hasErrors()) {
                try {
                    checkout.preview(cart, details);
                    session.setAttribute(REVIEW, new Review(UUID.randomUUID(), details, null));
                    return "redirect:/checkout/review";
                } catch (CommerceException ex) {
                    errors.reject("checkout", ex.getMessage());
                }
            }
            model.addAttribute("cart", carts.view(cart));
            return "checkout/details";
        }
    }

    @GetMapping("/review")
    public String showReview(HttpSession session, Model model, RedirectAttributes flash) {
        SessionCart cart = SessionCart.from(session);
        synchronized (cart) {
            Review review = (Review) session.getAttribute(REVIEW);
            if (review == null || !cart.getToken().equals(review.details().getToken())) {
                flash.addFlashAttribute("shopError", "Please review your current cart before placing an order.");
                return "redirect:/cart";
            }
            try {
                model.addAttribute("price", checkout.preview(cart, review.details()));
                model.addAttribute("cart", carts.view(cart));
                model.addAttribute("details", review.details());
                model.addAttribute("reviewId", review.id());
                return "checkout/review";
            } catch (CommerceException ex) {
                flash.addFlashAttribute("shopError", ex.getMessage());
                return "redirect:/cart";
            }
        }
    }

    @PostMapping
    public String place(@RequestParam UUID reviewId, HttpSession session, RedirectAttributes flash) {
        SessionCart cart = SessionCart.from(session);
        synchronized (cart) {
            Review review = (Review) session.getAttribute(REVIEW);
            if (review == null || !review.id().equals(reviewId)) {
                flash.addFlashAttribute("shopError", "These checkout details expired. Please review your cart again.");
                return "redirect:/cart";
            }
            if (review.receipt() != null) {
                return "redirect:/checkout/receipt/" + review.receipt();
            }
            try {
                ShelterOrder order = checkout.placeOrder(cart, review.details());
                UUID code = order.getReceiptCode();
                session.setAttribute(REVIEW, new Review(reviewId, review.details(), code));
                session.setAttribute(RECEIPT + code, Boolean.TRUE);
                return "redirect:/checkout/receipt/" + code;
            } catch (CommerceException ex) {
                flash.addFlashAttribute("shopError", ex.getMessage());
                return "redirect:/cart";
            }
        }
    }

    @GetMapping("/receipt/{code}")
    public String receipt(@PathVariable UUID code, HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(RECEIPT + code))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("order", orders.findByReceiptCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "checkout/receipt";
    }

    private record Review(UUID id, CheckoutRequest details, UUID receipt) implements Serializable {}
}
