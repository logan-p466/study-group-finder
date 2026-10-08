package p466.studygroupfinder.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import p466.studygroupfinder.model.StudyGroup;

@DataJpaTest
public class StudyGroupRepositoryTest {

	@Autowired
	private StudyGroupRepository groupRepo;

	@Test
	public void shouldSaveAndFindById() {
		StudyGroup saved = groupRepo.save(
				new StudyGroup(null, "CSCI-P 466", "Bahramian", "Exam Prep", "Weekly review", "Logan",
						"Tue/Thu 6 PM", "Luddy Hall", 10));

		Optional<StudyGroup> found = groupRepo.findById(saved.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getTitle()).isEqualTo("Exam Prep");
		assertThat(found.get().getCourse()).isEqualTo("CSCI-P 466");
	}

	@Test
	public void shouldFindByCourse() {
		groupRepo.save(new StudyGroup(null, "CSCI-B 405", "Tao", "Algorithms Study", "DP practice", "Logan",
				"Mon 7 PM", "Library", 6));
		groupRepo.save(new StudyGroup(null, "CSCI-P 466", "Bahramian", "Exam Prep", "Weekly review", "Logan",
				"Tue/Thu 6 PM", "Luddy Hall", 10));

		List<StudyGroup> results = groupRepo.findByCourse("CSCI-P 466");

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTitle()).isEqualTo("Exam Prep");
	}

}
