package ru.aviasales.admin.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ad_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "supports_segmentation", nullable = false)
    private Boolean supportsSegmentation;

    @Column(nullable = false)
    private Boolean active;
}