package p466.studygroupfinder.web;

//import controller
import org.springframework.stereotype.Controller;
// import getmapping
import org.springframework.web.bind.annotation.GetMapping;

// lets component scanning register class as web handler
@Controller
public class HomeController {

    // handles root GET request
    @GetMapping("/")
    public String home() {
        // send http redirect so browser performs
        // a new GET to the list endpoint
        // temporary for sprint 1 to prevent extra home page work
        return "redirect:/groups";
    }
}
