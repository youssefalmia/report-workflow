package com.youssef.reportworkflow.domain;

import org.springframework.data.jpa.repository.*;

/**
 * @author Jozef
 */
public interface ReportTransitionLogRepository extends JpaRepository<ReportTransitionLog, Long> {
}
