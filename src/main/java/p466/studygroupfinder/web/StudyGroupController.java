package p466.studygroupfinder.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
// import StudyGroupRepository (data source)
import p466.studygroupfinder.data.StudyGroupRepository;
// import Domain model StudyGroup
import p466.studygroupfinder.model.StudyGroup;

@Controller
@RequestMapping("/groups")
public class StudyGroupController {
    // constructor injection tells spring controller needs a studygrouprepository bean
    // spring supplies generated implementation
    private final StudyGroupRepository studyGroupRepo;
    public StudyGroupController(StudyGroupRepository studyGroupRepo) {
        this.studyGroupRepo = studyGroupRepo;
    }

    // GET /groups calls findAll when no course is supplied
    // and findByCourse when a nonblank exact course is supplied
    @GetMapping
    public String listGroups(
            @RequestParam(required = false) String course,
            Model model) {
        List<StudyGroup> groups;

        if (course != null && !course.isBlank()) {
            groups = studyGroupRepo.findByCourse(course);
        } else {
            groups = studyGroupRepo.findAll();
        }

        model.addAttribute("groups", groups);
        model.addAttribute("course", course == null ? "" : course);

        return "groups/list";
    }

    // GET /groups/new places a new StudyGroup in the model under the default name studyGroup
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("studyGroup", new StudyGroup());
        return "groups/create";
    }

    // POST /groups binds request fields to a StudyGroup
    // @Valid runs entity constraints and Errors reports any failures
    @PostMapping
    public String createGroup(@Valid StudyGroup studyGroup, Errors errors) {
        if (errors.hasErrors()) {
            return "groups/create";
        }

        studyGroupRepo.save(studyGroup);
        return "redirect:/groups";
    }
}
