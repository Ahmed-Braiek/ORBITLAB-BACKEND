# OrbitLab — Backend

Java 21 / Spring Boot 3.5 REST API. Phase 1 runs entirely on localhost.

## Run

```bash
mvn spring-boot:run
```

Verify: `http://localhost:8080/api/health`, `http://localhost:8080/api/planets`, `http://localhost:8080/api/planets/earth`.

These initial planet facts are locally defined and illustrative; NASA/JPL API integration and MySQL persistence are scheduled after the baseline client/server integration. No credit card, hosting, or environment secrets needed.
