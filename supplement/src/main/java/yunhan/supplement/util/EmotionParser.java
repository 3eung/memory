
package yunhan.supplement.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class EmotionParser {

    public static List<String> extractKeywords(String json) {
        List<String> emotions = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode detectedEmotions = root.get("emotions_detected");

            if (detectedEmotions != null && detectedEmotions.isArray()) {
                for (JsonNode node : detectedEmotions) {
                    emotions.add(node.asText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emotions;
    }
}

