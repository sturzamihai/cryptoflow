package ro.mihaisturza.cryptoflow.image;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "*")
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
}
