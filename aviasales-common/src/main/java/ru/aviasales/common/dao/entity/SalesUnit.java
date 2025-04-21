package ru.aviasales.common.dao.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sales_units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Для реализации оптимистичной блокировки
    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_category_id", nullable = false)
    private SalesCategory category;

    @Column(name = "custom_commission_percent")
    private Double customCommissionPercent;

    @Column(name = "is_custom_commission", nullable = false)
    private Boolean isCustomCommission = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Double getActualCommission() {
        return isCustomCommission ? customCommissionPercent : category.getDefaultCommissionPercent();
    }
}
