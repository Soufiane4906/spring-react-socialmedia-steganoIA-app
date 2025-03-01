package socialMediaApp.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import socialMediaApp.mappers.PostImageMapper;
import socialMediaApp.models.PostImage;
import socialMediaApp.repositories.PostImageRepository;
import socialMediaApp.responses.postImage.PostImageResponse;
import socialMediaApp.utils.ImageUtil;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class PostImageService {

    private final PostImageRepository postImageRepository;
    private final PostService postService;
    private final PostImageMapper postImageMapper;
    private final RestTemplate restTemplate;

    private static final String FLASK_API_URL = "http://localhost:5000/upload"; // Adjust Flask API URL

    public PostImageService(PostImageRepository postImageRepository, PostService postService,
                            PostImageMapper postImageMapper, RestTemplate restTemplate) {
        this.postImageRepository = postImageRepository;
        this.postService = postService;
        this.postImageMapper = postImageMapper;
        this.restTemplate = restTemplate;
    }

    public PostImageResponse upload(MultipartFile file, int postId) throws IOException {
        // 🔹 Call Flask API for image validation
        Map<String, Object> validationResponse = validateImageWithFlask(file);

        if (validationResponse.containsKey("error")) {
            throw new IllegalArgumentException(validationResponse.get("error").toString());
        }

        // ✅ If valid, proceed with saving the image
        PostImage postImage = new PostImage();
        postImage.setName(file.getOriginalFilename());
        postImage.setType(file.getContentType());
        postImage.setData(ImageUtil.compressImage(file.getBytes()));
        postImage.setPost(postService.getById(postId));
        postImageRepository.save(postImage);

        return postImageMapper.imageToResponse(postImage);
    }

    /**
     * Calls the Flask API to check for steganography & AI-generated detection.
     * Returns a structured response.
     */
    private Map<String, Object> validateImageWithFlask(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Convert MultipartFile to HTTP request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 🔹 Call Flask API
        ResponseEntity<Map> response = restTemplate.exchange(FLASK_API_URL, HttpMethod.POST, requestEntity, Map.class);

        // 🔹 Extract response
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return Map.of("error", "Flask API error: No response received.");
        }

        Map<String, Object> result = response.getBody();
        Map<String, Object> steganography = (Map<String, Object>) result.get("steganography");
        Map<String, Object> aiDetection = (Map<String, Object>) result.get("ai_detection");

        boolean containsSteganography = steganography != null && steganography.containsKey("signature_detected")
                && Boolean.TRUE.equals(steganography.get("signature_detected"));

        boolean isAIGenerated = aiDetection != null && aiDetection.containsKey("is_ai_generated")
                && Boolean.TRUE.equals(aiDetection.get("is_ai_generated"));

        // ❌ If AI or steganography detected, return error message
        if (containsSteganography) {
            return Map.of("error", "Image contains hidden steganography data.");
        }

        if (isAIGenerated) {
            return Map.of("error", "AI-generated images are not allowed.");
        }

        // ✅ If image passes validation
        return Map.of("valid", true);
    }

    public byte[] download(int id) {
        Optional<PostImage> postImage = postImageRepository.findPostImageByPost_Id(id);
        return postImage.map(image -> ImageUtil.decompressImage(image.getData())).orElse(null);
    }
}
