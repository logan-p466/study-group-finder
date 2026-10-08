# Study Group Finder

Study Group Finder is a web app that helps students find and organize study groups for their courses. In Sprint 1 it delivers one end-to-end slice: a student can create a study group for a specific course and instructor (with title, description, creator, meeting time, location, and max capacity) through a validated form, browse the group list, and search groups by course, with the data persisted in a relational database so it survives navigation, reload, and an app restart.

**Team:** Logan Reddell (team of 1)

**Stack:** Java 17, Spring Boot 3.5.5, Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA, H2 (file database), Maven, JUnit / Spring Boot Test / MockMvc.

## Run

```
./mvnw spring-boot:run
```

Then open http://localhost:8080 (redirects to /groups).

## Test

```
./mvnw test
```

## Demo steps

1. Open `/groups` — the study group list (empty state on first run).
2. Click **New Study Group** and submit the create form with a course, instructor, title, description, creator, meeting time, location, and max capacity.
3. You are redirected to `/groups` and the new group appears in the list.
4. Use the **Search by course** box on the list page to filter the list to one course; **Clear** shows all groups again.
5. Restart the app and open `/groups` again — the group is still listed, because persistence uses a file-based H2 database (`./data/studygroupfinder`).

See `docs/` for the domain model, architecture, and the Sprint 1 review/retrospective.
