package p466.studygroupfinder.security;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
// Form data is separate from the Student entity, which stores the password hash.
@Data
public class RegistrationForm {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 40, message = "Username must be 3-40 characters")
    @Pattern(regexp = "(?i)[a-z0-9._-]+",
            message = "Use letters, numbers, dots, dashes, or underscores")
    private String username;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}