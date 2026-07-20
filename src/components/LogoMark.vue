<script setup>
// 融销通品牌标识 —— 一株破土的嫩芽（茎 + 三片叶）。
// 用 currentColor 继承父级颜色，stroke 风格与 Element Plus 线性图标保持一致，
// 既可作 Logo 锁定字标使用，也可作为「农户」角色的语义图标。
defineProps({
  // 图标边长（px）
  size: { type: [Number, String], default: 24 }
})
</script>

<template>
  <svg
    class="logo-mark"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <!-- 茎 -->
    <path d="M12 21 V11.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <!-- 左叶 -->
    <path d="M12 13.2 C8.4 12.7 5.4 10.6 5 7.4 C8.7 7.7 11.7 9.8 12 13.2 Z" fill="currentColor" />
    <!-- 右叶 -->
    <path d="M12 11 C15.6 10.5 18.6 8.4 19 5.2 C15.3 5.5 12.3 7.6 12 11 Z" fill="currentColor" />
    <!-- 顶芽 -->
    <path d="M12 8.8 C12 6.4 13.3 4.8 15.2 4.3 C15.4 6.7 14.0 8.3 12 8.8 Z" fill="currentColor" />
  </svg>
</template>

<style scoped>
.logo-mark {
  display: block;
  flex-shrink: 0;
}
</style>
