package com.edu.weiboapi.controller;

import com.edu.weiboapi.common.Result;
import com.edu.weiboapi.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.OPTIONS })
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.error("账号或密码不能为空");
        }

        String checkSql = "SELECT COUNT(*) FROM user WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, username);
        if (count != null && count > 0) {
            return Result.error("账号已存在");
        }

        String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes());
        String nickname = user.getNickname() == null ? username : user.getNickname();
        String insertSql = "INSERT INTO user(username, password, nickname) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, username, md5Pwd, nickname);

        return Result.success("注册成功");
    }

    // ======================== 登录修复 ========================
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, Object> map) {
        String username = map.get("username").toString();
        String password = map.get("password").toString();

        // 登录时密码也要 MD5 加密！！！
        String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes());

        String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, username, md5Pwd);

        if (users != null && !users.isEmpty()) {
            return Result.success(users.get(0));
        } else {
            return Result.error("账号或密码错误");
        }
    }

    // 获取用户信息（按用户名）
    @GetMapping("/getInfo")
    public Result<?> getInfo(@RequestParam String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(User.class),
                    username);
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("用户不存在");
        }
    }

    // 获取用户信息（按用户ID）
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestParam Long userId) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(User.class),
                    userId);
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("用户不存在");
        }
    }

    // 修改昵称
    @PostMapping("/updateNickname")
    public Result<?> updateNickname(@RequestBody Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        String nickname = map.get("nickname").toString();
        jdbcTemplate.update("UPDATE user SET nickname = ? WHERE id = ?", nickname, userId);
        return Result.success("ok");
    }

    // 修改密码
    @PostMapping("/updatePwd")
    public Result<?> updatePwd(@RequestBody Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        String pwd = map.get("password").toString();

        // 修改密码也要加密！！！
        String md5Pwd = DigestUtils.md5DigestAsHex(pwd.getBytes());

        jdbcTemplate.update("UPDATE user SET password = ? WHERE id = ?", md5Pwd, userId);
        return Result.success("ok");
    }

    // 通用用户信息更新接口
    @PostMapping("/update")
    public Result<?> update(@RequestBody Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());

        StringBuilder sql = new StringBuilder("UPDATE user SET ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (map.containsKey("avatar")) {
            sql.append("avatar = ?, ");
            params.add(map.get("avatar"));
        }
        if (map.containsKey("nickname")) {
            sql.append("nickname = ?, ");
            params.add(map.get("nickname"));
        }
        if (map.containsKey("bio")) {
            sql.append("bio = ?, ");
            params.add(map.get("bio"));
        }
        if (map.containsKey("domain")) {
            sql.append("domain = ?, ");
            params.add(map.get("domain"));
        }
        if (map.containsKey("gender")) {
            sql.append("gender = ?, ");
            params.add(map.get("gender"));
        }
        if (map.containsKey("birthday")) {
            sql.append("birthday = ?, ");
            params.add(map.get("birthday"));
        }

        // 移除末尾的逗号和空格
        if (sql.toString().endsWith(", ")) {
            sql.setLength(sql.length() - 2);
        }

        sql.append(" WHERE id = ?");
        params.add(userId);

        try {
            jdbcTemplate.update(sql.toString(), params.toArray());
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }
}