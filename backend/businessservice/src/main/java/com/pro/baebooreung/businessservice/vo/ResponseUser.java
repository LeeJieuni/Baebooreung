package com.pro.baebooreung.businessservice.vo;

import com.pro.baebooreung.userservice.domain.WorkStatus;
import lombok.Data;

import java.util.List;

@Data
public class ResponseUser {
    private String id;
    private String email;
    private String name;
    private String specialKey;
    private String grade;
    private String phone;
    private WorkStatus workStatus;
    private List<ResponseRoute> routeList;
}
