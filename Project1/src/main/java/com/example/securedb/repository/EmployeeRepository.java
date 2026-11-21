package com.example.securedb.repository;

import com.example.securedb.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository. Derived finder methods are translated into
 * parameterized prepared statements, so untrusted input can never break
 * out of its value position and inject SQL into the query.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Parameterized under the hood: safe against SQL injection by design.
    List<Employee> findByDepartment(String department);
}
