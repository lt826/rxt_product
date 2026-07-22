/**
 * 手机号脱敏：隐藏中间 4 位，如 13812345678 -> 138****5678。
 * 非手机号格式（长度不足、座机、非数字等）原样返回；空值返回空串。
 * 仅用于前端展示脱敏，不影响数据库中的真实数据。
 *
 * @param {string|number|null|undefined} phone
 * @returns {string}
 */
export function maskPhone(phone) {
  const p = String(phone ?? '').trim()
  if (!p) return ''
  // 先取出纯数字，兼容 "138-1234-5678"、"138 1234 5678" 等带分隔符写法
  const digits = p.replace(/\D/g, '')
  if (digits.length === 11 && /^1\d{10}$/.test(digits)) {
    return digits.slice(0, 3) + '****' + digits.slice(7)
  }
  // 不是标准 11 位手机号，原样返回（座机、客服电话等）
  return p
}
