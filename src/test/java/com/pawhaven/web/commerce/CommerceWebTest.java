package com.pawhaven.web.commerce;

import com.pawhaven.commerce.CheckoutRequest;
import com.pawhaven.commerce.Product;
import com.pawhaven.commerce.ProductRepository;
import com.pawhaven.commerce.SessionCart;
import com.pawhaven.commerce.ShelterOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "pawhaven.test.suite=commerce-web")
@AutoConfigureMockMvc
class CommerceWebTest {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired ShelterOrderRepository orders;

    @Test
    void twoProductPurchaseRendersItsReceiptAndObserverHistoryAndPreventsDoubleSubmission() throws Exception {
        var session = new MockHttpSession();
        Product tote = product("Shelter Canvas Tote");
        Product rope = product("Braided Rope Toy");
        int initialTotes = tote.getStock();
        long initialOrders = orders.count();
        mvc.perform(get("/shop").param("category", "Toys"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(rope.getName())))
                .andExpect(content().string(not(containsString(tote.getName()))));

        add(tote, session);
        add(rope, session);
        mvc.perform(post("/cart/items/{id}/update", tote.getId()).session(session).with(csrf()).param("quantity", "2"))
                .andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/cart").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("15,500 ₸")));
        CheckoutRequest details = (CheckoutRequest) mvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk()).andReturn().getModelAndView().getModel().get("details");
        mvc.perform(post("/checkout/review").with(csrf()).session(session)
                        .param("token", details.getToken()).param("customerName", "Demo Supporter")
                        .param("email", "supporter@example.test").param("fulfillment", "DELIVERY")
                        .param("address", "1 Demo Road").param("promoCode", "PAWS10").param("donation", "400")
                        .param("giftWrapping", "true").param("total", "0.01"))
                .andExpect(redirectedUrl("/checkout/review"));

        var review = mvc.perform(get("/checkout/review").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("14,850 ₸")))
                .andExpect(content().string(containsString("1 Demo Road"))).andReturn();
        String reviewId = review.getModelAndView().getModel().get("reviewId").toString();
        String receipt = mvc.perform(post("/checkout").with(csrf()).session(session).param("reviewId", reviewId))
                .andExpect(redirectedUrlPattern("/checkout/receipt/*")).andReturn().getResponse().getRedirectedUrl();

        mvc.perform(get(receipt).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("14,850 ₸")))
                .andExpect(content().string(containsString(rope.getName())));
        mvc.perform(get(receipt)).andExpect(status().isNotFound());
        mvc.perform(post("/checkout").with(csrf()).session(session).param("reviewId", reviewId))
                .andExpect(redirectedUrl(receipt));
        assertThat(orders.count()).isEqualTo(initialOrders + 1);
        assertThat(product(tote.getName()).getStock()).isEqualTo(initialTotes - 2);
        mvc.perform(get("/cart").session(session)).andExpect(content().string(containsString("Your cart is empty")));

        mvc.perform(get("/admin/orders")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/orders").with(user("staff").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Demo Supporter")));
        mvc.perform(get("/admin/inventory").with(user("staff").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Low stock")))
                .andExpect(content().string(containsString(tote.getName())));
        mvc.perform(post("/admin/products/{id}/stock", tote.getId()).with(csrf())
                        .with(user("staff").roles("ADMIN")).param("stock", "7"))
                .andExpect(redirectedUrl("/admin/products"));
        mvc.perform(get("/admin/products").with(user("staff").roles("ADMIN"))).andExpect(status().isOk());
        assertThat(product(tote.getName()).getStock()).isEqualTo(7);
        mvc.perform(get("/admin/inventory").with(user("staff").roles("ADMIN")))
                .andExpect(content().string(containsString("Admin stock adjustment")));
    }

    @Test
    void invalidQuantitiesCouponsAndDeliveryDetailsLeaveTheCartEditable() throws Exception {
        var session = new MockHttpSession();
        Product bowl = product("Sage Ceramic Bowl");
        add(bowl, session);
        mvc.perform(post("/cart/items/{id}/update", bowl.getId()).session(session).with(csrf()).param("quantity", "0"))
                .andExpect(flash().attributeExists("shopError"));
        mvc.perform(post("/cart/items/{id}/add", bowl.getId()).session(session).with(csrf()).param("quantity", "999"))
                .andExpect(flash().attributeExists("shopError"));
        assertThat(SessionCart.from(session).getItemCount()).isEqualTo(1);

        String token = SessionCart.from(session).getToken();
        mvc.perform(post("/checkout/review").session(session).with(csrf()).param("token", token)
                        .param("customerName", "Test Guest").param("email", "guest@example.test").param("fulfillment", "DELIVERY"))
                .andExpect(model().attributeHasFieldErrors("details", "deliveryAddressValid"));
        mvc.perform(post("/checkout/review").session(session).with(csrf()).param("token", token)
                        .param("customerName", "Test Guest").param("email", "guest@example.test").param("promoCode", "MISSPELLED"))
                .andExpect(content().string(containsString("promotional code is not valid")));
        mvc.perform(post("/cart/items/{id}/remove", bowl.getId()).session(session))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cart/items/{id}/remove", bowl.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/checkout").session(session)).andExpect(redirectedUrl("/cart"));
    }

    private Product product(String name) {
        return products.findAllByOrderByIdAsc().stream().filter(p -> p.getName().equals(name)).findFirst().orElseThrow();
    }

    private void add(Product product, MockHttpSession session) throws Exception {
        mvc.perform(post("/cart/items/{id}/add", product.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/shop"));
    }
}
