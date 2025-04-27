package com.reliaquest.api.service;


import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.entity.EmployeeByIdResponse;

import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String name);

    EmployeeByIdResponse getEmployeeById(String id);

    int getHighestSalaryOfEmployees();

    List<Employee> getTopHighestEarningEmployees(int size);

    EmployeeByIdResponse createEmployee(Employee employee);

    void deleteEmployee(String id);
}
