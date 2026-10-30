# Study Group Finder — Design Decisions and Actual Outcomes

**Scope:** Project 1, Sprints 1–3, through the deployed `main` version after the CI-gated Render auto-deployment of PR #4 (merge `1e3cf11`). Decisions are described as **implemented** where verified, not as proposed work. See [`domain-model.md`](domain-model.md) and [`architecture.md`](architecture.md) for the exact current model and topology.

## 1. Start with a small vertical slice

Sprint 1 delivered listing, exact course search, validated study-group creation, and persistent local H2 storage. Spring MVC controllers, Thymeleaf templates and repository interfaces provided the end-to-end path through browser, controller, validation, data access, and database. Optional meeting time/location stayed as simple strings instead of introducing recurring-schedule or calendar objects. This made the first increment small enough to build and test.

**Final result:** Public browse and search remain part of Release 1.0; group creation now requires an authenticated user. Searching by course still uses `StudyGroupRepository.findByCourse(course)` and exact equality.

## 2. Extend the domain with accounts and membership

Sprint 2 added two JPA entities:

- `Student` for unique username and BCrypt password hash.
- `Membership` for a student's joining a study group, with real lazy `@ManyToOne` links to `Student` and `StudyGroup`, and a unique `(student_id, study_group_id)` constraint.

The alternative of storing a delimited member list on each group was rejected because relationships, unique memberships, member counts, and existence checks are easier and safer in a relational table.

### Creator attribution: server-controlled text, not a foreign key

**Actual released model:** `StudyGroup.createdBy` still has Java type **`String`**. The project did **not** replace it with a `Student` relation. Instead, the server determines its value from `SecurityContextHolder` and a `StudentRepository` lookup, then assigns `current.getUsername()` in the group creation method. Client-supplied `createdBy` cannot choose a different creator when a group is created. This preserves the simple existing table while addressing the security concern of trusting arbitrary form text.

**Trade-off:** Username-based creator attribution is not relationally enforced against `Student` and makes future ownership actions harder if usernames change. Creator-only editing was deferred, so the smaller design is adequate for this increment.

## 3. Prefer JPA to a late JDBC rewrite

Some P466 practicums demonstrate Spring Data JDBC. This repository, however, already uses **Spring Data JPA** with `@Entity`, `JpaRepository`, `@ManyToOne` and Hibernate. Reimplementing the entire persistence layer in JDBC near release would have increased regression risk without improving the agreed functionality. The final version retains JPA and verified its behavior with tests and a production PostgreSQL demo.

Domain tables are `student_account`, `study_group`, and `group_membership`; not a fabricated creator table, schedule table, or student/group list column.

## 4. Move from manual sessions to role-based Spring Security

Sprint 2 initially used an `HttpSession` attribute containing a student ID and a manually validated login POST. At the October 22 review, the instructor requested actual Spring Security practice with an administrator role and study group suspension, rather than spending the remaining sprint on creator editing.

**Implemented:** `spring-boot-starter-security`, `spring-security-test`, `PasswordEncoder`, a repository-backed `UserDetailsService`, and `SecurityFilterChain`. Public paths remain browse/search/registration/login/health. Other paths require authentication; `/admin/**` explicitly requires `ROLE_ADMIN`. Spring Security's form login, logout and CSRF filters are active. Passwords use BCrypt; public registration always assigns `STUDENT`. The production `demo-admin` account received an `ADMIN` role through a controlled **Render database** update followed by fresh login, not through registration or hardcoded credentials.

**Residual cleanup:** `AuthController.java` still declares obsolete Sprint 2 manual POST login/logout methods and a `studentId` constant. These are not the deployed authorization mechanism; removing them would reduce confusion, but the security filter chain handles the real flow. This technical debt should not be hidden in documentation.

## 5. Moderate by suspension instead of deletion

The new work is [SGF-36 (Moderate Groups epic)](https://loganreddell.atlassian.net/browse/SGF-36) and [SGF-37 (administrator suspension story)](https://loganreddell.atlassian.net/browse/SGF-37). [SGF-35 (creator editing)](https://loganreddell.atlassian.net/browse/SGF-35) was deferred.

A group has a `String status` initialized to `ACTIVE`. An administrator can change it to `SUSPENDED` without deleting its database row or memberships. Public listing/search omit suspended groups, and joining rejects them server-side. This approach allows moderation without data destruction and demonstrates both authentication and authorization.

**Verified in the live system:** `Sprint 3 Admin Test` changed from ACTIVE to SUSPENDED; the admin page still displayed its record; the public listing no longer displayed it; a regular student received 403 at the admin page. The automated `AdminSecurityIntegrationTest` also verifies direct POST authorization and suspended-group joining.

**Limitation:** There is no unsuspend operation, history/audit trail, reason field, or per-group creator-editor permission in this release.

## 6. Preserve independent local, test, and production databases

- **Local default:** file-backed H2 enables persistence without a Postgres service.
- **Tests:** in-memory H2 with `create-drop` avoids touching personal/production data.
- **Local PostgreSQL test:** Windows PostgreSQL database `sgf`, addressed from a Linux Dev Container using `host.docker.internal`.
- **Production:** Render-managed PostgreSQL, configured by `DATABASE_URL` supplied in `render.yaml`; Docker entrypoint converts that URL for Spring JDBC properties.

This separation prevented committing database passwords. One real test failure came from inherited `SPRING_DATASOURCE_*` environment variables overriding H2 settings; tests subsequently ran with these variables removed. When promoting the production demo admin, an initial pgAdmin SQL query targeted the **local** `sgf` database, illustrating why the connected server/database must be confirmed before performing changes. Once corrected on Render PostgreSQL, administrator login worked.

**Trade-off:** Hibernate `ddl-auto=update` is expedient for the course but not a robust production migration system. A future version should use Flyway or Liquibase for reproducible versioned changes.

## 7. Use a portable Docker build and gated CI/CD

The final `Dockerfile` uses Java 17 build/JRE stages and the Maven wrapper. The application JAR runs with a shell entrypoint that sets JDBC values from Render's managed database connection. Local Docker builds were verified after solving a Windows file-access issue by passing the Dockerfile contents through standard input. Source case-sensitivity (`DockerFile` versus `Dockerfile`), Windows CRLF line endings, and Git index permissions required correction before the Linux build would succeed.

The [CI workflow](../.github/workflows/ci.yml) has two ordered jobs: `build-test` (Maven `verify`) and `docker-build` (depends on tests). The initial workflow had a YAML indentation mistake causing no jobs to run; it was corrected before merging. Pushing a workflow file required a GitHub token with workflow permissions. Branch rules require a PR and both checks for `main`.

**Fail-then-fixed evidence:** [failed run 40991230782](https://github.com/logan-p466/study-group-finder/actions/runs/40991230782) had a deliberately failing JUnit assertion; tests failed, Docker was skipped, merge was blocked. The test was removed; [fixed run 40991543560](https://github.com/logan-p466/study-group-finder/actions/runs/40991543560) passed both jobs. The temporary [PR #3](https://github.com/logan-p466/study-group-finder/pull/3) was closed without merge.

**Automatic deploy evidence:** [PR #4](https://github.com/logan-p466/study-group-finder/pull/4) passed both CI jobs ([run 40996253828](https://github.com/logan-p466/study-group-finder/actions/runs/40996253828), 32 tests passed), merged into `main` as `1e3cf11`, and Render reported **Auto-Deploy** and `Live` for that commit. The visible page heading changed to **IU Study Group Finder** without a manual redeploy.

## 8. Known limits and deliberate omissions

- **Concurrency:** duplicate joins are database-constrained, but the check-then-save capacity rule is not atomic under high concurrency; a group can potentially exceed capacity if two users join its final slot at once.
- **Search/meetings:** exact course text and free-form meeting time/location, not normalized catalog, full-text search, or calendars.
- **Creator ownership:** creator username is a plain string, not a `Student` FK.
- **Roles:** global STUDENT/ADMIN distinction; no group-specific role hierarchy or permission editor.
- **Operations:** Render Free web service can cold-start; free managed PostgreSQL has a limited lifetime. Web-container storage is not used as the production data store.
- **Maintenance:** lingering manual login/logout controller methods and `ddl-auto=update` should be cleaned up in a future increment.
- **Scope:** Recurring meetings, calendar export, creator-only editing, advanced course features, and unsuspension were not claimed as delivered.

## 9. Pattern connections to the course

| PoEAA concept | This project |
|---|---|
| **Domain Model** | Student, StudyGroup, Membership objects |
| **Data Mapper** | Hibernate/JPA maps entities and foreign-key associations to H2/PostgreSQL |
| **Template View** | Thymeleaf renders MVC model attributes as HTML |
| **Server Session State** | Spring Security-managed authenticated session in Release 1.0; manually stored student ID was the Sprint 2 implementation |

## 10. Release and Jira bookkeeping

The sprint scope was 21 estimated points: SGF-30 (3), SGF-31 (3), SGF-32 (3), SGF-33 (5), SGF-34 (2), and SGF-37 (5); SGF-35 was deferred. **Implementation verification and Jira workflow status are separate facts**: the current Jira statuses were still In Progress/To Do when checked, despite the verified deployed functionality. Transition them only when the final evidence and release are complete. Historical tags `sprint1`, `sprint2`, `sprint3` and a `v1.0` GitHub Release had **not** yet been published when this document was prepared; do not fabricate them.
