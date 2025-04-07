package ru.aviasales.admin.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "advertisements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ad_type_id", nullable = false)
    private AdType adType;

    @Column(nullable = false)
    private String title;

    @Column(name = "company_name")
    private String companyName;

    @Column(nullable = false)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "advertisement_user_segments",
            joinColumns = @JoinColumn(name = "advertisement_id"),
            inverseJoinColumns = @JoinColumn(name = "user_segment_id")
    )
    private Set<UserSegment> targetSegments;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}