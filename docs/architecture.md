# Study Group Finder - Sprint 1 Architecture

## Layered request flow

```text
+--------------------+
|      Browser       |
| GET /              |
| GET /groups        |
| GET /groups/new    |
| POST /groups       |
+---------+----------+
          |
          v
+--------------------+                  redirects GET / to /groups
|   HomeController   |
+--------------------+

+------------------------+
| StudyGroupController   |
| - list/search groups   |
| - show create form     |
| - validate and save    |
+-----------+------------+
            |
            | constructor-injected dependency
            v
+------------------------+
| StudyGroupRepository   |
| JpaRepository          |
| findByCourse(String)   |
+-----------+------------+
            |
            | Spring Data JPA / Hibernate
            v
+------------------------+
| File-based H2 database |
| ./data/studygroupfinder|
+------------------------+

Controller view results:

StudyGroupController -> groups/list.html   -> rendered list/search page
StudyGroupController -> groups/create.html -> rendered creation form

Form binding:

groups/create.html -> StudyGroup -> Bean Validation -> repository save -> redirect:/groups
```

## Domain box

```text
+----------------------------------+
| StudyGroup                       |
+----------------------------------+
| id: Long                         |
| course: String                   |
| instructor: String               |
| title: String                    |
| description: String              |
| createdBy: String                |
| maxCapacity: Integer             |
| meetingTime: String              |
| location: String                 |
+----------------------------------+
```

## Responsibility boundaries

- The browser sends HTTP requests and displays HTML.
- Controllers handle request mappings, prepare the model, validate submitted objects, and choose a view or redirect.
- `StudyGroup` holds the sprint's domain data and validation rules.
- `StudyGroupRepository` is the only application component that talks to persistence.
- Spring Data JPA and Hibernate map the entity to the relational H2 database.
- Thymeleaf templates render server-side HTML and bind form fields to `StudyGroup`.
- Controllers contain no SQL, and there is no service layer because the Sprint 1 workflow is intentionally small and the course permits direct controller-to-repository interaction.