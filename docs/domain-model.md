# Study Group Finder — Current Domain Model (Release 1.0)

**Released implementation:** [v1.0](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0), commit `54783d1`. The persistence layer uses **Spring Data JPA + Hibernate**, not Spring Data JDBC. The three JPA entities are `Student`, `StudyGroup`, and `Membership`.

## 1. Entity relationship model

```text
+-----------------------------+                  +-----------------------------+
| Student                     |                  | StudyGroup                  |
| table: student_account      |                  | table: study_group          |
|-----------------------------|                  |-----------------------------|
| id: Long (PK)               |                  | id: long (PK)               |
| username: String (unique)   |                  | course: String              |
| passwordHash: String        |                  | instructor: String          |
| role: String                |                  | title: String               |
+-----------------------------+                  | description: String         |
            1                                    | createdBy: String           |
            |                                    | status: String              |
            |                                    | maxCapacity: int            |
            | many                               | meetingTime: String         |
+-----------------------------+                  | location: String            |
| Membership                  |                  +-----------------------------+
| table: group_membership     |                             1
|-----------------------------|                             |
| id: Long (PK)               |                             | many
| student: Student (FK)       +-----------------------------+
| studyGroup: StudyGroup (FK) |
+-----------------------------+

Unique membership pair: (student_id, study_group_id)
```

**Critical distinction:** `StudyGroup.createdBy` is **not** a JPA relationship or foreign key. It remains a Java `String`, storing a username. The application now populates it from the authenticated student, rather than accepting the form's `createdBy` value. The two JPA `@ManyToOne` relationships are on `Membership`.

## 2. Student

Source: `src/main/java/p466/studygroupfinder/model/Student.java`.

| Field | Java type | Persistence and meaning |
|---|---|---|
| `id` | `Long` | `@Id`, identity-generated primary key |
| `username` | `String` | Required, unique, at most 40 characters at DB level; normalized to lowercase on registration |
| `passwordHash` | `String` | Required, length 255; password encoded using BCrypt; never store plaintext |
| `role` | `String` | Column length 20; field initialized to `STUDENT`, manually provisioned admins use `ADMIN` |

Registration explicitly sets `STUDENT`. The repository looks up accounts with `findByUsername` and checks duplicates with `existsByUsername`. Public registration has no way to request `ADMIN`; the demonstrated `demo-admin` user was promoted through a **production database update**, and a new login loaded the new role.

## 3. StudyGroup

Source: `src/main/java/p466/studygroupfinder/model/StudyGroup.java`.

| Field | Actual Java type | Validation / behavior |
|---|---|---|
| `id` | **`long`** | `@Id` and generated with `GenerationType.AUTO` |
| `course` | `String` | `@NotBlank`; search matches the full course value exactly |
| `instructor` | `String` | `@NotBlank` |
| `title` | `String` | `@NotBlank` |
| `description` | `String` | Optional group details |
| `createdBy` | **`String`** | Set by `StudyGroupController.createGroup()` from the authenticated username; **not a Student entity relation** |
| `status` | `String` | Initialized `ACTIVE`; admin may set `SUSPENDED`; old rows with null status are treated as active in public listing |
| `maxCapacity` | **`int`** | `@Min(2)`, `@Max(100)`; submitted as part of create form |
| `meetingTime` | `String` | Optional, free-form text; not a date/time entity |
| `location` | `String` | Optional, free-form text |

### Creation, visibility, and moderation

- Creating a group requires an authenticated account. Validation is checked on the server. The controller sets `createdBy` to `current.getUsername()` and `status` to `ACTIVE`, then persists the row.
- The public list and exact-course search load matching groups, then omit rows whose status is `SUSPENDED` (case-insensitive comparison). Existing null statuses are not excluded.
- `GET /admin/groups` lists **all** groups, including suspended. `POST /admin/groups/{id}/suspend` changes an existing group's status to `SUSPENDED` and saves it; the row and prior memberships remain.
- `POST /groups/{id}/join` rejects a suspended group **at the server**, even when a user sends a direct request without using the UI.

There is **no** group owner foreign key, role-specific owner entity, creator-only edit action, or separate schedule/meeting table in the final implementation. Do not infer any of these from backlog stories.

## 4. Membership

Source: `src/main/java/p466/studygroupfinder/model/Membership.java`.

| Field | Java type | Persistence and meaning |
|---|---|---|
| `id` | `Long` | Identity-generated PK |
| `student` | **`Student`** | `@ManyToOne(fetch = LAZY, optional = false)` via `student_id` (non-null FK) |
| `studyGroup` | **`StudyGroup`** | `@ManyToOne(fetch = LAZY, optional = false)` via `study_group_id` (non-null FK) |

The table is `group_membership`, with a database **unique constraint** on `(student_id, study_group_id)`.

The join flow rejects anonymous users, nonexistent or suspended groups, duplicate memberships, and a group at maximum capacity. `countByStudyGroup_Id` calculates the membership count; `existsByStudent_IdAndStudyGroup_Id` checks whether the user already joined. The unique pair constraint provides a second safeguard against duplicates. **Capacity is checked in application logic, not through database locking**, so simultaneous last-slot requests remain a known limitation.

## 5. Key modeling decisions and evolution

| Stage | Model and behavior |
|---|---|
| Sprint 1 | Only `StudyGroup`, initially including creator-entered text; local H2 for persistence |
| Sprint 2 | Added `Student` and `Membership`; usernames/password hashes, unique membership pairs, capacity and account-based creator attribution |
| Sprint 3 | Added persistent student `role` and group `status`; secured admin suspension; switched deployed authentication to Spring Security and production storage to PostgreSQL |

The **field type** of `StudyGroup.createdBy` did not change from `String` to `Student` in Sprint 2 or Sprint 3. The **trust boundary** changed: it is now server-supplied, not creator-editable. This is an explicit final implementation choice, not unfinished documentation.

For architecture and deployment details, see [`architecture.md`](architecture.md) and [`design-decisions.md`](design-decisions.md).
