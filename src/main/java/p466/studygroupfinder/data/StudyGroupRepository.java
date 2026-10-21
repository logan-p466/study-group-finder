package p466.studygroupfinder.data;
// import java list
import java.util.List;
// import jpa repository
import org.springframework.data.jpa.repository.JpaRepository;
// import studygroup domain model
import p466.studygroupfinder.model.StudyGroup;

// jparepository supplies findall, findbyid, save, deletion, and flush behavior
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    // spring data parses findbycourse and generates a query that matches the 'course' property exactly
    List<StudyGroup> findByCourse(String course);
}
