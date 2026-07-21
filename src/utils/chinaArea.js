// 中国行政区划数据 + 省市县字符串与级联数组互转工具
// 数据来自 element-china-area-data（pcaTextArr：value/label 均为名称文本）
import { pcaTextArr } from 'element-china-area-data'

export { pcaTextArr }

// 省市县数组 + 详细地址 -> 单一字符串
export function joinArea(area, street = '') {
  const head = Array.isArray(area) ? area.filter(Boolean).join('') : ''
  return head + (street || '')
}

// 单一字符串 -> { area, street }：按省/市/县名前缀匹配；无法识别时 area 为空、原文进 street
export function parseArea(detail) {
  if (!detail) return { area: [], street: '' }
  const text = String(detail)
  let rest = text
  let province = ''
  let city = ''
  let county = ''
  for (const p of pcaTextArr) {
    if (rest.startsWith(p.value)) {
      province = p.value
      rest = rest.slice(p.value.length)
      for (const c of (p.children || [])) {
        if (rest.startsWith(c.value)) {
          city = c.value
          rest = rest.slice(c.value.length)
          for (const a of (c.children || [])) {
            if (rest.startsWith(a.value)) {
              county = a.value
              rest = rest.slice(a.value.length)
              break
            }
          }
          break
        }
      }
      break
    }
  }
  if (!province) return { area: [], street: text }
  return { area: [province, city, county].filter(Boolean), street: rest }
}
