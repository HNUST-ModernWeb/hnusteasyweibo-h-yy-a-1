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
public class PostController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/post/list")
    public Result<?> list() {
        String sql = "SELECT p.*, u.nickname, u.avatar, " +
                "(SELECT COUNT(*) FROM comment WHERE post_id = p.id) as comment_count " +
                "FROM post p " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "ORDER BY p.create_time DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        List<Post> posts = new ArrayList<>();
        for (Map<String, Object> row : result) {
            posts.add(mapToPost(row));
        }
        return Result.success(posts);
    }

    @PostMapping("/post/publish")
    public Result<?> publish(@RequestBody Post post) {
        try {
            String imagesJson = null;
            String tagsJson = null;
            String visibility = post.getVisibility() != null ? post.getVisibility() : "public";
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                imagesJson = objectMapper.writeValueAsString(post.getImages());
            }
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                tagsJson = objectMapper.writeValueAsString(post.getTags());
            }
            String sql = "INSERT INTO post(user_id, content, images, tags, visibility) VALUES (?,?,?,?,?)";
            jdbcTemplate.update(sql, post.getUserId(), post.getContent(), imagesJson, tagsJson, visibility);
            return Result.success("发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            String visibility = post.getVisibility() != null ? post.getVisibility() : "public";
            String sql = "INSERT INTO post(user_id, content, visibility) VALUES (?,?,?)";
            jdbcTemplate.update(sql, post.getUserId(), post.getContent(), visibility);
            return Result.success("发布成功");
        }
    }

    @PostMapping("/post/like")
    public Result<?> like(@RequestBody Map<String, Long> map) {
        Long postId = map.get("postId");
        String sql = "UPDATE post SET like_count = like_count + 1 WHERE id=?";
        jdbcTemplate.update(sql, postId);
        return Result.success("点赞成功");
    }

    @DeleteMapping("/post/{id}")
    public Result<?> delete(@PathVariable Long id) {
        String sql = "DELETE FROM post WHERE id=?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败，动态不存在");
        }
    }

    @GetMapping("/post/my")
    public Result<?> myPost(@RequestParam Long userId) {
        String sql = "SELECT p.*, u.nickname, u.avatar, " +
                "(SELECT COUNT(*) FROM comment WHERE post_id = p.id) as comment_count " +
                "FROM post p " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "WHERE p.user_id = ? ORDER BY p.create_time DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        List<Post> posts = new ArrayList<>();
        for (Map<String, Object> row : result) {
            posts.add(mapToPost(row));
        }
        return Result.success(posts);
    }

    @GetMapping("/post/user/{userId}")
    public Result<?> getUserPosts(@PathVariable Long userId) {
        String sql = "SELECT p.*, u.nickname, u.avatar " +
                "FROM post p " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.create_time DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        List<Post> posts = new ArrayList<>();
        for (Map<String, Object> row : result) {
            posts.add(mapToPost(row));
        }
        return Result.success(posts);
    }

    @GetMapping("/post/hot")
    public Result<?> hot() {
        String sql = "SELECT p.*, u.nickname, u.avatar, " +
                "(SELECT COUNT(*) FROM comment WHERE post_id = p.id) as comment_count " +
                "FROM post p " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "ORDER BY p.like_count DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        List<Post> posts = new ArrayList<>();
        for (Map<String, Object> row : result) {
            posts.add(mapToPost(row));
        }
        return Result.success(posts);
    }

    @PostMapping("/post/repost")
    public Result<?> repost(@RequestBody Map<String, Object> map) {
        Long userId = ((Number) map.get("userId")).longValue();
        Long repostFromId = ((Number) map.get("repostFromId")).longValue();
        String repostContent = (String) map.get("repostContent");

        String sql = "INSERT INTO post(user_id, content, repost_from_id, repost_content) VALUES (?,?,?,?)";
        jdbcTemplate.update(sql, userId, repostContent, repostFromId, repostContent);
        return Result.success("转发成功");
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