package p466.studygroupfinder.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// Practicum 4's password encoder, without the Spring Security web starter.
// tells spring this class defines beans
@Configuration
public class SecurityConfig {
    // provides one passwordencoder injected into authcontroller
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}