package yunhan.supplement.controller;

import org.springframework.web.bind.annotation.*;
import yunhan.supplement.Service.GoogleTranslateService;

@RestController
@RequestMapping("/translate")
public class TranslationController {

    private final GoogleTranslateService translateService;

    public TranslationController(GoogleTranslateService translateService) {
        this.translateService = translateService;
    }

    @GetMapping
    public String translate(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String target) {
        return translateService.translateText(text, target);
    }
}
