
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.storage.*;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class FirebaseStorageService {
//
//    private Storage storage;
//    private String bucketName;
//
//    public FirebaseStorageService() throws IOException {
//        System.out.println("🔍 Firebase 서비스 계정 키 로드 중...");
//
//        try {
//            // ✅ Firebase 서비스 계정 JSON 파일 로드
//            ClassPathResource resource = new ClassPathResource("firebase/firebase-service-account.json");
//            if (!resource.exists()) {
//                throw new IOException("❌ Firebase 서비스 계정 키 파일이 존재하지 않습니다.");
//            }
//
//            InputStream serviceAccount = resource.getInputStream();
//            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
//                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
//
//            // ✅ Firebase Storage 초기화
//            storage = StorageOptions.newBuilder()
//                    .setCredentials(credentials)
//                    .build()
//                    .getService();
//
//            // ✅ 프로젝트 ID 기반 버킷 이름 자동 설정
//            String projectId = storage.getOptions().getProjectId();
//            if (projectId == null) {
//                throw new IOException("❌ Firebase 프로젝트 ID를 찾을 수 없습니다.");
//            }
//
//            bucketName = projectId + ".appspot.com";
//
//            System.out.println("✅ Firebase Storage 초기화 완료: " + bucketName);
//
//        } catch (Exception e) {
//            throw new IOException("❌ Firebase Storage 초기화 실패: " + e.getMessage(), e);
//        }
//    }
//
//    public String uploadImage(MultipartFile file) throws IOException {
//        System.out.println("🔍 Firebase Storage로 이미지 업로드 시작...");
//
//        if (file == null || file.isEmpty()) {
//            throw new IOException("❌ 업로드할 파일이 존재하지 않습니다.");
//        }
//
//        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//        Bucket bucket = storage.get(bucketName);
//
//        if (bucket == null) {
//            throw new IOException("❌ Firebase Storage 버킷이 초기화되지 않았습니다.");
//        }
//
//        Blob blob = bucket.create(fileName, file.getInputStream(), file.getContentType());
//
//        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;
//        System.out.println("✅ Firebase Storage 업로드 완료: " + fileUrl);
//        return fileUrl;
//    }
//}



package yunhan.supplement.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class FirebaseStorageService {

    private final Storage storage;
//    private final String bucketName = "supplement-33176"; // Firebase Storage 버킷 이름
    private final String bucketName = "supplement-33176.firebasestorage.app";

    public FirebaseStorageService() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/firebase/firebase-service-account.json");
        this.storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Bucket bucket = storage.get(bucketName);
        Blob blob = bucket.create(fileName, file.getInputStream(), file.getContentType());

//        return "https://storage.googleapis.com/" + bucketName + "/" + fileName; // 업로드된 이미지의 URL 반환
        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "?alt=media";

    }
}
