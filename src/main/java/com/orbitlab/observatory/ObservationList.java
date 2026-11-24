package com.orbitlab.observatory;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="observation_lists")
public class ObservationList {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String name;
 @Column(length=600) private String description;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected ObservationList(){}
 public ObservationList(String name,String description){this.name=name;this.description=description;}
 public Long getId(){return id;}public String getName(){return name;}public String getDescription(){return description;}public Instant getCreatedAt(){return createdAt;}
 public void setName(String name){this.name=name;}public void setDescription(String description){this.description=description;}
}
