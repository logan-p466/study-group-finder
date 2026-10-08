# Sprint 1 — Review and Retrospective

## Completed

- Create + browse + search vertical slice working end to end: a student can create a study group for a specific course and instructor (with meeting time, location, and max capacity) through a validated form, see it in the group list, and filter the list by course.
- JPA persistence with a file-based H2 database, so groups survive navigation, reload, and app restart.
- Bean Validation on the create form (required course/instructor/title/creator, capacity 2–100) with per-field errors.
- Automated tests: MockMvc controller tests (list, valid create, invalid create, search by course) and @DataJpaTest repository tests (save/findById, findByCourse); `./mvnw test` passes.
- Docs: README with run/demo steps, domain model, and architecture notes in `docs/`.

## Planned but not completed

- **Join a study group** story — not built in Sprint 1; returned to the backlog for Sprint 2.

## What went well

- Reused the proven HarvestBox / Taco Cloud patterns (project layout, controller + validation flow, test shapes), which kept the build small and predictable.

## Difficulty encountered

- Sprint setup (Jira planning, repo skeleton) started on the deadline day, which compressed the actual build time.

## Improvement for Sprint 2

- Do the Jira sprint setup and the repo skeleton on day 1 of the sprint, not the last day, so implementation gets the full sprint window.
