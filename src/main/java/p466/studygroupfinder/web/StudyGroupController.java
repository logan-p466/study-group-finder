package p466.studygroupfinder.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.model.StudyGroup;

@Controller
@RequestMapping("/groups")
public class StudyGroupController {

	private final StudyGroupRepository groupRepo;

	public StudyGroupController(StudyGroupRepository groupRepo) {
		this.groupRepo = groupRepo;
	}

	@GetMapping
	public String listGroups(@RequestParam(name = "course", required = false) String course, Model model) {
		if (course != null && !course.isBlank()) {
			model.addAttribute("groups", groupRepo.findByCourse(course.trim()));
			model.addAttribute("course", course.trim());
		} else {
			model.addAttribute("groups", groupRepo.findAll());
		}
		return "groups/list";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("studyGroup", new StudyGroup());
		return "groups/create";
	}

	@PostMapping
	public String processGroup(@Valid @ModelAttribute("studyGroup") StudyGroup studyGroup, Errors errors) {
		if (errors.hasErrors()) {
			return "groups/create";
		}
		groupRepo.save(studyGroup);
		return "redirect:/groups";
	}

}
