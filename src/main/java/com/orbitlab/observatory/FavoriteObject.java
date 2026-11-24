package com.orbitlab.observatory;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="favorite_objects",uniqueConstraints=@UniqueConstraint(name="uk_favorite_object",columnNames={"object_type","object_id"}))
public class FavoriteObject {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="object_type",nullable=false,length=24) private String objectType;
 @Column(name="object_id",nullable=false,length=120) private String objectId;
 @Column(nullable=false,length=140) private String displayName;
 @Column(nullable=false,updatable=false) private Instant savedAt=Instant.now();
 protected FavoriteObject(){}
 public FavoriteObject(String objectType,String objectId,String displayName){this.objectType=objectType;this.objectId=objectId;this.displayName=displayName;}
 public Long getId(){return id;} public String getObjectType(){return objectType;}
 public String getObjectId(){return objectId;} public String getDisplayName(){return displayName;} public Instant getSavedAt(){return savedAt;}
}
