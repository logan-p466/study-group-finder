package p466.studygroupfinder.web;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import p466.studygroupfinder.data.StudentRepository;
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StudentRepository studentRepo;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Test
    public void testRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationForm"));
    }
    @Test
    public void testRegisterWithInvalidUsername() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "ab")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
        verify(studentRepo, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
    @Test
    public void testRegisterWithMismatchedPasswords() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "alex")
                        .param("password", "password123")
                        .param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "confirmPassword"));
        verify(studentRepo, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
    @Test
    public void testLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginForm"));
    }
    @Test
    public void testLoginWithBlankPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "alex")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "password"));
    }
}
