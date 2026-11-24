# ORBITLAB — SOA Technical Contract (Phase 8)

## Architecture
```mermaid
flowchart LR
    A[Angular + Three.js local frontend :4200] -->|REST JSON| B[Java Spring Boot :8080]
    B -->|JPA Hibernate| C[(Local MySQL orbitlab)]
    B -->|HTTPS GET| D[NASA JPL Horizons]
    B -->|HTTPS GET| E[NASA JPL Close Approach Data]
```

- All frontend calls to persisted data go through the Java service. No direct browser-to-MySQL access.
- The database stores one local profile's favorite objects and observation lists; no login exists.
- JPL calls are made by Java and cached. Planet graphics are visual approximations; Horizons samples are genuine scientific vectors.

## REST endpoints
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Java health check |
| GET | `/api/planets` | Planet metadata |
| GET | `/api/planets/{id}` | Planet by ID |
| GET | `/api/ephemerides?date=YYYY-MM-DD` | Horizons heliocentric positions |
| GET | `/api/asteroids/close-approaches` | JPL near-Earth close approaches |
| GET | `/api/observatory/favorites` | Read favorites |
| POST | `/api/observatory/favorites` | Add favorite |
| DELETE | `/api/observatory/favorites/{id}` | Remove favorite |
| GET | `/api/observatory/lists` | Read lists |
| POST | `/api/observatory/lists` | Create list |
| PUT | `/api/observatory/lists/{id}` | Edit list |
| DELETE | `/api/observatory/lists/{id}` | Delete list |
| GET | `/api/observatory/lists/{id}/entries` | Read list entries |
| POST | `/api/observatory/lists/{id}/entries` | Add list entry |
| DELETE | `/api/observatory/lists/{id}/entries/{entryId}` | Remove entry |

### REST behavior
- POST returns 201; DELETE returns 204; GET and PUT return 200.
- Invalid validated JSON returns 400; missing resources return 404; duplicate favorite/entry returns 409.
- External NASA/JPL failures return 502 if no cached result is available.
- Planet object identifiers are lowercased for persistence. API does not offer multiuser authorization.

## Persistence model
```mermaid
erDiagram
 OBSERVATION_LIST ||--o{ OBSERVATION_ENTRY : contains
 OBSERVATION_LIST { bigint id PK string name string description timestamp createdAt }
 OBSERVATION_ENTRY { bigint id PK bigint list_id FK string objectType string objectId string displayName string note timestamp addedAt }
 FAVORITE_OBJECT { bigint id PK string objectType string objectId string displayName timestamp savedAt }
```

Favorites and entries may refer to planets or asteroids. Object IDs are logical references, not foreign keys to a NASA data table.

## Academic validation
1. Java is a genuinely implemented REST service with database-backed CRUD.
2. Angular consumes Java service via HTTP.
3. Java consumes external NASA/JPL services, independent of the frontend.
4. Maven integration tests use H2 in CI; local MySQL smoke test must still be performed.
5. Angular build CI checks TypeScript compilation. Neither CI pipeline replaces an end-to-end browser/GPU test.

## Security boundary
Development-only and localhost-oriented. CORS allows localhost:4200; this is **not authentication**. There are no accounts or authorization, so do not publish the backend with public access.

## Data truthfulness
Horizons: real time-indexed positions using heliocentric ecliptic reference, with visual radial compression. Rotation, cloud motion, textures, ring patterns and physical scale are stylized, not observationally verified for the selected instant.
