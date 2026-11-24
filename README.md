# OrbitLab — Java backend

Local Spring Boot 3.5, Java 21. No deployment.

## Run
```sh
mvn spring-boot:run
```

## API
- `GET /api/health`
- `GET /api/planets`
- `GET /api/asteroids/close-approaches` (JPL close approaches)
- `GET /api/ephemerides?date=2026-11-24` (**NASA/JPL Horizons** Sun-centered position vectors)

The ephemeris endpoint retrieves 3 samples at midnight UTC for selected day -1, day, and day +1 for all 8 planets (source units AU, coordinate system Ecliptic J2000). The first request requires internet access and can take several seconds; results cache for 12 hours. It reports HTTP 502 when JPL is unavailable rather than returning invented astronomical coordinates.

This is an educational visualization, not navigational software. The Angular 3D scene uses non-linear distance scaling for legibility. See: https://ssd-api.jpl.nasa.gov/doc/horizons.html
