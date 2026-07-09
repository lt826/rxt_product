import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000
})

// 公开 API 路径（不需要认证即可访问）
const PUBLIC_PATHS = [
  'knowledge/list',
  'expert/list',
  'goods/list'
]

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

// 响应拦截器
request.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status
    const url = err.config?.url || ''

    if (status === 401) {
      localStorage.removeItem('rxt_token')
      localStorage.removeItem('rxt_user')
      router.push('/login')
      ElMessage.warning('登录已过期，请重新登录')
    } else if (status === 403) {
      // 公开 API：返回空数据，不 reject
      if (PUBLIC_PATHS.some(p => url.includes(p))) {
        return { code: 200, data: [] }
      }
      // 其他 403 静默 reject
      return Promise.reject(err)
    } else {
      ElMessage.error(err.response?.data?.message || err.message || '请求失败')
      return Promise.reject(err)
    }
  }
)

export default request
