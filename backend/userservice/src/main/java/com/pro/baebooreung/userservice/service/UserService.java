package com.pro.baebooreung.userservice.service;

import com.pro.baebooreung.userservice.domain.UserEntity;
import com.pro.baebooreung.userservice.dto.CheckinDto;
import com.pro.baebooreung.userservice.dto.StartDto;
import com.pro.baebooreung.userservice.dto.UserDto;
import com.pro.baebooreung.userservice.dto.UserProfileDto;
import com.pro.baebooreung.userservice.vo.ResponseUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDto);

//    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();

    UserDto getUserById(int id);

    UserDto getUserDetailsByEmail(String userName);

    ResponseUser setUsertoDriver(int id);

    UserDto setStart(StartDto startDto);

    public void setCheckIn(CheckinDto checkinDto);

    public void setEnd(int id);

    public UserProfileDto getUserProfile(int userId);
}
