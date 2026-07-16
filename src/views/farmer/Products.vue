<script setup>
import { ref, computed, onMounted } from 'vue'
import { getGoodsListAPI, getMyGoodsAPI, publishGoodsAPI, deleteGoodsAPI } from '../../api/goods'
import { uploadImageAPI } from '../../api/upload'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search } from '@element-plus/icons-vue'

const userStore = useUserStore()

const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const uploadingImage = ref(false)

const form = ref({
  title: '', price: null, content: '', type: '', address: '', picture: ''
})

const imageFile = ref(null)
const imagePreview = ref('')
const imageUrlInput = ref('')

// 数据源切换 & 筛选条件
const mode = ref('all')
const priceMin = ref(null)
const priceMax = ref(null)
const sellerName = ref('')
const sortOrder = ref('')

const isMine = (item) => !!(item.seller && item.seller === userStore.userInfo?.userName)

const filteredProducts = computed(() => {
  let arr = products.value.slice()
  const s = sellerName.value.trim()
  if (s) arr = arr.filter(p => p.seller && String(p.seller).includes(s))
  if (priceMin.value != null && priceMin.value !== '') {
    arr = arr.filter(p => Number(p.price) >= Number(priceMin.value))
  }
  if (priceMax.value != null && priceMax.value !== '') {
    arr = arr.filter(p => Number(p.price) <= Number(priceMax.value))
  }
  if (sortOrder.value === 'asc') {
    arr = arr.slice().sort((a, b) => Number(a.price) - Number(b.price))
  } else if (sortOrder.value === 'desc') {
    arr = arr.slice().sort((a, b) => Number(b.price) - Number(a.price))
  }
  return arr
})

onMounted(async () => { await loadProducts() })

async function loadProducts() {
  loading.value = true
  try {
    const res = mode.value === 'mine'
      ? await getMyGoodsAPI({ type: '1' })
      : await getGoodsListAPI({ type: '1' })
    if (res.code === 200) {
      products.value = (res.data || []).map(g => ({
        id: g.goodsId, title: g.title, price: g.price, content: g.content,
        seller: g.ownName, type: g.type, address: g.address,
        picture: g.picture, createTime: g.createTime
      }))
    }
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

function onModeChange() {
  resetFilters()
  loadProducts()
}

function resetFilters() {
  priceMin.value = null
  priceMax.value = null
  sellerName.value = ''
  sortOrder.value = ''
}

async function handleImageChange(file) {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.warning('只支持上传 JPG、PNG、GIF、WEBP 格式的图片')
    return false
  }
  if (file.raw.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  imageFile.value = file.raw
  uploadingImage.value = true
  try {
    // 上传到后端，拿到可访问的图片 URL（不再存 base64，避免超过数据库字段长度）
    const res = await uploadImageAPI(file.raw)
    if (res.code === 200) {
      form.value.picture = res.data
      imagePreview.value = res.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.message || '图片上传失败')
      imageFile.value = null
    }
  } catch {
    ElMessage.error('图片上传失败，请重试')
    imageFile.value = null
  } finally {
    uploadingImage.value = false
  }
  return false
}

function removeImage() {
  imageFile.value = null
  imagePreview.value = ''
  form.value.picture = ''
  imageUrlInput.value = ''
}

function useImageUrl() {
  if (imageUrlInput.value.trim()) {
    form.value.picture = imageUrlInput.value.trim()
    imagePreview.value = imageUrlInput.value.trim()
    ElMessage.success('图片链接已设置')
  }
}

function openPublishDialog() {
  dialogVisible.value = true
  form.value = { title: '', price: null, content: '', type: '', address: '', picture: '' }
  imageFile.value = null
  imagePreview.value = ''
  imageUrlInput.value = ''
}

async function submitPublish() {
  if (!form.value.title?.trim()) return ElMessage.warning('请输入商品名称')
  if (!form.value.price || parseFloat(form.value.price) <= 0) return ElMessage.warning('请输入有效的价格')
  if (!form.value.content?.trim()) return ElMessage.warning('请输入商品描述')
  if (!form.value.type?.trim()) return ElMessage.warning('请输入商品类型')
  if (!form.value.address?.trim()) return ElMessage.warning('请输入产地地址')

  submitting.value = true
  try {
    if (uploadingImage.value) return ElMessage.warning('图片正在上传，请稍候')
    const contentWithCategory = '【商品类型：' + form.value.type.trim() + '】\n' + form.value.content.trim()
    const res = await publishGoodsAPI({
      title: form.value.title.trim(), price: parseFloat(form.value.price),
      content: contentWithCategory, type: '1', address: form.value.address.trim(),
      picture: form.value.picture || ''
    })
    if (res.code === 200) {
      ElMessage.success('商品发布成功！')
      dialogVisible.value = false
      await loadProducts()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch {
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function deleteProduct(item) {
  try {
    await ElMessageBox.confirm('确定要删除商品"' + item.title + '"吗？', '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    loading.value = true
    const res = await deleteGoodsAPI(item.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadProducts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  } finally {
    loading.value = false
  }
}

function viewDetail(item) {
  ElMessage.info('查看详情：' + item.title)
}
</script>

<template>
  <div class="products-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品货源</h2>
        <p class="page-sub">管理你的商品，发布新的农产品</p>
      </div>
      <el-button type="primary" @click="openPublishDialog" class="publish-btn">
        <el-icon><Plus /></el-icon> 发布商品
      </el-button>
    </div>

    <!-- 切换 + 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="mode" @change="onModeChange">
        <el-radio-button value="all">全部货源</el-radio-button>
        <el-radio-button value="mine">我的发布</el-radio-button>
      </el-radio-group>
      <div class="filter-controls">
        <span class="filter-label">价格</span>
        <el-input-number v-model="priceMin" :min="0" :precision="2" placeholder="最低"
          controls-position="right" class="price-input" />
        <span class="filter-sep">-</span>
        <el-input-number v-model="priceMax" :min="0" :precision="2" placeholder="最高"
          controls-position="right" class="price-input" />
        <span class="filter-label">农户</span>
        <el-input v-model="sellerName" placeholder="农户名" clearable class="seller-input"
          :disabled="mode === 'mine'" />
        <el-select v-model="sortOrder" placeholder="默认排序" clearable class="sort-select">
          <el-option label="价格升序" value="asc" />
          <el-option label="价格降序" value="desc" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <div class="goods-grid">
        <div v-for="item in filteredProducts" :key="item.id" class="goods-card">
          <div class="goods-img">
            <img v-if="item.picture && item.picture.startsWith('http')" :src="item.picture" :alt="item.title" @error="item.picture = ''" />
            <span v-else>📦</span>
          </div>
          <div class="goods-info">
            <div class="goods-title">{{ item.title }}</div>
            <div class="goods-price-row">
              <span class="price">¥{{ item.price }}</span>
              <span class="goods-seller">{{ item.seller }}</span>
            </div>
            <div class="goods-tags">
              <el-tag v-if="item.type" size="small" type="success" effect="plain">{{ item.type }}</el-tag>
              <el-tag v-if="item.address" size="small" type="info" effect="plain">{{ item.address }}</el-tag>
            </div>
            <div class="goods-actions">
              <el-button size="small" type="primary" plain @click="viewDetail(item)">查看</el-button>
              <el-button v-if="mode === 'mine' || isMine(item)" size="small" type="danger" plain @click="deleteProduct(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && products.length === 0" description="暂无商品数据，点击上方按钮发布你的第一个商品">
        <el-button type="primary" @click="openPublishDialog">立即发布</el-button>
      </el-empty>
      <el-empty v-else-if="!loading && filteredProducts.length === 0" description="没有符合条件的商品">
        <el-button @click="resetFilters">清空筛选条件</el-button>
      </el-empty>
    </div>

    <!-- Publish dialog -->
    <el-dialog v-model="dialogVisible" title="发布农产品" width="550px" destroy-on-close>
      <el-form :model="form" label-width="95px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.title" placeholder="如：有机红富士苹果" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" controls-position="right" style="width:100%">
            <template #suffix>元/斤</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="商品类型" required>
          <el-input v-model="form.type" placeholder="如：水果、蔬菜、粮食等" />
        </el-form-item>
        <el-form-item label="产地地址" required>
          <el-input v-model="form.address" placeholder="如：山东省烟台市栖霞市" />
        </el-form-item>
        <el-form-item label="商品描述" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="品质特点、种植方式、供货量等" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="image-upload-container">
            <div v-if="imagePreview" class="image-preview">
              <img :src="imagePreview" alt="预览" />
              <div class="image-preview-actions">
                <el-button type="danger" size="small" :icon="Delete" circle @click="removeImage" />
              </div>
            </div>
            <el-upload v-else :show-file-list="false" :on-change="handleImageChange" :auto-upload="false"
              :disabled="uploadingImage" accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" class="image-uploader">
              <div class="upload-trigger">
                <el-icon :size="32"><Plus /></el-icon>
                <div v-if="uploadingImage" class="uploading-text">图片上传中...</div>
                <div v-else>点击上传图片</div>
                <div v-if="!uploadingImage" class="upload-hint">JPG / PNG / GIF / WEBP，最大 5MB</div>
              </div>
            </el-upload>
            <div v-if="!imagePreview" class="url-input-section">
              <div class="url-divider">或使用图片链接</div>
              <div class="url-input-row">
                <el-input v-model="imageUrlInput" placeholder="请输入图片URL地址" clearable style="flex:1" />
                <el-button type="primary" size="small" @click="useImageUrl">确定</el-button>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting || uploadingImage" @click="submitPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.products-page { max-width: 1200px; margin: 0 auto; }

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.publish-btn {
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 14px 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}
.filter-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-label { font-size: 13px; color: var(--text-muted); }
.filter-sep { color: var(--text-muted); }
.price-input { width: 110px; }
.seller-input { width: 150px; }
.sort-select { width: 130px; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
@media (max-width: 900px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .goods-grid { grid-template-columns: 1fr; } }

.goods-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: all 0.25s;
}
.goods-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.goods-img {
  width: 100%;
  height: 180px;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  overflow: hidden;
}
.goods-img img { width: 100%; height: 100%; object-fit: cover; }
.goods-img span { display: none; }

.goods-info { padding: 14px; }
.goods-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.price {
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #ff4d4f, #ff7a45);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
  line-height: 1.4;
  letter-spacing: 0.02em;
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.25);
}
.goods-seller { font-size: 12px; color: var(--text-muted); }
.goods-tags { display: flex; gap: 4px; margin-bottom: 10px; flex-wrap: wrap; }
.goods-actions { display: flex; gap: 8px; }
.goods-actions .el-button { flex: 1; }

/* Image upload */
.image-upload-container { width: 100%; }
.image-uploader { width: 100%; }
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 160px;
  border: 2px dashed var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.3s;
  background: var(--bg);
}
.upload-trigger:hover { border-color: var(--green); background: var(--green-mist); }
.upload-trigger .el-icon { color: var(--text-muted); margin-bottom: 8px; }
.upload-trigger > div { font-size: 14px; color: var(--text-secondary); }
.upload-hint { font-size: 12px; color: var(--text-muted) !important; margin-top: 2px; }
.uploading-text { color: var(--green); font-weight: 500; }
.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg);
}
.image-preview img { width: 100%; height: 100%; object-fit: contain; }
.image-preview-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}
.url-input-section { margin-top: 16px; }
.url-divider {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  position: relative;
  margin-bottom: 12px;
}
.url-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 40%;
  height: 1px;
  background: var(--border);
}
.url-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 0;
  width: 40%;
  height: 1px;
  background: var(--border);
}
.url-input-row { display: flex; gap: 8px; }
</style>
