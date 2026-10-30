# Sprint 1 Review and Retrospective (Historical Record)

**This file records the first sprint as it occurred.** For the current three-entity, role-secured Release 1.0 model, see [`domain-model.md`](domain-model.md), [`architecture.md`](architecture.md), and [`sprint3-retrospective.md`](sprint3-retrospective.md). Statements about “next sprint” below refer to the plan *at the end of Sprint 1*, not the present status.

## Sprint 1 goal

Deliver a running Study Group Finder with one end-to-end slice: a student can create a study group and see it listed, with the data persisted.

## Delivered Sprint 1 increment

- **SGF-16:** create a study group for a specific course and instructor.
- **SGF-11:** browse available groups without signing in.
- **SGF-12:** search by exact course text.
- **SGF-17:** capture and display meeting time and location.
- Validate required group fields and capacities before saving.
- Persist groups in file-backed H2 across application restarts.
- Cover controller, repository, and application context behavior with automated tests.
- Document the initial vertical slice and how to run it.

At this point `StudyGroup` was the only JPA entity, and `createdBy` was accepted as plain creator text. Registration, authentication, and joining were not Sprint 1 deliverables.

## Backlog and direction discussed at the Sprint 1 review

Joining was a Release 1 idea but not committed to Sprint 1. Accounts and login were later work. Potential directions were to add a membership record and link creation to an authenticated user; the original model document suggested that a future creator field might become an actual `Student` relationship.

**What actually happened later:** Sprint 2 did add `Student` and `Membership` entities and membership foreign-key relations. Sprint 3 added role-based security and suspension. The `StudyGroup.createdBy` Java field nevertheless **remained `String`** in the released code, with its username set by the backend from the signed-in student. The earlier *possibility* of replacing the field with a `Student` foreign key should not be described as delivered.

## What worked in Sprint 1

- A small, vertical UI/controller/repository/database slice was achievable and testable.
- Constructor-injected repositories separated persistence concerns from controllers.
- MVC forms and Thymeleaf models followed the same general structure as the course practicums.
- Automated tests preserved validation and redirect behavior for later changes.

## What was difficult

- Setting up the repository, development environment, and Jira consumed time that could have been used for the application.
- The broader product backlog needed to be trimmed to a small first increment.

## Improvement identified for the following sprint

Create the Jira sprint, branch, repository skeleton, Dev Container, and an initial passing build at the start of the sprint. Future authentication and joining should build on the verified vertical slice instead of replacing it.

## End-of-project perspective

Those Sprint 1 capabilities remained in the live [Release 1.0 application](https://sgf-p466-group2-web.onrender.com/groups), now supplemented by registration, membership, security, administrator suspension, and managed PostgreSQL. A final CI run for PR #4 reported **32 tests passing** across all tested components. The [GitHub v1.0 release](https://github.com/logan-p466/study-group-finder/releases/tag/v1.0) was published October 30, 2026. This postscript is the only Release 1.0 update to the historical Sprint 1 retrospective.
