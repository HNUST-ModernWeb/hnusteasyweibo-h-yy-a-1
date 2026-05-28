package com.edu.weiboapi.controller;

import com.edu.weiboapi.common.Result;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public Result<?> getMessages(@RequestParam Long userId) {
        String sql = "SELECT * FROM message WHERE user_id = ? ORDER BY create_time DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        return Result.success(result);
    }

    @GetMapping("/unreadCount")
    public Result<?> getUnreadCount(@RequestParam Long userId) {
        String sql = "SELECT COUNT(*) FROM message WHERE user_id = ? AND is_read = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return Result.success(count);
    }

    @PostMapping("/read")
    public Result<?> markAsRead(@RequestParam Long userId) {
        jdbcTemplate.update("UPDATE message SET is_read = TRUE WHERE user_id = ?", userId);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read")
    public Result<?> markOneAsReadPut(@RequestParam Long id) {
        jdbcTemplate.update("UPDATE message SET is_read = TRUE WHERE id = ?", id);
        return Result.success("已标记为已读");
    }

    @GetMapping("/detail")
    public Result<?> getMessageDetail(@RequestParam Long id) {
        String sql = "SELECT * FROM message WHERE id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, id);
        return Result.success(result);
    }

    @PostMapping("/readOne")
    public Result<?> markOneAsRead(@RequestParam Long id) {
        jdbcTemplate.update("UPDATE message SET is_read = TRUE WHERE id = ?", id);
        return Result.success("已标记为已读");
    }

    @DeleteMapping("/delete")
    public Result<?> deleteMessage(@RequestParam Long id) {
        jdbcTemplate.update("DELETE FROM message WHERE id = ?", id);
        return Result.success("删除成功");
    }

    public void sendMessage(Long userId, Long fromUserId, String fromNickname, String fromAvatar,
            String type, Long postId, Long commentId, String content) {
        String sql = "INSERT INTO message(user_id, from_user_id, from_nickname, from_avatar, " +
                "type, post_id, comment_id, content) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, fromUserId, fromNickname, fromAvatar, type, postId, commentId, content);
    }
}