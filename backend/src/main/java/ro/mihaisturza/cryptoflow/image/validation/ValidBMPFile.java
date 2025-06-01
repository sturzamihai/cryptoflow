package ro.mihaisturza.cryptoflow.image.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BMPValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBMPFile {
    String message() default "Invalid BMP file";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    long maxSize() default 100 * 1024 * 1024; // 100MB default
}