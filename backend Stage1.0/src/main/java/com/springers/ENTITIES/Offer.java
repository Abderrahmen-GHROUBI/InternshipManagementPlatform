package com.springers.ENTITIES;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
@Builder
@Table(name = "offer")
public class Offer {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization")
    private Specialization specialization;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;
    
    private String nomSociete;
    
    private int NbPlaces;
    
    private Boolean isActive;
    
    private String Localisation;
    
    private String mailRH;
    
    @ManyToOne
    @JoinColumn(name = "admin_id")
    @JsonIgnoreProperties("offers")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Admin adminoffer;
    
    
    @OneToMany(mappedBy = "offerApplication",fetch = FetchType.EAGER)
    @JsonIgnoreProperties("offerApplication")
    private Set<OfferApplication> offerApplications = new HashSet<OfferApplication>();

}
