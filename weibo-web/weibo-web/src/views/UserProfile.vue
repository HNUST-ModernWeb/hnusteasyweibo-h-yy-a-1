<template>
  <div class="user-profile-page">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="wrap">
        <div class="nav-left" @click="goBack">
          ← 返回
        </div>
        <div class="logo"></div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="container">
      <!-- 用户头部信息 -->
      <div class="profile-header">
        <el-avatar size="120" :src="userInfo.avatar">
          {{ userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}
        </el-avatar>
        <div class="profile-basic">
          <h2>{{ userInfo.nickname || '未知用户' }}</h2>
          <p>账号：{{ userInfo.username }}</p>
          <p>{{ userInfo.bio || '暂无简介' }}</p>
        </div>
        <div v-if="userId !== myUserId" class="profile-actions">
          <el-button type="primary" :plain="isFollowingUser" @click="toggleFollow">
            {{ isFollowingUser ? '已关注' : '+ 关注' }}
          </el-button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-value">{{ userPosts.length }}</span>
          <span class="stat-label">动态</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ followingCount }}</span>
          <span class="stat-label">关注</span>
        </div>
      </div>

      <!-- 用户资料 -->
      <div class="profile-info">
        <div class="info-item">
          <span class="info-label">性别：</span>
          <span>{{ userInfo.gender === 'male' ? '男' : userInfo.gender === 'female' ? '女' : '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">生日：</span>
          <span>{{ userInfo.birthday || '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">个性域名：</span>
          <span>{{ userInfo.domain || '未设置' }}</span>
        </div>
      </div>

      <!-- 用户动态列表 -->
      <div class="posts-section">
        <h3>TA的动态</h3>
        <div v-if="userPosts.length === 0" class="empty-tip">
          暂无动态
        </div>
        <div class="post-card" v-for="post in userPosts" :key="post.id">
          <div class="post-content">{{ post.content }}</div>
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <el-image v-for="(img, idx) in post.images" :key="idx" :src="img" class="post-image" fit="cover" />
          </div>
          <div v-if="post.tags && post.tags.length > 0" class="post-tags">
            <el-tag v-for="(tag, idx) in post.tags" :key="idx" class="post-tag">#{{ tag }}</el-tag>
          </div>
          <div class="post-footer">
            <span>{{ formatTime(post.create_time) }}</span>
            <span>👍 {{ post.likeCount || 0 }}</span>
            <span>💬 评论</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userId = ref(route.params.userId)
const myUserId = ref(localStorage.getItem('userId'))
const userInfo = ref({})
const userPosts = ref([])
const followerCount = ref(0)
const followingCount = ref(0)
const isFollowingUser = ref(false)

const goBack = () => {
  router.back()
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

const loadUserInfo = async () => {
  try {
    const res = await axios.get(`http://localhost:8081/user/info?userId=${userId.value}`)
    userInfo.value = res.data.data
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}

const loadUserPosts = async () => {
  try {
    const res = await axios.get(`http://localhost:8081/post/user/${userId.value}`)
    userPosts.value = res.data.data
  } catch (err) {
    console.error('获取用户动态失败:', err)
  }
}

const loadStats = async () => {
  try {
    const followerRes = await axios.get(`http://localhost:8081/follow/followerCount?userId=${userId.value}`)
    followerCount.value = followerRes.data.data || 0

    const followingRes = await axios.get(`http://localhost:8081/follow/followingCount?userId=${userId.value}`)
    followingCount.value = followingRes.data.data || 0

    const isFollowingRes = await axios.get(`http://localhost:8081/follow/isFollowing?followerId=${myUserId.value}&followingId=${userId.value}`)
    isFollowingUser.value = isFollowingRes.data.data || false
  } catch (err) {
    console.error('获取统计信息失败:', err)
  }
}

const toggleFollow = async () => {
  try {
    if (isFollowingUser.value) {
      await axios.delete('http://localhost:8081/follow', {
        data: {
          followerId: myUserId.value,
          followingId: userId.value
        }
      })
      ElMessage.success('取消关注成功')
      isFollowingUser.value = false
      followerCount.value--
    } else {
      await axios.post('http://localhost:8081/follow', {
        followerId: myUserId.value,
        followingId: userId.value
      })
      ElMessage.success('关注成功')
      isFollowingUser.value = true
      followerCount.value++
    }
  } catch (err) {
    ElMessage.error('操作失败')
    console.error(err)
  }
}

onMounted(() => {
  loadUserInfo()
  loadUserPosts()
  loadStats()
})
</script>

<style scoped>
.user-profile-page {
  background-color: #f4f5f7;
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  padding: 10px 0;
}

.wrap {
  width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  cursor: pointer;
  color: #666;
  font-size: 14px;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #e6162d;
}

.container {
  width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}

.profile-header {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.profile-basic h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.profile-basic p {
  margin: 5px 0;
  color: #666;
}

.profile-actions {
  margin-left: auto;
}

.profile-stats {
  background: #fff;
  padding: 20px 30px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  gap: 80px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.profile-info {
  background: #fff;
  padding: 20px 30px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.info-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #999;
}

.posts-section {
  background: #fff;
  padding: 20px 30px;
  border-radius: 12px;
}

.posts-section h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.post-card {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.post-card:last-child {
  border-bottom: none;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.post-image {
  width: 180px;
  height: 180px;
  border-radius: 8px;
}

.post-tags {
  margin-bottom: 15px;
}

.post-tag {
  margin-right: 10px;
  background: #f5f5f5;
  border: none;
}

.post-footer {
  display: flex;
  gap: 30px;
  font-size: 14px;
  color: #999;
}
</style>
