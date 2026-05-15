package ru.msu.cmc.cipher.astrolib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.services.SearchService;

@Controller
public class ObjectController {
    private final SearchService searchService;

    public ObjectController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/objects/{id}")
    public String showObject(@PathVariable Long id, Model model) {
        AstroObjects object = searchService.getObjectById(id);
        if (object == null) {
            return "redirect:/search/name";
        }

        model.addAttribute("object", object);
        model.addAttribute("staticCharacteristics", searchService.getStaticCharacteristics(id));
        model.addAttribute("movingCharacteristics", searchService.getMovingCharacteristics(id));
        model.addAttribute("isStaticType",
            object.getType() == AstroObjects.ObjType.STAR
                || object.getType() == AstroObjects.ObjType.NEBULA
                || object.getType() == AstroObjects.ObjType.GALAXY
        );
        return "objects/detail";
    }
}
