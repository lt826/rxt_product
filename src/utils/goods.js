// 商品展示工具
// 农户发布时会将商品类型注入 content 前缀：【商品类型：水果】\n描述...
// 这里负责把它解析出来用于展示，并把描述清理干净。

const CATEGORY_RE = /【商品类型\s*[:：]\s*([^\】]+)】/

/** 从 content 中解析商品类型（水果/蔬菜/粮食…），没有则返回空串 */
export function parseCategory(content) {
  const m = CATEGORY_RE.exec(String(content || ''))
  return m ? m[1].trim() : ''
}

/** 去掉 content 中的【商品类型：xx】前缀，返回干净的描述文本 */
export function cleanContent(content) {
  return String(content || '')
    .replace(CATEGORY_RE, '')
    .replace(/^[\s\n]+/, '')
    .trim()
}

/** 价格格式化：保留两位小数，整数去掉 .00；无效或非正数返回空串（用「面议」兜底） */
export function formatPrice(price) {
  const n = Number(price)
  if (!isFinite(n) || n <= 0) return ''
  const s = n.toFixed(2)
  return /\.00$/.test(s) ? s.replace(/\.00$/, '') : s
}

/** 产地简化展示：最多取前 6 个字符（省/市级），便于做徽标 */
export function shortAddress(address) {
  const s = String(address || '').trim()
  if (!s) return ''
  return s.length > 6 ? s.slice(0, 6) + '…' : s
}
