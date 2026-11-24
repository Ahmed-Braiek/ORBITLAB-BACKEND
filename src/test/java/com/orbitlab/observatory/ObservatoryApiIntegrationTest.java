package com.orbitlab.observatory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ObservatoryApiIntegrationTest {
 @Autowired MockMvc mvc;

 @Test void favoriteCanBeCreatedListedAndDeleted() throws Exception {
  String json=mvc.perform(post("/api/observatory/favorites")
    .contentType(MediaType.APPLICATION_JSON)
    .content("{\"objectType\":\"planet\",\"objectId\":\"earth\",\"displayName\":\"Earth\"}"))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.objectId").value("earth"))
    .andReturn().getResponse().getContentAsString();
  long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(json).path("id").asLong();
  mvc.perform(get("/api/observatory/favorites")).andExpect(status().isOk())
    .andExpect(jsonPath("$[0].objectId").value("earth"));
  mvc.perform(delete("/api/observatory/favorites/"+id)).andExpect(status().isNoContent());
  mvc.perform(delete("/api/observatory/favorites/"+id)).andExpect(status().isNotFound());
 }

 @Test void observationListAndEntriesSupportCrud() throws Exception {
  String created=mvc.perform(post("/api/observatory/lists")
    .contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"Night Sky\",\"description\":\"SOA demo\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Night Sky"))
    .andReturn().getResponse().getContentAsString();
  long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(created).path("id").asLong();
  mvc.perform(put("/api/observatory/lists/"+id).contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"Observations\",\"description\":\"My targets\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Observations"));
  String entry=mvc.perform(post("/api/observatory/lists/"+id+"/entries").contentType(MediaType.APPLICATION_JSON)
    .content("{\"objectType\":\"planet\",\"objectId\":\"mars\",\"displayName\":\"Mars\",\"note\":\"Observe\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.displayName").value("Mars"))
    .andReturn().getResponse().getContentAsString();
  long entryId=new com.fasterxml.jackson.databind.ObjectMapper().readTree(entry).path("id").asLong();
  mvc.perform(get("/api/observatory/lists/"+id+"/entries")).andExpect(status().isOk())
    .andExpect(jsonPath("$[0].objectId").value("mars"));
  mvc.perform(delete("/api/observatory/lists/"+id+"/entries/"+entryId)).andExpect(status().isNoContent());
  mvc.perform(delete("/api/observatory/lists/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/observatory/lists/"+id+"/entries")).andExpect(status().isNotFound());
 }

 @Test void rejectsInvalidObjectTypeAndBlankNames() throws Exception {
  mvc.perform(post("/api/observatory/favorites").contentType(MediaType.APPLICATION_JSON)
    .content("{\"objectType\":\"starship\",\"objectId\":\"x\",\"displayName\":\"X\"}"))
    .andExpect(status().isBadRequest());
  mvc.perform(post("/api/observatory/lists").contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"   \",\"description\":\"invalid\"}"))
    .andExpect(status().isBadRequest());
 }
}
