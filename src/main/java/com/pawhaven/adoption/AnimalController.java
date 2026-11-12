package com.pawhaven.adoption;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/animals")
public class AnimalController {
    private final AdoptionService adoption;

    public AnimalController(AdoptionService adoption) { this.adoption = adoption; }

    @GetMapping
    public String catalog(@RequestParam(required = false) Species species,
                          @RequestParam(required = false) AdoptionStatus status, Model model) {
        model.addAttribute("animals", adoption.catalog(species, status));
        model.addAttribute("selectedSpecies", species);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("speciesOptions", Species.values());
        model.addAttribute("statusOptions", AdoptionStatus.values());
        return "animals/catalog";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("animal", adoption.animal(id));
        return "animals/detail";
    }

    @GetMapping("/{id}/apply")
    public String form(@PathVariable long id, Model model, RedirectAttributes flash) {
        Animal animal = adoption.animal(id);
        if (animal.getStatus() != AdoptionStatus.AVAILABLE) {
            flash.addFlashAttribute("adoptionProblem", "This animal is currently unavailable for applications.");
            return "redirect:/animals/" + id;
        }
        model.addAttribute("animal", animal);
        model.addAttribute("adoptionForm", new AdoptionForm());
        return "animals/apply";
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable long id, @Valid @ModelAttribute("adoptionForm") AdoptionForm form,
                        BindingResult errors, Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            model.addAttribute("animal", adoption.animal(id));
            return "animals/apply";
        }
        try {
            adoption.apply(id, form);
            flash.addFlashAttribute("adoptionNotice", "Application received. The shelter team will review your request.");
        } catch (AdoptionConflict conflict) {
            flash.addFlashAttribute("adoptionProblem", conflict.getMessage());
        }
        return "redirect:/animals/" + id;
    }
}
