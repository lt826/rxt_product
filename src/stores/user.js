import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, registerAPI } from '../api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('rxt_token') || null)
  const userInfo = ref(JSON.parse(localStorage.getItem('rxt_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || null)
  const nickName = computed(() => userInfo.value?.nickName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  async function login(credentials) {
    const res = await loginAPI({
      userName: credentials.username,
      password: credentials.password,
      role: credentials.role
    })
    if (res.code === 200) {
      // 后端角色可能返回大写（如 FARMER），统一转小写以匹配前端路由
      const data = { ...res.data }
      if (data.role) {
        data.role = String(data.role).toLowerCase()
      }
      token.value = data.token
      userInfo.value = data
      localStorage.setItem('rxt_token', data.token)
      localStorage.setItem('rxt_user', JSON.stringify(data))
      ElMessage.success('登录成功')
      return data
    } else {
      ElMessage.error(res.message || '登录失败')
      throw new Error(res.message)
    }
  }

  async function register(userData) {
    const res = await registerAPI(userData)
    if (res.code === 200) {
      ElMessage.success('注册成功')
      return true
    } else {
      ElMessage.error(res.message || '注册失败')
      throw new Error(res.message)
    }
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('rxt_token')
    localStorage.removeItem('rxt_user')
  }

  return { token, userInfo, isLoggedIn, role, nickName, avatar, login, register, logout }
})
