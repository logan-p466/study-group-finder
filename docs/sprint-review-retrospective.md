# Sprint 1 Review and Retrospective

## Sprint goal

Deliver a running Study Group Finder with one end-to-end slice: a student can create a study group and see it listed, with the data persisted.

## Completed in Sprint 1

- SGF-16: create a study group for a specific course and instructor.
- SGF-11: browse available study groups without signing in.
- SGF-12: search study groups by exact course text.
- SGF-17: record and display meeting time and location.
- Validation prevents invalid groups from being saved.
- File-based H2 keeps saved groups after an application restart.
- Automated controller, repository, and context tests cover the increment.
- Documentation and grader instructions are committed with the code.

## Planned for Release 1 but not committed to Sprint 1

Joining a study group was part of the Release 1 backlog, but it was not committed to Sprint 1. Account creation and sign-in were also Release 1 stories outside this sprint. Those stories remain in the backlog.

## What went well

- The vertical slice follows the same controller, domain, repository, database, and Thymeleaf flow practiced in Taco Cloud and HarvestBox.
- Keeping one entity made the first sprint small enough to build, test, and demonstrate end to end.
- Constructor injection and repository interfaces kept persistence separate from the web layer.
- Automated tests made validation and redirect behavior repeatable.

## What caused difficulty or slowed the sprint

- Environment and repository setup consumed too much of the implementation window.
- Translating the broader Release 1 backlog into a deliberately small Sprint 1 model required careful scope decisions.

## Concrete process change for Sprint 2

Create the Jira sprint, branch, repository skeleton, Dev Container, and first passing build on day 1. This gives implementation and testing the full sprint instead of compressing them near the deadline.

## Likely Sprint 2 direction

- Add account creation and sign-in using the techniques taught in the course.
- Add a membership or join model that connects a student to a study group.
- Track current membership count and prevent joining when the maximum capacity is reached.
- Replace the temporary `createdBy` text field with a relationship to the authenticated user when the account model exists.