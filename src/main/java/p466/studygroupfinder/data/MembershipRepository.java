package p466.studygroupfinder.data;

import org.springframework.data.jpa.repository.JpaRepository;
import p466.studygroupfinder.model.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    // get count of study group
    long countByStudyGroup_Id(Long groupId);
    // check if student is member of group
    boolean existsByStudent_IdAndStudyGroup_Id(Long studentId, Long groupId);
}