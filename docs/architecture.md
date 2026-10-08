# Architecture — Study Group Finder (Sprint 1)

Layered Spring MVC structure, following the Taco Cloud style used in the practicums:

```
Browser
   |
   v
Thymeleaf views (templates/groups/list.html, templates/groups/create.html)
   |
   v
StudyGroupController  (web/)   +  HomeController ( / -> redirect:/groups )
   |
   v
StudyGroupRepository   (data/)  Spring Data JPA
   |
   v
H2 database (file: ./data/studygroupfinder)
```

Domain model lives in `model/` (the `StudyGroup` JPA entity).

## Layer responsibilities

- **Browser** — the user opens /groups, submits the create form, and is redirected back to the list after a successful save.
- **Thymeleaf views** — server-rendered HTML: the list renders every persisted group (with an empty state), the create form binds to a StudyGroup and shows per-field validation errors.
- **Controllers (web/)** — StudyGroupController handles GET /groups, GET /groups/new, POST /groups; it validates input with Bean Validation and returns the form view again on errors, otherwise saves and redirects. HomeController redirects / to /groups.
- **Repository (data/)** — StudyGroupRepository extends JpaRepository, giving CRUD plus a derived findByCourse query; the list page's search-by-course form calls findByCourse when a course is given, findAll otherwise. No hand-written SQL.
- **H2 database** — relational store on disk, so groups survive navigation, reload, and app restarts; Hibernate creates/updates the schema (ddl-auto=update).
