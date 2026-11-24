package com.orbitlab.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class EphemerisControllerTest {
 @Test void parsesVectorCsv(){
   String raw="header\n$$SOE\n2461000.5, A.D. 2026-Nov-24 00:00:00.0000, 1.0E+00, -2.5E-01, 5.0E-03, 0, 0, 0,\n$$EOE\n";
   List<EphemerisController.Vector> result=EphemerisController.parseCsvVectors(raw);
   assertEquals(1,result.size());assertEquals(1.0,result.get(0).xAu());
   assertEquals(-.25,result.get(0).yAu());assertEquals(.005,result.get(0).zAu());
 }
 @Test void rejectsMissingMarkers(){
   assertThrows(IllegalArgumentException.class,()->EphemerisController.parseCsvVectors("No ephemeris"));
 }
}
