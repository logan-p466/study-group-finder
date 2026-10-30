package p466.studygroupfinder.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import p466.studygroupfinder.model.StudyGroup;

@DataJpaTest
public class StudyGroupRepositoryTest {

    @Autowired
    private StudyGroupRepository studyGroupRepo;

    @Test
    public void shouldSaveAndFindById() {
        StudyGroup group = createGroup(
                "CSCI-P 466",
                "Software Engineering Study Group");

        StudyGroup saved = studyGroupRepo.save(group);
        StudyGroup found = studyGroupRepo.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle())
                .isEqualTo("Software Engineering Study Group");
    }

    @Test
    public void shouldFindByCourse() {
        studyGroupRepo.save(createGroup(
                "CSCI-P 466",
                "Software Engineering Study Group"));

        studyGroupRepo.save(createGroup(
                "CSCI-C 212",
                "Data Structures Study Group"));

        List<StudyGroup> results =
                studyGroupRepo.findByCourse("CSCI-P 466");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCourse()).isEqualTo("CSCI-P 466");
    }

    private StudyGroup createGroup(String course, String title) {
        StudyGroup group = new StudyGroup();
        group.setCourse(course);
        group.setInstructor("Professor Example");
        group.setTitle(title);
        group.setDescription("Weekly study session");
        group.setCreatedBy("Logan Reddell");
        group.setMaxCapacity(10);
        group.setMeetingTime("Tuesday 6:00 PM");
        group.setLocation("Luddy Hall");

        return group;
    }
}