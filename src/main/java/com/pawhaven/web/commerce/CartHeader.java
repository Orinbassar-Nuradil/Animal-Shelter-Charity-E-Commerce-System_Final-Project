package com.pawhaven.web.commerce;

import com.pawhaven.commerce.SessionCart;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CartHeader {
    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request) {
        SessionCart cart = SessionCart.peek(request.getSession(false));
        return cart == null ? 0 : cart.getItemCount();
    }
}
