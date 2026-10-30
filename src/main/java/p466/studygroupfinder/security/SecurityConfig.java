package p466.studygroupfinder.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import p466.studygroupfinder.data.StudentRepository;
import p466.studygroupfinder.model.Student;

// this follows practicum 4's structure
// public browsing/search, registration stay public
// group creation, joining, every admin route requires auth
// /admin/** requires ADMIN
// spring security now handles password verification, login session auth,
// logout, and CSRF protection

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService(StudentRepository students) {
        return username -> {
            Student student = students.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Unknown student"));
            String role = "ADMIN".equals(student.getRole())
                    ? "ADMIN" : "STUDENT";
            return User.withUsername(student.getUsername())
                    .password(student.getPasswordHash())
                    .roles(role)
                    .build();
        };
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/", "/groups", "/register", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/groups", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/groups?loggedOut")
                        .permitAll())
                .build();
    }
}
