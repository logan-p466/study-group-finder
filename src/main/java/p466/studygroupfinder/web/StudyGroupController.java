package p466.studygroupfinder.web;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import p466.studygroupfinder.data.MembershipRepository;
import p466.studygroupfinder.data.StudentRepository;
import p466.studygroupfinder.data.StudyGroupRepository;
import p466.studygroupfinder.model.Membership;
import p466.studygroupfinder.model.Student;
import p466.studygroupfinder.model.StudyGroup;


@Controller
@RequestMapping("/groups")
public class StudyGroupController {
    // constructor injection tells spring controller needs a studygrouprepository bean
    // spring supplies generated implementation
    private final StudyGroupRepository studyGroupRepo;
    // added for sprint 2
    private final StudentRepository studentRepo;
    private final MembershipRepository membershipRepo;


    // updated for sprint 2
    public StudyGroupController(StudyGroupRepository studyGroupRepo,
                                StudentRepository studentRepo,
                                MembershipRepository membershipRepo) {
        this.studyGroupRepo = studyGroupRepo;
        this.studentRepo = studentRepo;
        this.membershipRepo = membershipRepo;
    }

    // Read the ID stored in the HTTP session at login; never use a browser-submitted user ID.
    private Student signedInStudent(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(AuthController.SESSION_STUDENT_ID);
        if (!(id instanceof Long)) {
            return null;
        }
        return studentRepo.findById((Long) id).orElse(null);
    }

    // GET /groups calls findAll when no course is supplied
    // and findByCourse when a nonblank exact course is supplied
    // sprint 2 supplies membership details
    @GetMapping
    public String listGroups(@RequestParam(required = false) String course,
                             Model model, HttpServletRequest request) {
        List<StudyGroup> groups;
        if (course != null && !course.isBlank()) {
            groups = studyGroupRepo.findByCourse(course);
        } else {
            groups = studyGroupRepo.findAll();
        }
        model.addAttribute("groups", groups);
        model.addAttribute("course", course == null ? "" : course);
        Student current = signedInStudent(request);
        Map<Long, Long> memberCounts = new HashMap<>();
        Set<Long> joinedIds = new HashSet<>();
        Set<Long> fullIds = new HashSet<>();
        for (StudyGroup group : groups) {
            long count = membershipRepo.countByStudyGroup_Id(group.getId());
            memberCounts.put(group.getId(), count);
            if (count >= group.getMaxCapacity()) {
                fullIds.add(group.getId());
            }
            if (current != null && membershipRepo.existsByStudent_IdAndStudyGroup_Id(
                    current.getId(), group.getId())) {
                joinedIds.add(group.getId());
            }
        }
        model.addAttribute("memberCounts", memberCounts);
        model.addAttribute("joinedIds", joinedIds);
        model.addAttribute("fullIds", fullIds);
        model.addAttribute("signedIn", current != null);
        model.addAttribute("username", current == null ? "" : current.getUsername());
        return "groups/list";
    }


    // Existing Sprint 1 create form; login is required in Sprint 2.
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpServletRequest request) {
        if (signedInStudent(request) == null) {
            return "redirect:/login";
        }
        model.addAttribute("studyGroup", new StudyGroup());
        return "groups/create";
    }

    // Sprint 1 validation and save behavior remain. The creator is now set on the server.
    @PostMapping
    public String createGroup(@Valid StudyGroup studyGroup, Errors errors,
                              HttpServletRequest request) {
        Student current = signedInStudent(request);
        if (current == null) {
            return "redirect:/login";
        }
        if (errors.hasErrors()) {
            return "groups/create";
        }
        studyGroup.setId(0);
        studyGroup.setCreatedBy(current.getUsername());
        studyGroupRepo.save(studyGroup);
        return "redirect:/groups";
    }

    // Sprint 2: one saved membership per student/group, provided space remains.
    @PostMapping("/{id}/join")
    public String joinGroup(@PathVariable Long id, HttpServletRequest request,
                            RedirectAttributes redirect) {
        Student student = signedInStudent(request);
        if (student == null) {
            return "redirect:/login";
        }
        StudyGroup group = studyGroupRepo.findById(id).orElse(null);
        if (group == null) {
            redirect.addFlashAttribute("message", "Study group not found.");
            return "redirect:/groups";
        }
        if (membershipRepo.existsByStudent_IdAndStudyGroup_Id(student.getId(), id)) {
            redirect.addFlashAttribute("message", "You already joined this group.");
            return "redirect:/groups";
        }
        if (membershipRepo.countByStudyGroup_Id(id) >= group.getMaxCapacity()) {
            redirect.addFlashAttribute("message", "This group is full.");
            return "redirect:/groups";
        }
        Membership membership = new Membership();
        membership.setStudent(student);
        membership.setStudyGroup(group);
        try {
            membershipRepo.saveAndFlush(membership);
            redirect.addFlashAttribute("message", "You joined the group.");
        } catch (DataIntegrityViolationException duplicate) {
            redirect.addFlashAttribute("message", "You already joined this group.");
        }
        return "redirect:/groups";
    }

}
