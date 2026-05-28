<template>
  <div class="message-container">
    <div class="message-header">
      <h2>消息通知</h2>
      <el-button type="primary" size="small" @click="markAllRead">全部已读</el-button>
    </div>

    <div v-if="messageList.length === 0" class="empty-message">
      暂无消息
    </div>

    <div class="message-list">
      <div 
        v-for="msg in messageList" 
        :key="msg.id" 
        class="message-item"
        :class="{ 'unread': !msg.is_read }"
        @click="markAsRead(msg.id)"
      >
        <div class="message-avatar">
          <el-avatar size="48" :src="msg.from_avatar">
            {{ msg.from_nickname ? msg.from_nickname.charAt(0) : '?' }}
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-title">
            <span class="nickname">{{ msg.from_nickname }}</span>
            <span class="type-tag">{{ getTypeText(msg.type) }}</span>
          </div>
          <div class="message-body">
            <template v-if="msg.type === 'like'">
              点赞了你的动态
            </template>
            <template v-else-if="msg.type === 'comment'">
              评论了你的动态：{{ msg.content }}
            </template>
            <template v-else-if="msg.type === 'reply'">
              回复了你：{{ msg.content }}
            </template>
            <template v-else-if="msg.type === 'follow'">
              关注了你
            </template>
          </div>
          <div class="message-time">{{ formatTime(msg.create_time) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userId = ref(1)
const messageList = ref([])

const loadMessages = async () => {
  try {
    const res = await axios.get(`http://localhost:8081/message/list?userId=${userId.value}`)
    messageList.value = res.data.data
  } catch (err) {
    console.error(err)
  }
}

const markAsRead = async (id) => {
  try {
    await axios.post(`http://localhost:8081/message/readOne?id=${id}`)
    const msg = messageList.value.find(m => m.id === id)
    if (msg) {
      msg.is_read = true
    }
  } catch (err) {
    console.error(err)
  }
}

const markAllRead = async () => {
  try {
    await axios.post(`http://localhost:8081/message/read?userId=${userId.value}`)
    messageList.value.forEach(msg => {
      msg.is_read = true
    })
    ElMessage.success('已全部标记为已读')
  } catch (err) {
    console.error(err)
  }
}

const getTypeText = (type) => {
  const map = {
    'like': '点赞',
    'comment': '评论',
    'reply': '回复',
    'follow': '关注'
  }
  return map[type] || type
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-container {
  padding: 20px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.message-header h2 {
  margin: 0;
  font-size: 20px;
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 50px;
}

.message-list {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f9f9f9;
}

.message-item.unread {
  background-color: #fff9f9;
}

.message-avatar {
  margin-right: 12px;
}

.message-content {
  flex: 1;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-weight: bold;
  color: #333;
}

.type-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background-color: #ff7d00;
  color: white;
}

.message-body {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}
</style>