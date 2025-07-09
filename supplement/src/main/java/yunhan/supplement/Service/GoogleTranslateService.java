package yunhan.supplement.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleTranslateService {

    @Value("${google.cloud.translate.api-key}")
    private String apiKey;

    private final String URL_TEMPLATE = "https://translation.googleapis.com/language/translate/v2?key=%s&q=%s&target=%s";

    public String translateText(String text, String targetLanguage) {
        String url = String.format(URL_TEMPLATE, apiKey, text, targetLanguage);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
}
