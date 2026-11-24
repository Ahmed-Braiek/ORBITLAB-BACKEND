package com.orbitlab.observatory;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="observation_entries",uniqueConstraints=@UniqueConstraint(name="uk_list_object",columnNames={"list_id","object_type","object_id"}))
public class ObservationEntry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="list_id",nullable=false) private ObservationList list;
 @Column(name="object_type",nullable=false,length=24) private String objectType;
 @Column(name="object_id",nullable=false,length=120) private String objectId;
 @Column(nullable=false,length=140) private String displayName;
 @Column(length=600) private String note;
 @Column(nullable=false,updatable=false) private Instant addedAt=Instant.now();
 protected ObservationEntry(){}
 public ObservationEntry(ObservationList list,String type,String objectId,String displayName,String note){this.list=list;this.objectType=type;this.objectId=objectId;this.displayName=displayName;this.note=note;}
 public Long getId(){return id;}public String getObjectType(){return objectType;}public String getObjectId(){return objectId;}public String getDisplayName(){return displayName;}public String getNote(){return note;}public Instant getAddedAt(){return addedAt;}
}
