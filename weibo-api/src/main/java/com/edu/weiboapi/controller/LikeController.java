package com.edu.weiboapi.controller;

import com.edu.weiboapi.common.Result;
import com.edu.weiboapi.entity.Post;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/like")
public class LikeController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> map) {
        try {
            Long userId = Long.valueOf(map.get("userId").toString());
            Long postId = Long.valueOf(map.get("postId").toString());

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `like` WHERE user_id = ? AND post_id = ?",
                    Integer.class, userId, postId);

            if (count == 0) {
                jdbcTemplate.update("INSERT INTO `like`(user_id, post_id) VALUES(?,?)", userId, postId);
                jdbcTemplate.update("UPDATE post SET like_count = like_count + 1 WHERE id = ?", postId);
            }
            return Result.success("点赞成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success("点赞成功");
        }
    }

    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody Map<String, Object> map) {
        try {
            Long userId = Long.valueOf(map.get("userId").toString());
            Long postId = Long.valueOf(map.get("postId").toString());

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `like` WHERE user_id = ? AND post_id = ?",
                    Integer.class, userId, postId);

            if (count > 0) {
                jdbcTemplate.update("DELETE FROM `like` WHERE user_id = ? AND post_id = ?", userId, postId);
                jdbcTemplate.update("UPDATE post SET like_count = like_count - 1 WHERE id = ?", postId);
                return Result.success("取消点赞成功");
            } else {
                jdbcTemplate.update("INSERT INTO `like`(user_id, post_id) VALUES(?,?)", userId, postId);
                jdbcTemplate.update("UPDATE post SET like_count = like_count + 1 WHERE id = ?", postId);

                Map<String, Object> userInfo = jdbcTemplate.queryForMap(
                        "SELECT nickname, avatar FROM user WHERE id = ?", userId);
                String nickname = (String) userInfo.get("nickname");

                Long postUserId = jdbcTemplate.queryForObject(
                        "SELECT user_id FROM post WHERE id = ?",
                        Long.class, postId);

                if (postUserId != null && !userId.equals(postUserId)) {
                    String msgSql = "INSERT INTO message(user_id, content, detail, msg_type, is_read) VALUES (?,?,?,?,?)";
                    jdbcTemplate.update(msgSql, postUserId, nickname + " 点赞了你的动态", null, "like", 0);
                }

                return Result.success("点赞成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success("点赞成功");
        }
    }

    @GetMapping("/mine")
    public Result<?> myLikes(@RequestParam Long userId) {
        try {
            String sql = "SELECT p.*, u.nickname, u.avatar, " +
                    "(SELECT COUNT(*) FROM comment WHERE post_id = p.id) as comment_count " +
                    "FROM post p " +
                    "JOIN `like` l ON p.id = l.post_id " +
                    "LEFT JOIN user u ON p.user_id = u.id " +
                    "WHERE l.user_id = ?";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
            List<Post> posts = new ArrayList<>();
            for (Map<String, Object> row : result) {
                posts.add(mapToPost(row));
            }
            return Result.success(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(new ArrayList<>());
        }
    }

    private Post mapToPost(Map<String, Object> row) {
        Post post = new Post();
        post.setId(((Number) row.get("id")).longValue());
        post.setUserId(((Number) row.get("user_id")).longValue());
        post.setContent((String) row.get("content"));
        if (row.get("like_count") != null) {
            post.setLikeCount(((Number) row.get("like_count")).intValue());
        }
        post.setCreateTime((java.time.LocalDateTime) row.get("create_time"));
        post.setNickname((String) row.get("nickname"));
        post.setAvatar((String) row.get("avatar"));
        post.setVisibility((String) row.get("visibility"));

        if (row.get("repost_from_id") != null) {
            post.setRepostFromId(((Number) row.get("repost_from_id")).longValue());
        }
        post.setRepostContent((String) row.get("repost_content"));

        if (row.get("comment_count") != null) {
            post.setCommentCount(((Number) row.get("comment_count")).intValue());
        }

        String imagesJson = (String) row.get("images");
        if (imagesJson != null && !imagesJson.isEmpty()) {
            try {
                post.setImages(objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
                }));
            } catch (Exception e) {
                post.setImages(new ArrayList<>());
            }
        }

        String tagsJson = (String) row.get("tags");
        if (tagsJson != null && !tagsJson.isEmpty()) {
            try {
                post.setTags(objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
                }));
            } catch (Exception e) {
                post.setTags(new ArrayList<>());
            }
        }

        return post;
    }
}