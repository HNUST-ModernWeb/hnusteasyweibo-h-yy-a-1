package com.edu.weiboapi.listener;

import com.edu.weiboapi.event.CommentEvent;
import com.edu.weiboapi.event.FollowEvent;
import com.edu.weiboapi.event.LikeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MessageEventListener {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @EventListener
    public void handleLikeEvent(LikeEvent event) {
        try {
            Long postUserId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM post WHERE id = ?",
                    Long.class, event.getPostId());

            if (postUserId != null && !event.getUserId().equals(postUserId)) {
                String sql = "INSERT INTO message(user_id, from_user_id, from_nickname, from_avatar, type, post_id, content) VALUES (?,?,?,?,?,?,?)";
                jdbcTemplate.update(sql, postUserId, event.getUserId(), event.getNickname(),
                        event.getAvatar(), "like", event.getPostId(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleCommentEvent(CommentEvent event) {
        System.out.println("========== 收到评论事件 ==========");
        System.out.println("事件内容: userId=" + event.getUserId() + ", postId=" + event.getPostId() + ", parentId="
                + event.getParentId());
        try {
            if (event.getParentId() != null && event.getReplyToUserId() != null) {
                System.out.println("发送回复消息给: " + event.getReplyToUserId());
                String sql = "INSERT INTO message(user_id, from_user_id, from_nickname, from_avatar, type, post_id, comment_id, content) VALUES (?,?,?,?,?,?,?,?)";
                jdbcTemplate.update(sql, event.getReplyToUserId(), event.getUserId(),
                        event.getNickname(), event.getAvatar(), "reply",
                        event.getPostId(), event.getParentId(), event.getContent());
                System.out.println("回复消息发送成功");
            } else {
                Long postUserId = jdbcTemplate.queryForObject(
                        "SELECT user_id FROM post WHERE id = ?",
                        Long.class, event.getPostId());
                System.out.println("动态作者ID: " + postUserId + ", 评论者ID: " + event.getUserId());

                if (postUserId != null && !event.getUserId().equals(postUserId)) {
                    System.out.println("发送评论消息给: " + postUserId);
                    String sql = "INSERT INTO message(user_id, from_user_id, from_nickname, from_avatar, type, post_id, content) VALUES (?,?,?,?,?,?,?)";
                    jdbcTemplate.update(sql, postUserId, event.getUserId(),
                            event.getNickname(), event.getAvatar(), "comment",
                            event.getPostId(), event.getContent());
                    System.out.println("评论消息发送成功");
                } else {
                    System.out.println("跳过：评论者是自己或动态不存在");
                }
            }
        } catch (Exception e) {
            System.out.println("消息发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleFollowEvent(FollowEvent event) {
        try {
            String sql = "INSERT INTO message(user_id, from_user_id, from_nickname, from_avatar, type) VALUES (?,?,?,?,?)";
            jdbcTemplate.update(sql, event.getFollowingId(), event.getFollowerId(),
                    event.getNickname(), event.getAvatar(), "follow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}