package p466.studygroupfinder.web;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import p466.studygroupfinder.data.StudentRepository;
import p466.studygroupfinder.model.Student;
import p466.studygroupfinder.security.LoginForm;
import p466.studygroupfinder.security.RegistrationForm;
@Controller
public class AuthController {
    public static final String SESSION_STUDENT_ID = "studentId";
    private final StudentRepository studentRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(StudentRepository studentRepo, PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.passwordEncoder = passwordEncoder;
    }
    // create blank registration form stores it under the model attribute registrationForm, and
    // returns the Thymeleaf view auth/register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }
    // register post
    // save and flush WRITES
    //
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           Errors errors) {
        // Compare usernames without case differences when saving or looking them up.
        String username = form.getUsername() == null ? ""
                : form.getUsername().trim().toLowerCase(Locale.ROOT);
        form.setUsername(username);
        if (!errors.hasFieldErrors("username") && studentRepo.existsByUsername(username)) {
            errors.rejectValue("username", "duplicate", "Username is already registered.");
        }
        if (!errors.hasFieldErrors("password") && !errors.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (errors.hasErrors()) {
            return "auth/register";
        }
        Student student = new Student();
        student.setUsername(username);
        student.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        // student role
        student.setRole("STUDENT");
        try {
            studentRepo.saveAndFlush(student);
        } catch (DataIntegrityViolationException duplicate) {
            // Database uniqueness also handles simultaneous registrations.
            errors.rejectValue("username", "duplicate", "Username is already registered.");
            return "auth/register";
        }
        // REDIRECT
        return "redirect:/login?registered";
    }
    // LOGIN
    // creates a blank form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }
    // POST LOGIN
    //
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        Errors errors, HttpServletRequest request) {
        if (errors.hasErrors()) {
            return "auth/login";
        }
        String username = form.getUsername().trim().toLowerCase(Locale.ROOT);
        form.setUsername(username);
        Student student = studentRepo.findByUsername(username).orElse(null);
        // IF INVALID
        if (student == null || !passwordEncoder.matches(form.getPassword(), student.getPasswordHash())) {
            errors.rejectValue("password", "invalid", "Invalid username or password.");
            return "auth/login";
        }
        // CREATE AN HTTP SESSION
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        } else {
            request.changeSessionId();
        }
        session.setAttribute(SESSION_STUDENT_ID, student.getId());
        return "redirect:/groups";
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/groups?loggedOut";
    }
}
