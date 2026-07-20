<script setup>
// 角色语义图标：统一「农户=嫩芽（品牌标识）+ 其余角色=Element Plus 线性图标」，
// 让多角色入口的图标视觉成套、不再依赖 emoji。
import LogoMark from './LogoMark.vue'

defineProps({
  role: { type: String, required: true },
  size: { type: [Number, String], default: 24 }
})

// 农户复用品牌嫩芽；其余角色用 EP 图标名（全局已注册）
const ICON_MAP = {
  farmer: null, // 由 LogoMark 渲染
  buyer: 'ShoppingBag',
  bank: 'OfficeBuilding',
  expert: 'Service',
  admin: 'Setting'
}
</script>

<template>
  <LogoMark v-if="role === 'farmer'" :size="size" />
  <el-icon v-else :size="Number(size)"><component :is="ICON_MAP[role]" /></el-icon>
</template>
