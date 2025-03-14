package com.youssef.reportworkflow.unit.repository;

import com.youssef.reportworkflow.domain.*;
import jakarta.inject.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;

/**
 * @author Jozef
 */
@DataJpaTest
public class ReportRepositoryTest {
    @Inject
    ReportRepository reportRepository;

}
