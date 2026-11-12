package com.pawhaven.adoption;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminAdoptionController {
    private final AdoptionService adoption;

    public AdminAdoptionController(AdoptionService adoption) { this.adoption = adoption; }

    @GetMapping("/animals")
    public String animals(Model model) {
        model.addAttribute("animals", adoption.catalog(null, null));
        return "admin/animals";
    }

    @GetMapping("/animals/new")
    public String newAnimal(Model model) {
        model.addAttribute("animalForm", new AnimalForm());
        return formOptions(model);
    }

    @PostMapping("/animals")
    public String addAnimal(@Valid @ModelAttribute("animalForm") AnimalForm form,
                            BindingResult errors, Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) return formOptions(model);
        Animal animal = adoption.addAnimal(form);
        flash.addFlashAttribute("adoptionNotice", animal.getName() + " has been added to the catalog.");
        return "redirect:/admin/animals";
    }

    @PostMapping("/animals/{id}/status")
    public String changeStatus(@PathVariable long id, @RequestParam AdoptionStatus status, RedirectAttributes flash) {
        try {
            adoption.changeAvailability(id, status);
            flash.addFlashAttribute("adoptionNotice", "Animal availability saved.");
        } catch (AdoptionConflict conflict) {
            flash.addFlashAttribute("adoptionProblem", conflict.getMessage());
        }
        return "redirect:/admin/animals";
    }

    @GetMapping("/applications")
    public String applications(Model model) {
        model.addAttribute("applications", adoption.applications());
        return "admin/applications";
    }

    @PostMapping("/applications/{id}/decision")
    public String review(@PathVariable long id, @RequestParam ApplicationStatus decision, RedirectAttributes flash) {
        try {
            adoption.decide(id, decision);
            flash.addFlashAttribute("adoptionNotice", "Application " + decision.getLabel().toLowerCase(java.util.Locale.ROOT) + ".");
        } catch (AdoptionConflict conflict) {
            flash.addFlashAttribute("adoptionProblem", conflict.getMessage());
        }
        return "redirect:/admin/applications";
    }

    private String formOptions(Model model) {
        model.addAttribute("speciesOptions", Species.values());
        model.addAttribute("sexOptions", Sex.values());
        return "admin/animals-new";
    }
}
