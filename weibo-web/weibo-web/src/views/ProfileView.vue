<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <div class="nav-wrap">
        <div class="logo">社交分享平台</div>
        <div class="nav-user">
          <el-button @click="$router.push('/home')">首页</el-button>
          <el-button type="danger" @click="logout">退出登录</el-button>
        </div>
      </div>
    </div>

    <div class="main-container">
      <!-- 左侧 -->
      <div class="left-box">
        <div class="user-card">
          <el-avatar size="80" :src="avatarUrl">
            {{ nickname ? nickname.charAt(0) : '?' }}
          </el-avatar>
          <div class="name">{{ nickname }}</div>
          <div class="account">账号：{{ username }}</div>
          <div class="desc">{{ domain || username }}</div>
        </div>

        <div class="menu-card">
          <div class="menu-item" :class="{ active: activeKey === 'post' }" @click="activeKey = 'post'">
            📝 我的动态
          </div>
          <div class="menu-item" :class="{ active: activeKey === 'like' }" @click="getMyLikedPosts">
            ❤️ 我的点赞
          </div>
          <div class="menu-item" :class="{ active: activeKey === 'following' }" @click="getFollowingList">
            👥 我的关注
          </div>
          <div class="menu-item" :class="{ active: activeKey === 'setting' }" @click="activeKey = 'setting'">
            ⚙️ 个人设置
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="right-box">
        <!-- 我的动态 -->
        <div v-if="activeKey === 'post'">
          <div class="title-bar">
            <h3>我的发布</h3>
            <el-button type="primary" @click="$router.push('/publish')">发布新动态</el-button>
          </div>

          <div v-if="myPostList.length === 0" class="empty">
            <el-empty description="你还没有发布任何动态" />
          </div>

          <div class="post-item" v-for="item in myPostList" :key="item.id">
            <div class="post-header">
              <div class="post-time">{{ formatTime(item.createTime) }}</div>
              <el-button size="small" type="danger" plain @click="deletePost(item.id)">删除</el-button>
            </div>
            <div class="post-content">{{ item.content }}</div>

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

            <!-- 操作栏 -->
            <div class="post-action">
              <div class="action-btn" @click="likePost(item)">👍 点赞 {{ item.likeCount || 0 }}</div>
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
              <input v-model="commentContent" placeholder="写下你的评论..." @keyup.enter="submitComment(item.id)"
                class="comment-input" />
            </div>
          </div>
        </div>

        <!-- 我的点赞 -->
        <div v-if="activeKey === 'like'">
          <div class="title-bar">
            <h3>我点赞的动态</h3>
          </div>

          <div v-if="likePostList.length === 0" class="empty">
            <el-empty description="你还没有点赞任何动态" />
          </div>

          <div class="post-item" v-for="item in likePostList" :key="item.id">
            <div class="post-header">
              <div class="user-info">
                <el-avatar size="48" :src="item.avatar">
                  {{ item.nickname ? item.nickname.charAt(0) : '?' }}
                </el-avatar>
                <div class="user-name">{{ item.nickname }}</div>
              </div>
              <div class="post-time">{{ formatTime(item.create_time) }}</div>
            </div>
            <div class="post-content">{{ item.content }}</div>
            <!-- 图片展示 -->
            <div v-if="item.images && item.images.length > 0" class="post-images">
              <el-image v-for="(img, index) in item.images" :key="index" :src="img" class="post-image" fit="cover" />
            </div>
            <!-- 操作栏 -->
            <div class="post-action">
              <div class="action-btn" @click="likePost(item)">👍 点赞 {{ item.likeCount || 0 }}</div>
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
              <input v-model="commentContent" placeholder="写下你的评论..." @keyup.enter="submitComment(item.id)"
                class="comment-input" />
            </div>
          </div>
        </div>

        <!-- 我的关注 -->
        <div v-if="activeKey === 'following'">
          <div class="title-bar">
            <h3>我的关注</h3>
            <span class="count">共 {{ followingList.length }} 人</span>
          </div>

          <div v-if="followingList.length === 0" class="empty">
            <el-empty description="你还没有关注任何人" />
          </div>

          <div class="following-list">
            <div class="following-item" v-for="user in followingList" :key="user.following_id">
              <el-avatar size="50" :src="user.avatar">
                {{ user.nickname ? user.nickname.charAt(0) : '?' }}
              </el-avatar>
              <div class="following-info">
                <div class="following-name">{{ user.nickname }}</div>
              </div>
              <el-button size="small" type="danger" plain @click="unfollowUser(user.following_id)">
                取消关注
              </el-button>
            </div>
          </div>
        </div>

        <!-- 个人设置 -->
        <div v-if="activeKey === 'setting'">
          <div class="title-bar">
            <h3>账号信息设置</h3>
          </div>

          <!-- 头像设置 -->
          <div class="profile-section">
            <div class="section-title">头像设置</div>
            <div class="avatar-setting">
              <div class="avatar-preview">
                <el-image
                  :src="avatarUrl || 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=default%20avatar%20portrait&image_size=square'"
                  fit="cover" class="avatar-img" />
              </div>
              <div class="avatar-actions">
                <el-upload action="http://localhost:8081/upload/image" :show-file-list="false"
                  :on-success="handleAvatarUpload" :before-upload="beforeAvatarUpload" accept="image/*" name="file">
                  <el-button size="small" type="primary">更换头像</el-button>
                </el-upload>
                <p class="avatar-tip">支持JPG、PNG格式，建议尺寸200x200</p>
              </div>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="profile-section">
            <div class="section-title">基本信息</div>

            <div class="form-item">
              <label class="form-label">昵称</label>
              <div class="form-content">
                <el-input v-model="newNickname" placeholder="请输入昵称" style="width: 250px;" />
                <span class="form-hint">4~30个字符，支持中英文、数字</span>
                <el-button type="primary" size="small" @click="updateNickname" style="margin-left: 10px;">保存</el-button>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">简介</label>
              <div class="form-content">
                <el-input v-model="bio" type="textarea" :rows="3" placeholder="介绍一下自己吧" style="width: 350px;" />
                <span class="form-hint">1~140个字符</span>
                <el-button type="primary" size="small" @click="updateBio" style="margin-left: 10px;">保存</el-button>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">个性名</label>
              <div class="form-content">

                <el-input v-model="domain" placeholder="输入个性域名" style="width: 150px;" />
                <span class="form-hint">4~20个字符的数字或字母，设置后不可更改</span>
                <el-button type="primary" size="small" @click="updateDomain" style="margin-left: 10px;">保存</el-button>
              </div>
            </div>
          </div>

          <!-- 个人资料 -->
          <div class="profile-section">
            <div class="section-title">个人基本资料</div>

            <div class="form-item">
              <label class="form-label">性别</label>
              <div class="form-content">
                <label class="radio-label">
                  <input type="radio" v-model="gender" value="male" /> 男
                </label>
                <label class="radio-label">
                  <input type="radio" v-model="gender" value="female" /> 女
                </label>
                <el-button type="primary" size="small" @click="updateGender" style="margin-left: 10px;">保存</el-button>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">生日</label>
              <div class="form-content">
                <el-select v-model="birthYear" placeholder="年" style="width: 100px;">
                  <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
                </el-select>
                <el-select v-model="birthMonth" placeholder="月" style="width: 80px;">
                  <el-option v-for="month in monthOptions" :key="month" :label="month" :value="month" />
                </el-select>
                <el-select v-model="birthDay" placeholder="日" style="width: 80px;">
                  <el-option v-for="day in dayOptions" :key="day" :label="day" :value="day" />
                </el-select>
                <el-button type="primary" size="small" @click="updateBirthday" style="margin-left: 10px;">保存</el-button>
              </div>
            </div>
          </div>

          <!-- 账号安全 -->
          <div class="profile-section">
            <div class="section-title">账号安全</div>

            <div class="form-item">
              <label class="form-label">修改密码</label>
              <div class="form-content">
                <el-input v-model="newPassword" placeholder="请输入新密码" type="password" style="width: 250px;" />
                <el-button type="danger" size="small" @click="updatePassword"
                  style="margin-left: 10px;">确认修改</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElEmpty } from 'element-plus'

const router = useRouter()
const activeKey = ref('post')
const myPostList = ref([])
const likePostList = ref([])
const allCommentList = ref([])
const followingList = ref([])
const openCommentId = ref(null)
const commentContent = ref('')
const commentList = ref([])
const repostPost = ref(null)
const repostContent = ref('')
const isRepostModalVisible = ref(false)

const userId = ref(localStorage.getItem('userId'))
const nickname = ref(localStorage.getItem('nickname'))
const username = ref(localStorage.getItem('username'))
const newNickname = ref('')
const newPassword = ref('')
const avatarUrl = ref(localStorage.getItem('avatar') || '')
const bio = ref('')
const domain = ref('')
const gender = ref('female')
const birthYear = ref('')
const birthMonth = ref('')
const birthDay = ref('')

// 年份选项
const yearOptions = Array.from({ length: 60 }, (_, i) => 2024 - i)
// 月份选项
const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)
// 日期选项
const dayOptions = Array.from({ length: 31 }, (_, i) => i + 1)
onMounted(() => {
  // 加载我的动态
  axios.get(`http://localhost:8081/post/my?userId=${userId.value}`).then(res => {
    myPostList.value = res.data.data
  })

  // 加载所有评论
  axios.get('http://localhost:8081/comment/all').then(res => {
    allCommentList.value = res.data.data
  })

  // 加载用户信息到表单
  loadUserInfo()
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await axios.get(`http://localhost:8081/user/info?userId=${userId.value}`)
    const user = res.data.data

    // 填充表单
    newNickname.value = user.nickname || ''
    bio.value = user.bio || ''
    domain.value = user.domain || ''
    gender.value = user.gender || 'female'

    // 解析生日
    if (user.birthday) {
      const birthday = user.birthday.split('-')
      birthYear.value = birthday[0] || ''
      birthMonth.value = birthday[1] || ''
      birthDay.value = birthday[2] || ''
    }

    // 更新昵称显示
    if (user.nickname) {
      nickname.value = user.nickname
      localStorage.setItem('nickname', user.nickname)
    }

    // 更新头像显示
    if (user.avatar) {
      avatarUrl.value = user.avatar
      localStorage.setItem('avatar', user.avatar)
    }
  } catch (err) {
    console.error('加载用户信息失败:', err)
  }
}

// 点赞功能（支持取消点赞）
const likePost = async (item) => {
  try {
    let res = await axios.post("http://localhost:8081/like/toggle", {
      userId: userId.value,
      postId: item.id
    })
    if (res.data.data === "点赞成功") {
      item.likeCount = (item.likeCount || 0) + 1
    } else if (res.data.data === "取消点赞成功") {
      item.likeCount = (item.likeCount || 0) - 1
    }
    ElMessage.success(res.data.data);
  } catch (err) {
    ElMessage.success("操作成功");
    console.error(err);
  }
};

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
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    let params = {
      postId: postId,
      userId: parseInt(userId.value),
      nickname: nickname.value,
      content: commentContent.value
    }
    await axios.post('http://localhost:8081/comment/add', params)
    ElMessage.success('评论成功')
    commentContent.value = ''
    // 刷新评论列表
    let res = await axios.get(`http://localhost:8081/comment/list?postId=${postId}`)
    commentList.value = res.data.data
    // 更新评论数量（同时更新我的动态和我点赞的动态）
    const post1 = myPostList.value.find(p => p.id === postId)
    if (post1) {
      post1.commentCount = (post1.commentCount || 0) + 1
    }
    const post2 = likePostList.value.find(p => p.id === postId)
    if (post2) {
      post2.commentCount = (post2.commentCount || 0) + 1
    }
  } catch (err) {
    ElMessage.error('评论失败')
    console.error(err)
  }
}

// 显示转发弹窗
const showRepostModal = (post) => {
  repostPost.value = post
  repostContent.value = ""
  isRepostModalVisible.value = true
}

// 获取我点赞的动态
const getMyLikedPosts = async () => {
  activeKey.value = 'like'
  let res = await axios.get('http://localhost:8081/like/mine?userId=' + userId.value)
  likePostList.value = res.data.data
}

// 获取我的关注列表
const getFollowingList = async () => {
  activeKey.value = 'following'
  try {
    let res = await axios.get(`http://localhost:8081/follow/list?userId=${userId.value}`)
    followingList.value = res.data.data
  } catch (err) {
    ElMessage.error('获取关注列表失败')
    console.error(err)
  }
}

// 删除动态
const deletePost = async (postId) => {
  try {
    await axios.delete(`http://localhost:8081/post/${postId}`)
    ElMessage.success('删除成功')
    myPostList.value = myPostList.value.filter(item => item.id !== postId)
  } catch (err) {
    ElMessage.error('删除失败')
    console.error(err)
  }
}

// 取消关注
const unfollowUser = async (followingId) => {
  try {
    await axios.delete('http://localhost:8081/follow', {
      data: {
        followerId: userId.value,
        followingId: followingId
      }
    })
    ElMessage.success('取消关注成功')
    followingList.value = followingList.value.filter(item => item.following_id !== followingId)
  } catch (err) {
    ElMessage.error('取消关注失败')
    console.error(err)
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

// 头像上传成功处理
const handleAvatarUpload = async (res) => {
  console.log('头像上传响应:', res)
  if (res && res.code === 200) {
    avatarUrl.value = res.msg
    // 保存到localStorage，用于页面显示
    localStorage.setItem('avatar', res.msg)
    // 保存到数据库
    try {
      await axios.post('http://localhost:8081/user/update', {
        userId: userId.value,
        avatar: res.msg
      })
      ElMessage.success('头像上传成功')
    } catch (err) {
      ElMessage.error('头像保存到数据库失败')
      console.error(err)
    }
  } else {
    ElMessage.error('头像上传失败')
  }
}

// 更新简介
const updateBio = async () => {
  try {
    await axios.post('http://localhost:8081/user/update', {
      userId: userId.value,
      bio: bio.value
    })
    ElMessage.success('简介更新成功')
  } catch (err) {
    ElMessage.error('更新失败')
    console.error(err)
  }
}

// 更新个性域名
const updateDomain = async () => {
  try {
    await axios.post('http://localhost:8081/user/update', {
      userId: userId.value,
      domain: domain.value
    })
    ElMessage.success('域名更新成功')
  } catch (err) {
    ElMessage.error('更新失败')
    console.error(err)
  }
}

// 更新性别
const updateGender = async () => {
  try {
    await axios.post('http://localhost:8081/user/update', {
      userId: userId.value,
      gender: gender.value
    })
    ElMessage.success('性别更新成功')
  } catch (err) {
    ElMessage.error('更新失败')
    console.error(err)
  }
}

// 更新生日
const updateBirthday = async () => {
  if (!birthYear.value || !birthMonth.value || !birthDay.value) {
    ElMessage.warning('请选择完整日期')
    return
  }
  try {
    await axios.post('http://localhost:8081/user/update', {
      userId: userId.value,
      birthday: `${birthYear.value}-${birthMonth.value}-${birthDay.value}`
    })
    ElMessage.success('生日更新成功')
  } catch (err) {
    ElMessage.error('更新失败')
    console.error(err)
  }
}

// 退出登录
const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出登录')
  router.push('/login')
}
const updateNickname = async () => {
  if (!newNickname.value) {
    ElMessage.warning('请输入昵称')
    return
  }
  try {
    await axios.post("http://localhost:8081/user/updateNickname", {
      userId: userId.value,
      nickname: newNickname.value
    })
    ElMessage.success("昵称修改成功！")
    localStorage.setItem("nickname", newNickname.value)
    nickname.value = newNickname.value
  } catch (e) {
    ElMessage.error("修改失败")
  }
}

// 修改密码
const updatePassword = async () => {
  if (!newPassword.value) {
    ElMessage.warning("请输入新密码")
    return
  }
  try {
    await axios.post("http://localhost:8081/user/updatePwd", {
      userId: userId.value,
      password: newPassword.value
    })
    ElMessage.success("密码修改成功！")
    newPassword.value = ""
  } catch (e) {
    ElMessage.error("修改失败")
  }
}
// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.page-container {
  background-color: #f4f5f7;
  min-height: 100vh;
}

.top-nav {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

.nav-wrap {
  width: 960px;
  height: 65px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 19px;
  font-weight: 700;
  color: #ff8200;
}

.main-container {
  width: 960px;
  margin: 25px auto 0;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  padding: 0 20px;
  box-sizing: border-box;
}

.left-box {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.name {
  font-size: 18px;
  font-weight: 600;
  margin: 10px 0 4px;
}

.account,
.desc {
  font-size: 13px;
  color: #999;
  margin: 4px 0;
}

.menu-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item {
  padding: 14px 20px;
  font-size: 15px;
  cursor: pointer;
}

.menu-item.active {
  background: #fff2e8;
  color: #ff8200;
}

.right-box {
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  min-height: 500px;
}

.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.empty {
  padding: 60px 0;
}

.post-item {
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;
}

.post-time {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.post-content {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  margin-bottom: 12px;
}

.post-author {
  font-size: 13px;
  color: #ff7d00;
  margin-bottom: 8px;
}

.post-action-row {
  display: flex;
  gap: 30px;
  border-top: 1px solid #f5f5f7;
  padding-top: 12px;
}

.action-btn {
  color: #777;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  color: #ff7d00;
}

.post-action {
  display: flex;
  gap: 40px;
  border-top: 1px solid #f5f5f5;
  padding-top: 14px;
  margin-top: 14px;
}

.comment-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f5f5f5;
}

.comment-list {
  margin-bottom: 12px;
}

.comment-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
}

.comment-box {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.comment-item {
  padding: 6px 0;
  font-size: 14px;
}

.comment-item .nick {
  color: #ff7d00;
  font-weight: 500;
}

.no-comment {
  font-size: 13px;
  color: #ccc;
}

.comment-input {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

/* 个人设置页面样式 */
.profile-section {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px #00000008;
}

.section-title {
  padding: 18px 24px;
  border-bottom: 1px solid #f5f5f5;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fafafa;
  border-radius: 12px 12px 0 0;
}

/* 头像设置 */
.avatar-setting {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 24px;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #f0f0f0;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-tip {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 表单项目 */
.form-item {
  padding: 16px 24px;
  border-bottom: 1px dashed #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  display: inline-block;
  width: 100px;
  font-size: 14px;
  color: #666;
  vertical-align: top;
}

.form-content {
  display: inline-block;
  vertical-align: top;
}

.form-hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.domain-prefix {
  color: #999;
  font-size: 14px;
}

.radio-label {
  margin-right: 20px;
  font-size: 14px;
  color: #333;
}

/* 我的动态头部（包含时间和删除按钮） */
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

/* 关注列表 */
.count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
}

.following-list {
  margin-top: 10px;
}

.following-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.following-info {
  flex: 1;
  margin-left: 12px;
}

.following-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

/* 动态图片 */
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0;
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
</style>