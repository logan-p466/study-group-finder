# Study Group Finder

Study Group Finder is the Sprint 1 vertical slice for CSCI-P 466 Project I. A visitor can browse saved study groups, search by exact course text, and open a form to create a new group. A valid group is saved through Spring Data JPA to a file-based H2 database and appears in the list; an invalid form is shown again with validation errors and saves nothing.

## Team

- Logan Reddell
- 
## Technology

- Java 17
- Spring Boot 3.5.5
- Maven Wrapper
- Spring MVC
- Thymeleaf
- Bean Validation
- Spring Data JPA
- H2 Database

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

## Grader workflow

1. Start the application and open `http://localhost:8080/`.
2. Confirm the empty-state message appears when no groups exist.
3. Click `New Study Group`.
4. Create a valid group and confirm the browser redirects to `/groups` and displays it.
5. Search using the exact course text and confirm groups for other courses are not displayed.
6. Submit another form with a blank title and confirm an error appears and nothing is saved.
7. Stop and restart the application; the previously saved group should still appear.
8. Run `./mvnw test` and confirm all seven tests pass.

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
