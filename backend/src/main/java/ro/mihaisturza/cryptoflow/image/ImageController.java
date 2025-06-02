package ro.mihaisturza.cryptoflow.image;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ro.mihaisturza.cryptoflow.consumer.ProcessedImage;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "*")
@Profile("backend")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @PostMapping("/encrypt")
    public ResponseEntity<ImageProcessingResponse> encryptImage(@Valid ImageProcessingRequest request) {
        return processImage(request, Operation.ENCRYPT);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<ImageProcessingResponse> decryptImage(@Valid ImageProcessingRequest request) {
        return processImage(request, Operation.DECRYPT);
    }

    private ResponseEntity<ImageProcessingResponse> processImage(ImageProcessingRequest request, Operation operation) {
        UUID id = imageService.processImage(request.getFile(), request.getKey(), operation,
                request.getEncryptionMode());
        return ResponseEntity.ok(new ImageProcessingResponse(id));

    }

    @GetMapping("/processed")
    public ResponseEntity<List<ProcessedImage>> getProcessedImages() {
        List<ProcessedImage> processedImages = imageService.getProcessedImages();
        return ResponseEntity.ok(processedImages);
    }
}
