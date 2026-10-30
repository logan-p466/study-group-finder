# IU Study Group Finder

**CSCI-P 466 — Software Engineering for Information Systems II · Project 1 · Group 2**
**Repository:** https://github.com/logan-p466/study-group-finder
**Jira:** https://loganreddell.atlassian.net/jira/software/projects/SGF/boards/2/backlog
**Live application:** https://sgf-p466-group2-web.onrender.com/groups
**Health check:** https://sgf-p466-group2-web.onrender.com/actuator/health
**Administrator dashboard:** https://sgf-p466-group2-web.onrender.com/admin/groups

Study Group Finder is a server-rendered Java 17 / Spring Boot 3.5.5 application for organizing course study groups. It uses Spring MVC, Thymeleaf, Spring Data **JPA** with Hibernate, Bean Validation, Spring Security, H2 for local development/tests, and managed PostgreSQL on Render. The production application runs in a Docker container and is deployed automatically after passing GitHub Actions checks.

## Delivered functionality (Release 1.0 scope)

- **Visitors:** browse active groups and search by **exact course text**, without creating an account.
- **Students:** register, sign in/out, create a study group, see current membership/capacity, and join when space is available. A student cannot join the same group twice.
- **Group creation:** requires course, instructor, title, and capacity from 2 to 100; description, meeting time, and location are optional. The server assigns `createdBy` to the authenticated username instead of trusting form input.
- **Identity and security:** registration saves BCrypt password hashes and assigns `STUDENT`. Spring Security uses a repository-backed `UserDetailsService`, form authentication, sessions, role checks, and CSRF for POST forms.
- **Administrator:** a separately provisioned `ADMIN` user can open `/admin/groups`, view active and suspended groups, and suspend groups without deleting them. Suspended groups disappear from public browsing/search and reject new memberships server-side. Normal students receive HTTP 403 when accessing administrator functionality.
- **Persistence:** JPA entities map to PostgreSQL in production. User accounts, memberships, and group status survived a Render web-service restart in the production demonstration.

The site is intentionally a small server-rendered course project. Creator-only editing, calendar export, recurring meetings, advanced search, and other later-release ideas are **not** delivered in v1.0.

### Actual persistence model

| Java entity | Database table | Important properties |
|---|---|---|
| `Student` | `student_account` | `id: Long`, unique `username: String`, `passwordHash: String`, `role: String` (`STUDENT`/`ADMIN`) |
| `StudyGroup` | `study_group` | `id: long`, `course`, `instructor`, `title`, `description`, **`createdBy: String`**, `status: String`, `maxCapacity: int`, `meetingTime`, `location` |
| `Membership` | `group_membership` | `id: Long`, JPA `@ManyToOne` to `Student` and `StudyGroup`, unique `(student_id, study_group_id)` |

**Important:** `createdBy` is **still a String field in the released Java entity**. It now contains the authenticated creator's username assigned by the server; it is **not** a foreign key or `@ManyToOne Student` relationship. The *membership* links are true JPA relationships. See [`docs/domain-model.md`](docs/domain-model.md).

## Demo and grading

Use the public group list without logging in. Register a student, sign in, create a group, and join a group. To demonstrate moderation, sign in as `demo-admin`, open `/admin/groups`, suspend a **disposable** group, and verify that it disappears from `/groups`. Check that a normal student's request for `/admin/groups` is refused with HTTP 403. The production admin test group `Sprint 3 Admin Test` was suspended for the course demonstration.

**Demo credentials for the instructor:** `demo-admin` is a production administrator username. **Before grading, provide a temporary demo-only password directly to the instructor, or fill in a disposable grading credential here only if sharing that password in a public repository is acceptable.** No application password or PostgreSQL secret is included in this repository. A separate grading student account can be created with public registration.

| Account | Username | Password/availability |
|---|---|---|
| Administrator | `demo-admin` | **Action required:** share disposable demo credential with instructor |
| Student | Register a new non-admin user | Set password during registration; or provide a disposable pre-created grading account |

Administrators are provisioned **after registration** through an explicit database update to the role column; public registration never offers ADMIN. Production and local PostgreSQL databases are separate.

## Run locally (no Docker; H2)

Requires **Java 17** and the Maven wrapper, from the repository root:

```bash
env -u SPRING_DATASOURCE_URL \
    -u SPRING_DATASOURCE_USERNAME \
    -u SPRING_DATASOURCE_PASSWORD \
    bash ./mvnw spring-boot:run
```

Open `http://localhost:8080/groups`. By default, data is in file-backed H2 under `./data/studygroupfinder` (not PostgreSQL). Remove exported production datasource variables when running H2. The `data/` directory is not source code and must not be committed.

### Run all automated tests

```bash
env -u SPRING_DATASOURCE_URL \
    -u SPRING_DATASOURCE_USERNAME \
    -u SPRING_DATASOURCE_PASSWORD \
    bash ./mvnw clean verify
```

Tests use a separate **in-memory H2** datasource configured in `src/test/resources/application.properties`, with schema recreation on each Spring context. GitHub Actions PR #4 CI verified **32 tests, 0 failures, 0 errors, 0 skipped**: [run 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828). The tests cover repository persistence, validation, registration/password hashes, login and authorization, group creation, membership capacity/duplicates, and administrator suspension/browse restrictions.

### Run locally against Windows PostgreSQL

First create database `sgf` in local PostgreSQL. From the IntelliJ Linux Dev Container, where `host.docker.internal` resolves to Windows:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/sgf'
export SPRING_DATASOURCE_USERNAME='postgres'
read -rs -p 'Local PostgreSQL password: ' SPRING_DATASOURCE_PASSWORD
echo
export SPRING_DATASOURCE_PASSWORD
bash ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

`host.docker.internal` is for this **local** environment only and is not used in Render. Never check the password into Git.

### Build and smoke-test with Docker

With Docker Desktop running, from the project directory on Windows PowerShell:

```powershell
docker build -t sgf-sprint3 .
docker run --rm --name sgf-smoke -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default sgf-sprint3
```

Stop any other service listening on 8080 first. Check `http://localhost:8080/groups` and `http://localhost:8080/actuator/health`. Override the Dockerfile's production-profile default with `SPRING_PROFILES_ACTIVE=default` so this **local smoke test** uses H2; its container filesystem is temporary.

On some Windows/Dev Container bind mounts, Docker may not read the Dockerfile directly. An equivalent build in PowerShell is:

```powershell
Get-Content -Raw .\Dockerfile | docker build --progress=plain -f - -t sgf-sprint3 .
```

## Environment configuration

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `default` for local H2 or `prod` for production settings (Render sets `prod`) |
| `SPRING_DATASOURCE_URL` | JDBC URL for a directly configured PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Database credentials, supplied at runtime only |
| `DATABASE_URL` | Render-managed PostgreSQL connection string provided through `render.yaml`'s `fromDatabase` reference; `docker-entrypoint.sh` converts it into JDBC properties |
| `PORT` | HTTP port read by `application-prod.properties` (fallback 8080) |
| `JAVA_TOOL_OPTIONS` | JVM limits/GC settings set by the Render Blueprint |

The prod profile uses `spring.jpa.hibernate.ddl-auto=update` (appropriate for this coursework prototype, not a production migration solution); H2's console is disabled. The `/actuator/health` endpoint is exposed. All secrets must remain outside tracked source code and documentation.

## Continuous integration and delivery

1. Pull requests into `main` trigger `.github/workflows/ci.yml`.
2. `build-test` sets up Temurin Java 17, runs `bash ./mvnw -B -ntp verify`, and uploads Surefire test reports.
3. `docker-build` depends on `build-test` and builds the Docker image only after tests succeed.
4. The `main` branch has a ruleset requiring PRs and the two checks.
5. `render.yaml` defines one Free Docker web service and one Free PostgreSQL database in Ohio, with `autoDeployTrigger: checksPass`. Render builds/starts the container after the successful merge.

**Actual verification:**

| Evidence | Link / outcome |
|---|---|
| Intentional failed run | [40991230782](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782): `build-test` failed; `docker-build` skipped; merge blocked |
| Corrective successful run | [40991543560](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560): both jobs passed; [demo PR #3](https://github.com/logan-p466/study-group-finder/pull/3) was closed without merge |
| Sprint 3 implementation | [PR #2](https://github.com/logan-p466/study-group-finder/pull/2), merge commit `32dc27e` |
| Successful CI for automatic deploy | [40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828): 32 tests passed and both jobs succeeded |
| Automatic production deploy | [PR #4](https://github.com/logan-p466/study-group-finder/pull/4), merged as `1e3cf11`; Render showed trigger **Auto-Deploy**, `Live`, in 2m44s |

The first Render deployment was a **Blueprint** deployment for `32dc27e` on October 29, 2026, 10:39 PM Eastern. The later `1e3cf11` deployment was triggered automatically by the `main` merge, **not** manually, and changed the public heading to **IU Study Group Finder**.

## Limitations and release status

- Search requires an exact course string, and time/location are plain text rather than calendar records.
- `createdBy` is a server-assigned username string, not a referentially enforced owner foreign key.
- The membership unique constraint prevents duplicate joins; the capacity check is **not atomic under concurrent last-slot requests**. A stronger locking/transaction strategy is future work.
- The code still contains legacy Sprint 2 manual login/logout controller methods. Spring Security's filter chain handles the deployed login/logout flow, but removing the obsolete controller methods is useful cleanup.
- Render Free hosting has possible cold starts, ephemeral web-container storage, and time limits on Free PostgreSQL databases. This is a course demonstration, not a production SLA; preserve the database for the grading period.

**GitHub release status at documentation preparation:** there were **no existing** `sprint1`, `sprint2`, or `sprint3` tags and **no published `v1.0` GitHub Release** when checked. **Before Assignment 4 submission**, create and verify those three historical tags and the `v1.0` release, then update this paragraph with their actual links. No release has been invented here.

## Documents

- [`docs/domain-model.md`](docs/domain-model.md): **current Release 1.0** fields, entities, relationships, and constraints.
- [`docs/architecture.md`](docs/architecture.md): current security, MVC, persistence, infrastructure, and request flows.
- [`docs/design-decisions.md`](docs/design-decisions.md): decisions across all three sprints, trade-offs, and observed results.
- [`docs/sprint-review-retrospective.md`](docs/sprint-review-retrospective.md): historical Sprint 1 retrospective, with an explicit final-state clarification.
- [`docs/sprint3-retrospective.md`](docs/sprint3-retrospective.md): actual Sprint 3 results, CI evidence, deployment, and final remaining actions.
