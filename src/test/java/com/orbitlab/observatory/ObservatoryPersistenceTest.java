package com.orbitlab.observatory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
@DataJpaTest
class ObservatoryPersistenceTest {
 @Autowired FavoriteRepository favorites;
 @Autowired ObservationListRepository lists;
 @Autowired ObservationEntryRepository entries;
 @Test void savesFavorite(){
  FavoriteObject saved=favorites.save(new FavoriteObject("planet","earth","Earth"));
  assertThat(favorites.findByObjectTypeAndObjectId("planet","earth")).isPresent();
  assertThat(saved.getId()).isNotNull();
 }
 @Test void savesListAndEntry(){
  ObservationList list=lists.save(new ObservationList("My night sky","A local demonstration list"));
  entries.save(new ObservationEntry(list,"planet","mars","Mars","Observe next week"));
  assertThat(entries.findByListIdOrderByAddedAtDesc(list.getId())).hasSize(1);
 }
}
