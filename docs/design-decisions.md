# Sprint 2 design decisions (October 19, 2026)

## Scope agreed in the October 13 review
The instructor and student agreed that the next increment would focus on student
signup/login and joining an existing study group. Joining is higher priority.
Browsing/searching remain public, and the existing create-group flow is kept.

## MVC login + HTTP session, without the Spring Security web framework
The review did not agree to add Spring Security authentication and authorization. The solution uses ordinary
Spring MVC form controllers, `HttpSession`, and a `Student` JPA entity. After
a correct login the server stores the student ID in the session; privileged
routes consult that ID rather than trusting a username in browser form data.
The session ID is rotated when signing in and invalidated at logout.

For password storage, a `PasswordEncoder` bean uses `BCryptPasswordEncoder`,
following Practicum 4. Only `spring-security-crypto` is included; the application
does not include `spring-boot-starter-security`, a login filter chain, or
Spring Security authorization rules. BCrypt salts and hashes passwords rather
than storing them as plaintext. This is still a teaching/demo login flow: the
custom `HttpSession` handling does not automatically provide CSRF protection,
rate limiting, or account recovery. The application should be used locally, not deployed as
a public service without a security review.

## Membership relation
A separate `Membership` entity connects an existing `Student` to an existing
`StudyGroup` with foreign keys and a unique student/group constraint.
The join controller rejects duplicate memberships and already-full groups.
The more advanced simultaneous-join/overbooking edge case remains a known risk
as described in the existing Release 1 MVP plan and was not requested in the
October 13 review.

## Rejected alternative
Storing student IDs as a delimited string in `StudyGroup` would create
duplicate and integrity issues and make capacity queries awkward. A separate
JPA entity better matches the relational structure used in the practicums.

## Preservation
Existing StudyGroup columns and the file-backed H2 location remain intact.
Two new tables are added via the existing `ddl-auto=update` setting. The
`createdBy` value is now derived from the authenticated session for new rows.
We did not migrate to SQLite or add recurring scheduling, join notes,
profile features, or any other feature not part of the review plan.
