
package p466.studygroupfinder.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.data.StudentRepository;
import p466.studygroupfinder.data.MembershipRepository;
import p466.studygroupfinder.model.Student;
import p466.studygroupfinder.model.StudyGroup;

@WebMvcTest(StudyGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyGroupRepository studyGroupRepo;

    @MockBean
    private StudentRepository studentRepo;

    @MockBean
    private MembershipRepository membershipRepo;

    private void mockSignedInStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setUsername("student");
        student.setRole("STUDENT");

        when(studentRepo.findByUsername("student"))
                .thenReturn(Optional.of(student));
    }

    @Test
    public void testListGroups() throws Exception {
        when(studyGroupRepo.findAll())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/list"))
                .andExpect(model().attributeExists("groups"));

        when(studyGroupRepo.findByCourse("CSCI-P 466"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/groups")
                        .param("course", "CSCI-P 466"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/list"))
                .andExpect(model().attributeExists("groups"));

        verify(studyGroupRepo).findByCourse("CSCI-P 466");
    }

    @Test
    public void testAnonymousCreateRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/groups/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    public void testCreateForm() throws Exception {
        mockSignedInStudent();

        mockMvc.perform(get("/groups/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/create"))
                .andExpect(model().attributeExists("studyGroup"));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    public void testCreateGroupValid() throws Exception {
        mockSignedInStudent();

        mockMvc.perform(post("/groups")
                        .param("course", "CSCI-P 466")
                        .param("instructor", "Professor Example")
                        .param("title", "Software Engineering Study Group")
                        .param("description", "Review Sprint 1 topics")
                        .param("createdBy", "Logan Reddell")
                        .param("maxCapacity", "10")
                        .param("meetingTime", "Tuesday 6:00 PM")
                        .param("location", "Luddy Hall"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups"));

        verify(studyGroupRepo).save(any(StudyGroup.class));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    public void testCreateGroupBlankTitle() throws Exception {
        mockSignedInStudent();

        mockMvc.perform(post("/groups")
                        .param("course", "CSCI-P 466")
                        .param("instructor", "Professor Example")
                        .param("title", "")
                        .param("description", "Review Sprint 1 topics")
                        .param("createdBy", "Logan Reddell")
                        .param("maxCapacity", "10")
                        .param("meetingTime", "Tuesday 6:00 PM")
                        .param("location", "Luddy Hall"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "studyGroup", "title"));

        verify(studyGroupRepo, never())
                .save(any(StudyGroup.class));
    }
}
