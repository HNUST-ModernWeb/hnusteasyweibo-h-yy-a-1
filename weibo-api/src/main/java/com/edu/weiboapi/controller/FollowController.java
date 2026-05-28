package com.edu.weiboapi.controller;

import com.edu.weiboapi.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class FollowController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/follow")
    public Result<?> follow(@RequestBody Map<String, Long> map) {
        Long followerId = map.get("followerId");
        Long followingId = map.get("followingId");

        if (followerId.equals(followingId)) {
            return Result.error("不能关注自己");
        }

        try {
            String sql = "INSERT INTO follow(follower_id, following_id) VALUES (?,?)";
            jdbcTemplate.update(sql, followerId, followingId);

            Map<String, Object> userInfo = jdbcTemplate.queryForMap(
                    "SELECT nickname, avatar FROM user WHERE id = ?", followerId);
            String nickname = (String) userInfo.get("nickname");

            String msgSql = "INSERT INTO message(user_id, content, detail, msg_type, is_read) VALUES (?,?,?,?,?)";
            jdbcTemplate.update(msgSql, followingId, nickname + " 关注了你", null, "follow", 0);

            return Result.success("关注成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("关注失败，可能已关注");
        }
    }

    @DeleteMapping("/follow")
    public Result<?> unfollow(@RequestBody Map<String, Long> map) {
        Long followerId = map.get("followerId");
        Long followingId = map.get("followingId");

        String sql = "DELETE FROM follow WHERE follower_id=? AND following_id=?";
        int rows = jdbcTemplate.update(sql, followerId, followingId);
        if (rows > 0) {
            return Result.success("取消关注成功");
        } else {
            return Result.error("取消关注失败");
        }
    }

    @GetMapping("/follow/check")
    public Result<?> checkFollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        String sql = "SELECT COUNT(*) FROM follow WHERE follower_id=? AND following_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, followerId, followingId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", count > 0);
        return Result.success(result);
    }

    @GetMapping("/follow/list")
    public Result<?> getFollowingList(@RequestParam Long userId) {
        String sql = "SELECT f.following_id, u.nickname, u.avatar " +
                "FROM follow f " +
                "LEFT JOIN user u ON f.following_id = u.id " +
                "WHERE f.follower_id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        return Result.success(result);
    }

    @GetMapping("/follow/count")
    public Result<?> getFollowCount(@RequestParam Long userId) {
        String followingCountSql = "SELECT COUNT(*) FROM follow WHERE follower_id=?";
        String followerCountSql = "SELECT COUNT(*) FROM follow WHERE following_id=?";

        Integer followingCount = jdbcTemplate.queryForObject(followingCountSql, Integer.class, userId);
        Integer followerCount = jdbcTemplate.queryForObject(followerCountSql, Integer.class, userId);

        Map<String, Integer> result = new HashMap<>();
        result.put("followingCount", followingCount);
        result.put("followerCount", followerCount);
        return Result.success(result);
    }

    @GetMapping("/follow/followerCount")
    public Result<?> getFollowerCount(@RequestParam Long userId) {
        String sql = "SELECT COUNT(*) FROM follow WHERE following_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return Result.success(count);
    }

    @GetMapping("/follow/followingCount")
    public Result<?> getFollowingCount(@RequestParam Long userId) {
        String sql = "SELECT COUNT(*) FROM follow WHERE follower_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return Result.success(count);
    }

    @GetMapping("/follow/isFollowing")
    public Result<?> isFollowing(@RequestParam Long followerId, @RequestParam Long followingId) {
        String sql = "SELECT COUNT(*) FROM follow WHERE follower_id=? AND following_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, followerId, followingId);
        return Result.success(count > 0);
    }
}