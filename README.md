# Study Group Finder

**CSCI-P 466, Project 1, Group 2**
**Repository:** https://github.com/logan-p466/study-group-finder
**Jira:** https://loganreddell.atlassian.net/jira/software/projects/SGF/boards/2/backlog
**Live application:** [INSERT ACTUAL RENDER WEB URL]/groups

Study Group Finder is a Java 17, Spring Boot 3.5.5 web application using Spring MVC, Thymeleaf, Spring Data JPA/Hibernate, Bean Validation, Spring Security, and PostgreSQL. It provides a public study-group directory and registration-based group participation, plus administrator moderation.

## Release 1.0 features

- Visitors can browse study groups and filter by exact course text.
- Students can register, sign in, create groups, and join groups with available capacity.
- Registration uses BCrypt password hashes; student roles default to `STUDENT`.
- Joining enforces unique memberships and group capacity on the server. Group creation checks form validation and uses the authenticated username instead of trusting a submitted creator field.
- Spring Security handles form login, logout, session authentication, role-based access, and CSRF protection on state-changing requests.
- Administrators can view all groups at `/admin/groups` and suspend a group. Suspended groups remain in PostgreSQL, are excluded from public browse/search results, and reject joining requests. Registration cannot assign administrator privileges.
- The production application runs in Docker on Render with a managed PostgreSQL database, `/actuator/health` monitoring, and GitHub Actions checks gating automatic deployments.

Features for later releases, including recurring schedules, calendar exports, creator-only editing, and extended course-catalog options, are **not** part of v1.0.

## Demo and grading instructions

**Live group list:** [INSERT ACTUAL RENDER WEB URL]/groups
**Administrator page:** [INSERT ACTUAL RENDER WEB URL]/admin/groups

1. Browse the public groups without signing in, then search for a course.
2. Sign in with the student demo account. Create a group, join another group, and verify membership and capacity behavior.
3. Sign out and sign in with the administrator demo account. Open `/admin/groups` and suspend a sample group.
4. Verify the suspended group is absent from public search and that a regular student cannot access `/admin/groups` or join the suspended group.
5. Check `/actuator/health` and confirm that data survives an application restart or redeploy.

| Demo account | Username | Password |
|---|---|---|
| Student | [INSERT DISPOSABLE DEMO STUDENT USERNAME] | [INSERT DEMO-ONLY PASSWORD] |
| Administrator | [INSERT DISPOSABLE DEMO ADMIN USERNAME] | [INSERT DEMO-ONLY PASSWORD] |

These must be **temporary accounts specifically intended for course grading**. Never publish personal, database, or permanent administrator credentials. Ensure the administrator role is provisioned directly in the database, never through public registration. Reset or disable demo credentials after grading.

## Run locally without Docker (H2)

Requirements: Java 17, Maven wrapper, and a terminal in the project root. The default profile uses file-based H2 under `./data/`.

```bash
env -u SPRING_DATASOURCE_URL \
    -u SPRING_DATASOURCE_USERNAME \
    -u SPRING_DATASOURCE_PASSWORD \
    ./mvnw spring-boot:run
```

Open http://localhost:8080/groups.

## Run tests

The automated tests use an isolated in-memory H2 database, not the production database. With PostgreSQL credentials removed from the environment:

```bash
env -u SPRING_DATASOURCE_URL \
    -u SPRING_DATASOURCE_USERNAME \
    -u SPRING_DATASOURCE_PASSWORD \
    ./mvnw clean verify
```

**Final test evidence:** [INSERT ACTUAL NUMBER PASSED AND LINK TO GREEN CI RUN]. The test suite covers MVC forms, registration, authentication, role restrictions, joining, duplicate memberships, capacity, suspension, and earlier sprint regression paths.

## Run using local PostgreSQL

Create the `sgf` database in a local PostgreSQL server and set the following environment variables in the IntelliJ Dev Container terminal:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/sgf'
export SPRING_DATASOURCE_USERNAME='postgres'
read -rs -p 'PostgreSQL password: ' SPRING_DATASOURCE_PASSWORD
echo
export SPRING_DATASOURCE_PASSWORD
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

`host.docker.internal` refers to the **local Windows host as seen from the Dev Container**; it is not the production database address. Do not commit a database password.

## Build and run with Docker

With Docker Desktop running, execute these commands from the repository's Windows directory in PowerShell. Stop other processes listening on port 8080 first.

```powershell
docker build -t sgf-sprint3 .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default sgf-sprint3
```

In another terminal, verify `http://localhost:8080/groups` and `http://localhost:8080/actuator/health`. The default H2 storage inside this throwaway container is ephemeral; production data is stored in managed PostgreSQL.

## Environment variables and configuration

| Variable | Purpose | Where supplied |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Selects default local H2 or `prod` profile | Local shell or Render Blueprint (`prod`) |
| `SPRING_DATASOURCE_URL` | JDBC URL for direct PostgreSQL connections | Local/operational environment when using PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username | Local/operational environment |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password | Local secret environment, never Git |
| `DATABASE_URL` | Render-managed PostgreSQL connection string, converted by Docker entrypoint to JDBC properties | Render `fromDatabase` reference in `render.yaml` |
| `PORT` | HTTP port, defaulting to 8080 when absent | Render runtime or local default |
| `JAVA_TOOL_OPTIONS` | Memory/GC options for the production JVM | Render Blueprint |

The application has `application.properties` for H2 development and `application-prod.properties` for production behavior. Hibernate/JPA maps the persistent objects to SQL tables. Secrets belong in environment variables or Render service configuration, **not** the repository.

## Automated CI/CD

The GitHub Actions workflow `.github/workflows/ci.yml` runs on pull requests targeting `main` and pushes to `main`:

1. Check out the repository and install Temurin Java 17.
2. Run Maven build and tests (`verify`) and upload test reports.
3. If the test job passes, build the Docker image.
4. Branch protection requires passing checks before merging to `main`.
5. Render uses `render.yaml` with `autoDeployTrigger: checksPass` and deploys checked commits after GitHub status checks pass. Render builds and starts the Docker service; the entrypoint configures managed PostgreSQL at runtime.

**Verified successful CI:** [INSERT GITHUB ACTIONS RUN URL]
**Failed-then-fixed CI example:** [INSERT FAILED RUN URL AND FIXED RUN URL]
**Observed automatic deployment:** [INSERT DEPLOYED COMMIT SHA AND RENDER EVENT/TIMESTAMP]

## Release and limitations

Git tags: [`sprint1`](https://github.com/logan-p466/study-group-finder/tags), `sprint2`, `sprint3`.
GitHub release: [INSERT ACTUAL v1.0 GITHUB RELEASE URL].

Known limitations: matching course search uses exact text; concurrent last-slot joins are not guaranteed to be atomic without additional database locking; the Render Free tier may have cold starts, ephemeral web filesystem storage, and limits on managed PostgreSQL lifetime. The database should remain available throughout the grading period.

## Documentation

- [`docs/design-decisions.md`](docs/design-decisions.md): architecture and trade-off decisions, including PoEAA patterns.
- [`docs/architecture.md`](docs/architecture.md): layers, entities, routes, security, and deployment components.
- [`docs/domain-model.md`](docs/domain-model.md): historical Sprint 1 model.
- [`docs/sprint-review-retrospective.md`](docs/sprint-review-retrospective.md): historical Sprint 1 retrospective.
- [`docs/sprint3-retrospective.md`](docs/sprint3-retrospective.md): final-sprint review and project retrospective.
