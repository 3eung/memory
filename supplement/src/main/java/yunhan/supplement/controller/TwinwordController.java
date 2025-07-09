package yunhan.supplement.controller;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//import yunhan.supplement.Service.TwinwordService;
//
//import java.util.concurrent.CompletableFuture;
//
//@RestController
//@RequestMapping("/twinword")
//@CrossOrigin("*")
//public class TwinwordController {
//
//    private final TwinwordService twinwordService;
//
//    public TwinwordController(TwinwordService twinwordService) {
//        this.twinwordService = twinwordService;
//    }
//
//    @PostMapping("/analyze")
//    public CompletableFuture<String> analyzeEmotion(@RequestParam String text) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("🔥 현재 인증 사용자: " + (auth != null ? auth.getName() : "익명 사용자"));
//
//        return twinwordService.analyzeEmotion(text);
//    }
//}




import org.springframework.web.bind.annotation.*;
import yunhan.supplement.Service.TwinwordService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/twinword")
@CrossOrigin("*")
public class TwinwordController {

    private final TwinwordService twinwordService;

    public TwinwordController(TwinwordService twinwordService) {
        this.twinwordService = twinwordService;
    }

    @PostMapping("/analyze")
    public CompletableFuture<String> analyzeEmotion(@RequestParam String text) {
        return twinwordService.analyzeEmotion(text);
    }
}
