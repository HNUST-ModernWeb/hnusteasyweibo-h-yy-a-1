<template>
  <div style="width: 100vw; height: 100vh; display: flex; justify-content: center; align-items: center; background: #f5f7fa;">
    <div style="width: 420px; padding: 45px; background: #fff; border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.08);">
      <h2 style="text-align: center; margin-bottom: 35px; color: #222; font-weight: 600;">用户注册</h2>

      <el-input v-model="form.username" placeholder="请输入账号" style="margin-bottom: 20px; height: 44px;" />
      <el-input v-model="form.password" type="password" placeholder="请输入密码" style="margin-bottom: 20px; height: 44px;" />
      <el-input v-model="form.nickname" placeholder="请输入昵称" style="margin-bottom: 25px; height: 44px;" />

      <el-button type="primary" @click="handleRegister" style="width: 100%; height: 46px; font-size: 16px;">
        完成注册
      </el-button>
      <el-button @click="router.push('/login')" style="width: 100%; margin-top:10px;">
        已有账号？返回登录
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
  password: '',
  nickname: ''
})

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请完善信息')
    return
  }
  try {
    await axios.post('http://localhost:8081/user/register', {
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname || form.value.username
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (err) {
    ElMessage.error('注册失败，账号可能已存在')
  }
}
</script>