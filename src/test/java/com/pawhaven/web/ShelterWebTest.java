package com.pawhaven.web;

import com.pawhaven.support.DonationRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShelterWebTest {
    @Autowired MockMvc mvc;
    @Autowired DonationRepository donations;

    @Test
    void homepageRendersSeededAnimalsProductsAndLocalHero() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Maple")))
                .andExpect(content().string(containsString("Shelter Canvas Tote")))
                .andExpect(content().string(containsString("/images/animals/milo.jpg")));
    }

    @Test
    void signInProtectsAdminAndCsrfProtectsWrites() throws Exception {
        mvc.perform(get("/admin/products")).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(post("/support").param("token", "forged").param("amount", "500"))
                .andExpect(status().isForbidden());
        mvc.perform(formLogin().user("admin").password("incorrect"))
                .andExpect(redirectedUrl("/login?error"));
        var login = mvc.perform(formLogin().user("admin").password("ShelterDemo17!"))
                .andExpect(redirectedUrl("/admin")).andReturn();
        mvc.perform(get("/admin").session((MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Shelter overview")));
    }

    @Test
    void donationRejectsInvalidAmountAndAcceptsValidTokenOnlyOnce() throws Exception {
        MockHttpSession session = new MockHttpSession();
        var form = mvc.perform(get("/support").session(session)).andExpect(status().isOk()).andReturn();
        String token = (String) form.getModelAndView().getModel().get("token");
        long before = donations.count();

        mvc.perform(post("/support").with(csrf()).session(session).param("token", token))
                .andExpect(model().attributeHasFieldErrors("donationForm", "amount"));
        mvc.perform(post("/support").with(csrf()).session(session).param("token", token).param("amount", "-1"))
                .andExpect(model().attributeHasFieldErrors("donationForm", "amount"));
        assertThat(donations.count()).isEqualTo(before);

        mvc.perform(post("/support").with(csrf()).session(session).param("token", token).param("amount", "1234.50"))
                .andExpect(redirectedUrl("/support"))
                .andExpect(flash().attribute("donatedAmount", new BigDecimal("1234.50")));
        mvc.perform(post("/support").with(csrf()).session(session).param("token", token).param("amount", "1234.50"))
                .andExpect(redirectedUrl("/support")).andExpect(flash().attributeExists("notice"));
        assertThat(donations.count()).isEqualTo(before + 1);
    }
}
