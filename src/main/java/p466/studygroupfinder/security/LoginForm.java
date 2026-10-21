package p466.studygroupfinder.security;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LoginForm {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}