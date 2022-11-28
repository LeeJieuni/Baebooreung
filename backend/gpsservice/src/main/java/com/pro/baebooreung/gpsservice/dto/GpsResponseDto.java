<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/dto/GpsSaveDto.java
package com.pro.baebooreung.checkinservice.dto;
========
package com.pro.baebooreung.gpsservice.dto;
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/dto/GpsResponseDto.java

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@Getter
public class GpsResponseDto {
    private String latitude;
    private String longitude;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestDateTime;

    @Builder
    public GpsResponseDto(String latitude, String longitude, LocalDateTime requestDateTime){
        this.latitude = latitude;
        this.longitude = longitude;
        this.requestDateTime = requestDateTime;
    }

}

