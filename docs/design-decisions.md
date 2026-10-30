# Study Group Finder — Design Decisions

Release 1.0 delivers public browse/search, student accounts, authenticated creation/joining, membership capacity and administrator suspension, hosted on Render with PostgreSQL and automated CI/CD.

## MVC and a small domain model

Spring MVC and Thymeleaf provide server-rendered forms and tables. The first sprint established a complete browser/controller/repository/database slice; later stories extended that structure. `Student`, `StudyGroup` and `Membership` express the core domain without introducing a separate frontend or scheduling subsystem.

| PoEAA pattern | Application and benefit |
|---|---|
| Domain Model | Student, StudyGroup and Membership give accounts, groups and joining distinct representations that can be extended with roles/status |
| Data Mapper | JPA/Hibernate maps entities and membership associations to H2/PostgreSQL, allowing production storage to change without rewriting the UI |
| Template View | Thymeleaf renders controller model attributes and validation errors, keeping page presentation separate from persistence |
| Server Session State | Spring Security keeps authentication in the server session across requests, avoiding repeated credential handling in controllers |

## Relational membership and creator attribution

A membership row has foreign keys to its student and study group, with a unique pair to prevent duplicate joins. This supports membership counts and existence checks directly. Capacity is checked before inserting a membership; concurrent requests for the last slot would require stronger locking for high-volume use.

The group creator is stored as a username string supplied by the server from the authenticated account. This fits the release's attribution requirement. A referential owner association would be useful for future ownership features. Description and meeting fields use simple text; exact course matching and free-form meetings keep the release focused on finding and joining groups.

## Spring Security and non-destructive moderation

The October 20 Sprint 2 review requested CI/CD and production deployment. The October 22 clarification with Professor Bahramian agreed on an administrator role and group suspension in place of optional creator editing, which remains in the post-course backlog as SGF-35.

Spring Security supplies form authentication, BCrypt password encoding, CSRF protection and server-side ADMIN restrictions. Registration always creates a student. Suspending a group preserves its record and memberships, removes it from public browse/search, and prevents new joins. This provides moderation without deleting student data. The admin table retains suspended rows; repeating the demo uses a new disposable group.

## Separate development, test and production storage

File-backed H2 makes local setup simple and preserves development data. Isolated in-memory H2 makes tests repeatable. Managed PostgreSQL preserves production records across web-service restarts. Spring profiles and runtime environment variables separate these environments; database secrets remain outside Git. Hibernate schema update is sufficient for this course prototype; versioned migrations would be appropriate as the schema grows.

## Portable container and automatic delivery

A Java 17 JDK stage builds the JAR with the Maven wrapper, and a smaller JRE stage runs it. The entrypoint maps Render's PostgreSQL connection string to JDBC properties, while Spring reads the platform port. LF shell scripts and the wrapper make the Linux image portable from the Windows development environment.

GitHub Actions verifies tests before building Docker. Required checks protect `main`; Render's `checksPass` trigger waits for CI on the commit it deploys. The [failed run](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782) and [recovery](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560) demonstrate the gate. [PR #4](https://github.com/logan-p466/study-group-finder/pull/4) demonstrates successful delivery: heading change, green CI, merge `1e3cf11`, Render Auto-Deploy/Live in 2m44s, and the new heading on the live site.

Render Free hosting is suitable for this course demonstration. Cold starts require opening the site before the demo. Production data belongs in PostgreSQL, and its free-plan expiry must be managed through the grading window.
