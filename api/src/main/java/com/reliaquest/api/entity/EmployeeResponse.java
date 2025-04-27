package com.reliaquest.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class EmployeeResponse {

    private String status;
    private List<Employee> data;
    private String message;
}
