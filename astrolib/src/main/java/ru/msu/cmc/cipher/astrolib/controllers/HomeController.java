package ru.msu.cmc.cipher.astrolib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("quickActions", List.of(
            Map.of(
                "title", "Поиск по названию",
                "description", "Найти объект или явление по названию, альтернативному имени или идентификатору каталога",
                "icon", "/icons/Zoom.svg",
                "href", "/search/name",
                "accent", "amber"
            ),
            Map.of(
                "title", "Поиск объектов",
                "description", "Найти объекты по фильтрам типов, подтипов и характеристик",
                "icon", "/icons/Planet.svg",
                "href", "/search/objects",
                "accent", "blue"
            ),
            Map.of(
                "title", "Поиск явлений",
                "description", "Найти астрономические явления по фильтрам вида, времени и других характеристик",
                "icon", "/icons/Moon.svg",
                "href", "#",
                "accent", "violet"
            ),
            Map.of(
                "title", "Регистрация открытия",
                "description", "Зарегистрировать новый объект или явление",
                "icon", "/icons/Star.svg",
                "href", "/discoveries/new",
                "accent", "mint"
            )
        ));

        model.addAttribute("stats", List.of(
            Map.of("label", "Объектов обнаружено", "value", "12 480"),
            Map.of("label", "Явлений зарегистрировано", "value", "1 274"),
            Map.of("label", "Созвездий в базе", "value", "88"),
            Map.of("label", "Последний загруженный объект", "value", "Земля")
        ));

        return "index";
    }
}




