package com.youssef.reportworkflow.domain;

import com.youssef.reportworkflow.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

/**
 * @author Jozef
 */
@Entity
@Table(name = "report_transitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportTransitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportState newState;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public ReportTransitionLog(Report report, User performedBy, ReportState newState) {
        this.report = report;
        this.performedBy = performedBy;
        this.newState = newState;
    }
}

