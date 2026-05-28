package com.edu.weiboapi.entity;

import java.time.LocalDateTime;

public class Like {
    // 对应表 like 的字段
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createTime;

    // 【必须】无参构造（框架反射用）
    public Like() {}

    // 【可选】全参构造（方便自己写代码）
    public Like(Long id, Long userId, Long postId, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.createTime = createTime;
    }

    // 【必须】全量 Getter + Setter（不用Lombok必须手动写）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}