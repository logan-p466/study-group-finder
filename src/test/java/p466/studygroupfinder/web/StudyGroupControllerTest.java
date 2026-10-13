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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.model.StudyGroup;

@WebMvcTest(StudyGroupController.class)
public class StudyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyGroupRepository studyGroupRepo;

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
    public void testCreateForm() throws Exception {
        mockMvc.perform(get("/groups/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/create"))
                .andExpect(model().attributeExists("studyGroup"));
    }

    @Test
    public void testCreateGroupValid() throws Exception {
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
    public void testCreateGroupBlankTitle() throws Exception {
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
                .andExpect(model().attributeHasFieldErrors("studyGroup", "title"));

        verify(studyGroupRepo, never()).save(any(StudyGroup.class));
    }
}