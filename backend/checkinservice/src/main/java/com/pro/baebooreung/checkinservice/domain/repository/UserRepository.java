<<<<<<<< HEAD:backend/checkinservice/src/main/java/com/pro/baebooreung/checkinservice/domain/repository/UserRepository.java
package com.pro.baebooreung.checkinservice.domain.repository;

import com.pro.baebooreung.checkinservice.domain.User;
========
package com.pro.baebooreung.gpsservice.domain.repository;

import com.pro.baebooreung.gpsservice.domain.User;
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/src/main/java/com/pro/baebooreung/gpsservice/domain/repository/UserRepository.java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepository {
    @PersistenceContext
    private final EntityManager em;

    public User findOne(int id){
        User findUser = em.find(User.class, id);
        return findUser;
    }

    public void save(User user){
        em.persist(user);
    }
}
