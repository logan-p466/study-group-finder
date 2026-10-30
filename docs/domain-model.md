# Study Group Finder - Sprint 1 Domain Model
## Main domain object

`StudyGroup` is the only persistent domain object in Sprint 1. It represents one study group that a student creates
for one course and instructor.

| Field | Java type | Required in Sprint 1 | Meaning |
|---|---|---:|---|
| `id` | `Long` | Generated | Database identity for the saved group. |
| `course` | `String` | Yes | Exact course text used for display and exact-match search. |
| `instructor` | `String` | Yes | Instructor whose version of the course the group supports. |
| `title` | `String` | Yes | Human-readable name of the study group. |
| `description` | `String` | No | Topics, goals, or notes supplied by the creator. |
| `createdBy` | `String` | Yes | Creator name used until accounts are implemented in a later sprint. |
| `maxCapacity` | `Integer` | Yes | Creator-declared maximum, validated from 2 through 100. |
| `meetingTime` | `String` | No | Meeting date/time information entered for Sprint 1. |
| `location` | `String` | No | Free-text meeting location or online meeting information. |

## Validation rules

- `course`, `instructor`, `title`, and `createdBy` must not be blank.
- `maxCapacity` is required and must be between 2 and 100.
- `description`, `meetingTime`, and `location` are optional in this sprint.
- If validation fails, the form is shown again and the repository is not called.

## Modeling decision

`maxCapacity`, `meetingTime`, and `location` are fields on `StudyGroup` instead of separate `Membership` or
`Meeting` objects. Sprint 1 only creates, lists, and searches groups. Joining, member counts, accounts, and
recurring schedules are later work, so separate models would add relationships that this sprint does not yet use.

`createdBy` is currently text rather than a relationship to a user entity. Account creation and sign-in are Release
1 backlog stories, but they were not committed to Sprint 1.