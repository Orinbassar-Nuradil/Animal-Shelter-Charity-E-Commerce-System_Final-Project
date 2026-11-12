package com.pawhaven.adoption;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdoptionWebTest {
    @Autowired MockMvc mvc;
    @Autowired AdoptionService adoption;
    @Autowired ApplicationRepository applications;

    @Test
    void catalogFiltersBothDimensionsAndMissingAnimalsReturn404() throws Exception {
        mvc.perform(get("/animals").param("species", "CAT").param("status", "ON_HOLD"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cleo")))
                .andExpect(content().string(not(containsString("Bruno"))))
                .andExpect(content().string(not(containsString("Maple"))));
        mvc.perform(get("/animals/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/admin/applications")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesAProfileAndReviewsAnApplicationSubmittedThroughItsPublicForm() throws Exception {
        mvc.perform(get("/admin/animals/new")).andExpect(status().isOk());
        mvc.perform(post("/admin/animals").with(csrf())
                        .param("species", "DOG").param("name", "Juniper").param("sex", "FEMALE")
                        .param("ageMonths", "18").param("breed", "Collie mix")
                        .param("description", "Juniper enjoys long walks.")
                        .param("careNotes", "Keep her on a regular walking schedule."))
                .andExpect(redirectedUrl("/admin/animals"));

        Animal dog = adoption.catalog(Species.DOG, AdoptionStatus.AVAILABLE).stream()
                .filter(animal -> animal.getName().equals("Juniper")).findFirst().orElseThrow();
        String profileUrl = "/animals/" + dog.getId();
        mvc.perform(get(profileUrl)).andExpect(status().isOk()).andExpect(content().string(containsString("Juniper")));
        mvc.perform(get(profileUrl + "/apply")).andExpect(status().isOk());
        mvc.perform(post(profileUrl + "/apply").with(csrf()).param("email", "invalid").param("phone", "----"))
                .andExpect(model().attributeHasFieldErrors("adoptionForm", "email", "phone", "reason"));
        mvc.perform(post(profileUrl + "/apply").with(csrf())
                        .param("applicantName", "Demo Applicant").param("email", "guest@example.test")
                        .param("phone", "+7 (700) 555-0100").param("housing", "A quiet home near a park.")
                        .param("reason", "We have time for daily walks."))
                .andExpect(redirectedUrl(profileUrl));
        AdoptionApplication pending = applications.findByAnimalIdAndStatus(dog.getId(), ApplicationStatus.PENDING).get(0);
        assertThat(dog.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
        mvc.perform(get("/admin/applications")).andExpect(status().isOk())
                .andExpect(content().string(containsString("guest@example.test")));
        mvc.perform(post("/admin/applications/{id}/decision", pending.getId()).with(csrf()).param("decision", "APPROVED"))
                .andExpect(redirectedUrl("/admin/applications"));
        assertThat(adoption.animal(dog.getId()).getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
        assertThat(pending.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }
}
