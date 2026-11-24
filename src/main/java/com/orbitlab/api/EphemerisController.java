package com.orbitlab.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ephemerides")
@CrossOrigin(origins="http://localhost:4200")
public class EphemerisController {
    private static final String API = "https://ssd.jpl.nasa.gov/api/horizons.api";
    private static final Map<String,String> IDS = Map.of(
        "mercury","199","venus","299","earth","399","mars","499",
        "jupiter","599","saturn","699","uranus","799","neptune","899");
    private static final Duration TTL = Duration.ofHours(12);
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final ObjectMapper json=new ObjectMapper();
    private final Map<LocalDate,CacheEntry> cache=new java.util.concurrent.ConcurrentHashMap<>();
    private record CacheEntry(Instant fetchedAt, Map<String,Object> payload){}
    public record Vector(double xAu,double yAu,double zAu) {}
    public record Body(String id,String horizonsId,List<Vector> positionsAu) {}

    @GetMapping
    public Map<String,Object> get(@RequestParam String date) {
        final LocalDate day;
        try { day=LocalDate.parse(date); }
        catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use date=YYYY-MM-DD");}
        if(day.isBefore(LocalDate.of(1900,1,2)) || day.isAfter(LocalDate.of(2099,12,30)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supported dates: 1900-01-02 to 2099-12-30");
        CacheEntry saved=cache.get(day);
        if(saved!=null && Instant.now().isBefore(saved.fetchedAt().plus(TTL)))return saved.payload();
        List<CompletableFuture<Body>> tasks=IDS.entrySet().stream()
            .map(entry->CompletableFuture.supplyAsync(()->query(entry.getKey(),entry.getValue(),day)))
            .toList();
        try {
            List<Body> bodies=tasks.stream().map(CompletableFuture::join).toList();
            Map<String,Object> data=new LinkedHashMap<>();
            data.put("source","NASA/JPL Horizons");
            data.put("referenceFrame","Ecliptic J2000");
            data.put("center","Sun center (500@10)");
            data.put("units","AU");
            data.put("sampleDates",List.of(day.minusDays(1).toString(),day.toString(),day.plusDays(1).toString()));
            data.put("date",day.toString());
            data.put("bodies",bodies);
            data.put("scientific",true);
            data.put("retrievedAt",Instant.now().toString());
            cache.put(day,new CacheEntry(Instant.now(),data));
            return data;
        }catch(Exception ex){
            tasks.forEach(f->f.cancel(true));
            if(saved!=null){
                Map<String,Object> stale=new LinkedHashMap<>(saved.payload());
                stale.put("staleCache",true);return stale;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"JPL Horizons temporarily unavailable; try again",ex);
        }
    }

    private Body query(String id,String horizonsId,LocalDate date) {
        try {
            Map<String,String> params=new LinkedHashMap<>();
            params.put("format","json");
            params.put("COMMAND","'"+horizonsId+"'");
            params.put("OBJ_DATA","'NO'");
            params.put("MAKE_EPHEM","'YES'");
            params.put("EPHEM_TYPE","'VECTORS'");
            params.put("CENTER","'500@10'");
            params.put("REF_PLANE","'ECLIPTIC'");
            params.put("REF_SYSTEM","'ICRF'");
            params.put("OUT_UNITS","'AU-D'");
            params.put("VEC_TABLE","'2'");
            params.put("VEC_LABELS","'NO'");
            params.put("CSV_FORMAT","'YES'");
            params.put("START_TIME","'"+date.minusDays(1)+"'");
            params.put("STOP_TIME","'"+date.plusDays(2)+"'");
            params.put("STEP_SIZE","'1 d'");
            StringBuilder url=new StringBuilder(API+"?");
            for(var p:params.entrySet()){
                if(url.charAt(url.length()-1)!='?')url.append('&');
                url.append(URLEncoder.encode(p.getKey(),StandardCharsets.UTF_8))
                   .append('=').append(URLEncoder.encode(p.getValue(),StandardCharsets.UTF_8));
            }
            HttpRequest request=HttpRequest.newBuilder(URI.create(url.toString()))
                .timeout(Duration.ofSeconds(22)).header("User-Agent","OrbitLab-Local-Education/0.4")
                .GET().build();
            HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200)throw new IllegalStateException("Horizons HTTP "+response.statusCode());
            JsonNode root=json.readTree(response.body());
            String version=root.path("signature").path("version").asText("");
            if(!version.startsWith("1."))throw new IllegalStateException("Unknown Horizons API version "+version);
            String raw=root.path("result").asText("");
            List<Vector> vectors=parseCsvVectors(raw);
            if(vectors.size()<3)throw new IllegalStateException("Horizons returned fewer than 3 position samples for "+id);
            return new Body(id,horizonsId,vectors.subList(0,3));
        }catch(Exception ex){throw new IllegalStateException("Horizons query failed for "+id+": "+ex.getMessage(),ex);}
    }

    /** The Horizons JSON result wraps a legacy CSV table marked $$SOE and $$EOE. */
    static List<Vector> parseCsvVectors(String raw){
        int start=raw.indexOf("$$SOE"),end=raw.indexOf("$$EOE");
        if(start<0||end<=start)throw new IllegalArgumentException("Missing Horizons ephemeris markers");
        List<Vector> vectors=new ArrayList<>();
        for(String row:raw.substring(start+5,end).split("\\R")){
            if(row.isBlank())continue;
            String[] fields=row.split(",");
            if(fields.length<5)throw new IllegalArgumentException("Unexpected Horizons vector row");
            vectors.add(new Vector(Double.parseDouble(fields[2].trim()),
                Double.parseDouble(fields[3].trim()),Double.parseDouble(fields[4].trim())));
        }
        return vectors;
    }
}
