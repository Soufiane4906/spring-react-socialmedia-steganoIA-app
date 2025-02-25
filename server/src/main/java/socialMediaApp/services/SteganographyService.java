package socialMediaApp.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import socialMediaApp.models.User;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.utils.SteganographyUtil;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public class SteganographyService {

    private final UserRepository userRepository;
    private final SecretKey secretKey; // Store a key for encryption

    public SteganographyService(UserRepository userRepository) throws Exception {
        this.userRepository = userRepository;
        this.secretKey = SteganographyUtil.generateKey(); // Generate a key once
    }

    /**
     * Applies steganography by hiding user details securely in an image.
     */
    public byte[] applySteganography(byte[] imageData) throws Exception {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("User is not authenticated.");
        }

        String dataToHide = currentUser.getId() + "|" + currentUser.getEmail();
        return SteganographyUtil.hideData(imageData, dataToHide, secretKey);
    }

    /**
     * Checks if an image already contains hidden data.
     */
    public boolean isImageAlreadySteganographed(byte[] imageData) throws Exception {
     return SteganographyUtil.isImageSteganographed(imageData, secretKey);
    }

    /**
     * Gets the authenticated user via Spring Security.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
        return user.orElse(null);
    }
}
