package p466.studygroupfinder.web;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.model.StudyGroup;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import p466.studygroupfinder.data.StudentRepository;
import p466.studygroupfinder.data.MembershipRepository;
import p466.studygroupfinder.model.Student;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired StudyGroupRepository groups;
    @Autowired StudentRepository students;
    @Autowired MembershipRepository memberships;

    private StudyGroup sampleGroup() {
        StudyGroup group = new StudyGroup();
        group.setTitle("Sample group");
        group.setCourse("CSCI-P 466");
        group.setInstructor("Example");
        group.setMaxCapacity(2);
        group.setStatus("ACTIVE");
        return groups.saveAndFlush(group);
    }
    @Test
    void publicListIsAvailable() throws Exception {
        mvc.perform(get("/groups")).andExpect(status().isOk());
    }
    @Test
    void studentCannotAccessAdminPage() throws Exception {
        mvc.perform(get("/admin/groups")
                        .with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }
    @Test
    void adminCanSuspendGroup() throws Exception {
        StudyGroup group = sampleGroup();
        mvc.perform(post("/admin/groups/" + group.getId() + "/suspend")
                        .with(user("moderator").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(groups.findById(group.getId()).orElseThrow()
                .getStatus()).isEqualTo("SUSPENDED");
    }
    @Test
    void studentCannotSuspendGroup() throws Exception {
        StudyGroup group = sampleGroup();
        mvc.perform(post("/admin/groups/" + group.getId() + "/suspend")
                        .with(user("student").roles("STUDENT"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(groups.findById(group.getId()).orElseThrow()
                .getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void suspendedGroupHiddenFromBrowseAndSearch() throws Exception {
        StudyGroup group = sampleGroup();
        group.setStatus("SUSPENDED");
        groups.saveAndFlush(group);
        mvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sample group"))));
        mvc.perform(get("/groups").param("course", "CSCI-P 466"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sample group"))));
    }
    @Test
    void suspendedGroupRejectsDirectJoin() throws Exception {
        Student student = new Student();
        student.setUsername("testmember");
        student.setPasswordHash("dummy-for-authenticated-mock-request");
        student.setRole("STUDENT");
        student = students.saveAndFlush(student);
        StudyGroup group = sampleGroup();
        group.setStatus("SUSPENDED");
        groups.saveAndFlush(group);
        mvc.perform(post("/groups/" + group.getId() + "/join")
                        .with(user("testmember").roles("STUDENT"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(memberships.countByStudyGroup_Id(group.getId())).isZero();
    }
}