package com.orbitlab.api;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Force a credential-free provider regardless of the developer's real shell environment.
@SpringBootTest(properties={"ASTRA_AI_PROVIDER=groq","GROQ_API_KEY=","GEMINI_API_KEY=","GEMINI_API_KEY_1=","GEMINI_API_KEY_2=","GEMINI_API_KEY_3="})
@AutoConfigureMockMvc
class SpaceGuideControllerTest {
 @Autowired MockMvc mvc;
 @Test void reportsUnconfiguredWithoutAnApiKey() throws Exception {
  mvc.perform(get("/api/space-guide/status")).andExpect(status().isOk())
     .andExpect(jsonPath("$.configured").value(false));
 }
 @Test void neverCallsAiProviderWithoutKey() throws Exception {
  mvc.perform(post("/api/space-guide/chat").contentType(MediaType.APPLICATION_JSON)
    .content("{\"message\":\"Why is Mars red?\",\"history\":[],\"selectedPlanet\":\"mars\"}"))
    .andExpect(status().isServiceUnavailable());
 }
 @Test void validatesEmptyPrompt() throws Exception {
  mvc.perform(post("/api/space-guide/chat").contentType(MediaType.APPLICATION_JSON)
    .content("{\"message\":\"\",\"history\":[]}"))
    .andExpect(status().isBadRequest());
 }
 @Test void routesOnlyUnambiguousPlanetMentions() {
  assertEquals("mars",SpaceGuideController.findNavigationTarget("Why is Mars red?"));
  assertNull(SpaceGuideController.findNavigationTarget("Compare Mars with Venus"));
  assertNull(SpaceGuideController.findNavigationTarget("Tell me about galaxies"));
 }
}
