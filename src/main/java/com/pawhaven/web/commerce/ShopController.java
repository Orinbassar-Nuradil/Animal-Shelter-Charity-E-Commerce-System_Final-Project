package com.pawhaven.web.commerce;

import com.pawhaven.commerce.CartService;
import com.pawhaven.commerce.CommerceException;
import com.pawhaven.commerce.Product;
import com.pawhaven.commerce.ProductRepository;
import com.pawhaven.commerce.SessionCart;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopController {
    private final ProductRepository products;
    private final CartService carts;

    public ShopController(ProductRepository products, CartService carts) {
        this.products = products;
        this.carts = carts;
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(defaultValue = "") String category, Model model) {
        var catalog = products.findAllByOrderByIdAsc().stream().filter(Product::isActive).toList();
        model.addAttribute("categories", catalog.stream().map(Product::getCategory).distinct().toList());
        model.addAttribute("category", category);
        model.addAttribute("products", catalog.stream()
                .filter(p -> category.isBlank() || p.getCategory().equals(category)).toList());
        return "shop/index";
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("cart", carts.view(SessionCart.from(session)));
        return "cart/index";
    }

    @PostMapping("/cart/items/{id}/add")
    public String add(@PathVariable long id, @RequestParam(defaultValue = "1") int quantity,
                      HttpSession session, RedirectAttributes flash) {
        try {
            carts.add(SessionCart.from(session), id, quantity);
            flash.addFlashAttribute("shopNotice", "Added to your cart.");
        } catch (CommerceException ex) {
            flash.addFlashAttribute("shopError", ex.getMessage());
        }
        return "redirect:/shop";
    }

    @PostMapping("/cart/items/{id}/update")
    public String update(@PathVariable long id, @RequestParam int quantity,
                         HttpSession session, RedirectAttributes flash) {
        try {
            carts.update(SessionCart.from(session), id, quantity);
            flash.addFlashAttribute("shopNotice", "Cart quantity updated.");
        } catch (CommerceException ex) {
            flash.addFlashAttribute("shopError", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{id}/remove")
    public String remove(@PathVariable long id, HttpSession session, RedirectAttributes flash) {
        carts.remove(SessionCart.from(session), id);
        flash.addFlashAttribute("shopNotice", "Item removed from your cart.");
        return "redirect:/cart";
    }
}
