package com.reliaquest.api.controller;


import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.entity.EmployeeByIdResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Controller", description = "APIs for Managing Employees")
public class EmployeeController implements IEmployeeController {

    @Autowired
    EmployeeService employeeService;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Operation(summary = "Get all employees", description = "Retrieve a list of all employees")
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() throws EmployeeServiceException {
        logger.debug("Request to get all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        logger.info("Retrieved all employees successfully");
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Search employees by name", description = "Retrieve a list of employees matching the provided name")
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@RequestParam String name) throws EmployeeServiceException {
        logger.debug("Searching employees by name: {}", name);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(name);
        logger.info("Employees search by name '{}' found {} results", name, employees.size());
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get employee by ID", description = "Retrieve employee details using their ID")
    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) throws EmployeeServiceException {
        logger.debug("Request to get employee by ID: {}", id);
        EmployeeByIdResponse employee = employeeService.getEmployeeById(id);
        logger.info("Employee with ID: {} retrieved successfully", id);
        return ResponseEntity.ok(employee.getData());
    }

    @Operation(summary = "Get highest salary", description = "Retrieve the highest salary among all employees")
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() throws EmployeeServiceException {
        logger.debug("Request to get highest salary among all employees");
        int highestSalary = employeeService.getHighestSalaryOfEmployees();
        logger.info("The highest salary {} retrieved successfully", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Operation(summary = "Get top 10 highest earning employees", description = "Retrieve the names of top 10 employees with highest earnings")
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() throws EmployeeServiceException {
        logger.debug("Request to get top 10 highest earning employees");
        List<Employee> employees = employeeService.getTopHighestEarningEmployees(10);
        List<String> employeeNames = employees.stream()
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());
        logger.info("Top 10 highest earning employees retrieved successfully");
        return ResponseEntity.ok(employeeNames);
    }

    @Operation(summary = "Create a new employee", description = "Create a new employee with provided details")
    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) throws EmployeeServiceException {
        logger.debug("Request to create new employee: {}", employee);
        EmployeeByIdResponse savedEmployee = employeeService.createEmployee(employee);
        logger.info("Employee created successfully: {}", savedEmployee.getData().getId());
        return new ResponseEntity<>(savedEmployee.getData(), HttpStatus.CREATED);
    }

    @Operation(summary = "Delete employee by ID", description = "Delete an employee based on their ID")
    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) throws EmployeeServiceException {
        logger.debug("Request to delete employee by ID: {}", id);
        employeeService.deleteEmployee(id);
        logger.info("Employee with ID: {} deleted successfully", id);
        return ResponseEntity.ok("Employee with id " + id + " got deleted successfully");
    }
}