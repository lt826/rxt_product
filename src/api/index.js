import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000
})

// 请求拦截器：自动注入 JWT Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('rxt_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (err) => Promise.reject(err)
)

request.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message || '请求失败'

    if (status === 401) {
      // Token 失效，清除登录状态并跳转登录页
      localStorage.removeItem('rxt_token')
      localStorage.removeItem('rxt_user')
      router.push('/login')
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default request
