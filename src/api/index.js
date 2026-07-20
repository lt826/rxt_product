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
  'goods/list',
  'homepage-feature/list'
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

// 登录失效处理：仅 401（真正的未认证/会话过期）才清凭证并回登录页。
// 注意：本系统后端对"当前角色无权访问该接口"也返回 403（如管理员访问消息接口），
// 这属于业务级鉴权，不应登出，故 403 不触发退出，只按接口失败由页面兜底为空。
let authExpired = false
function handleAuthExpired() {
  if (authExpired) return
  authExpired = true
  localStorage.removeItem('rxt_token')
  localStorage.removeItem('rxt_user')
  ElMessage.warning('登录已过期，请重新登录')
  router.push('/login').catch(() => {}).finally(() => { authExpired = false })
}

// 响应拦截器
request.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status
    const url = err.config?.url || ''

    if (status === 401) {
      handleAuthExpired()
      return Promise.reject(err)
    }
    if (status === 403) {
      // 公开接口未登录访问：返回空数据，不 reject
      if (PUBLIC_PATHS.some(p => url.includes(p))) {
        return { code: 200, data: [] }
      }
      // 其余 403（角色无权限等）静默 reject，由页面自行兜底为空数据
      return Promise.reject(err)
    }
    ElMessage.error(err.response?.data?.message || err.message || '请求失败')
    return Promise.reject(err)
  }
)

export default request
