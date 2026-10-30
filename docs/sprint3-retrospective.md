# Sprint 3 Review and Project Retrospective

**Course / project:** CSCI-P 466, Study Group Finder, Group 2
**Sprint:** SGF Sprint 3 (Jira planned dates October 21–27, 2026)
**Instructor follow-up:** October 22, 2026
**Release:** v1.0

## Delivered release scope and instructor review

The final release scope combines the Sprint 1 public group directory and Sprint 2 user-registration/membership increment with administrator moderation (**SGF-37**) and deployment infrastructure (**SGF-30–34**). The October 22 instructor meeting replaced creator-only editing (**SGF-35**, deferred outside Project 1) with a genuine Spring Security ADMIN role. Moderators can view and suspend groups without deleting existing records; suspended groups disappear from public browse/search and cannot be joined.

The release uses a PostgreSQL-backed Render web service, a Java 17 Docker image, GitHub Actions tests and image builds, and `autoDeployTrigger: checksPass` for automatic deployment of checked commits. The application uses Spring MVC, Thymeleaf, JPA/Hibernate, and Spring Security.

## Sprint estimates and outcomes

| Jira issue | Agreed deliverable | Estimated points |
|---|---|---:|
| SGF-30 | PostgreSQL profile and health endpoint | 3 |
| SGF-31 | Portable Docker image | 3 |
| SGF-32 | GitHub Actions CI | 3 |
| SGF-33 | CI-gated Render deployment | 5 |
| SGF-34 | Regression tests, README, versioned release | 2 |
| SGF-37 | Administrator-only suspension | 5 |
| **Total** | | **21** |

Sprint 1 committed 17 points and Sprint 2 planned approximately 16. Sprint 3's 21-point estimate included substantial infrastructure work, so comparing the point totals alone understates the setup and integration risk. **Actual points completed:** [INSERT VERIFIED TOTAL FROM JIRA]. **Deferred:** SGF-35. Other incomplete items, if any: [INSERT OR STATE NONE].

## Final demonstration and evidence

| Evidence | Verified result |
|---|---|
| Public application | [INSERT ACTUAL RENDER URL] |
| Deployment commit | [INSERT ACTUAL `main` COMMIT SHA] |
| GitHub Actions success | [INSERT ACTUAL WORKFLOW RUN URL] |
| Failed test/check and subsequent fix | [INSERT BOTH REAL RUN URLS OR DESCRIBE EVIDENCE] |
| Render automatic deployment | [INSERT EVENT/TIMESTAMP/SCREENSHOT REFERENCE] |
| Maven automated tests | [INSERT PASSED TEST COUNT AND COMMAND OUTPUT] |
| Database persistence after restart/redeploy | [INSERT WHAT WAS OBSERVED] |
| Administrator and student authorization tests | [INSERT VERIFIED TEST RESULTS] |
| Version tags and release | [INSERT LINKS FOR sprint1, sprint2, sprint3, v1.0] |

Demo coverage includes anonymous browsing/search; registration and password validation; student sign-in; group creation; joining; duplicate and full-group rejection; administrator sign-in; suspension; access denial to students on `/admin/**`; hidden suspended groups; and rejection of direct joining requests to suspended groups.

## Estimation: what went well and what did not

The small Sprint 1 model was straightforward to estimate because group creation and public browsing fit a clear MVC/JPA vertical slice. The second sprint introduced cross-cutting behavior: accounts, password hashes, membership relationships, and validation. Final-sprint infrastructure involved more integration uncertainty than the feature count alone suggested, especially environment-specific configuration, Docker runtime behavior, and CI-to-Render handoff.

A better future estimate would reserve explicit capacity for infrastructure investigation, deployment verification, and full regressions rather than treating them as overhead attached to feature stories.

## Most useful PoEAA patterns

- **Domain Model:** Student, StudyGroup, and Membership formed a natural representation of the application's rules.
- **Data Mapper:** Hibernate/JPA mapped the entity graph to H2 and PostgreSQL without database code in the controllers.
- **Template View:** Thymeleaf provided simple, testable server-rendered pages.
- **Server Session State:** Spring Security took responsibility for authenticated session management, replacing the earlier manually stored student ID.

## Hardest engineering and automation problems

The local environment combined a Linux IntelliJ Dev Container with Windows PostgreSQL and Docker Desktop, which required correctly distinguishing localhost inside each environment. PostgreSQL datasource variables leaked into H2-based test runs and initially caused application-context errors. The security conversion required adapting MockMvc tests for CSRF and authenticated principals instead of legacy manual session attributes. GitHub Actions and Render required separate validation of build/test checks, database connection settings, and the live release.

**Final deployment-specific difficulty:** [INSERT THE ACTUAL MOST DIFFICULT CI/RENDER PROBLEM AND ITS RESOLUTION].

## Next-project improvements

Establish a clean, repeatable CI build and a container smoke test early in the project. Keep test database configuration isolated from developer production-profile variables. Define the public deployment contract and the evidence needed for acceptance criteria at the beginning of each sprint. Treat release automation and role-based security as substantial work items with regression coverage, not merely configuration changes.

## Remaining limitations

The group capacity limit can race under concurrent final-slot joins without stronger locking. Course search uses exact text. Future Release 2 and Release 3 enhancements remain in the backlog. These are acknowledged scope choices rather than undisclosed omissions.
