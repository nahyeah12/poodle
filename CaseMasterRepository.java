// repository/CaseMasterRepository.java
package com.ppi.utility.importer.repository;

import com.ppi.utility.importer.model.CaseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for interacting with the CASE_MASTER_TBL using Spring Data JPA.
 * Extends JpaRepository to get standard CRUD operations automatically.
 * The first type parameter is the Entity type, the second is the type of its Primary Key.
 */
@Repository
public interface CaseMasterRepository extends JpaRepository<CaseMaster, String> {
    // Spring Data JPA will automatically provide implementations for standard CRUD operations
    // e.g., save(), findById(), findAll(), delete() etc.
}
