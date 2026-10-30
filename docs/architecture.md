# Study Group Finder — Release 1.0 Architecture

## Technology and layers

Study Group Finder uses Java 17, Spring Boot 3.5.5, Spring MVC, Thymeleaf, Bean Validation, Spring Security, Spring Data JPA/Hibernate, H2, and PostgreSQL.

```text
Browser / HTML forms
    |
Spring Security filter chain (authentication, roles, CSRF)
    |
Spring MVC controllers
    |-- HomeController: root redirect
    |-- AuthController: registration and login-page view
    |-- StudyGroupController: browse, search, create, join
    `-- AdminController: view and suspend groups
    |
Spring Data JPA repositories
    |-- StudentRepository
    |-- StudyGroupRepository
    `-- MembershipRepository
    |
Hibernate/JPA entities
    |-- Student
    |-- StudyGroup
    `-- Membership
    |
H2 (local/tests) or PostgreSQL (production)
```

Spring MVC controllers render Thymeleaf templates and delegate database access to repositories. The security filter processes login/logout requests and checks privileges before protected controller actions execute.

## Domain model and database rules

| Entity | Key fields | Role |
|---|---|---|
| `Student` | `id`, `username`, `passwordHash`, `role` | Registered user; role `STUDENT` or `ADMIN` |
| `StudyGroup` | `id`, `course`, `instructor`, `title`, `createdBy`, `description`, `maxCapacity`, `meetingTime`, `location`, `status` | Group metadata and `ACTIVE`/`SUSPENDED` status |
| `Membership` | `id`, `student_id`, `study_group_id` | Links one student to one group; unique student/group pair |

Each student can join many groups and each group can have many students through `Membership`. Server-side checks prevent duplicate joins and refuse groups at capacity. The `createdBy` value comes from the authenticated username rather than a user-submitted field. Suspended groups and their membership records remain in the database.

## HTTP endpoints

| Endpoint | Behavior | Access |
|---|---|---|
| `GET /` | Redirect to group list | Public |
| `GET /groups` | Browse active groups; optional course filter | Public |
| `GET /register`, `POST /register` | Registration form and validated account creation | Public |
| `GET /login` | Login page | Public |
| `POST /login`, `POST /logout` | Spring Security form authentication and logout | Spring Security filters |
| `GET /groups/new`, `POST /groups` | Create a group | Signed-in student or admin |
| `POST /groups/{id}/join` | Join an active group with available capacity | Signed-in student or admin |
| `GET /admin/groups` | View all groups, including suspended ones | `ADMIN` only |
| `POST /admin/groups/{id}/suspend` | Mark a group `SUSPENDED` without deleting it | `ADMIN` only |
| `GET /actuator/health` | Health check | Available to deployment health checks |

Spring Security maps the persisted student role to `ROLE_STUDENT` or `ROLE_ADMIN`; it restricts `/admin/**` to administrators. Public registration always creates `STUDENT` accounts and never accepts role elevation from request input. CSRF protection applies to state-changing requests. The join endpoint checks group status independently of public-list filtering.

## Environment and deployment

- **Local development:** file-backed H2 database under `./data/` with default Spring profile.
- **Automated tests:** isolated H2 in-memory database, not production PostgreSQL.
- **Production:** `prod` Spring profile and managed PostgreSQL on Render.
- **Image:** multi-stage Java 17 Dockerfile builds a runnable Spring Boot JAR; `docker-entrypoint.sh` configures the data source from Render's runtime database connection string.
- **CI:** GitHub Actions executes Maven verification and a Docker image build for PRs/pushes to `main`.
- **CD:** Render Blueprint deploys only after successful GitHub checks via `autoDeployTrigger: checksPass`.
- **Health and secrets:** `/actuator/health` monitors the running app; all database secrets are supplied at runtime.

## Patterns from Patterns of Enterprise Application Architecture

- **Domain Model:** `Student`, `StudyGroup`, and `Membership` represent business entities and relationships.
- **Data Mapper:** Hibernate/JPA handles persistence mappings behind repository interfaces.
- **Template View:** Thymeleaf generates pages from server-side controller models.
- **Server Session State:** Spring Security manages authenticated browser sessions; Sprint 2 used a manually managed `HttpSession` student ID before this change.

## Known limitations

Course search is exact-text matching. Concurrent joins targeting the final available group slot can require stronger transaction/locking guarantees; checking the count in application code alone does not eliminate the race. Free-tier hosting is not a high-availability production service.
