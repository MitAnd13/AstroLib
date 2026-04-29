package ru.msu.cmc.cipher.astrolib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.msu.cmc.cipher.astrolib.forms.NameSearchForm;
import ru.msu.cmc.cipher.astrolib.forms.ObjectFilterForm;
import ru.msu.cmc.cipher.astrolib.services.SearchService;

@Controller
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/name")
    public String searchByName(@ModelAttribute("searchForm") NameSearchForm searchForm, Model model) {
        boolean hasQuery = searchForm.getQuery() != null && !searchForm.getQuery().isBlank();
        model.addAttribute("hasQuery", hasQuery);
        model.addAttribute("objectResults", hasQuery ? searchService.searchObjectsByName(searchForm.getQuery()) : java.util.List.of());
        model.addAttribute("eventResults", hasQuery ? searchService.searchEventsByName(searchForm.getQuery()) : java.util.List.of());
        return "search/name";
    }

    @GetMapping("/objects")
    public String searchObjects(@ModelAttribute("filterForm") ObjectFilterForm filterForm, Model model) {
        boolean hasType = filterForm.getObjectKind() != null && !filterForm.getObjectKind().isBlank();
        model.addAttribute("hasType", hasType);
        model.addAttribute("objectResults", hasType ? searchService.searchObjectsByFilters(filterForm) : java.util.List.of());
        return "search/objects";
    }
}
