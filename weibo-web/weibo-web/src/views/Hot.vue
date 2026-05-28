<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="wrap">
        <div class="logo">社交分享平台</div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="hot-page">
      <div class="container">
        <div class="sidebar">
          <div class="side-item" @click="$router.push('/home')">
            <span>🏠</span>
            <span>首页</span>
          </div>
          <div class="side-item" @click="$router.push('/profile')">
            <span>👤</span>
            <span>个人中心</span>
          </div>
          <div class="side-item active">
            <span>🔥</span>
            <span>热门动态</span>
          </div>
          <div class="side-item" @click="$router.push('/message')">
            <span>💬</span>
            <span>消息</span>
          </div>
        </div>

        <div class="main-content">
          <!-- 标题 -->
          <h2 class="hot-title">🔥 热门动态</h2>

          <!-- 无数据提示 -->
          <div v-if="list.length === 0" class="empty-tip">
            暂无热门动态
          </div>

          <!-- 热门动态列表（按点赞数排序） -->
          <div class="post-card" v-for="item in list" :key="item.id">
            <div class="post-head">
              <div class="user-info">
                <el-avatar size="48" :src="item.avatar">
                  {{ item.nickname ? item.nickname.charAt(0) : '?' }}
                </el-avatar>
                <div class="name">{{ item.nickname }}</div>
              </div>
            </div>
            <p class="post-content">{{ item.content }}</p>

            <!-- 图片展示 -->
            <div v-if="item.images && item.images.length > 0" class="post-images">
              <el-image v-for="(img, index) in item.images" :key="index" :src="img" class="post-image" fit="cover" />
            </div>

            <!-- 标签展示 -->
            <div v-if="item.tags && item.tags.length > 0" class="post-tags">
              <el-tag v-for="(tag, index) in item.tags" :key="index" class="post-tag">
                #{{ tag }}
              </el-tag>
            </div>

            <div class="post-action">
              <div class="action-btn" @click="handleLike(item)">👍 {{ item.likeCount || item.like_count || 0 }}</div>
              <div class="action-btn" @click="showComment(item)">💬 评论 {{ item.commentCount || 0 }}</div>
              <div class="action-btn" @click="showRepostModal(item)">↩️ 转发</div>
            </div>

            <!-- 评论区域 -->
            <div v-if="openCommentId === item.id" class="comment-section">
              <div class="comment-list">
                <div class="comment-item" v-for="comment in commentList" :key="comment.id">
                  <span style="color:#ff7d00;font-weight:bold;">{{ comment.nickname }}</span>
                  <span>：{{ comment.content }}</span>
                </div>
              </div>
              <input v-model="commentText" placeholder="写下你的评论..." @keyup.enter="submitComment(item.id)"
                class="comment-input-field" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 转发弹窗 -->
    <el-dialog title="转发动态" v-model="isRepostModalVisible" width="500px">
      <div v-if="repostPost" class="repost-content">
        <div class="repost-original">
          <div class="repost-header">
            <el-avatar size="40" :src="repostPost.avatar">
              {{ repostPost.nickname ? repostPost.nickname.charAt(0) : '?' }}
            </el-avatar>
            <span class="repost-author">{{ repostPost.nickname }}</span>
          </div>
          <p>{{ repostPost.content }}</p>
        </div>
        <textarea v-model="repostContent" placeholder="说说你的想法..." class="repost-textarea"></textarea>
      </div>
      <template #footer>
        <el-button @click="closeRepostModal">取消</el-button>
        <el-button type="primary" @click="submitRepost">转发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const list = ref([])
const openCommentId = ref(null)
const commentList = ref([])
const commentText = ref('')
const isRepostModalVisible = ref(false)
const repostPost = ref(null)
const repostContent = ref('')

// 获取当前用户信息
const myUserId = ref(1)
const myNickName = ref('哈哈')

// 获取热门动态（按点赞数从高到低）
const getHotList = async () => {
  try {
    const res = await axios.get('http://localhost:8081/post/hot')
    list.value = res.data.data
  } catch (err) {
    ElMessage.error('加载失败')
    console.error(err)
  }
}

// 点赞（和首页一样的接口）
const handleLike = async (item) => {
  try {
    let res = await axios.post("http://localhost:8081/like/toggle", {
      userId: myUserId.value,
      postId: item.id
    })
    if (res.data.data === "点赞成功") {
      item.likeCount = (item.likeCount || item.like_count || 0) + 1
    } else if (res.data.data === "取消点赞成功") {
      item.likeCount = (item.likeCount || item.like_count || 0) - 1
    }
    ElMessage.success(res.data.data);
  } catch (err) {
    ElMessage.success("操作成功");
    console.error(err);
  }
}

// 显示评论
const showComment = async (item) => {
  if (openCommentId.value === item.id) {
    openCommentId.value = null
    return
  }
  openCommentId.value = item.id
  const res = await axios.get('http://localhost:8081/comment/list?postId=' + item.id)
  commentList.value = res.data.data
}

// 提交评论
const submitComment = async (postId) => {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    let params = {
      postId: postId,
      userId: myUserId.value,
      nickname: myNickName.value,
      content: commentText.value
    }
    await axios.post('http://localhost:8081/comment/add', params)
    ElMessage.success('评论成功')
    commentText.value = ''
    // 刷新评论列表
    let res = await axios.get(`http://localhost:8081/comment/list?postId=${postId}`)
    commentList.value = res.data.data
    // 更新评论数量
    const post = list.value.find(p => p.id === postId)
    if (post) {
      post.commentCount = (post.commentCount || 0) + 1
    }
  } catch (err) {
    ElMessage.error('评论失败')
    console.error(err)
  }
}

// 显示转发弹窗
const showRepostModal = (post) => {
  repostPost.value = post
  repostContent.value = ''
  isRepostModalVisible.value = true
}

// 关闭转发弹窗
const closeRepostModal = () => {
  isRepostModalVisible.value = false
  repostPost.value = null
  repostContent.value = ''
}

// 提交转发
const submitRepost = async () => {
  if (!repostContent.value.trim()) {
    ElMessage.warning('请输入转发内容')
    return
  }
  try {
    await axios.post('http://localhost:8081/post/repost', {
      userId: myUserId.value,
      repostFromId: repostPost.value.id,
      content: repostContent.value
    })
    ElMessage.success('转发成功')
    closeRepostModal()
    getHotList()
  } catch (err) {
    ElMessage.error('转发失败')
    console.error(err)
  }
}

onMounted(() => {
  getHotList()
})
</script>

<style scoped>
/* 最外层：控制整个页面背景色 */
.page-container {
  background-color: #f4f5f7;
  min-height: 100vh;
}

/* 导航栏 */
.header {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

.wrap {
  width: 960px;
  height: 60px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #ff7d00;
}

/* 主体容器：增加顶部间距，留出空隙 */
.container {
  width: 960px;
  margin: 25px auto 0;
  display: flex;
  gap: 24px;
  padding: 0 20px;
  box-sizing: border-box;
}

.hot-page {
  width: 100%;
}

/* ========= 左侧样式 完全保持不变 ========= */
.sidebar {
  width: 200px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  transition: 0.2s;
}

.side-item:hover {
  background: #f5f5f5;
}

.side-item.active {
  background: #fff2e8;
  color: #ff7d00;
  font-weight: 500;
}

.main-content {
  flex: 1;
}

/* 标题 */
.hot-title {
  font-size: 22px;
  margin-bottom: 18px;
  color: #333;
}

/* 空数据 */
.empty-tip {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  color: #999;
}

/* 动态卡片 */
.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.04);
}

.post-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.user-info .name {
  font-weight: 600;
  color: #333;
}

.post-content {
  color: #555;
  line-height: 1.6;
  margin-bottom: 14px;
}

/* 图片展示 */
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.post-image {
  width: calc(33.33% - 6px);
  height: 120px;
  border-radius: 8px;
}

/* 标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.post-tag {
  background: #fff2e8;
  color: #ff7d00;
  font-size: 12px;
  padding: 2px 8px;
}

.post-action {
  display: flex;
  gap: 40px;
  border-top: 1px solid #f5f5f5;
  padding-top: 14px;
}

.action-btn {
  color: #777;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  color: #ff7d00;
}

/* 评论区域 */
.comment-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f5f5f5;
}

.comment-input-field {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  margin-top: 12px;
}

.comment-list {
  margin-top: 8px;
}

.comment-item {
  padding: 6px 0;
  font-size: 14px;
}

/* 转发弹窗 */
.repost-content {
  padding: 10px;
}

.repost-original {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.repost-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.repost-author {
  font-weight: 600;
}

.repost-textarea {
  width: 100%;
  height: 100px;
  padding: 10px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  resize: none;
}
</style>