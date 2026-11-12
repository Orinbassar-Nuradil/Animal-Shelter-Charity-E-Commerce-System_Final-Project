package com.pawhaven.web;

import com.pawhaven.adoption.AdoptionStatus;
import com.pawhaven.adoption.AnimalRepository;
import com.pawhaven.adoption.ApplicationRepository;
import com.pawhaven.adoption.ApplicationStatus;
import com.pawhaven.commerce.ProductRepository;
import com.pawhaven.commerce.ShelterOrderRepository;
import com.pawhaven.support.DonationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShelterController {
    private final AnimalRepository animals;
    private final ProductRepository products;
    private final ApplicationRepository applications;
    private final ShelterOrderRepository orders;
    private final DonationRepository donations;

    public ShelterController(AnimalRepository animals, ProductRepository products,
                            ApplicationRepository applications, ShelterOrderRepository orders,
                            DonationRepository donations) {
        this.animals = animals;
        this.products = products;
        this.applications = applications;
        this.orders = orders;
        this.donations = donations;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredAnimals", animals.findTop3ByStatusOrderByIdAsc(AdoptionStatus.AVAILABLE));
        model.addAttribute("featuredProducts", products.findTop3ByActiveTrueOrderByIdAsc());
        model.addAttribute("availableCount", animals.countByStatus(AdoptionStatus.AVAILABLE));
        model.addAttribute("adoptedCount", animals.countByStatus(AdoptionStatus.ADOPTED));
        model.addAttribute("productCount", products.count());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("animalCount", animals.count());
        model.addAttribute("pendingCount", applications.countByStatus(ApplicationStatus.PENDING));
        model.addAttribute("orderCount", orders.count());
        model.addAttribute("donationCount", donations.count());
        return "admin/dashboard";
    }
}
