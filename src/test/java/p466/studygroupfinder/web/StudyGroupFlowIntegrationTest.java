package p466.studygroupfinder.web;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import p466.studygroupfinder.data.*;
import p466.studygroupfinder.model.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyGroupFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired StudentRepository students;
    @Autowired StudyGroupRepository groups;
    @Autowired MembershipRepository memberships;
    @Autowired PasswordEncoder passwordEncoder;
    private Student student(String username) {
        Student student = new Student();
        student.setUsername(username);
        student.setPasswordHash(passwordEncoder.encode("password123"));
        return students.saveAndFlush(student);
    }
    private MockHttpSession session(Student student) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(AuthController.SESSION_STUDENT_ID, student.getId());
        return s;
    }
    private StudyGroup group(int capacity) {
        StudyGroup group = new StudyGroup();
        group.setCourse("CSCI-P 466");
        group.setInstructor("Bahramian");
        group.setTitle("Review group");
        group.setCreatedBy("instructor");
        group.setMaxCapacity(capacity);
        return groups.saveAndFlush(group);
    }
    @Test void anonymousBrowsingStillWorks() throws Exception {
        mvc.perform(get("/groups")).andExpect(status().isOk());
    }
    @Test void courseSearchStillWorks() throws Exception {
        group(3);
        mvc.perform(get("/groups").param("course", "CSCI-P 466"))
                .andExpect(status().isOk()).andExpect(model().attributeExists("groups"));
    }
    @Test void registrationStoresAHash() throws Exception {
        mvc.perform(post("/register").param("username", "NewStudent")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(redirectedUrl("/login?registered"));
        Student created = students.findByUsername("newstudent").orElseThrow();
        assertThat(created.getPasswordHash()).doesNotContain("password123");
        assertThat(passwordEncoder.matches("password123", created.getPasswordHash())).isTrue();
    }
    @Test void registrationRejectsDuplicateUsername() throws Exception {
        student("alex");
        mvc.perform(post("/register").param("username", "ALEX")
                        .param("password", "password123").param("confirmPassword", "password123"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
    }
    @Test void registrationRequiresMatchingPasswords() throws Exception {
        mvc.perform(post("/register").param("username", "alex")
                        .param("password", "password123").param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "confirmPassword"));
        assertThat(students.findByUsername("alex")).isEmpty();
    }
    @Test void registrationRejectsShortPassword() throws Exception {
        mvc.perform(post("/register").param("username", "alex")
                        .param("password", "short").param("confirmPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "password"));
    }
    @Test void loginAuthenticatesExistingStudent() throws Exception {
        student("alex");
        mvc.perform(post("/login").param("username", "alex")
                        .param("password", "password123"))
                .andExpect(redirectedUrl("/groups"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_STUDENT_ID,
                        org.hamcrest.Matchers.notNullValue()));
    }
    @Test void badPasswordRejected() throws Exception {
        student("alex");
        mvc.perform(post("/login").param("username", "alex")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("loginForm", "password"));
    }
    @Test void anonymousCannotCreateOrJoin() throws Exception {
        StudyGroup group = group(2);
        mvc.perform(get("/groups/new")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/groups/" + group.getId() + "/join"))
                .andExpect(redirectedUrl("/login"));
        assertThat(memberships.countByStudyGroup_Id(group.getId())).isZero();
    }
    @Test void authenticatedCreateSavesCreator() throws Exception {
        Student creator = student("alex");
        mvc.perform(post("/groups").session(session(creator))
                        .param("course", "CSCI-P 466").param("instructor", "Bahramian")
                        .param("title", "New group").param("maxCapacity", "3")
                        .param("createdBy", "spoofed"))
                .andExpect(redirectedUrl("/groups"));
        assertThat(groups.findByCourse("CSCI-P 466")).hasSize(1);
        assertThat(groups.findByCourse("CSCI-P 466").get(0).getCreatedBy()).isEqualTo("alex");
    }
    @Test void joinPersistsOneMembership() throws Exception {
        Student student = student("alex");
        StudyGroup group = group(2);
        mvc.perform(post("/groups/" + group.getId() + "/join").session(session(student)))
                .andExpect(redirectedUrl("/groups"));
        assertThat(memberships.existsByStudent_IdAndStudyGroup_Id(student.getId(),group.getId())).isTrue();
    }
    @Test void duplicateJoinRejected() throws Exception {
        Student student = student("alex");
        StudyGroup group = group(2);
        mvc.perform(post("/groups/" + group.getId() + "/join").session(session(student)));
        mvc.perform(post("/groups/" + group.getId() + "/join").session(session(student)));
        assertThat(memberships.countByStudyGroup_Id(group.getId())).isEqualTo(1);
    }
    @Test void fullGroupRejectsAdditionalStudent() throws Exception {
        StudyGroup group = group(2);
        for (String name : new String[]{"alex", "beth", "chris"}) {
            mvc.perform(post("/groups/" + group.getId() + "/join")
                    .session(session(student(name))));
        }
        assertThat(memberships.countByStudyGroup_Id(group.getId())).isEqualTo(2);
    }
}
