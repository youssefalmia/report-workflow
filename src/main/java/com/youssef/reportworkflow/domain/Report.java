package com.youssef.reportworkflow.domain;

import com.youssef.reportworkflow.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

/**
 * @author Jozef
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportState state = ReportState.CREATED;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer; // Nullable until assigned

    @ManyToOne
    @JoinColumn(name = "validator_id")
    private User validator; // Nullable until assigned

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportTransitionLog> transitionLogs = new ArrayList<>();
}

