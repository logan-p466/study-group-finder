# Sprint 1 Review and Retrospective

CSCI-P 466 · Group 2 · Logan Reddell

## Goal and delivered increment

Sprint 1 delivered a small vertical slice: create a study group, persist it and display it in the browser. The completed work included public browsing (SGF-11), exact-course search (SGF-12), creation for a course/instructor (SGF-16), and meeting time/location (SGF-17). Server-side validation checked required fields and capacity. File-backed H2 preserved groups across application restarts. Controller, repository and application-context tests covered the slice.

## Estimation and process

The narrow browse/search/create scope was manageable, and the sprint completed 17 estimated points. Repository setup, the development environment and Jira took more time than expected. Defining a smaller first increment helped keep the work achievable.

Constructor-injected repositories and MVC/Thymeleaf forms made the browser-to-database path clear. Tests for validation and redirects provided a useful foundation for accounts and membership in Sprint 2 and security in Sprint 3.

For the next project, I would establish the repository, development environment, Jira sprint and passing CI build at the start. I would keep browser-flow checks with each story so integration work is included in the estimate.

## Release 1.0 continuity

The Sprint 1 browse/search/create functionality is available in the [live application](https://sgf-p466-group2-web.onrender.com/groups). Creation requires sign-in in the final release. Accounts and membership extend the model, and administrators can suspend groups. The final suite contains 32 tests across the three sprints. See [the current domain model](domain-model.md) and [Sprint 3 retrospective](sprint3-retrospective.md).
