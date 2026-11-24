# OrbitLab — Local Java Spring Boot Backend

Java 21 · Spring Boot 3.5 · MySQL 8 or MySQL-compatible MariaDB via XAMPP (compatibility should be checked).

## Phase 6 local database setup (Windows)

Start MySQL in XAMPP or your local MySQL service. Open phpMyAdmin and run:

```sql
CREATE DATABASE IF NOT EXISTS orbitlab
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'orbitlab_app'@'localhost' IDENTIFIED BY 'CHANGE_THIS_LOCAL_PASSWORD';
GRANT ALL PRIVILEGES ON orbitlab.* TO 'orbitlab_app'@'localhost';
```

Choose a unique local password and **do not commit it to Git**. In PowerShell, set environment variables for the current terminal:

```powershell
$env:ORBITLAB_DB_URL="jdbc:mysql://localhost:3306/orbitlab"
$env:ORBITLAB_DB_USERNAME="orbitlab_app"
$env:ORBITLAB_DB_PASSWORD="YOUR_LOCAL_PASSWORD"
mvn spring-boot:run
```

For CMD, use `set "ORBITLAB_DB_USERNAME=orbitlab_app"` and `set "ORBITLAB_DB_PASSWORD=YOUR_LOCAL_PASSWORD"`. The Java service uses Hibernate `ddl-auto=update` to create/alter local tables during development. This is **not** a production migration strategy.

## REST API

- `GET /api/health`
- `GET /api/planets`, `GET /api/planets/{id}`
- `GET /api/asteroids/close-approaches`: NASA/JPL close approaches
- `GET /api/ephemerides?date=2026-11-24`: Horizons heliocentric coordinates
- `GET /api/observatory/favorites`
- `POST /api/observatory/favorites` with JSON `{"objectType":"planet","objectId":"earth","displayName":"Earth"}`
- `DELETE /api/observatory/favorites/{id}`
- `GET /api/observatory/lists`
- `POST /api/observatory/lists` with JSON `{"name":"Weekend observations","description":"My selected planets"}`
- `PUT /api/observatory/lists/{id}`
- `DELETE /api/observatory/lists/{id}`
- `GET /api/observatory/lists/{id}/entries`
- `POST /api/observatory/lists/{id}/entries` with JSON `{"objectType":"planet","objectId":"mars","displayName":"Mars","note":"Observe this week"}`
- `DELETE /api/observatory/lists/{id}/entries/{entryId}`

## Testing

Run `mvn clean test`. Repository integration tests use in-memory H2 rather than your local MySQL; a real MySQL smoke test is still required.

## Security and limitations

The Observatory currently supports **one local profile, without login or authorization**. Do not expose the backend outside localhost or deploy this configuration publicly. API secrets and database credentials must never be committed. All astronomy graphics remain educational visualizations: JPL positions are real; distances in the frontend are compressed. The Java service may contact NASA/JPL over the internet but no hosting or paid API is needed.
