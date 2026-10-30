package p466.studygroupfinder.web;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.model.StudyGroup;

// new controller for sprint 3, admin controller
//

@Controller
@RequestMapping("/admin/groups")
public class AdminController {
    private final StudyGroupRepository groups;
    public AdminController(StudyGroupRepository groups) {
        this.groups = groups;
    }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", groups.findAll());
        return "admin/groups";
    }
    @PostMapping("/{id}/suspend")
    public String suspend(@PathVariable Long id) {
        StudyGroup group = groups.findById(id).orElse(null);
        if (group != null) {
            group.setStatus("SUSPENDED");
            groups.saveAndFlush(group);
        }
        return "redirect:/admin/groups";
    }
}