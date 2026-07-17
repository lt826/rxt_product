import { getUserInfoAPI } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 农户发布商品 / 发货前的前置校验：必须已填写「手机号」和「地址」。
 *
 * 登录态里的 userInfo 不含 phone/address（登录返回的 LoginVO 未带这些字段），
 * 因此每次实时拉取后端用户资料进行校验，保证与数据库一致。
 *
 * @param {Object} options
 * @param {string} options.userName 当前登录用户名
 * @param {object} [options.router]  vue-router 实例，用于引导跳转到「基本信息」
 * @returns {Promise<boolean>} true-已完善，可继续操作；false-未完善或拉取失败
 */
export async function ensureFarmerContactReady({ userName, router } = {}) {
  if (!userName) {
    ElMessage.warning('请先登录')
    return false
  }

  let user = {}
  try {
    const res = await getUserInfoAPI(userName)
    if (res && res.code === 200) user = res.data || {}
  } catch {
    // 拦截器已统一提示请求失败，这里直接拦截操作
    return false
  }

  const phoneOk = !!(user.phone || '').toString().trim()
  const addrOk = !!(user.address || '').toString().trim()
  if (phoneOk && addrOk) return true

  const missing = []
  if (!phoneOk) missing.push('手机号')
  if (!addrOk) missing.push('地址')

  try {
    await ElMessageBox.confirm(
      `为方便买家与您联系和发货，请先完善${missing.join('和')}后再进行此操作。`,
      '请完善联系信息',
      { confirmButtonText: '去完善', cancelButtonText: '稍后', type: 'warning' }
    )
    // 用户点击「去完善」：跳转到个人中心-基本信息
    if (router) router.push('/profile/basic')
  } catch {
    // 用户点击「稍后」或关闭：保持原状，仅拦截本次操作
  }
  return false
}
