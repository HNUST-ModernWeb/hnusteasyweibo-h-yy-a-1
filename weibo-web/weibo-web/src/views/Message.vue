<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="header">
      <div class="wrap">
        <div class="logo">社交分享平台</div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="message-page">
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
          <div class="side-item" @click="$router.push('/hot')">
            <span>🔥</span>
            <span>热门动态</span>
          </div>
          <div class="side-item active">
            <span>💬</span>
            <span>消息</span>
          </div>
        </div>

        <div class="main-content">
          <h2 class="msg-title">💬 消息通知</h2>

          <!-- 消息列表 点击查看详情 -->
          <div class="msg-card" v-for="item in msgList" :key="item.id" @click="openDetail(item.id)"
            :class="{ unread: item.is_read === 0 || item.is_read === false }">
            <div class="msg-icon">{{ getIcon(item.msg_type) }}</div>
            <div class="msg-content">
              <div class="msg-text">
                {{ item.content }}
              </div>
              <div class="msg-time">{{ formatTime(item.create_time) }}</div>
            </div>
            <div class="read-tag" v-if="item.is_read === 0 || item.is_read === false">未读</div>
          </div>

          <div v-if="msgList.length === 0" class="empty">暂无消息</div>
        </div>
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <div class="detail-mask" v-if="showDetail" @click.self="showDetail = false">
      <div class="detail-dialog">
        <h3 class="dialog-title">消息详情</h3>
        <div class="dialog-content">
          <p>
            <span style="color:#ff7d00;font-weight:bold;">{{ detailInfo.from_nickname }}</span>
            {{ getMessageText(detailInfo) }}
            <span v-if="detailInfo.content">：{{ detailInfo.content }}</span>
          </p>
        </div>
        <div class="dialog-time">发送时间：{{ formatTime(detailInfo.create_time) }}</div>
        <button class="close-btn" @click="showDetail = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const msgList = ref([])
const userId = localStorage.getItem('userId')

// 详情弹窗
const showDetail = ref(false)
const detailInfo = ref({})

// 获取消息列表
const getMsgList = async () => {
  try {
    const res = await axios.get('http://localhost:8081/message/list', {
      params: { userId }
    })
    msgList.value = res.data.data
  } catch (err) {
    console.error('消息加载失败', err)
  }
}

// 点击查看详情
const openDetail = async (id) => {
  try {
    // 标记已读
    await axios.put('http://localhost:8081/message/read', null, {
      params: { id }
    })

    // 获取详情
    const res = await axios.get('http://localhost:8081/message/detail', {
      params: { id }
    })
    detailInfo.value = res.data.data[0]
    showDetail.value = true

    // 刷新列表，未读消失
    getMsgList()
  } catch (e) {
    console.error(e)
  }
}

// 类型对应图标
const getIcon = (type) => {
  switch (type) {
    case 'like': return '👍'
    case 'comment': return '💬'
    case 'reply': return '↩️'
    case 'follow': return '👤'
    default: return '✨'
  }
}

// 获取消息文本
const getMessageText = (item) => {
  switch (item.type) {
    case 'like': return ' 点赞了你的动态'
    case 'comment': return ' 评论了你的动态'
    case 'reply': return ' 回复了你'
    case 'follow': return ' 关注了你'
    default: return ''
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  getMsgList()
})
</script>

<style scoped>
/* 你原有全局布局样式 完全保留 */
.page-container {
  background-color: #f4f5f7;
  min-height: 100vh;
}

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

.container {
  width: 960px;
  margin: 25px auto 0;
  display: flex;
  gap: 24px;
  padding: 0 20px;
  box-sizing: border-box;
}

.message-page {
  width: 100%;
}

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

.msg-title {
  font-size: 22px;
  margin-bottom: 18px;
  color: #333;
}

/* 消息条目 */
.msg-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  transition: 0.2s;
}

.msg-card:hover {
  background: #fafafa;
}

.msg-card.unread {
  border-left: 4px solid #ff4444;
}

.msg-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-content {
  flex: 1;
}

.msg-text {
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
}

.msg-time {
  font-size: 12px;
  color: #999;
}

.read-tag {
  color: #ff4444;
  font-size: 12px;
  border: 1px solid #ff4444;
  padding: 2px 6px;
  border-radius: 4px;
}

.empty {
  background: #fff;
  border-radius: 12px;
  padding: 50px;
  text-align: center;
  color: #999;
}

/* 详情弹窗样式 */
.detail-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.detail-dialog {
  width: 450px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.dialog-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.dialog-content p {
  line-height: 1.8;
  color: #555;
}

.dialog-time {
  margin: 15px 0;
  font-size: 13px;
  color: #999;
}

.close-btn {
  width: 100%;
  height: 36px;
  background: #ff7d00;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>