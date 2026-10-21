package p466.studygroupfinder.data;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import p466.studygroupfinder.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // find username
    Optional<Student> findByUsername(String username);
    // check if username exists
    boolean existsByUsername(String username);
}