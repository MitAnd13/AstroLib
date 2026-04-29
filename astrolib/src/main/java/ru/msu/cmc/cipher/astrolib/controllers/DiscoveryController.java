package ru.msu.cmc.cipher.astrolib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.msu.cmc.cipher.astrolib.forms.DiscoveryForm;
import ru.msu.cmc.cipher.astrolib.services.DiscoveryService;

@Controller
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/discoveries/new")
    public String newDiscovery(Model model) {
        if (!model.containsAttribute("discoveryForm")) {
            model.addAttribute("discoveryForm", new DiscoveryForm());
        }
        return "discoveries/new";
    }

    @PostMapping("/discoveries/new")
    public String createDiscovery(@ModelAttribute("discoveryForm") DiscoveryForm discoveryForm, Model model) {
        model.addAttribute("discoveryForm", discoveryForm);

        try {
            discoveryService.createObject(discoveryForm);
            model.addAttribute("successMessage", "Объект успешно добавлен в базу данных");
            model.addAttribute("discoveryForm", new DiscoveryForm());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (Exception exception) {
            Throwable rootCause = exception;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            String message = rootCause.getMessage();
            if (message == null || message.isBlank()) {
                message = exception.getClass().getSimpleName();
            }
            model.addAttribute("errorMessage", "Не удалось сохранить объект: " + message);
        }

        return "discoveries/new";
    }
}
