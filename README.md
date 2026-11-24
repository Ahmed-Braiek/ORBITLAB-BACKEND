# OrbitLab Backend — Java 21 Spring Boot

Run locally with `mvn spring-boot:run`.

Endpoints:
- `GET /api/health`
- `GET /api/planets`
- `GET /api/planets/{id}`
- `GET /api/asteroids/close-approaches` — real NASA/JPL close-approach records, server-side HTTPS request, 30-minute in-memory cache, no API key.

The CAD endpoint returns data for near-Earth close approaches; it does not imply that those asteroids will hit Earth. Ephemerides and physical planet assets remain separate work. No hosting or paid API.

Source: https://ssd-api.jpl.nasa.gov/doc/cad.html
