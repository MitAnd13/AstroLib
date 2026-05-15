package ru.msu.cmc.cipher.astrolib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.services.SearchService;

@Controller
public class EventController {
    private final EventDAO eventDAO;
    private final SearchService searchService;

    public EventController(EventDAO eventDAO, SearchService searchService) {
        this.eventDAO = eventDAO;
        this.searchService = searchService;
    }

    @GetMapping("/events/{id}")
    public String showEvent(@PathVariable Long id, Model model) {
        Events event = eventDAO.getById(id);
        if (event == null) {
            return "redirect:/search/events";
        }

        model.addAttribute("event", event);
        model.addAttribute("links", searchService.getEventLinks(id));
        return "events/detail";
    }
}
