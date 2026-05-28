<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="wrap">
        <div class="logo">社交分享平台</div>
        <div class="nav-right">
          <span class="nickname">{{ nickname }}</span>
          <el-button size="small" @click="toPublish" type="primary">发布动态</el-button>
        </div>
      </div>
    </div>

    <!-- 主体：左侧边栏 + 中间内容 -->
    <div class="container">
      <!-- 左侧导航栏 -->
      <div class="sidebar">
        <div class="side-item active" @click="goTo('/home')">
          <span>🏠</span>
          <span>首页</span>
        </div>
        <div class="side-item" @click="goTo('/profile')">
          <span>👤</span>
          <span>个人中心</span>
        </div>
        <div class="side-item" @click="goTo('/hot')">
          <span>🔥</span>
          <span>热门动态</span>
        </div>
        <div class="side-item" @click="goTo('/message')">
          <span>💬</span>
          <span>消息</span>
        </div>
      </div>

      <!-- 中间动态列表 -->
      <div class="main-content">
        <div v-if="postList.length === 0" class="empty-tip">
          暂无动态，快去发布第一条分享吧
        </div>

        <div class="post-card" v-for="post in postList" :key="post.id">
          <div class="post-head">
            <div class="user-avatar-wrapper" @click="goToUserProfile(post.userId)">
              <el-avatar size="48" :src="post.avatar">
                {{ post.nickname ? post.nickname.charAt(0) : '?' }}
              </el-avatar>
              <div class="name">{{ post.nickname }}</div>
            </div>
            <div class="post-meta">
              <div class="time">{{ formatTime(post.create_time) }}</div>
            </div>
          </div>

          <div class="post-text">
            {{ post.content }}
          </div>

          <!-- 图片展示 -->
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <el-image v-for="(img, index) in post.images" :key="index" :src="img" class="post-image" fit="cover"
              preview-src-list="post.images" />
          </div>

          <!-- 标签展示 -->
          <div v-if="post.tags && post.tags.length > 0" class="post-tags">
            <el-tag v-for="(tag, index) in post.tags" :key="index" class="post-tag">
              #{{ tag }}
            </el-tag>
          </div>

          <!-- 可见性标识 -->
          <div v-if="post.visibility !== 'public'" class="visibility-badge">
            {{ post.visibility === 'private' ? '🔒 仅自己可见' : '👫 仅关注可见' }}
          </div>

          <div class="post-action">
            <div class="action-btn" @click="likePost(post.id)">
              👍 点赞 {{ post.likeCount || 0 }}
            </div>
            <div class="action-btn" @click="showComment(post.id)">💬 评论 {{ post.commentCount || 0 }}</div>
            <div class="action-btn" @click="showRepostModal(post)">↩️ 转发</div>
          </div>

          <!-- 评论区域 -->
          <div class="comment-box" v-if="openCommentId === post.id">
            <!-- 所有人的评论列表 -->
            <div class="comment-list">
              <div class="comment-item" v-for="item in commentList" :key="item.id">
                <span style="color:#ff7d00;font-weight:bold;">{{ item.nickname }}</span>
                <span v-if="item.replyToNickname"> @{{ item.replyToNickname }}：</span>
                <span v-else>：</span>
                <span>{{ item.content }}</span>
                <span class="reply-btn" @click="replyToComment(item)">回复</span>
              </div>
              <div v-if="commentList.length === 0" style="color:#999;font-size:14px;">
                暂无评论，快来抢沙发～
              </div>
            </div>
            <!-- 自己发布评论 -->
            <div class="comment-input">
              <span v-if="replyComment" style="color:#ff7d00;margin-right:8px;">回复 @{{ replyComment.nickname }}：</span>
              <el-input v-model="commentText" :placeholder="replyComment ? '写下你的回复...' : '写下你的评论...'"
                clearable></el-input>
              <el-button type="primary" @click="submitComment(post.id)">发表</el-button>
              <el-button v-if="replyComment" type="text" @click="cancelReply">取消回复</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户资料弹窗 -->
      <el-dialog title="用户详情" v-model:visible="showUserModal" width="600px" @close="closeUserModal">
        <div class="user-profile">
          <div class="profile-header">
            <el-avatar size="100" :src="targetUser.avatar">
              {{ targetUser.nickname ? targetUser.nickname.charAt(0) : '?' }}
            </el-avatar>
            <div class="profile-info">
              <h3>{{ targetUser.nickname || '未知用户' }}</h3>
              <p>账号：{{ targetUser.username }}</p>
              <p>{{ targetUser.bio || '暂无简介' }}</p>
              <p>性别：{{ targetUser.gender === 'male' ? '男' : targetUser.gender === 'female' ? '女' : '未设置' }}</p>
              <p>生日：{{ targetUser.birthday || '未设置' }}</p>
            </div>
            <div v-if="targetUser.id !== myUserId" class="profile-actions">
              <el-button type="primary" plain @click="toggleFollow(targetUser.id)">
                {{ isFollowing(targetUser.id) ? '已关注' : '+ 关注' }}
              </el-button>
            </div>
          </div>

          <div class="profile-posts">
            <h4>TA的动态 ({{ targetUserPosts.length }})</h4>
            <div v-if="targetUserPosts.length === 0" class="empty-tip">
              暂无动态
            </div>
            <div v-for="post in targetUserPosts" :key="post.id" class="mini-post">
              <div class="mini-post-content">{{ post.content }}</div>
              <div v-if="post.images && post.images.length > 0" class="mini-post-images">
                <el-image v-for="(img, idx) in post.images" :key="idx" :src="img" class="mini-image" fit="cover" />
              </div>
              <div class="mini-post-info">
                <span>{{ formatTime(post.create_time) }}</span>
                <span>👍 {{ post.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- 转发弹窗 -->
      <el-dialog title="转发" v-model:visible="isRepostModalVisible" width="500px" @close="closeRepostModal">
        <div class="repost-modal">
          <!-- 原动态内容 -->
          <div v-if="repostPost.nickname" class="repost-original">
            <div class="repost-original-header">
              <el-avatar size="36" :src="repostPost.avatar">
                {{ repostPost.nickname ? repostPost.nickname.charAt(0) : '?' }}
              </el-avatar>
              <span class="repost-original-name">{{ repostPost.nickname }}</span>
            </div>
            <div class="repost-original-content">{{ repostPost.content }}</div>
          </div>

          <!-- 转发输入框 -->
          <div class="repost-input-wrapper">
            <el-textarea v-model="repostContent" placeholder="写下你的转发想法..." :rows="4"
              class="repost-textarea"></el-textarea>
          </div>

          <div class="repost-footer">
            <el-button type="primary" @click="submitRepost">转发</el-button>
            <el-button @click="closeRepostModal">取消</el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const postList = ref([])
const nickname = ref(localStorage.getItem('nickname') || '匿名用户')
const openCommentId = ref(null)
const commentList = ref([])
const commentText = ref("")
const myUserId = ref(localStorage.getItem("userId"))
const myNickName = ref(localStorage.getItem("nickname"))
const followingList = ref([])
// 用户资料弹窗
const showUserModal = ref(false)
const targetUser = ref({})
const targetUserPosts = ref([])
// 转发弹窗
const isRepostModalVisible = ref(false)
const repostPost = ref({})
const repostContent = ref("")

// 回复评论
const replyComment = ref(null)

// 获取动态列表和关注列表
onMounted(() => {
  axios.get('http://localhost:8081/post/list').then(res => {
    postList.value = res.data.data
  })
  loadFollowingList()
})

// 加载关注列表
const loadFollowingList = async () => {
  try {
    const res = await axios.get(`http://localhost:8081/follow/list?userId=${myUserId.value}`)
    followingList.value = res.data.data.map(item => item.following_id.toString())
  } catch (err) {
    console.error('加载关注列表失败:', err)
  }
}

// 检查是否关注
const isFollowing = (userId) => {
  return followingList.value.includes(userId.toString())
}

// 关注/取消关注
const toggleFollow = async (userId) => {
  try {
    if (isFollowing(userId)) {
      await axios.delete('http://localhost:8081/follow', {
        data: {
          followerId: myUserId.value,
          followingId: userId
        }
      })
      ElMessage.success('取消关注成功')
      followingList.value = followingList.value.filter(id => id !== userId.toString())
    } else {
      await axios.post('http://localhost:8081/follow', {
        followerId: myUserId.value,
        followingId: userId
      })
      ElMessage.success('关注成功')
      followingList.value.push(userId.toString())
    }
  } catch (err) {
    ElMessage.error('操作失败')
    console.error(err)
  }
}

const goTo = (path) => {
  router.push(path)
}

const toPublish = () => {
  router.push('/publish')
}

// 点赞
// ✅ 首页正确点赞（对接 /like/toggle）
const likePost = async (postId) => {
  try {
    let res = await axios.post("http://localhost:8081/like/toggle", {
      userId: localStorage.getItem("userId"),
      postId: postId,
    });
    ElMessage.success(res.data.data);

    // 刷新动态列表
    axios.get("http://localhost:8081/post/list").then((res) => {
      postList.value = res.data.data;
    });
  } catch (err) {
    ElMessage.success("操作成功");
    console.error(err);
  }
};

const showComment = async (postId) => {
  openCommentId.value = postId
  const res = await axios.get('http://localhost:8081/comment/list?postId=' + postId)
  commentList.value = res.data.data
}

// 发表评论
const openComment = async (postId) => {
  openCommentId.value = postId
  let res = await axios.get(`http://localhost:8081/comment/list?postId=${postId}`)
  commentList.value = res.data.data
}

// 发布自己的评论
const submitComment = async (postId) => {
  if (!commentText.value) {
    ElMessage.warning("请输入评论内容")
    return
  }
  let params = {
    postId: postId,
    userId: myUserId.value,
    nickname: myNickName.value,
    content: commentText.value,
    parentId: replyComment.value ? replyComment.value.id : null,
    replyToNickname: replyComment.value ? replyComment.value.nickname : null,
    replyToUserId: replyComment.value ? replyComment.value.userId : null
  }
  await axios.post("http://localhost:8081/comment/add", params)
  ElMessage.success("评论成功")
  commentText.value = ""
  replyComment.value = null
  // 发布后刷新评论列表
  let res = await axios.get(`http://localhost:8081/comment/list?postId=${postId}`)
  commentList.value = res.data.data
}

// 回复评论
const replyToComment = (comment) => {
  replyComment.value = comment
}

// 取消回复
const cancelReply = () => {
  replyComment.value = null
  commentText.value = ""
}

// 简洁时间格式化
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

// 跳转到用户详情页
const goToUserProfile = (userId) => {
  router.push(`/user/${userId}`)
}

// 关闭弹窗
const closeUserModal = () => {
  showUserModal.value = false
}

// 显示转发弹窗
const showRepostModal = (post) => {
  repostPost.value = post
  repostContent.value = ""
  isRepostModalVisible.value = true
}

// 关闭转发弹窗
const closeRepostModal = () => {
  isRepostModalVisible.value = false
  repostPost.value = {}
  repostContent.value = ""
}

// 提交转发
const submitRepost = async () => {
  if (!repostContent.value.trim()) {
    ElMessage.warning("请输入转发内容")
    return
  }
  try {
    await axios.post('http://localhost:8081/post/repost', {
      userId: myUserId.value,
      repostFromId: repostPost.value.id,
      repostContent: repostContent.value
    })
    ElMessage.success("转发成功")
    closeRepostModal()
    // 刷新动态列表
    axios.get('http://localhost:8081/post/list').then(res => {
      postList.value = res.data.data
    })
  } catch (err) {
    ElMessage.error("转发失败")
    console.error(err)
  }
}
</script>

<style scoped>
/* 全局 */
.home-page {
  background-color: #f4f5f7;
  min-height: 100vh;
}

/* 用户资料弹窗样式 */
.user-profile {
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.profile-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.profile-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.profile-actions {
  margin-left: auto;
}

.profile-posts {
  margin-top: 20px;
}

.profile-posts h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
}

.mini-post {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 10px;
}

.mini-post-content {
  font-size: 14px;
  line-height: 1.6;
}

.mini-post-images {
  display: flex;
  gap: 5px;
  margin-top: 10px;
}

.mini-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.mini-post-info {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

/* 顶部导航 */
.header {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

.wrap {
  width: 960px;
  height: 60px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #ff7d00;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nickname {
  color: #555;
  cursor: pointer;
}

/* 整体布局 */
.container {
  width: 960px;
  margin: 30px auto;
  display: flex;
  gap: 24px;
}

/* 左侧边栏 */
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

/* 中间内容 */
.main-content {
  flex: 1;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

/* 动态卡片 */
.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.post-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.user-avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.user-avatar-wrapper .name {
  font-weight: 600;
  color: #333;
  font-size: 13px;
  margin-top: 6px;
}

.post-meta {
  flex: 1;
}

.post-meta .time {
  font-size: 12px;
  color: #aaa;
  margin-top: 2px;
}

.post-text {
  color: #444;
  line-height: 1.7;
  font-size: 15px;
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

/* 可见性标识 */
.visibility-badge {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
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

.comment-box {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f5f5f7;
}

.comment-item {
  padding: 6px 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reply-btn {
  margin-left: auto;
  color: #999;
  font-size: 12px;
  cursor: pointer;
}

.reply-btn:hover {
  color: #ff7d00;
}

.comment-item .nick {
  color: #ff7d00;
  font-weight: 500;
}

.comment-input {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

/* 转发弹窗样式 */
.repost-modal {
  padding: 10px;
}

.repost-original {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 15px;
}

.repost-original-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.repost-original-name {
  font-weight: 500;
  color: #333;
}

.repost-original-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.repost-input-wrapper {
  margin-bottom: 15px;
}

.repost-textarea {
  width: 100%;
}

.repost-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>