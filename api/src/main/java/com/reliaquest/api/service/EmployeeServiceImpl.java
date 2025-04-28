package com.reliaquest.api.service;

import com.reliaquest.api.dto.DeleteResponse;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.entity.EmployeeByIdResponse;
import com.reliaquest.api.entity.EmployeeResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String BASE_URL = "http://localhost:8112/api/v1";
    private static final String EMPLOYEE_URL = BASE_URL + "/employee";
    private static final String EMPLOYEE_BY_ID_URL = BASE_URL + "/employee/";

    private final RestTemplate restTemplate;

    public EmployeeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Employee> getAllEmployees() throws EmployeeServiceException {
        try {
            EmployeeResponse response = restTemplate.getForObject(EMPLOYEE_URL, EmployeeResponse.class);

            if (response == null || response.getData() == null) {
                throw new EmployeeServiceException("No employees found in response");
            }
            return response.getData();
        } catch (RestClientException e) {
            throw handleRestClientException("Failed to fetch employees from external API", e);
        }
    }

    @Override
    public EmployeeByIdResponse getEmployeeById(String id) throws EmployeeServiceException {
        validateId(id);

        try {
            EmployeeByIdResponse response = restTemplate.getForObject(EMPLOYEE_BY_ID_URL + id, EmployeeByIdResponse.class);

            if (response == null || response.getData() == null) {
                throw new EmployeeServiceException("Employee not found with ID: " + id);
            }
            return response;
        } catch (RestClientException e) {
            throw handleRestClientException("Error while fetching employee with ID: " + id, e);
        }
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String name) throws EmployeeServiceException {
        if (name == null || name.trim().isEmpty()) {
            throw new EmployeeServiceException("Name parameter cannot be empty");
        }

        try {
            List<Employee> employees = getAllEmployees();
            return employees.stream()
                    .filter(employee -> employee.getEmployeeName() != null &&
                            employee.getEmployeeName().equalsIgnoreCase(name.trim()))
                    .collect(Collectors.toList());
        } catch (EmployeeServiceException e) {
            throw e;
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
    public EmployeeByIdResponse createEmployee(EmployeeCreateRequest employee) throws EmployeeServiceException {
        try {
            HttpEntity<EmployeeCreateRequest> requestEntity = new HttpEntity<>(employee, getJsonHeaders());
            ResponseEntity<EmployeeByIdResponse> response = restTemplate.exchange(
                    EMPLOYEE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    EmployeeByIdResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().getData() == null) {
                throw new EmployeeServiceException("Create operation failed with status: " + response.getStatusCode());
            }

            if (!"Successfully processed request.".equalsIgnoreCase(response.getBody().getStatus())) {
                throw new EmployeeServiceException("Create operation failed: " + response.getBody().getStatus());
            }

            return response.getBody();
        } catch (Exception e) {
            throw handleRestClientException("Error creating employee", e);
        }
    }

    @Override
    public void deleteEmployee(String name) throws EmployeeServiceException {
        validateId(name);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, getJsonHeaders());

            ResponseEntity<DeleteResponse> response = restTemplate.exchange(
                    EMPLOYEE_URL,
                    HttpMethod.DELETE,
                    requestEntity,
                    DeleteResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().isData()) {
                throw new EmployeeServiceException("Delete operation failed");
            }
        } catch (Exception e) {
            throw handleRestClientException("Error deleting employee", e);
        }
    }


    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void validateId(String id) throws EmployeeServiceException {
        if (id == null || id.trim().isEmpty()) {
            throw new EmployeeServiceException("Employee ID cannot be empty");
        }
    }

    private EmployeeServiceException handleRestClientException(String message, Exception e) {
        if (e instanceof HttpClientErrorException.BadRequest) {
            return new EmployeeServiceException("Bad request: " + e.getMessage(), e);
        } else if (e instanceof HttpClientErrorException.NotFound) {
            return new EmployeeServiceException("Resource not found: " + e.getMessage(), e);
        } else if (e instanceof HttpServerErrorException) {
            return new EmployeeServiceException("Server error: " + e.getMessage(), e);
        } else if (e instanceof RestClientException) {
            return new EmployeeServiceException(message, e);
        } else {
            return new EmployeeServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }
}
