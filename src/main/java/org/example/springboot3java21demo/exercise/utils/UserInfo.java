package org.example.springboot3java21demo.exercise.utils;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserInfo implements java.io.Serializable {

    private static final long serialVersionUID = -3522051445403971732L;

    private Integer userId;
    private String username;
    @Getter
    private Date birthDate;
    private Integer age;
    private float fRate;
    private char ch;

    public String getBirthDatestr() {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        return formater.format(getBirthDate());
    }

    public UserInfo(Integer userId, String username, Date birthDate, Integer age, float fRate, char ch) {
        super();
        this.userId = userId;
        this.username = username;
        this.birthDate = birthDate;
        this.age = age;
        this.fRate = fRate;
        this.ch = ch;
    }

    @Override
    public String toString() {
        return "UserInfo [userId=" + userId + ", \tusername=" + username + ", \tbirthDate=" + getBirthDatestr()
                + ", \tage=" + age + ", fRate=" + fRate + ", ch=" + ch + "]";
    }

}