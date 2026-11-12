package com.pawhaven.commerce;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Component
class ProductSeeder implements ApplicationRunner {
    private final ProductRepository products;

    ProductSeeder(ProductRepository products) {
        this.products = products;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (products.count() != 0) return;
        products.saveAll(List.of(
            product("canvas-tote", "Shelter Canvas Tote", "A sturdy cotton tote for market days, with long handles and our shelter paw print.", 6500, 5, "Merchandise"),
            product("rope-toy", "Braided Rope Toy", "A soft cotton tug rope for supervised games with your dog.", 2500, 16, "Toys"),
            product("ceramic-bowl", "Sage Ceramic Bowl", "A glazed ceramic bowl with a wide base for everyday meals and fresh water.", 4200, 9, "Feeding"),
            product("fleece-blanket", "Fleece Pet Blanket", "A washable fleece blanket for a quiet corner of the sofa or a travel crate.", 5600, 10, "Comfort"),
            product("catnip-mouse", "Catnip Mouse", "A little fabric mouse filled with catnip for a lively afternoon of play.", 1200, 22, "Toys"),
            product("dog-kibble", "Dog Kibble", "A two-kilogram bag of everyday dry food for adult dogs.", 7200, 12, "Food"),
            product("cat-food", "Cat Food", "A pack of four wet-food tins for adult cats.", 2800, 18, "Food"),
            product("collar", "Everyday Collar", "An adjustable woven dog collar with a smooth buckle and ID tag ring.", 3500, 8, "Accessories"),
            product("pet-bed", "Nest Pet Bed", "A round cushioned bed with soft sides and a removable cover.", 12500, 4, "Comfort"),
            product("enamel-pin", "PawHaven Enamel Pin", "A small paw-shaped enamel pin to carry a little of the shelter with you.", 1900, 25, "Merchandise")
        ));
    }

    private Product product(String slug, String name, String description, int price, int stock, String category) {
        return new Product(name, description, BigDecimal.valueOf(price), stock, category, "/images/products/" + slug + ".svg");
    }
}
