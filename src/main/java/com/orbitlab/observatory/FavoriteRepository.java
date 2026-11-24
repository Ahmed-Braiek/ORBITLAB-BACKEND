package com.orbitlab.observatory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface FavoriteRepository extends JpaRepository<FavoriteObject,Long>{
 Optional<FavoriteObject> findByObjectTypeAndObjectId(String objectType,String objectId);
}
