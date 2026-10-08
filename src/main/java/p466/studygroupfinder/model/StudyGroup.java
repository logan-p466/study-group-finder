package p466.studygroupfinder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StudyGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Course is required")
	private String course;

	@NotBlank(message = "Instructor is required")
	private String instructor;

	@NotBlank(message = "Title is required")
	private String title;

	private String description;

	@NotBlank(message = "Created by is required")
	private String createdBy;

	private String meetingTime;

	private String location;

	@Min(value = 2, message = "Capacity must be at least 2")
	@Max(value = 100, message = "Capacity must be at most 100")
	private int maxCapacity;

}
