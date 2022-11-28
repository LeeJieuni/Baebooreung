<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/Grade.java
package com.pro.baebooreung.checkinservice.domain;
========
package com.pro.baebooreung.gpsservice.domain;
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/Grade.java

public enum Grade {
    UNAUTHORIZED, // 권한받지 않은 드라이버
    DRIVER,  // 권한 받은 드라이버
    MANAGER,  // 관리자
    ADMIN  // 총관리자(EX. 개발자)
}
