package com.youssef.reportworkflow.mapper;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.dto.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Component
public class ReportMapper {
    public ReportDTO toDTO(Report report) {
        return new ReportDTO(
                report.getId(),
                report.getTitle(),
                report.getOwner().getUsername(),
                report.getState()
        );
    }

    public List<ReportDTO> toDTOList(List<Report> reports) {
        return reports.stream()
                .map(this::toDTO)
                .toList();
    }
}
