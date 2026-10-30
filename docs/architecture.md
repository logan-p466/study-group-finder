# Study Group Finder — Implemented Architecture (Release 1.0)

This describes the application merged into `main` and deployed on Render, including Sprint 3's Spring Security and administrator suspension work. The application is **Spring Boot 3.5.5, Java 17, Spring MVC, Thymeleaf, Spring Data JPA/Hibernate**, and PostgreSQL in production.

## Architecture at a glance

```text
Browser (public visitor / signed-in student / administrator)
    |
    v
Spring Security FilterChain
    |  form authentication, CSRF, role restrictions
    v
Spring MVC Controllers
    |-- HomeController                 redirects / to /groups
    |-- AuthController                 registration and GET login view
    |-- StudyGroupController           browse/search/create/join
    |-- AdminController                admin listing and suspension
    |
    v
Spring Data JpaRepository interfaces
    |-- StudentRepository
    |-- StudyGroupRepository
    |-- MembershipRepository
    |
    v
Hibernate / JPA domain entities
    |-- Student           (student_account)
    |-- StudyGroup        (study_group)
    |-- Membership        (group_membership)
    |
    v
H2 file DB (local) / H2 memory DB (tests) / PostgreSQL (Render)
```

Server-rendered templates are in `src/main/resources/templates/`. The application is not a REST JSON frontend or a separate JavaScript client. `Model` attributes from controllers drive the HTML pages. Repositories generate persistence queries through Spring Data JPA; the app does **not** implement the practicums' Spring Data JDBC repository model.

## Persistent entities and relationships

- `Student` (`student_account`): `Long id`, unique normalized `username`, BCrypt `passwordHash`, role string initialized to `STUDENT`.
- `StudyGroup` (`study_group`): generated primitive `long id`, `course`, `instructor`, `title`, optional `description`, server-assigned **`createdBy: String`**, `status: String` (`ACTIVE` or `SUSPENDED`), primitive `int maxCapacity`, optional `meetingTime`, `location`.
- `Membership` (`group_membership`): `Long id`, lazy **`@ManyToOne Student`** and **`@ManyToOne StudyGroup`** with non-null FKs, unique constraint `(student_id, study_group_id)`.

**Creator is not a JPA association.** `createdBy` still contains the creator's username text; the controller sets it using the authenticated user. There is no `@ManyToOne` creator relation in `StudyGroup`. See [`domain-model.md`](domain-model.md) for every field and constraint.

## Implemented web routes

| Method and path | Who can use it | Implementation |
|---|---|---|
| `GET /` | Everyone | Redirect to `/groups` |
| `GET /groups` | Everyone | Browse active groups; optional `?course=...` exact match |
| `GET /register` | Everyone | Registration form |
| `POST /register` | Everyone; CSRF required | Validate, normalize username, BCrypt hash, create `STUDENT` |
| `GET /login` | Everyone | Thymeleaf login view |
| `POST /login` | Everyone; CSRF required | Spring Security form authentication |
| `POST /logout` | Authenticated; CSRF required | Spring Security logout |
| `GET /groups/new` | Signed-in user | Group creation form |
| `POST /groups` | Signed-in user; CSRF required | Validate, set creator to authenticated username, save ACTIVE group |
| `POST /groups/{id}/join` | Signed-in user; CSRF required | Check suspension, existing membership and capacity; save membership |
| `GET /admin/groups` | **ADMIN only** | View all groups and statuses |
| `POST /admin/groups/{id}/suspend` | **ADMIN only**; CSRF required | Mark group SUSPENDED without deleting it |
| `GET /actuator/health` | Everyone | Spring Boot Actuator health check |

The current `AuthController` **still contains obsolete Sprint 2 manual POST login/logout methods** in its source file, but the Spring Security authentication and logout filters handle the deployed endpoints. This is cleanup debt; do not describe the manual `studentId` session attribute as the active authorization model.

## Authentication and authorization

`SecurityConfig` provides:

- A `BCryptPasswordEncoder` bean for hashed passwords.
- A `UserDetailsService` that loads a `Student` by username and builds a Spring Security user with `ROLE_ADMIN` if `student.role` equals `ADMIN`, otherwise `ROLE_STUDENT`.
- A `SecurityFilterChain`: public listing/registration/login/health; `/admin/**` requires ADMIN; remaining routes require authentication; CSRF remains enabled.
- Spring Security form login and logout.

`StudyGroupController` reads `SecurityContextHolder` to get the authenticated username and retrieves that `Student` from the database. The public list exposes the administration link when the **persisted student's** role is ADMIN. Production `demo-admin` was registered as STUDENT, promoted by updating the **Render** database, then signed in again; local Windows PostgreSQL is a different database.

Authorization is checked server-side: hiding a Suspend button is not the security boundary. Automated tests verify a student gets 403 at the admin endpoint and cannot execute suspension. Public listing filters suspended groups, and a direct join POST to a suspended group cannot add a membership.

## Runtime profiles and database boundaries

| Context | Persistence/configuration |
|---|---|
| Local default | `spring.datasource.url=jdbc:h2:file:./data/studygroupfinder`; Hibernate `ddl-auto=update` |
| Automated tests | `jdbc:h2:mem:studygroupfinder-test`; `ddl-auto=create-drop` in test resources |
| Local production-profile test | Windows PostgreSQL, `sgf`; Dev Container connects using `host.docker.internal` and explicit datasource variables |
| Render deployment | Managed Render PostgreSQL supplied as `DATABASE_URL`; Docker entrypoint generates JDBC datasource variables; `prod` profile; `PORT` default 8080 |

Production Postgres data is **not** stored in the web container filesystem. The live user/group/status records survived a web-service restart. `ddl-auto=update` is an expedient prototype schema strategy, not versioned migration tooling.

## Build, CI and deployed service

- Build: Java 17 Maven wrapper, Spring Boot JAR. `Dockerfile` uses a multi-stage `eclipse-temurin:17-jdk` build and a `17-jre` runtime, then runs `docker-entrypoint.sh`.
- CI: `.github/workflows/ci.yml` triggers for PRs into `main` and pushes to `main`; `build-test` runs Maven verification; dependent `docker-build` builds the image only if tests pass. Branch protection requires the checks.
- CD: `render.yaml` defines `sgf-p466-group2-web` (Free Docker web service), `sgf-p466-group2-db` (Free PostgreSQL), Ohio region, `main` branch, health path `/actuator/health`, `autoDeployTrigger: checksPass`.
- First live deployment: merged [PR #2](https://github.com/logan-p466/study-group-finder/pull/2), commit `32dc27e`, Render **Blueprint** deploy on October 29, 2026.
- Verified automatic deploy: merged [PR #4](https://github.com/logan-p466/study-group-finder/pull/4), merge commit `1e3cf11`, Render **Auto-Deploy**; CI run [40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828) passed with **32 tests** and both required jobs.
- Live site: https://sgf-p466-group2-web.onrender.com/groups (heading: **IU Study Group Finder**).

CI failure gating was demonstrated on a **separate throwaway branch**, [PR #3](https://github.com/logan-p466/study-group-finder/pull/3): [failure](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782) blocked Docker/merge; [fix](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560) passed both jobs; PR closed without merge.

## Enterprise application patterns and known constraints

| Pattern | Actual example |
|---|---|
| Domain Model | `Student`, `StudyGroup`, `Membership` objects hold the application's domain data |
| Data Mapper | JPA/Hibernate maps entities and associations to relational tables |
| Template View | Thymeleaf renders HTML from Spring MVC model attributes |
| Server Session State | Spring Security maintains the authenticated session; Sprint 2's custom session handling was superseded |

The group course search is exact-match, group meetings are plain text, creator attribution is not referentially enforced, simultaneous capacity checks can race without locking, and Render Free plans have cold-start and retention limits. These are documented trade-offs, not implemented future features.
