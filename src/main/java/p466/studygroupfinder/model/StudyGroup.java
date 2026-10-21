package p466.studygroupfinder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

// generates setters, equals, hashcode, and tostring
@Data
// makes jpa constructor requirement explicit
@NoArgsConstructor
// marks class for jpa persistence
// our domain is not split yet, only 1 JPA entity which is StudyGroup
@Entity
@Table(name = "study_group")
public class StudyGroup {
    // jpa id and genval id the db generated primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // rejects null empty or whitespace only required text
    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Instructor is required")
    private String instructor;

    @NotBlank(message = "Title is required")
    private String title;

    // optional
    private String description;

//    @NotBlank(message = "Created by is required") not used anymore due to login
    private String createdBy;

    // might change later specific of capacity based on further meetings
    // enforce capacity
    @NotNull(message = "Maximum capacity is required")
    @Min(value = 2, message = "Maximum capacity must be at least 2")
    @Max(value = 100, message = "Maximum capacity must be at most 100")
    private int maxCapacity;

    // optional
    private String meetingTime;

    private String location;
}
