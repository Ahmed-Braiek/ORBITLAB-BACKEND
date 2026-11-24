package com.orbitlab.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Real JPL close-approach data. The Java backend is the only service calling JPL.
 * No NASA API key or credit card is required for the public CAD endpoint.
 */
@RestController
@RequestMapping("/api/asteroids")
@CrossOrigin(origins = "http://localhost:4200")
public class AsteroidController {
    private static final URI SOURCE = URI.create(
        "https://ssd-api.jpl.nasa.gov/cad.api?date-min=now&date-max=%2B30&dist-max=0.05&limit=25&fullname=true");
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile Snapshot snapshot;
    private record Snapshot(Instant created, Map<String,Object> payload) {}

    @GetMapping("/close-approaches")
    public synchronized Map<String,Object> closeApproaches() {
        if (snapshot != null && Instant.now().isBefore(snapshot.created().plus(CACHE_TTL)))
            return snapshot.payload();
        try {
            HttpRequest request = HttpRequest.newBuilder(SOURCE).timeout(Duration.ofSeconds(15))
                .header("User-Agent", "OrbitLab-University-Project/0.3").GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "JPL responded with " + response.statusCode());
            JsonNode root = mapper.readTree(response.body());
            if (!root.path("signature").path("version").asText().startsWith("1."))
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected JPL API version");
            JsonNode fields = root.path("fields");
            List<Map<String,String>> objects = new ArrayList<>();
            if (fields.isArray() && root.path("data").isArray()) {
                for (JsonNode record : root.path("data")) {
                    Map<String,String> item = new LinkedHashMap<>();
                    for (int i=0; i<fields.size() && i<record.size();i++)
                        item.put(fields.get(i).asText(), record.get(i).isNull() ? "" : record.get(i).asText());
                    objects.add(item);
                }
            }
            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("source", "NASA/JPL Small-Body Close-Approach Data API");
            payload.put("liveData", true);
            payload.put("retrievedAt", Instant.now().toString());
            payload.put("count", objects.size());
            payload.put("asteroids", objects);
            snapshot = new Snapshot(Instant.now(), payload);
            return payload;
        } catch(ResponseStatusException ex) {
            throw ex;
        } catch(Exception ex) {
            if (snapshot != null) {
                Map<String,Object> cached = new LinkedHashMap<>(snapshot.payload());
                cached.put("liveData", false);
                cached.put("cached", true);
                return cached;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "NASA/JPL data temporarily unavailable", ex);
        }
    }
}
