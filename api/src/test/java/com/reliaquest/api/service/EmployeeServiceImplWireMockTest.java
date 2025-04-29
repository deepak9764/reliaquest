package com.reliaquest.api.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.entity.EmployeeByIdResponse;
import com.reliaquest.api.exception.EmployeeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WireMockTest(httpPort = 8112)
class EmployeeServiceImplWireMockTest {

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        employeeService = new EmployeeServiceImpl(restTemplate);
    }

    @Test
    void testGetAllEmployees() throws EmployeeServiceException {
        String response = """
        {
          "data": [
            {
              "id": "1",
              "employee_name": "Tiger Nixon",
              "employee_salary": 320800,
              "employee_age": 61,
              "employee_title": "Vice Chair Executive",
              "employee_email": "tnixon@company.com"
            }
          ],
          "status": "Successfully processed request."
        }
        """;

        WireMock.stubFor(WireMock.get("/api/v1/employee")
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));

        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(1, employees.size());
        assertEquals("Tiger Nixon", employees.get(0).getEmployeeName());
    }

    @Test
    void testGetEmployeeById() throws EmployeeServiceException {
        String id = "123";
        String response = """
        {
          "data": {
            "id": "123",
            "employee_name": "Bill Bob",
            "employee_salary": 89750,
            "employee_age": 24,
            "employee_title": "Documentation Engineer",
            "employee_email": "billBob@company.com"
          },
          "status": "Successfully processed request."
        }
        """;

        WireMock.stubFor(WireMock.get("/api/v1/employee/" + id)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));

        EmployeeByIdResponse employee = employeeService.getEmployeeById(id);
        assertEquals("Bill Bob", employee.getData().getEmployeeName());
    }

    @Test
    void testCreateEmployee() throws EmployeeServiceException {
        String requestBody = """
        {
          "name": "Jill Jenkins",
          "salary": 139082,
          "age": 48,
          "title": "Financial Advisor"
        }
        """;

        String responseBody = """
        {
          "data": {
            "id": "abc-123",
            "employee_name": "Jill Jenkins",
            "employee_salary": 139082,
            "employee_age": 48,
            "employee_title": "Financial Advisor",
            "employee_email": "jillj@company.com"
          },
          "status": "Successfully processed request."
        }
        """;

        WireMock.stubFor(WireMock.post("/api/v1/employee")
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)));

        EmployeeCreateRequest req = new EmployeeCreateRequest("Jill Jenkins", 139082, 48, "Financial Advisor");
        EmployeeByIdResponse result = employeeService.createEmployee(req);

        assertNotNull(result.getData());
        assertEquals("Jill Jenkins", result.getData().getEmployeeName());
    }

    @Test
    void testDeleteEmployee() throws EmployeeServiceException {
        String name = "Jill Jenkins";

        WireMock.stubFor(WireMock.request("DELETE", WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(WireMock.equalToJson("""
                {
                  "name": "Jill Jenkins"
                }
            """))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                        {
                          "data": true,
                          "status": "Successfully processed request."
                        }
                    """)));

        employeeService.deleteEmployee(name);
        WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(WireMock.equalToJson("""
                {
                  "name": "Jill Jenkins"
                }
            """)).withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE)));


    }

    @Test
    void testGetAllEmployees_whenServerError_shouldThrowException() {
        WireMock.stubFor(WireMock.get("/api/v1/employee")
                .willReturn(WireMock.aResponse().withStatus(500)));

        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void testGetEmployeeById_whenNotFound_shouldThrowException() {
        String invalidId = "invalid-id";

        WireMock.stubFor(WireMock.get("/api/v1/employee/" + invalidId)
                .willReturn(WireMock.aResponse().withStatus(404)));

        assertThrows(EmployeeServiceException.class, () -> employeeService.getEmployeeById(invalidId));
    }

    @Test
    void testCreateEmployee_whenBadRequest_shouldThrowException() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName(""); // Invalid name

        WireMock.stubFor(WireMock.post("/api/v1/employee")
                .willReturn(WireMock.aResponse().withStatus(400)));

        assertThrows(EmployeeServiceException.class, () -> employeeService.createEmployee(request));
    }

    @Test
    void testDeleteEmployee_whenResponseIsFalse_shouldThrowException() {
        String name = "Jill Jenkins";

        WireMock.stubFor(WireMock.request("DELETE", WireMock.urlEqualTo("/api/v1/employee"))
                .withRequestBody(WireMock.equalToJson("""
                { "name": "Jill Jenkins" }
            """))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                        {
                          "data": false,
                          "status": "Delete failed"
                        }
                    """)));

        assertThrows(EmployeeServiceException.class, () -> employeeService.deleteEmployee(name));
    }
}
