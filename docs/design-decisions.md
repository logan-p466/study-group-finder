# Study Group Finder — Design Decisions

## Scope of Release 1.0

Release 1.0 includes public group browsing and course search; account registration and sign-in; group creation and joining; capacity and duplicate membership checks; administrator-only suspension; automated testing; a Docker application image; PostgreSQL production persistence; and CI-gated Render deployment. Extended course catalogs, recurring meetings, calendar exports, and creator-only editing remain outside v1.0.

The Sprint 2 review was recorded in Jira on October 20, 2026, with a professor follow-up on October 22. The October 22 meeting replaced the optional group-creator edit feature (**SGF-35, deferred**) with administrator moderation (**SGF-36 Epic and SGF-37 Story**). A suspended group is not deleted: it remains available to administrators but is hidden from ordinary listings and cannot accept members.

Sprint 3 Jira delivery items: **SGF-30** PostgreSQL/health, **SGF-31** Docker, **SGF-32** CI, **SGF-33** Render CD, **SGF-34** testing/documentation/release, **SGF-37** administrator suspension. The agreed sprint estimate is 21 points.

## Spring MVC and Thymeleaf

Spring MVC controllers keep HTTP request handling separate from data access. Thymeleaf renders server-side templates; Bean Validation checks form inputs. Repositories expose database operations and avoid embedding database queries in page rendering code. The first sprint used this architecture for a small end-to-end working group directory.

## JPA instead of a JDBC rewrite

The application uses Spring Data **JPA** with Hibernate rather than the Spring Data JDBC implementation from Practicum 3. JPA entity relationships and `JpaRepository` already supported the working application. Rewriting persistence solely to match the practicum's library would add risk without expanding the product's behavior. Hibernate maps `Student`, `StudyGroup`, and `Membership` into relational tables.

## Separate Membership entity

A many-to-many relationship between students and groups is represented as a distinct `Membership` entity. The unique student/group pair prevents duplicate memberships, and membership counts support capacity limits. Keeping a delimited list of usernames inside a group row would make referential integrity, uniqueness, and counting harder.

The capacity check is enforced on the server but is not a guarantee against simultaneous last-slot races; robust enforcement would require concurrency control or database locking.

## Account security and administrator authorization

Sprint 2 used BCrypt hashes with a manually stored `studentId` in an `HttpSession`. In Release 1.0, Spring Security provides `UserDetailsService`, a password encoder, the `SecurityFilterChain`, login/logout handling, authenticated sessions, and CSRF protection. Roles are persisted in `Student.role` and exposed as Spring Security authorities `ROLE_STUDENT` and `ROLE_ADMIN`.

Regular registration always creates `STUDENT` users. Administrators are provisioned by a trusted database operation, never from a public form. `/admin/**` is protected by the server's security configuration, not merely hidden navigation links. Suspended groups are excluded from public browse/search and are rejected by the joining controller even when a direct POST request is submitted.

The ADMIN role is a global permission to moderate all groups. Group creator ownership is a separate per-record concept; creator-only editing was explicitly excluded from this release.

## Database choice and Spring profiles

- **Default development:** file-backed H2 for easy local runs and retained development data.
- **Tests:** in-memory H2 for repeatable, isolated execution.
- **Production:** Render-managed PostgreSQL for durable relational records independent of the web container.
- **Local PostgreSQL checks:** connection can use `host.docker.internal:5432` from the IntelliJ Dev Container, but that host name is not valid for the Render production service.

The Spring `prod` profile configures production behavior, including reading the runtime port and exposing `/actuator/health`. Credentials come from environment variables and are not committed. The automated test environment must not inherit PostgreSQL datasource variables that would override its H2 configuration.

## Build, CI/CD, and release

The multi-stage Dockerfile builds the application with Java 17 and copies only its runtime artifact into the final image. Docker's entrypoint translates Render's managed database connection into Spring's JDBC settings. GitHub Actions verifies Maven tests before building the Docker image. With required checks configured, Render uses `autoDeployTrigger: checksPass` to release tested `main` branch commits automatically. The web container filesystem is not relied upon for persistent data.

The versioned release consists of the repository, passing GitHub Actions checks, the public Render service, historical `sprint1`, `sprint2`, and `sprint3` tags, and a published GitHub `v1.0` Release. Actual live URLs and verification records belong in the README and Assignment 4 submission, not invented examples.

## PoEAA patterns and trade-offs

- **Domain Model:** distinct user, group, and membership concepts.
- **Data Mapper:** Hibernate/JPA persistence mapping.
- **Template View:** server-rendered Thymeleaf HTML.
- **Server Session State:** the application migrated from manually managed session IDs to Spring Security-managed sessions.

Known trade-offs include exact text-based course search, a text username stored as `StudyGroup.createdBy` rather than an ownership foreign key, and a capacity check that can need stronger concurrency control under simultaneous joins. These limitations do not prevent the required v1.0 demonstration.
