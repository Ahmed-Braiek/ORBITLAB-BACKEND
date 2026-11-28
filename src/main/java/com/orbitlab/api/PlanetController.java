package com.orbitlab.api;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") @CrossOrigin(origins="${ORBITLAB_CORS_ORIGINS:http://localhost:4200}") public class PlanetController {
 public record Planet(String id,String name,double radiusKm,double meanDistanceAu,double orbitalPeriodDays,String description) {}
 private static final List<Planet> PLANETS=List.of(
 new Planet("mercury","Mercury",2439.7,.387,87.97,"The smallest planet in the Solar System."),
 new Planet("venus","Venus",6051.8,.723,224.70,"A rocky planet with a dense atmosphere."),
 new Planet("earth","Earth",6371,1,365.25,"Our home world."),
 new Planet("mars","Mars",3389.5,1.524,686.98,"The red planet."),
 new Planet("jupiter","Jupiter",69911,5.203,4332.59,"The Solar System's largest planet."),
 new Planet("saturn","Saturn",58232,9.537,10759.22,"A giant planet surrounded by rings."),
 new Planet("uranus","Uranus",25362,19.19,30688.5,"An ice giant."),
 new Planet("neptune","Neptune",24622,30.07,60182,"A distant ice giant.") );
 @GetMapping("/planets") public List<Planet> planets(){return PLANETS;}
 @GetMapping("/planets/{id}") public Planet planet(@PathVariable String id){return PLANETS.stream().filter(p->p.id().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Planet not found"));}
 @GetMapping("/health") public java.util.Map<String,String> health(){return java.util.Map.of("status","UP","phase","1","mode","local");}
}
