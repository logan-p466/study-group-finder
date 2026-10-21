package p466.studygroupfinder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
// block duplicate membership rows
@Table(name = "group_membership", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "study_group_id"})
})
public class Membership {
    // id primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // links membership to student id
    // join
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // links membership to study group id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;
}
