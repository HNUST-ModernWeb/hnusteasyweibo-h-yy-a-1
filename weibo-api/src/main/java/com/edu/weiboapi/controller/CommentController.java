package com.edu.weiboapi.controller;

import com.edu.weiboapi.common.Result;
import com.edu.weiboapi.entity.Comment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public Result<?> list(@RequestParam Long postId) {
        String sql = "SELECT * FROM comment WHERE post_id=? ORDER BY create_time ASC";
        List<Comment> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Comment.class), postId);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Comment comment) {
        System.out.println("========== 收到评论请求 ==========");
        System.out.println("评论内容: postId=" + comment.getPostId() + ", userId=" + comment.getUserId() + ", content="
                + comment.getContent());

        String sql = "INSERT INTO comment(post_id, user_id, content, nickname, parent_id, reply_to_nickname, reply_to_user_id) VALUES (?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                comment.getPostId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getNickname(),
                comment.getParentId(),
                comment.getReplyToNickname(),
                comment.getReplyToUserId());

        Map<String, Object> userInfo = jdbcTemplate.queryForMap(
                "SELECT nickname, avatar FROM user WHERE id = ?", comment.getUserId());
        String nickname = (String) userInfo.get("nickname");
        String avatar = (String) userInfo.get("avatar");

        // 直接发送消息通知
        if (comment.getParentId() != null && comment.getReplyToUserId() != null) {
            // 回复评论
            System.out.println("发送回复消息给: " + comment.getReplyToUserId());
            String msgSql = "INSERT INTO message(user_id, content, detail, msg_type, is_read) VALUES (?,?,?,?,?)";
            jdbcTemplate.update(msgSql, comment.getReplyToUserId(),
                    nickname + " 回复了你", comment.getContent(), "reply", 0);
            System.out.println("回复消息发送成功");
        } else {
            // 评论动态
            Long postUserId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM post WHERE id = ?",
                    Long.class, comment.getPostId());
            System.out.println("动态作者ID: " + postUserId + ", 评论者ID: " + comment.getUserId());

            if (postUserId != null && !comment.getUserId().equals(postUserId)) {
                System.out.println("发送评论消息给: " + postUserId);
                String msgSql = "INSERT INTO message(user_id, content, detail, msg_type, is_read) VALUES (?,?,?,?,?)";
                jdbcTemplate.update(msgSql, postUserId,
                        nickname + " 评论了你的动态", comment.getContent(), "comment", 0);
                System.out.println("评论消息发送成功");
            } else {
                System.out.println("跳过：评论者是自己或动态不存在");
            }
        }

        return Result.success("发表成功");
    }

    @GetMapping("/all")
    public Result<?> all() {
        String sql = "SELECT * FROM comment";
        List<Comment> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Comment.class));
        return Result.success(list);
    }
}