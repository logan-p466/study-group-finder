# Sprint 3 Review and Retrospective — Study Group Finder

**CSCI-P 466 · Group 2 · Project 1**
**Jira sprint:** SGF Sprint 3, scheduled October 21–27, 2026; closed October 30, 2026 at 2:50 AM EDT. Verification and release continued October 29–30, after the scheduled sprint end.
**Sprint 2 review:** October 20, 2026. **Scope clarification:** Professor Bahramian, October 22, 2026.
**Repository:** https://github.com/logan-p466/study-group-finder
**Live application:** https://sgf-p466-group2-web.onrender.com/groups

## 1. Sprint objective and agreed deliverable

Deliver Release 1.0 with a production PostgreSQL database, Docker image, automated GitHub Actions tests and image build, required checks for changes to `main`, and Render hosting with automatic deployment after successful CI. The October 22 professor discussion revised the user-facing scope: implement an `ADMIN` role and the ability to **suspend** study groups without deletion; defer creator-only editing ([SGF-35](https://loganreddell.atlassian.net/browse/SGF-35)). Release 2 and Release 3 ideas remain outside Project 1.

## 2. Sprint backlog and outcome

| Jira work item | Estimate | Verified outcome |
|---|---:|---|
| [SGF-30](https://loganreddell.atlassian.net/browse/SGF-30): PostgreSQL and health | 3 | Local PostgreSQL tested; managed Render PostgreSQL in use; live `/actuator/health` reported working |
| [SGF-31](https://loganreddell.atlassian.net/browse/SGF-31): Docker | 3 | Multi-stage Java 17 Dockerfile; local image built/smoke-tested; GitHub CI Docker job passed |
| [SGF-32](https://loganreddell.atlassian.net/browse/SGF-32): CI and checks | 3 | `build-test` and dependent `docker-build` jobs; branch rules; failed-then-fixed real CI runs recorded |
| [SGF-33](https://loganreddell.atlassian.net/browse/SGF-33): Render CI/CD | 5 | Initial Blueprint service Live; later merge `1e3cf11` marked Auto-Deploy, 2m44s, with visible updated heading |
| [SGF-34](https://loganreddell.atlassian.net/browse/SGF-34): regression, docs, release | 2 | 32 green CI tests; documentation PR #5 merged as `54783d1`; annotated sprint tags and [published v1.0 release](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0) verified |
| [SGF-37](https://loganreddell.atlassian.net/browse/SGF-37): admin suspension | 5 | Live administrator dashboard, suspension status retained, public group hidden, normal user denied with 403; automated security tests passed |
| [SGF-35](https://loganreddell.atlassian.net/browse/SGF-35): creator editing | deferred | Removed from Sprint 3 scope after professor review; not implemented or claimed done |

**Completed scope: 21/21 estimated points.** All six committed Jira issues are Done. Sprint 3 (ID 37) closed on October 30, 2026 at 06:50:28 UTC (2:50:28 AM EDT). SGF-35 remained outside Sprint 3 with its explicit deferral. Sprint 1 completed 17 estimated story points and Sprint 2 completed 16. The larger final increment overran its scheduled October 27 end; release integration needed more time than estimated.

## 3. Final Release 1.0 demonstrated behavior

- Anonymous course group listing and exact-course search.
- Student registration, BCrypt password hashing, form login/logout, group creation and joining, membership count and capacity controls.
- Server-controlled creator attribution: `createdBy` **remains a `String`** containing the authenticated username; a relationship to `Student` was *not* implemented for group ownership.
- Group status (`ACTIVE` / `SUSPENDED`), admin listing and moderation, filtering suspended groups from public view, and server-side join restriction.
- Production `demo-admin` promotion in the **Render PostgreSQL database** followed by a fresh login. The live admin link appeared, and the admin route worked.
- A disposable group `Sprint 3 Admin Test` appeared on the public list, was shown ACTIVE in the admin dashboard, became SUSPENDED, stayed visible to the admin, and disappeared from public browsing. A regular student received HTTP **403 Forbidden** when attempting the admin page. Screenshots were captured.
- The web service was restarted and accounts, active groups, and SUSPENDED group state were still present afterward, demonstrating managed PostgreSQL persistence rather than reliance on temporary container storage.

The direct POST to join a suspended group is covered by `AdminSecurityIntegrationTest`; the supplied production screenshots do **not** independently show a live direct-POST attempt. The claims above distinguish automated coverage from the observed live demo.

## 4. CI evidence and regression tests

**Final verified green CI:** [GitHub Actions run 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828) on [PR #4](https://github.com/logan-p466/study-group-finder/pull/4). The Maven Surefire summary reported **32 tests, 0 failures, 0 errors, 0 skipped**, `BUILD SUCCESS`. Both required jobs passed: `build-test` and `docker-build`. The tests include 13 study-group-flow integration tests, 6 administrator/security integration tests, 5 study-group-controller tests, 5 auth-controller tests, 2 repository tests, and 1 application-context test.

**Failure and recovery exercise for Assignment 4:**

1. Created temporary [PR #3](https://github.com/logan-p466/study-group-finder/pull/3) with an intentionally failing JUnit assertion (`assertEquals(1, 2)`).
2. [Failed run 40991230782](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782): `build-test` failed, `docker-build` skipped because of its dependency, and required checks prevented merge.
3. Removed the artificial test; [recovery run 40991543560](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560) passed both jobs.
4. Closed demonstration PR #3 **without merging it**. The deliberately failing code was not added to `main`.

## 5. Real deployment and delivery proof

- Implementation [PR #2](https://github.com/logan-p466/study-group-finder/pull/2) merged to `main` as `32dc27e`.
- Render Blueprint first reported `Live` for the Docker web service on **October 29, 2026 at 10:39 PM Eastern**. It created `sgf-p466-group2-web` and `sgf-p466-group2-db`, with the managed PostgreSQL database separate from local H2 and Windows PostgreSQL.
- [PR #4](https://github.com/logan-p466/study-group-finder/pull/4) updated the visible heading to `IU Study Group Finder`; both CI jobs passed; merged as `1e3cf11`.
- Render's Deploys page explicitly showed **Auto-Deploy** for `1e3cf11` and `Live`, duration **2m44s**; no Manual Deploy was used for this test. The user confirmed the new heading appeared on the live site.
- Live health and group list endpoints were tested in the browser. The Free service may sleep when idle and has no production uptime guarantee.

## 6. What worked well

- The three-entity JPA/Thymeleaf structure was preserved while the team added a production PostgreSQL profile and authorization, avoiding a risky persistence rewrite.
- The previously built H2 tests supported regression verification when security roles, CSRF, and an administration endpoint were introduced.
- Separating the deliberate CI failure onto its own PR prevented the failure from contaminating `main`.
- CI-required checks and Render auto-deploy were proven with real run and commit evidence, rather than described hypothetically.
- Non-destructive suspension made a clear live demo: the suspended record remained for administrators and disappeared from the student view.

## 7. Difficulties and what they taught

- **Database confusion:** the app used local Windows PostgreSQL for development and separate Render PostgreSQL in production. Early admin role updates were accidentally made to the local `sgf` database; production did not change until the correct pgAdmin connection was used. A hostname/database verification step is essential before changing live data.
- **Authentication transition:** replacing ad hoc sessions with Spring Security required CSRF-aware request tests, `@WithMockUser` or `user(...)` processors, and a fresh login after changing a role.
- **Windows/Dev Container:** Docker struggled to read a bind-mounted Dockerfile until passed via PowerShell stdin; Git had casing (`DockerFile`/`Dockerfile`) and CRLF/trailing-whitespace issues; occasional stale `.git` lock files needed resolution.
- **GitHub CI:** the first pushed workflow had a YAML indentation issue that produced no jobs; adding a workflow file required an appropriately scoped token; required-check enforcement had to be configured on `main`.
- **Operational reality:** Render's first deployment had a separate Blueprint provisioning step, and live application validation was necessary to establish that the service and database really worked.

## 8. Course patterns, trade-offs, and process improvement

| PoEAA pattern | How it appeared |
|---|---|
| Domain Model | `Student`, `StudyGroup`, `Membership` |
| Data Mapper | JPA/Hibernate persisted entities/associations to H2/PostgreSQL |
| Template View | Thymeleaf displayed the public and admin views |
| Server Session State | Sprint 2 manual session ID; current runtime uses Spring Security session authentication |

**Trade-offs:** `createdBy` is server-controlled text, not a foreign key; searches are exact; meeting fields are free-form; joining's last-slot capacity check is not locked against races; Hibernate uses `ddl-auto=update` rather than versioned migrations. Creator editing and optional later-release features remain deferred.

For the next project: set up CI and an early Docker build in Sprint 1; verify Postgres and H2 environment isolation; establish documented role/session testing before production; add actual migrations and concurrency protection before supporting high-volume joining; maintain a release evidence checklist alongside the sprint board.

## 9. Final evidence and grading handoff

- [x] 32 tests green in GitHub CI; Docker build job passed.
- [x] Deliberate failing build and successful correction documented with permanent links.
- [x] Production Docker web service and PostgreSQL deployed; health and group listing demonstrated.
- [x] Production admin access, suspension, hidden group and non-admin 403 verified by supplied screenshots.
- [x] Persistence after Render web-service restart verified by the user.
- [x] A `main` merge triggered an automatic Render deployment.
- [x] Domain, architecture, design and sprint documentation matches the released implementation.
- [x] Annotated Git tags `sprint1`, `sprint2`, `sprint3`, and `v1.0` verified; [GitHub Release v1.0](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0) published October 30, 2026 at 2:41 AM EDT.
- [x] SGF-30 through SGF-34 and SGF-37 are Done; SGF-36 moderation epic is Done; Sprint 3 closed at 21/21 points.
- [ ] Supply the existing disposable `demo-admin` password to the grader. The password is not recorded in the release evidence, and its delivery has not been verified.
- [ ] Submit the completed official Assignment 4 worksheet and Jira, GitHub, and live-application links on Canvas; keep the service and database available through grading.

The previously suspended `Sprint 3 Admin Test` record demonstrates retained state. A grader repeating moderation should create a new disposable group because v1.0 has no unsuspend action.
