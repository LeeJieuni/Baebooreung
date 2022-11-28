<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/User.java
package com.pro.baebooreung.checkinservice.domain;
========
package com.pro.baebooreung.gpsservice.domain;
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/User.java

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true)
<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/User.java
    private String specialKey; // bearer token을 위한 랜덤값

    @Enumerated(EnumType.STRING)
    private Grade grade;
========
    private String special_key; // bearer token을 위한 랜덤값
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/User.java

    @Column(nullable = false, unique = true)
    private String encryptedPwd; // 암호화된 비밀번호

<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/User.java
    @Column(name = "profile",nullable = true)
========
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/User.java
    private String profile;

    private String phone;

    private Integer region;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = true)
    private WorkStatus workStatus;

    @Column(name = "route_id",nullable = true)
    private Integer routeId;

    @Column(name = "delivery_id",nullable = true)
    private Integer deliveryId;

    @Column(name = "fcm_token",nullable = true)
    private String fcmToken;

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
