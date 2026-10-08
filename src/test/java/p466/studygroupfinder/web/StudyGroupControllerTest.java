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

import java.util.List;

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
	private StudyGroupRepository groupRepo;

	@Test
	public void testListGroups() throws Exception {
		when(groupRepo.findAll()).thenReturn(List.of(
				new StudyGroup(1L, "CSCI-P 466", "Bahramian", "Exam Prep", "Weekly review", "Logan",
						"Tue/Thu 6 PM", "Luddy Hall", 10)));

		mockMvc.perform(get("/groups"))
				.andExpect(status().isOk())
				.andExpect(view().name("groups/list"))
				.andExpect(model().attributeExists("groups"));
	}

	@Test
	public void testCreateGroupValid() throws Exception {
		mockMvc.perform(post("/groups")
				.param("course", "CSCI-P 466")
				.param("instructor", "Bahramian")
				.param("title", "Exam Prep")
				.param("description", "Weekly review")
				.param("createdBy", "Logan")
				.param("meetingTime", "Tue/Thu 6 PM")
				.param("location", "Luddy Hall")
				.param("maxCapacity", "10"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/groups"));

		verify(groupRepo).save(any(StudyGroup.class));
	}

	@Test
	public void testCreateGroupBlankTitle() throws Exception {
		mockMvc.perform(post("/groups")
				.param("course", "CSCI-P 466")
				.param("instructor", "Bahramian")
				.param("title", "")
				.param("description", "Weekly review")
				.param("createdBy", "Logan")
				.param("maxCapacity", "10"))
				.andExpect(status().isOk())
				.andExpect(view().name("groups/create"))
				.andExpect(model().hasErrors());

		verify(groupRepo, never()).save(any(StudyGroup.class));
	}

	@Test
	public void testSearchByCourse() throws Exception {
		when(groupRepo.findByCourse("CSCI-P 466")).thenReturn(List.of(
				new StudyGroup(1L, "CSCI-P 466", "Bahramian", "Exam Prep", "Weekly review", "Logan",
						"Tue/Thu 6 PM", "Luddy Hall", 10)));

		mockMvc.perform(get("/groups").param("course", "CSCI-P 466"))
				.andExpect(status().isOk())
				.andExpect(view().name("groups/list"))
				.andExpect(model().attributeExists("groups"));

		verify(groupRepo).findByCourse("CSCI-P 466");
	}

}
