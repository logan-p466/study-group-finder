# Study Group Finder

Study Group Finder is the CSCI-P 466 Project I application. Sprint 1 added public group browsing, course search, and group creation with Spring Data JPA and H2. Sprint 2 extends that code with student registration, sign-in, group joining, membership counts, and capacity checks. Group creation now requires sign-in. Invalid forms display field-specific validation errors.

## Technology

- Java 17
- Spring Boot 3.5.5
- Maven Wrapper
- Spring MVC
- Thymeleaf
- Bean Validation
- Spring Data JPA
- H2 Database
- BCryptPasswordEncoder from spring-security-crypto (without Spring Security web authentication)

## Build

From the project root inside the Dev Container:

```bash
./mvnw clean package
```

## Test

```bash
./mvnw test
```

## Run

```bash
./mvnw spring-boot:run
```

Open:

```text
http://localhost:8080/
```

The root URL redirects to `/groups`.

## Browser workflow

1. Start the application and browse `/groups` without signing in.
2. Search by course to confirm the Sprint 1 browsing and filtering remain.
3. Open `/register`. Submit invalid fields and check validation messages appear beside them.
4. Register a valid account and sign in at `/login`.
5. Create a valid study group and confirm its creator is the signed-in account.
6. Submit a blank title on the create form and check the Sprint 1 field validation.
7. Join a group, try joining it again, then fill it to capacity using other accounts.
8. Stop and restart the application to check persisted records.
9. Run `./mvnw clean test` to check the MVC and integration tests.

## Development notes

See `docs/sprint2-consistency.md` for the comparison with Sprint 1 and Practicums 1-5. Follow your course policy on AI-assisted code; review and adapt reference changes yourself before submitting them.

## Database setup

No external database installation is required. The application creates a file-based H2 database under:

```text
./data/
```

The `data/` directory is intentionally ignored by Git because it is local runtime data. Hibernate creates and updates the schema through:

```properties
spring.jpa.hibernate.ddl-auto=update
```

The H2 console is enabled at:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL while the application is running:

```text
jdbc:h2:file:./data/studygroupfinder
```

## Dev Container

The repository includes `.devcontainer/devcontainer.json`. Open the project in IntelliJ IDEA Ultimate as a Dev Container. The container supplies Java 17 and Maven, forwards port 8080, makes the Maven wrapper executable, and downloads dependencies during setup.
