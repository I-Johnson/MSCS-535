package com.example.securedb.controller;

import com.example.securedb.model.Employee;
import com.example.securedb.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for secure database access. Served over HTTPS only.
 * Read endpoints require USER or ADMIN; write endpoints require ADMIN
 * (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // GET https://localhost:8443/api/employees  (USER or ADMIN)
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // GET https://localhost:8443/api/employees/1  (USER or ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET https://localhost:8443/api/employees/department/IT  (USER or ADMIN)
    @GetMapping("/department/{department}")
    public List<Employee> getEmployeesByDepartment(@PathVariable String department) {
        return employeeRepository.findByDepartment(department);
    }

    // POST https://localhost:8443/api/employees  (ADMIN only)
    // Body (JSON): {"name":"Jane Doe","email":"jane@example.com","department":"IT"}
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee saved = employeeRepository.save(employee);
        return ResponseEntity
                .created(URI.create("/api/employees/" + saved.getId()))
                .body(saved);
    }

    // DELETE https://localhost:8443/api/employees/1  (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
