package socialMediaApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIDetectionService {

    private final RestTemplate restTemplate;

    public AIDetectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isImageGeneratedByAI(byte[] imageData) {
        String url = "http://your-flask-or-spring-api/detect-ai"; // URL de l'API de détection d'IA
        // Envoyer l'image à l'API pour vérification
        Boolean isAIGenerated = restTemplate.postForObject(url, imageData, Boolean.class);
        return isAIGenerated != null && isAIGenerated;
    }
}