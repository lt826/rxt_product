<script setup>
// 商品大厅通用卡片 —— 参照农业电商大厅样式：
// 图片为主，叠加「产地直供」信任标 + 产地徽标；下方品类、标题、描述、红色价格、加购按钮。
import { ref, computed } from 'vue'
import { ShoppingCart, Delete, Edit } from '@element-plus/icons-vue'
import { parseCategory, cleanContent, formatPrice, shortAddress } from '../utils/goods'

const props = defineProps({
  item: { type: Object, required: true },
  // 是否显示「加入购物车」按钮（买家端用）
  showCart: { type: Boolean, default: true },
  // 是否处于管理模式（显示编辑/删除入口，农户端用）
  manageMode: { type: Boolean, default: false },
  // 当前用户是否有权管理该商品（编辑/删除）
  canManage: { type: Boolean, default: false }
})
const emit = defineEmits(['view', 'add-cart', 'delete', 'edit'])

const category = computed(() => parseCategory(props.item.content) || '农产品')
const desc = computed(() => cleanContent(props.item.content))
const priceText = computed(() => formatPrice(props.item.price))
const origin = computed(() => shortAddress(props.item.address))
const seller = computed(() => props.item.ownName || props.item.seller)
const hasImage = computed(() => {
  const p = props.item.picture
  return !!(p && /^https?:\/\//i.test(String(p)))
})
// 无图时取标题首字做占位字标，替代此前的 emoji
const mono = computed(() => String(props.item.title || '品').trim().charAt(0))
const imgError = ref(false)

function onView() {
  emit('view', props.item)
}
function onAddCart() {
  emit('add-cart', props.item)
}
function onDelete() {
  emit('delete', props.item)
}
function onEdit() {
  emit('edit', props.item)
}
function onImgError() {
  imgError.value = true
}
</script>

<template>
  <article class="pc-card" @click="onView">
    <div class="pc-media">
      <img
        v-if="hasImage && !imgError"
        :src="item.picture"
        :alt="item.title"
        class="pc-img"
        loading="lazy"
        @error="onImgError"
      />
      <div v-else class="pc-emoji" :class="{ 'pc-emoji--manage': manageMode && canManage }">{{ mono }}</div>

      <!-- 右上角：信任标 -->
      <span class="pc-chip pc-chip--trust">产地直供</span>
      <!-- 左下角：产地徽标 -->
      <span v-if="origin" class="pc-chip pc-chip--origin">
        <span class="pc-pin"></span>{{ origin }}
      </span>
    </div>

    <div class="pc-body">
      <div class="pc-cat"><span class="pc-dot"></span>{{ category }}</div>
      <h3 class="pc-title">{{ item.title }}</h3>
      <p v-if="desc" class="pc-desc">{{ desc }}</p>

      <div class="pc-foot">
        <div class="pc-price">
          <span class="pc-yen">¥</span><span class="pc-num">{{ priceText || '面议' }}</span><span class="pc-unit">/斤</span>
        </div>
        <span v-if="seller" class="pc-seller">{{ seller }}</span>
      </div>

      <div v-if="showCart" class="pc-actions">
        <button class="pc-cart" @click.stop="onAddCart">
          <el-icon><ShoppingCart /></el-icon><span>加入购物车</span>
        </button>
      </div>
      <div v-else-if="manageMode && canManage" class="pc-actions pc-actions--manage">
        <button class="pc-edit" @click.stop="onEdit">
          <el-icon><Edit /></el-icon><span>编辑</span>
        </button>
        <button class="pc-delbtn" @click.stop="onDelete">
          <el-icon><Delete /></el-icon><span>删除</span>
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.pc-card {
  position: relative;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
}
.pc-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--green-pale);
}

/* ===== 媒体区 ===== */
.pc-media {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, var(--green-mist), #eef7f0);
  overflow: hidden;
}
.pc-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.pc-card:hover .pc-img {
  transform: scale(1.06);
}
.pc-emoji {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  font-weight: 800;
  color: var(--green-deep);
  opacity: 0.9;
}
.pc-emoji--manage {
  opacity: 0.85;
}

/* 图片上的徽标 */
.pc-chip {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 9px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
  letter-spacing: 0.02em;
  line-height: 1;
  white-space: nowrap;
}
.pc-chip--trust {
  top: 10px;
  right: 10px;
  color: #fff;
  background: linear-gradient(135deg, var(--green), var(--green-light));
  box-shadow: 0 2px 8px rgba(27, 67, 50, 0.25);
}
.pc-chip--origin {
  bottom: 10px;
  left: 10px;
  color: var(--green-deep);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 1px 4px rgba(27, 67, 50, 0.12);
}
.pc-pin {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--green);
}

/* ===== 文本区 ===== */
.pc-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.pc-cat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 600;
  color: var(--green);
  letter-spacing: 0.04em;
  margin-bottom: 4px;
}
.pc-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--green-light);
}
.pc-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.4;
  margin: 0 0 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}
.pc-desc {
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.5;
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pc-foot {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
  margin-top: auto;
}
.pc-price {
  display: inline-flex;
  align-items: baseline;
  color: var(--price);
}
.pc-yen {
  font-size: 13px;
  font-weight: 700;
}
.pc-num {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.01em;
  line-height: 1;
}
.pc-unit {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
  margin-left: 2px;
}
.pc-seller {
  font-size: 11.5px;
  color: var(--text-muted);
  max-width: 45%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 加购按钮 */
.pc-actions {
  display: flex;
}
.pc-cart {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--green-mist);
  color: var(--green-deep);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, transform 0.12s;
}
.pc-cart .el-icon { font-size: 15px; }
.pc-cart:hover {
  background: var(--green);
  color: #fff;
}
.pc-cart:active {
  transform: scale(0.98);
}

/* 管理态：编辑 / 删除（农户端自己的商品） */
.pc-actions--manage {
  gap: 8px;
}
.pc-edit,
.pc-delbtn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 36px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, border-color 0.18s, transform 0.12s;
}
.pc-edit {
  border: 1px solid var(--green-pale);
  background: var(--surface);
  color: var(--green-deep);
}
.pc-edit .el-icon { font-size: 15px; }
.pc-edit:hover {
  background: var(--green-mist);
  border-color: var(--green-light);
}
.pc-delbtn {
  border: 1px solid #f5c2c2;
  background: var(--surface);
  color: #c0392b;
}
.pc-delbtn .el-icon { font-size: 15px; }
.pc-delbtn:hover {
  background: #fef0f0;
  border-color: #e4393c;
}
.pc-edit:active,
.pc-delbtn:active {
  transform: scale(0.98);
}
</style>
