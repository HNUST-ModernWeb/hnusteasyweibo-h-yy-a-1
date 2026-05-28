<template>
  <div class="publish-page">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <div class="nav-wrap">
        <div class="logo">社交分享平台</div>
        <div class="nav-user">
          <el-avatar />
          <span>{{ nickname }}</span>
          <el-button @click="router.push('/home')">返回首页</el-button>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="publish-card">
        <h2>✍️ 发布新动态</h2>
        <p class="tip">记录生活、分享心情、发表你的想法</p>

        <el-input v-model="content" type="textarea" :rows="6" placeholder="请输入内容，文明发言～" maxlength="500" show-word-limit
          class="text-area" />

        <!-- 图片上传区域 -->
        <div class="upload-section">
          <div class="upload-header">
            <span class="upload-label">📷 添加图片</span>
            <el-button size="small" @click="toggleEmojiPicker">
              {{ showEmojiPicker ? '收起表情' : '😊 添加表情' }}
            </el-button>
          </div>

          <!-- 表情选择器 -->
          <div v-if="showEmojiPicker" class="emoji-picker">
            <div class="emoji-grid">
              <span v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">
                {{ emoji }}
              </span>
            </div>
          </div>

          <!-- 图片列表 -->
          <div class="image-list" v-if="imageList.length > 0">
            <div v-for="(img, index) in imageList" :key="index" class="image-item">
              <el-image :src="img" fit="cover" />
              <span class="remove-btn" @click="removeImage(index)">×</span>
            </div>
          </div>

          <el-upload action="http://localhost:8081/upload/image" :show-file-list="false"
            :on-success="handleImageSuccess" :before-upload="beforeImageUpload" accept="image/*" name="file">
            <el-button size="small" type="primary" plain>
              <el-icon>
                <Plus />
              </el-icon> 选择图片
            </el-button>
          </el-upload>
          <span class="upload-tip">最多上传9张图片</span>
        </div>

        <!-- 标签区域 -->
        <div class="tag-section">
          <span class="tag-label">🏷️ 添加标签</span>
          <div class="tag-input-wrapper">
            <el-input v-model="tagInput" placeholder="输入标签后按回车添加" size="small" @keyup.enter="addTag"
              style="width: 200px;" />
            <el-button size="small" @click="addTag" type="primary" plain>添加</el-button>
          </div>
          <div class="tag-list" v-if="tags.length > 0">
            <el-tag v-for="(tag, index) in tags" :key="index" closable @close="removeTag(index)" class="tag-item">
              {{ tag }}
            </el-tag>
          </div>
          <div class="common-tags">
            <span class="common-label">常用标签：</span>
            <span v-for="tag in commonTags" :key="tag" class="common-tag" @click="quickAddTag(tag)">
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 可见性设置 -->
        <div class="visibility-section">
          <span class="visibility-label">👁️ 可见范围</span>
          <div class="visibility-options">
            <label v-for="option in visibilityOptions" :key="option.value" class="visibility-option">
              <input type="radio" v-model="visibility" :value="option.value" />
              <span>{{ option.label }}</span>
            </label>
          </div>
        </div>

        <div class="btn-box">
          <el-button size="large" @click="router.back()">取消</el-button>
          <el-button type="primary" size="large" @click="submitPublish">
            立即发布
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const content = ref('')
const nickname = ref(localStorage.getItem('nickname'))
const imageList = ref([])
const tags = ref([])
const tagInput = ref('')
const showEmojiPicker = ref(false)

const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
  '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
  '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫',
  '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬',
  '😮‍💨', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕',
  '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳',
  '🥸', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯',
  '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭',
  '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡',
  '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺',
  '👻', '👽', '👾', '🤖', '😺', '😸', '😹', '😻', '😼', '😽',
  '🙀', '😿', '😾', '🙈', '🙉', '🙊', '💋', '💌', '💘', '💝',
  '💖', '💗', '💓', '💞', '💕', '💟', '❣️', '💔', '❤️', '🧡',
  '💛', '💚', '💙', '💜', '🤎', '🖤', '🤍', '💯', '💢', '💥',
  '💫', '💦', '💨', '🕳️', '💣', '💬', '👋', '👍', '👎', '👏',
  '🙌', '🤝', '🙏', '💪', '🤘', '👌', '✌️', '🤞', '👈', '👉',
  '👆', '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤙', '💋',
  '🌹', '🌸', '🌺', '🌻', '🌼', '🌷', '🌱', '🌲', '🌳', '🌴',
  '🌵', '🌾', '🌿', '☘️', '🍀', '🍁', '🍂', '🍃', '🍄', '🌰',
  '🎃', '🎄', '🎆', '🎇', '🧨', '🎗️', '🎟️', '🎫', '🏮', '�灯笼'
]

const commonTags = ['日常', '美食', '旅行', '摄影', '穿搭', '美妆', '健身', '读书', '影视', '音乐']
const visibility = ref('public')
const visibilityOptions = [
  { value: 'public', label: '👥 全部人可见' },
  { value: 'private', label: '🔒 仅自己可见' },
  { value: 'friends', label: '👫 仅关注的人可见' }
]

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  content.value += emoji
}

const handleImageSuccess = (res) => {
  console.log('上传响应:', res)
  if (imageList.value.length >= 9) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  if (!res) {
    ElMessage.error('上传失败，服务器未返回数据')
    return
  }
  if (res.code && res.code !== 200) {
    ElMessage.error('上传失败: ' + (res.msg || '未知错误'))
    return
  }
  const imageUrl = res.msg || res.data?.data || res.data
  if (!imageUrl) {
    ElMessage.error('上传失败，图片URL为空')
    return
  }
  imageList.value.push(imageUrl)
  ElMessage.success('图片上传成功')
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const removeImage = (index) => {
  imageList.value.splice(index, 1)
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (!tag) {
    ElMessage.warning('请输入标签内容')
    return
  }
  if (tags.value.includes(tag)) {
    ElMessage.warning('该标签已添加')
    return
  }
  if (tags.value.length >= 10) {
    ElMessage.warning('最多添加10个标签')
    return
  }
  tags.value.push(tag)
  tagInput.value = ''
}

const quickAddTag = (tag) => {
  if (tags.value.includes(tag)) {
    ElMessage.warning('该标签已添加')
    return
  }
  if (tags.value.length >= 10) {
    ElMessage.warning('最多添加10个标签')
    return
  }
  tags.value.push(tag)
}

const removeTag = (index) => {
  tags.value.splice(index, 1)
}

// 发布
const submitPublish = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  try {
    await axios.post('http://localhost:8081/post/publish', {
      userId: localStorage.getItem('userId'),
      content: content.value.trim(),
      images: imageList.value,
      tags: tags.value,
      visibility: visibility.value
    })
    ElMessage.success('发布成功！')
    router.push('/home')
  } catch (err) {
    ElMessage.error('发布失败，请重试')
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.top-nav {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.nav-wrap {
  width: 1200px;
  margin: 0 auto;
  height: 65px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 19px;
  font-weight: 700;
  color: #ff8200;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.container {
  width: 1200px;
  margin: 40px auto;
  display: flex;
  justify-content: center;
}

.publish-card {
  width: 680px;
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.publish-card h2 {
  color: #333;
  margin: 0 0 8px;
}

.tip {
  color: #999;
  font-size: 14px;
  margin-bottom: 24px;
}

.text-area {
  margin-bottom: 20px;
}

/* 图片上传区域 */
.upload-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.upload-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.upload-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

/* 表情选择器 */
.emoji-picker {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 4px;
}

.emoji-item {
  font-size: 22px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: #f0f0f0;
}

/* 图片列表 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.remove-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}

/* 标签区域 */
.tag-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.tag-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  display: block;
  margin-bottom: 12px;
}

.tag-input-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-item {
  background: #fff2e8;
  color: #ff7d00;
  border-color: #ff7d00;
}

.common-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.common-label {
  font-size: 12px;
  color: #999;
}

.common-tag {
  font-size: 12px;
  color: #ff7d00;
  background: #fff2e8;
  padding: 2px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.common-tag:hover {
  background: #ffd4b8;
}

/* 可见性设置 */
.visibility-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.visibility-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  display: block;
  margin-bottom: 12px;
}

.visibility-options {
  display: flex;
  gap: 20px;
}

.visibility-option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
}

.visibility-option input[type="radio"] {
  width: 16px;
  height: 16px;
  accent-color: #ff7d00;
}

.visibility-option:hover {
  color: #ff7d00;
}

.btn-box {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}
</style>