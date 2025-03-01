package socialMediaApp.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import socialMediaApp.responses.postImage.PostImageResponse;
import socialMediaApp.services.PostImageService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/postimages")
public class PostImagesController {

    private final PostImageService postImageService;

    public PostImagesController(PostImageService postImageService) {
        this.postImageService = postImageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile file, @RequestParam int postId) {
        try {
            PostImageResponse response = postImageService.upload(file, postId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "File processing error."));
        }
    }


    @GetMapping("/download/{postId}")
    public ResponseEntity<?> download(@PathVariable int postId){
        byte[] image = postImageService.download(postId);
        if (image!=null){
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf("image/png")).body(image);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    }
}
