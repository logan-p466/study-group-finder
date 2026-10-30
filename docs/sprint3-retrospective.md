# Sprint 3 Review and Retrospective

CSCI-P 466 · Group 2 · Logan Reddell

Sprint 3 was scheduled for October 21–27, 2026 and closed October 30. The Sprint 2 review was October 20; the October 22 clarification with Professor Bahramian agreed on administrator suspension in place of optional creator editing.

## Goal and outcome

Deliver Release 1.0 on Render using PostgreSQL, Docker and CI-gated automatic deployment, while retaining Sprint 1 and 2 functionality and adding administrator suspension.

| Jira item | Points | Delivered outcome |
|---|---:|---|
| [SGF-30](https://loganreddell.atlassian.net/browse/SGF-30) | 3 | Production PostgreSQL profile, environment configuration, health checks and restart persistence |
| [SGF-31](https://loganreddell.atlassian.net/browse/SGF-31) | 3 | Multi-stage Java 17 Docker image and local smoke run |
| [SGF-32](https://loganreddell.atlassian.net/browse/SGF-32) | 3 | Maven verification, dependent Docker build, required checks and failure/recovery demonstration |
| [SGF-33](https://loganreddell.atlassian.net/browse/SGF-33) | 5 | Render web/database Blueprint and automatic deployment after passing CI |
| [SGF-34](https://loganreddell.atlassian.net/browse/SGF-34) | 2 | Regression tests, README/design documentation, sprint tags and v1.0 release |
| [SGF-37](https://loganreddell.atlassian.net/browse/SGF-37) | 5 | ADMIN authorization, retained suspended records, public filtering and join rejection |

All six items are Done, labeled `sprint2-review` and assigned Fix Version `Sprint 3`: **21/21 estimated points**. Sprint 1 completed 17 points and Sprint 2 completed 16. Deployment/testing/release accounted for 16 final-sprint points and administrator suspension for 5. This larger commitment overran the planned sprint end; environment and release integration needed more time than estimated. Optional creator editing (SGF-35) was deferred to the post-course backlog by the agreed October 22 scope decision.

## Working increment and evidence

The [live application](https://sgf-p466-group2-web.onrender.com/groups) supports public browse/search, registration/login, authenticated creation, joining, membership counts and duplicate/capacity checks. Administrators can suspend groups while preserving records and memberships. A student receives 403 at the admin page. Accounts, groups and suspension state persist after a web-service restart. The [README](../README.md#grader-demo) contains the demo credentials and reproducible steps.

The automated suite has **32 tests**, with zero failures, errors or skips in [run 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828). New security coverage checks roles, suspension, public filtering and rejection of direct joins to suspended groups. Earlier flows remain covered by controller, repository and integration tests.

The intentional failing assertion in [run 40991230782](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782) failed `build-test`, skipped Docker and blocked merge. [Run 40991543560](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560) passed after correction. The demonstration PR was closed without merging the artificial test.

For the real deployment demonstration, [commit 07c288e](https://github.com/logan-p466/study-group-finder/commit/07c288e492f55c953bac55456160c89c43705805) changed the heading to **IU Study Group Finder**. [PR #4](https://github.com/logan-p466/study-group-finder/pull/4) passed CI and merged as `1e3cf11`. Render recorded **Auto-Deploy**, **Live**, duration **2m44s**, and the new heading appeared at `/groups`. `autoDeployTrigger: checksPass` gates deployment on CI. Annotated `sprint1`, `sprint2`, `sprint3` and `v1.0` tags and the [published release](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0) identify the milestones.

## Patterns and lessons

Domain Model kept accounts, groups and memberships distinct. Data Mapper let JPA/Hibernate use H2 locally and PostgreSQL in production without changing the views. Template View kept HTML rendering in Thymeleaf. Server Session State let Spring Security maintain authentication across requests. These patterns supported extending the original application with roles and suspension.

The hardest deployment work was coordinating Windows/Dev Container paths and LF scripts, GitHub checks and Render's database configuration. Passing the Dockerfile over stdin resolved the local bind-mount build issue. An administrator role update initially targeted local PostgreSQL; confirming the connected server/database resolved the production setup. Security changes also required CSRF-aware tests and a fresh login after role changes.

Next time, I would establish Docker and CI in Sprint 1, test each story through its browser flow, isolate test environment variables, and reserve more time for deployment integration. Free hosting also needs a prepared demo: wake the service before use and preserve/export the database before its expiry.

## Remaining product backlog

Join notes, public description/topic display, structured meeting formats, recurring schedules, calendar export, catalog selection and creator editing remain future product work. Exact course matching and free-form meeting details fit the current scope. The capacity check would need concurrency protection for simultaneous last-slot requests, and a growing deployment would benefit from versioned schema migrations.
