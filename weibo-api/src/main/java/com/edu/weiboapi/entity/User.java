package com.edu.weiboapi.entity;

import java.time.LocalDateTime;

public class User {
    // 对应表字段，类型完全匹配
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private String domain;
    private String gender;
    private String birthday;
    private LocalDateTime createTime;

    // 无参构造（必须）
    public User() {}

    // 全参构造（可选）
    public User(Long id, String username, String password, String nickname, String avatar, String bio, String domain, String gender, String birthday, LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.bio = bio;
        this.domain = domain;
        this.gender = gender;
        this.birthday = birthday;
        this.createTime = createTime;
    }

    // 必须写全get/set方法（不用Lombok也能跑）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}