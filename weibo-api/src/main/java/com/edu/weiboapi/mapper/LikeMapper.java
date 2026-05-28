package com.edu.weiboapi.mapper;

import com.edu.weiboapi.entity.Like;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class LikeMapper {

    @Resource
    private JdbcTemplate jdbcTemplate;

    // 1. 查询用户是否已点赞某条动态
    public Like findByUserIdAndPostId(Long userId, Long postId) {
        String sql = "SELECT * FROM `like` WHERE user_id = ? AND post_id = ?";
        List<Like> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Like.class), userId, postId);
        return list.isEmpty() ? null : list.get(0);
    }

    // 2. 新增点赞记录
    public int insert(Like like) {
        String sql = "INSERT INTO `like`(user_id, post_id, create_time) VALUES (?,?,?)";
        return jdbcTemplate.update(sql,
                like.getUserId(),
                like.getPostId(),
                like.getCreateTime());
    }

    // 3. 删除点赞记录（取消点赞）
    public int deleteById(Long id) {
        String sql = "DELETE FROM `like` WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 4. 查询用户点赞过的所有动态ID
    public List<Long> findPostIdsByUserId(Long userId) {
        String sql = "SELECT post_id FROM `like` WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }
}