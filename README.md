# Study Group Finder

CSCI-P 466 · Group 2 · Logan Reddell

Study Group Finder lets students browse course groups, register and sign in, create groups, and join groups with available space. Administrators can suspend groups while preserving their records and memberships.

- [Live application](https://sgf-p466-group2-web.onrender.com/groups)
- [Jira project](https://loganreddell.atlassian.net/jira/software/projects/SGF/boards/2/backlog)
- [Release v1.0](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0)
- [Health check](https://sgf-p466-group2-web.onrender.com/actuator/health)

## Grader demo

The public demonstration administrator is **username `demo-admin`, password `demo-admin`**. Student accounts can be created at `/register`; registration redirects to the sign-in page, so sign in after registering each account. Use a unique username for each student.

1. Open the live application while signed out. Browse the groups and search using the complete course value, such as `C291`. Cancel clears the search.
2. Register a student and sign in. Choose **New Study Group**. Enter course `CSCI-P 466`, instructor `Bahramian`, a unique title such as `Grader Capacity Test`, capacity `2`, meeting time `Friday 5 PM`, and location `Wells Library`. Create the group. Its initial count is `0/2`; creation does not automatically join the creator.
3. Join the group. The count becomes `1/2` and the action changes to **Joined**, preventing a second join. Sign out, register and sign in as another student, and join the same group. It becomes `2/2`. A third signed-in student sees **Full** and cannot join. The server also checks duplicates and capacity.
4. As a student, open `/admin/groups`. The server returns **403 Forbidden**. Signed-out visitors are redirected to sign in.
5. Sign out and sign in as `demo-admin`. Create a new disposable group named `Grader Suspension Test`. Open **Administrator: Manage Groups** and suspend that row. It remains **SUSPENDED** in the admin table and disappears from public browse/search. The server rejects join requests for suspended groups and retains existing memberships. Use a new group when repeating this demo because this release has no unsuspend action.
6. Check `/actuator/health`: it reports `UP`. Accounts, groups and suspension state persist in managed PostgreSQL across web-service restarts.

## Run locally

Requirements: Java 17; the repository includes the Maven wrapper. From a clean checkout:

```bash
bash ./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080/groups`. The default profile uses file-backed H2 at `./data/studygroupfinder`, preserving local data between runs. Start without production datasource variables when using H2.

## Build and run with Docker

```bash
docker build -t sgf-sprint3 .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default sgf-sprint3
```

Open `http://localhost:8080/groups` and `http://localhost:8080/actuator/health`. This local smoke run uses H2 in the container; its data is temporary when the container is removed. Stop any other process using port 8080 first.

The local Windows build also succeeded with this equivalent PowerShell command when the bind mount prevented Docker from reading the file directly:

```powershell
Get-Content -Raw .\Dockerfile | docker build --progress=plain -f - -t sgf-sprint3 .
```

The multi-stage [Dockerfile](Dockerfile) builds with `eclipse-temurin:17-jdk` and runs the JAR with `eclipse-temurin:17-jre`. Its default profile is `prod`. [docker-entrypoint.sh](docker-entrypoint.sh) converts Render's PostgreSQL connection string into Spring JDBC properties at startup.

## Tests

```bash
bash ./mvnw clean verify
```

Windows: `.\mvnw.cmd clean verify`. Tests use isolated in-memory H2 with `create-drop`. If the shell has production datasource overrides, remove them before running tests; on Bash:

```bash
env -u SPRING_DATASOURCE_URL -u SPRING_DATASOURCE_USERNAME -u SPRING_DATASOURCE_PASSWORD bash ./mvnw clean verify
```

The suite contains **32 tests**: 13 group-flow integration, 6 admin/security integration, 5 group-controller, 5 auth-controller, 2 repository and 1 application-context test. It covers browse/search, validation, registration and password hashing, authentication, creation, membership counts, duplicate/capacity restrictions, administrator authorization, suspension, public filtering and direct-POST join rejection. [CI run 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828) passed all 32 with zero failures, errors or skips.

## Runtime configuration

[render.yaml](render.yaml) defines Free Docker service `sgf-p466-group2-web` and Free PostgreSQL 17 database `sgf-p466-group2-db`, both in Ohio. The service follows `main` and checks `/actuator/health`.

| Variable | Source and use |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Blueprint sets `prod`; use `default` for local H2 |
| `JAVA_TOOL_OPTIONS` | Blueprint sets `-XX:MaxRAMPercentage=65 -XX:+UseSerialGC` |
| `DATABASE_URL` | Blueprint `fromDatabase` connection-string reference to the managed PostgreSQL database |
| `PORT` | Render supplies the HTTP port; the prod profile reads it with fallback `8080` |
| `SPRING_DATASOURCE_URL` | Entrypoint derives the JDBC URL from `DATABASE_URL`; can be supplied directly for a local PostgreSQL run |
| `SPRING_DATASOURCE_USERNAME` | Entrypoint derives the database username; can be supplied directly for local PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Entrypoint derives the database password; can be supplied directly for local PostgreSQL |

Database credentials are injected at runtime and kept outside Git. Local H2 settings are in `application.properties`; production settings are in `application-prod.properties`. Production disables the H2 console, exposes health probes, and uses Hibernate schema update. Managed PostgreSQL stores production data separately from the web container.

## CI/CD and evidence

1. A pull request targeting `main`, or a push to `main`, starts [.github/workflows/ci.yml](.github/workflows/ci.yml).
2. GitHub Actions checks out the source, sets up Temurin Java 17 and runs `bash ./mvnw -B -ntp verify` in `build-test`. Surefire reports are uploaded even on failure.
3. `docker-build` depends on `build-test` and runs `docker build -t sgf-ci .`. A failed test skips the image build.
4. The active **Protect Main** ruleset requires a pull request and successful `build-test` and `docker-build` checks. Force pushes and branch deletion are blocked.
5. Merging starts CI for the new `main` commit. Render's **`autoDeployTrigger: checksPass`** waits for passing checks on that commit, builds the Docker image, injects the runtime environment and starts the application. The instance becomes Live after `/actuator/health` passes.

| Demonstration | Evidence |
|---|---|
| Failed test blocks delivery | [Run 40991230782](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782): intentional `assertEquals(1, 2)` failed `build-test`; Docker skipped and merge blocked |
| Corrected test passes | [Run 40991543560](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560): both jobs passed; temporary [PR #3](https://github.com/logan-p466/study-group-finder/pull/3) closed without merging the demonstration test |
| Real change reaches production | Heading changed to **IU Study Group Finder** in [commit 07c288e](https://github.com/logan-p466/study-group-finder/commit/07c288e492f55c953bac55456160c89c43705805), [PR #4](https://github.com/logan-p466/study-group-finder/pull/4); [CI 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828) passed; merge `1e3cf11` triggered Render **Auto-Deploy**, **Live**, duration **2m44s**; the heading is visible at `/groups` |

## Release scope

Sprint 3 delivered **SGF-30, SGF-31, SGF-32, SGF-33, SGF-34 and SGF-37**, all Done in Jira with label `sprint2-review` and Fix Version `Sprint 3`: **21/21 points**. It includes PostgreSQL/health, Docker, CI, automatic Render delivery, regression coverage, documentation and administrator suspension. The Sprint 2 review was October 20, 2026; the October 22 clarification agreed on administrator suspension in place of optional creator editing (SGF-35), which remains deferred to the post-course backlog.

Release 1.0 includes public browsing/exact-course search, registration/login, authenticated group creation, multiple groups for the same course/instructor, meeting time/location, joining, counts, capacity/duplicate checks and administrator suspension. Remaining backlog includes join notes, public display of group descriptions/topics, structured meeting formats, recurrence, calendar export, catalog selection and creator editing.

| Milestone | Source |
|---|---|
| Sprint 1 | [`sprint1`](https://github.com/logan-p466/study-group-finder/tree/sprint1), `6d36273` |
| Sprint 2 | [`sprint2`](https://github.com/logan-p466/study-group-finder/tree/sprint2), `5220e1a` |
| Sprint 3 | [`sprint3`](https://github.com/logan-p466/study-group-finder/tree/sprint3), `54783d1` |
| Release 1.0 | [`v1.0`](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0), published October 30, 2026 |

## Reliability and limits

Render Free instances serve this course demonstration and may sleep when idle. Open the site before the demo and allow time for a cold start. The web-container disk is temporary; production records reside in PostgreSQL. Free PostgreSQL has a 30-day lifetime; keep the service/database available through grading and export data before expiry. Search uses complete course text and meetings use free-form text. Duplicate memberships have a database unique constraint; simultaneous requests for the final slot are a concurrency limitation of the application-level capacity check.

## Design and retrospectives

- [Domain model](docs/domain-model.md)
- [Architecture](docs/architecture.md)
- [Design decisions](docs/design-decisions.md)
- [Sprint 1 retrospective](docs/sprint-review-retrospective.md)
- [Sprint 3 retrospective](docs/sprint3-retrospective.md)
