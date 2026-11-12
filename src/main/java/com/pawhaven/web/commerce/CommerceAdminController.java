package com.pawhaven.web.commerce;

import com.pawhaven.commerce.CommerceException;
import com.pawhaven.commerce.InventoryRecordRepository;
import com.pawhaven.commerce.InventoryService;
import com.pawhaven.commerce.ProductRepository;
import com.pawhaven.commerce.ShelterOrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class CommerceAdminController {
    private final ProductRepository products;
    private final InventoryService inventory;
    private final InventoryRecordRepository records;
    private final ShelterOrderRepository orders;

    public CommerceAdminController(ProductRepository products, InventoryService inventory,
                                   InventoryRecordRepository records, ShelterOrderRepository orders) {
        this.products = products;
        this.inventory = inventory;
        this.records = records;
        this.orders = orders;
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", products.findAllByOrderByIdAsc());
        return "admin/products";
    }

    @PostMapping("/products/{id}/stock")
    public String stock(@PathVariable long id, @RequestParam int stock, RedirectAttributes flash) {
        try {
            inventory.setStock(id, stock);
            flash.addFlashAttribute("shopNotice", "Stock quantity saved.");
        } catch (CommerceException ex) {
            flash.addFlashAttribute("shopError", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orders.findAllByOrderByIdDesc());
        return "admin/orders";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("records", records.findTop100ByOrderByIdDesc());
        return "admin/inventory";
    }
}
