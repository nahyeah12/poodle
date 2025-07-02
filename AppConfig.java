// config/AppConfig.java
package com.ppi.utility.importer.config;

import com.ppi.utility.importer.MainController;
import com.ppi.utility.importer.repository.CaseMasterRepository;
import com.ppi.utility.importer.service.ExcelProcessingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Removed: org.springframework.core.env.Environment;
// Removed: org.springframework.jdbc.core.JdbcTemplate;
// Removed: org.springframework.jdbc.datasource.DriverManagerDataSource;

// Removed: import javax.sql.DataSource;
// Removed: import java.util.Objects;

/**
 * Spring configuration class for the application.
 * Defines beans that should be managed by the Spring container.
 */
@Configuration
public class AppConfig {

    // Removed: private final Environment env;
    // Removed: public AppConfig(Environment env) { this.env = env; }

    // Removed: DataSource and JdbcTemplate beans, as Spring Boot JPA starter will auto-configure these.
    // @Bean
    // public DataSource oracleDataSource() { ... }
    // @Bean
    // public JdbcTemplate jdbcTemplate(DataSource dataSource) { ... }

    /**
     * Defines a Spring bean for the MainController.
     * This allows Spring to manage the lifecycle and dependencies of the MainController,
     * enabling dependency injection into it.
     *
     * @param excelProcessingService The ExcelProcessingService to be injected.
     * @return An instance of MainController.
     */
    @Bean
    public MainController mainController(ExcelProcessingService excelProcessingService) {
        return new MainController(excelProcessingService);
    }

    /**
     * Defines a Spring bean for the ExcelProcessingService.
     *
     * @param caseMasterRepository The CaseMasterRepository (JPA interface) to be injected.
     * @return An instance of ExcelProcessingService.
     */
    @Bean
    public ExcelProcessingService excelProcessingService(CaseMasterRepository caseMasterRepository) {
        return new ExcelProcessingService(caseMasterRepository);
    }

    // Removed: CaseMasterRepository bean, as Spring Data JPA automatically provides implementation for interfaces extending JpaRepository
    // @Bean
    // public CaseMasterRepository caseMasterRepository(JdbcTemplate jdbcTemplate) { ... }
}
