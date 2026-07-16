<script setup>
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { addGoodsCardToCart } from '../utils/goodsMessage'

// 聊天中的商品卡片消息：展示商品快照，买家可一键加入购物车
const props = defineProps({
  // 由 parseGoodsCard 解析出的商品卡片对象
  card: { type: Object, required: true },
  // 是否显示"加入购物车"按钮（仅买家）
  canBuy: { type: Boolean, default: false }
})

function addToCart() {
  if (addGoodsCardToCart(props.card)) {
    ElMessage.success('已加入购物车：' + props.card.title)
  } else {
    ElMessage.error('加入购物车失败')
  }
}
</script>

<template>
  <div class="gcm-card">
    <div class="gcm-thumb">
      <img v-if="card.picture && card.picture.startsWith('http')" :src="card.picture" :alt="card.title" />
      <span v-else>📦</span>
    </div>
    <div class="gcm-info">
      <div class="gcm-title">{{ card.title }}</div>
      <div class="gcm-meta">
        <span class="gcm-price">¥{{ card.price }}</span>
        <span v-if="card.address" class="gcm-addr">{{ card.address }}</span>
      </div>
      <el-button v-if="canBuy" size="small" type="primary" class="gcm-btn" @click="addToCart">
        <el-icon><ShoppingCart /></el-icon>&nbsp;加入购物车
      </el-button>
      <el-tag v-else size="small" type="success" effect="plain">商品分享</el-tag>
    </div>
  </div>
</template>

<style scoped>
.gcm-card {
  display: flex;
  gap: 10px;
  width: 260px;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
}
.gcm-thumb {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}
.gcm-thumb img { width: 100%; height: 100%; object-fit: cover; }
.gcm-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.gcm-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.gcm-meta { display: flex; align-items: baseline; gap: 8px; }
.gcm-price { color: var(--amber); font-weight: 700; font-size: 16px; }
.gcm-addr { font-size: 11px; color: var(--text-muted); }
.gcm-btn { width: 100%; align-self: flex-start; }
</style>
