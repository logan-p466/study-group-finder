package p466.studygroupfinder.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import p466.studygroupfinder.model.StudyGroup;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

	List<StudyGroup> findByCourse(String course);

}
