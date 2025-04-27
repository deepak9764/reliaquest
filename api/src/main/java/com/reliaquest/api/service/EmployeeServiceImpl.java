package com.reliaquest.api.service;


import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.entity.EmployeeByIdResponse;
import com.reliaquest.api.entity.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String BASE_URL = "https://dummy.restapiexample.com/api/v1";

    private final RestTemplate restTemplate;

    public EmployeeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Employee> getAllEmployees() throws EmployeeServiceException {
        try {
            String url = BASE_URL + "/employees";
            EmployeeResponse response = restTemplate.getForObject(url, EmployeeResponse.class);

            if (response == null || response.getData() == null) {
                throw new EmployeeServiceException("No employees found in response");
            }
            return response.getData();
        } catch (RestClientException e) {
            throw new EmployeeServiceException("Failed to fetch employees from external API", e);
        }
    }


    @Override
    public EmployeeByIdResponse getEmployeeById(String id) throws EmployeeServiceException {
        if (id == null || id.trim().isEmpty()) {
            throw new EmployeeServiceException("Employee ID cannot be null or empty");
        }

        try {
            String url = BASE_URL + "/employee/" + id;
            EmployeeByIdResponse response = restTemplate.getForObject(url, EmployeeByIdResponse.class);

            if (response == null || response.getData() == null) {
                throw new EmployeeServiceException("Employee not found with ID: " + id);
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            throw new EmployeeServiceException("Employee not found with ID: " + id, e);
        } catch (RestClientException e) {
            throw new EmployeeServiceException("Error while fetching employee with ID: " + id, e);
        }
    }

   @Override
    public List<Employee> getEmployeesByNameSearch(String name) throws EmployeeServiceException {
        if (name == null || name.trim().isEmpty()) {
            throw new EmployeeServiceException("Name parameter cannot be empty");
        }

        try {
            List<Employee> employees = getAllEmployees(); // getAllEmployees already has its own exception handling
            return employees.stream()
                    .filter(employee -> employee.getEmployeeName() != null &&
                            employee.getEmployeeName().equalsIgnoreCase(name.trim()))
                    .collect(Collectors.toList());
        } catch (EmployeeServiceException e) {
            throw e; // Re-throw existing EmployeeServiceException
        } catch (Exception e) {
            throw new EmployeeServiceException("Error searching employees by name: " + name, e);
        }
    }

    @Override
    public int getHighestSalaryOfEmployees() throws EmployeeServiceException {
        try {
            List<Employee> employeeList = getTopHighestEarningEmployees(1);
            if (employeeList.isEmpty()) {
                throw new EmployeeServiceException("No employees available to determine highest salary");
            }
            return employeeList.get(0).getEmployeeSalary();
        } catch (EmployeeServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new EmployeeServiceException("Error determining highest salary", e);
        }
    }

    @Override
    public List<Employee> getTopHighestEarningEmployees(int size) throws EmployeeServiceException {
        if (size <= 0) {
            throw new EmployeeServiceException("Size must be a positive number");
        }

        try {
            List<Employee> employeeList = getAllEmployees();
            if (employeeList.isEmpty()) {
                throw new EmployeeServiceException("No employees available");
            }

            return employeeList.stream()
                    .sorted(Comparator.comparingInt(Employee::getEmployeeSalary).reversed())
                    .limit(size)
                    .collect(Collectors.toList());
        } catch (EmployeeServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new EmployeeServiceException("Error fetching top earning employees", e);
        }
    }

    @Override
    public EmployeeByIdResponse createEmployee(Employee employee) throws EmployeeServiceException {
        if (employee == null) {
            throw new EmployeeServiceException("Employee data cannot be null");
        }
        if (employee.getEmployeeName() == null || employee.getEmployeeName().trim().isEmpty()) {
            throw new EmployeeServiceException("Employee name cannot be empty");
        }
        if (employee.getEmployeeSalary() < 0) {
            throw new EmployeeServiceException("Employee salary cannot be negative");
        }

        try {
            String url = BASE_URL + "/create";
            EmployeeByIdResponse response = restTemplate.postForObject(url, employee, EmployeeByIdResponse.class);

            if (response == null || response.getData() == null) {
                throw new EmployeeServiceException("Failed to create employee - no response data");
            }
            return response;
        } catch (HttpClientErrorException e) {
            throw new EmployeeServiceException("Invalid employee data: " + e.getStatusText(), e);
        } catch (HttpServerErrorException e) {
            throw new EmployeeServiceException("Employee service unavailable", e);
        } catch (Exception e) {
            throw new EmployeeServiceException("Unexpected error while creating employee", e);
        }
    }

    @Override
    public void deleteEmployee(String id) throws EmployeeServiceException {
        if (id == null || id.trim().isEmpty()) {
            throw new EmployeeServiceException("Employee ID cannot be empty");
        }

        try {
            String url = BASE_URL + "/delete/" + id;
            restTemplate.delete(url);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EmployeeServiceException("Employee not found with ID: " + id, e);
        } catch (HttpClientErrorException e) {
            throw new EmployeeServiceException("Invalid request for employee ID: " + id, e);
        } catch (HttpServerErrorException e) {
            throw new EmployeeServiceException("Employee service unavailable", e);
        } catch (Exception e) {
            throw new EmployeeServiceException("Unexpected error while deleting employee", e);
        }
    }
}

