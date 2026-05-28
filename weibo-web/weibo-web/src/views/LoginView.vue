<template>
  <div style="width: 100vw; height: 100vh; display: flex; justify-content: center; align-items: center; background: #f5f7fa;">
    <div style="width: 420px; padding: 45px; background: #fff; border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.08);">
      <h2 style="text-align: center; margin-bottom: 35px; color: #222; font-weight: 600;">欢迎来到社交分享平台</h2>

      <el-input
          v-model="form.username"
          placeholder="请输入账号"
          clearable
          style="margin-bottom: 20px; height: 44px;"
      />

      <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          style="margin-bottom: 25px; height: 44px;"
      />

      <el-button
          type="primary"
          @click="handleLogin"
          style="width: 100%; height: 46px; font-size: 16px; margin-bottom: 12px;">
        账号登录
      </el-button>

      <el-button
          @click="handleRegister"
          style="width: 100%; height: 46px; font-size: 16px;">
        注册新账号
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const form = ref({
  username: '',
  password: ''
})

// 登录
const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  try {
    const res = await axios.post('http://localhost:8081/user/login', form.value)

    ElMessage.success('登录成功！')

    // 存入用户信息（社交平台必备）
    localStorage.setItem('userId', res.data.data.id)
    localStorage.setItem('username', res.data.data.username)
    localStorage.setItem('nickname', res.data.data.nickname)

    router.push('/home')
  } catch (err) {
    ElMessage.error('账号或密码错误')
  }
}

// 跳转到注册页
const handleRegister = () => {
  router.push('/register')
}
</script>