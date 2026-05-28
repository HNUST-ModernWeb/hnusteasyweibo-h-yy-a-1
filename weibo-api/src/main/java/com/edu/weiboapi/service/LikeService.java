package com.edu.weiboapi.service;

import com.edu.weiboapi.common.Result;
import com.edu.weiboapi.entity.Like;
import com.edu.weiboapi.entity.Post;
import com.edu.weiboapi.mapper.LikeMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LikeService {

    @Resource
    private LikeMapper likeMapper;

    // 注入JdbcTemplate，直接操作post表，不用PostMapper
    @Resource
    private JdbcTemplate jdbcTemplate;

    // 点赞/取消点赞（核心逻辑）
    @Transactional
    public Result<?> toggleLike(Long userId, Long postId) {
        // 1. 检查是否已点赞
        Like existLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existLike != null) {
            // 已点赞 → 取消点赞
            likeMapper.deleteById(existLike.getId());
            // 动态点赞数-1（直接用jdbcTemplate执行SQL）
            String decSql = "UPDATE post SET like_count = like_count - 1 WHERE id = ? AND like_count > 0";
            jdbcTemplate.update(decSql, postId);
            return Result.success("取消点赞成功");
        } else {
            // 未点赞 → 新增点赞
            Like newLike = new Like();
            newLike.setUserId(userId);
            newLike.setPostId(postId);
            newLike.setCreateTime(LocalDateTime.now());
            likeMapper.insert(newLike);
            // 动态点赞数+1（直接用jdbcTemplate执行SQL）
            String incSql = "UPDATE post SET like_count = like_count + 1 WHERE id = ?";
            jdbcTemplate.update(incSql, postId);
            return Result.success("点赞成功");
        }
    }

    // 获取用户点赞过的所有动态
    public Result<?> getMyLikes(Long userId) {
        // 1. 查询用户点赞的所有postId
        List<Long> postIds = likeMapper.findPostIdsByUserId(userId);
        if (postIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 批量查询动态详情（直接用jdbcTemplate，不用PostMapper）
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < postIds.size(); i++) {
            placeholders.append("?");
            if (i != postIds.size() - 1) {
                placeholders.append(",");
            }
        }
        String sql = "SELECT p.*, u.nickname FROM post p LEFT JOIN user u ON p.user_id = u.id WHERE p.id IN (" + placeholders + ") ORDER BY p.create_time DESC";
        List<Post> likePosts = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Post.class), postIds.toArray());
        return Result.success(likePosts);
    }
}