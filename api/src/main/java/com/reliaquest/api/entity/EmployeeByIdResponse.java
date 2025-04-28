package com.reliaquest.api.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmployeeByIdResponse {

    private String status;
    private Employee data;
}
