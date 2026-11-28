package com.orbitlab.observatory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/observatory")
@CrossOrigin(origins="${ORBITLAB_CORS_ORIGINS:http://localhost:4200}")
public class ObservatoryController {
 private final FavoriteRepository favorites;
 private final ObservationListRepository lists;
 private final ObservationEntryRepository entries;
 public ObservatoryController(FavoriteRepository favorites,ObservationListRepository lists,ObservationEntryRepository entries){this.favorites=favorites;this.lists=lists;this.entries=entries;}
 public record ObjectInput(@NotBlank @Pattern(regexp="planet|asteroid") String objectType,@NotBlank @Size(max=120) String objectId,@NotBlank @Size(max=140) String displayName){}
 public record ListInput(@NotBlank @Size(max=100) String name,@Size(max=600) String description){}
 public record EntryInput(@NotBlank @Pattern(regexp="planet|asteroid") String objectType,@NotBlank @Size(max=120) String objectId,@NotBlank @Size(max=140) String displayName,@Size(max=600) String note){}
 public record FavoriteDto(Long id,String objectType,String objectId,String displayName,Instant savedAt){}
 public record ListDto(Long id,String name,String description,Instant createdAt){}
 public record EntryDto(Long id,String objectType,String objectId,String displayName,String note,Instant addedAt){}
 private static FavoriteDto dto(FavoriteObject f){return new FavoriteDto(f.getId(),f.getObjectType(),f.getObjectId(),f.getDisplayName(),f.getSavedAt());}
 private static ListDto dto(ObservationList l){return new ListDto(l.getId(),l.getName(),l.getDescription(),l.getCreatedAt());}
 private static EntryDto dto(ObservationEntry e){return new EntryDto(e.getId(),e.getObjectType(),e.getObjectId(),e.getDisplayName(),e.getNote(),e.getAddedAt());}
 private ObservationList requireList(Long id){return lists.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Observation list not found"));}
 @GetMapping("/favorites")
 public List<FavoriteDto> getFavorites(){return favorites.findAll().stream().map(ObservatoryController::dto).toList();}
 @PostMapping("/favorites")
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public FavoriteDto addFavorite(@Valid @RequestBody ObjectInput input){
  String type=input.objectType().toLowerCase(Locale.ROOT);String id=input.objectId().trim().toLowerCase(Locale.ROOT);
  if(favorites.findByObjectTypeAndObjectId(type,id).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Already in favorites");
  return dto(favorites.save(new FavoriteObject(type,id,input.displayName().trim())));
 }
 @DeleteMapping("/favorites/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void deleteFavorite(@PathVariable Long id){
  if(!favorites.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Favorite not found");
  favorites.deleteById(id);
 }
 @GetMapping("/lists")
 public List<ListDto> getLists(){return lists.findAll().stream().map(ObservatoryController::dto).toList();}
 @PostMapping("/lists") @ResponseStatus(HttpStatus.CREATED)
 public ListDto createList(@Valid @RequestBody ListInput input){return dto(lists.save(new ObservationList(input.name().trim(),input.description())));}
 @PutMapping("/lists/{id}")
 public ListDto updateList(@PathVariable Long id,@Valid @RequestBody ListInput input){ObservationList list=requireList(id);list.setName(input.name().trim());list.setDescription(input.description());return dto(lists.save(list));}
 @DeleteMapping("/lists/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
 public void deleteList(@PathVariable Long id){requireList(id);entries.deleteByListId(id);lists.deleteById(id);}
 @GetMapping("/lists/{id}/entries")
 public List<EntryDto> getEntries(@PathVariable Long id){requireList(id);return entries.findByListIdOrderByAddedAtDesc(id).stream().map(ObservatoryController::dto).toList();}
 @PostMapping("/lists/{id}/entries") @ResponseStatus(HttpStatus.CREATED)
 public EntryDto addEntry(@PathVariable Long id,@Valid @RequestBody EntryInput input){
  ObservationList list=requireList(id);String objectId=input.objectId().trim().toLowerCase(Locale.ROOT);
  if(entries.findByListIdAndObjectTypeAndObjectId(id,input.objectType(),objectId).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Object already in list");
  return dto(entries.save(new ObservationEntry(list,input.objectType(),objectId,input.displayName().trim(),input.note())));
 }
 @DeleteMapping("/lists/{listId}/entries/{entryId}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void deleteEntry(@PathVariable Long listId,@PathVariable Long entryId){
  ObservationEntry entry=entries.findByListIdAndId(listId,entryId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Entry not found"));
  entries.delete(entry);
 }
}
