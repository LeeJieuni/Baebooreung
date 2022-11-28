<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/Gps.java
package com.pro.baebooreung.checkinservice.domain;
========
package com.pro.baebooreung.gpsservice.domain;
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/Gps.java

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gps")
public class Gps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "route_id", nullable = true)
    private int routeId;

    @Column(name = "delivery_id", nullable = true)
    private int delivery_id;

    @Column(name = "gps_datetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestDateTime;
}
