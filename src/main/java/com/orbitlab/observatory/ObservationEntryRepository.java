package com.orbitlab.observatory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ObservationEntryRepository extends JpaRepository<ObservationEntry,Long>{
 List<ObservationEntry> findByListIdOrderByAddedAtDesc(Long listId);
 Optional<ObservationEntry> findByListIdAndId(Long listId,Long id);
 Optional<ObservationEntry> findByListIdAndObjectTypeAndObjectId(Long listId,String objectType,String objectId);
 long deleteByListId(Long listId);
}
