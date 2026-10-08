# Domain Model — Study Group Finder (Sprint 1)

## Entity: StudyGroup

The only domain object in Sprint 1. Represents one study group created by a student for a course.

| Field | Type | Constraints | Meaning |
|---|---|---|---|
| id | Long | @Id, @GeneratedValue(IDENTITY) | Surrogate primary key |
| course | String | @NotBlank | Course the group is for, e.g. "CSCI-P 466" |
| instructor | String | @NotBlank | Instructor of that course section (a group is for a specific course AND instructor) |
| title | String | @NotBlank | Short name of the group |
| description | String | optional | What the group meets about |
| createdBy | String | @NotBlank | Name of the student who created the group |
| meetingTime | String | optional | When the group meets, free text, e.g. "Tue/Thu 6 PM" |
| location | String | optional | Where the group meets, free text |
| maxCapacity | int | @Min(2), @Max(100) | Maximum number of members the group accepts |

## Modelling decision

In Sprint 1, `maxCapacity` is a plain field on StudyGroup rather than a derived value from a membership collection. Joining/membership is deliberately deferred: there is no Member entity and no join relationship yet, so capacity is data the creator declares, not a count the system enforces. The same applies to meeting details: `meetingTime` and `location` are plain free-text fields set at creation (covering the "specify when and where my group meets" story at creation time), not a scheduling model with recurrence. This keeps the Sprint 1 model to a single entity while the slice proves the more fundamental risk first — create + browse + search working end to end (form → validation → controller → JPA repository → database → list view) with data that persists across restarts. When the Join story is built in a later sprint, membership becomes a real relationship and capacity enforcement can be derived from it.

## Deferred to later sprints

- Join a study group (membership)
- Recurring meetings and meeting scheduling (beyond the free-text meeting time/location)
