
package yunhan.supplement.Service;

import org.asynchttpclient.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class TwinwordService {

    private static final String API_URL = "https://twinword-emotion-analysis-v1.p.rapidapi.com/analyze/";
    private static final String API_KEY = "28c0d04b05msh2230cf1fa9e6d16p1037a7jsn3bec65449056";
    private static final String API_HOST = "twinword-emotion-analysis-v1.p.rapidapi.com";

    public CompletableFuture<String> analyzeEmotion(String text) {
        AsyncHttpClient client = new DefaultAsyncHttpClient();

        return client.prepare("POST", API_URL)
                .setHeader("x-rapidapi-key", API_KEY)
                .setHeader("x-rapidapi-host", API_HOST)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .addFormParam("text", text)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    int statusCode = response.getStatusCode();
                    String responseBody = response.getResponseBody();

//                    // ✅ 디버깅: 응답 코드 & 응답 내용 출력
//                    System.out.println("🔥 API 응답 코드: " + statusCode);
//                    System.out.println("🔥 API 응답 내용: " + responseBody);

                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return statusCode == 200 ? responseBody : "Error: " + responseBody;
                });
    }
}



//@Service
//public class TwinwordService {
//
//    private static final String API_URL = "https://twinword-emotion-analysis-v1.p.rapidapi.com/analyze/";
//    private static final String API_KEY = "28c0d04b05msh2230cf1fa9e6d16p1037a7jsn3bec65449056";  // 🔥 여기에 API 키 입력
//    private static final String API_HOST = "twinword-emotion-analysis-v1.p.rapidapi.com";
//
//    public CompletableFuture<String> analyzeEmotion(String text) {
//        AsyncHttpClient client = new DefaultAsyncHttpClient();
//
//        return client.prepare("POST", API_URL)
//                .setHeader("x-rapidapi-key", API_KEY)
//                .setHeader("x-rapidapi-host", API_HOST)
//                .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                .setBody("text=" + text)
//                .execute()
//                .toCompletableFuture()
//                .thenApply(response -> {
//                    String responseBody = response.getResponseBody();
//                    try {
//                        client.close(); // 🔥 예외 발생 가능 -> try-catch 추가!
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return responseBody;
//                });
//    }
//}

