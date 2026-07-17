<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGoodsListAPI, getMyGoodsAPI, publishGoodsAPI, updateGoodsAPI, deleteGoodsAPI } from '../../api/goods'
import { uploadImageAPI } from '../../api/upload'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search } from '@element-plus/icons-vue'
import { parseCategory, cleanContent } from '../../utils/goods'
import ProductCard from '../../components/ProductCard.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const uploadingImage = ref(false)
// 正在编辑的商品 id；为 null 表示「新建发布」
const editingId = ref(null)

const form = ref({
  title: '', price: null, content: '', type: '', address: '', picture: ''
})

const imageFile = ref(null)
const imagePreview = ref('')
const imageUrlInput = ref('')

// 数据源切换 & 筛选条件
const mode = ref('all')
const keyword = ref('')
const priceMin = ref(null)
const priceMax = ref(null)
const sellerName = ref('')
const sortOrder = ref('')

const isMine = (item) => !!(item.seller && item.seller === userStore.userInfo?.userName)

const filteredProducts = computed(() => {
  let arr = products.value.slice()
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    arr = arr.filter(p =>
      String(p.title || '').toLowerCase().includes(kw) ||
      String(p.content || '').toLowerCase().includes(kw) ||
      String(p.seller || '').toLowerCase().includes(kw) ||
      String(p.address || '').toLowerCase().includes(kw)
    )
  }
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

onMounted(async () => {
  await loadProducts()
  // 从商品详情「编辑商品」跳来时，自动打开编辑弹窗
  const editId = route.query.edit
  if (editId) {
    const target = products.value.find(p => String(p.id) === String(editId))
    if (target) openEditDialog(target)
    router.replace({ path: '/farmer/products' })
  }
})

async function loadProducts() {
  loading.value = true
  try {
    const res = mode.value === 'mine'
      ? await getMyGoodsAPI({ type: '1' })
      : await getGoodsListAPI({ type: '1' })
    if (res.code === 200) {
      products.value = (res.data || []).map(g => ({
        id: g.goodsId, goodsId: g.goodsId, title: g.title, price: g.price, content: g.content,
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
  keyword.value = ''
  priceMin.value = null
  priceMax.value = null
  sellerName.value = ''
  sortOrder.value = ''
}

function viewDetail(item) {
  router.push('/farmer/goods/' + item.id)
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
  editingId.value = null
  form.value = { title: '', price: null, content: '', type: '', address: '', picture: '' }
  imageFile.value = null
  imagePreview.value = ''
  imageUrlInput.value = ''
  dialogVisible.value = true
}

// 编辑已发布商品：把保存时注入的【商品类型：xx】前缀拆回 type 与 content
function openEditDialog(item) {
  editingId.value = item.id
  form.value = {
    title: item.title || '',
    price: item.price != null ? Number(item.price) : null,
    content: cleanContent(item.content),
    type: parseCategory(item.content),
    address: item.address || '',
    picture: item.picture || ''
  }
  imageFile.value = null
  imagePreview.value = (item.picture && /^https?:\/\//i.test(item.picture)) ? item.picture : ''
  imageUrlInput.value = ''
  dialogVisible.value = true
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
    const payload = {
      title: form.value.title.trim(), price: parseFloat(form.value.price),
      content: contentWithCategory, type: '1', address: form.value.address.trim(),
      picture: form.value.picture || ''
    }
    const res = editingId.value
      ? await updateGoodsAPI(editingId.value, payload)
      : await publishGoodsAPI(payload)
    if (res.code === 200) {
      ElMessage.success(editingId.value ? '商品更新成功！' : '商品发布成功！')
      dialogVisible.value = false
      editingId.value = null
      await loadProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error(editingId.value ? '更新失败，请重试' : '发布失败，请重试')
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
</script>

<template>
  <div class="hall">
    <!-- 大厅头部：货源切换 + 发布 -->
    <div class="hall-bar">
      <div class="hall-tabs">
        <button class="hall-tab" :class="{ active: mode === 'all' }" @click="mode = 'all'; onModeChange()">全部货源</button>
        <button class="hall-tab" :class="{ active: mode === 'mine' }" @click="mode = 'mine'; onModeChange()">我的发布</button>
      </div>
      <el-button type="primary" class="hall-publish" @click="openPublishDialog">
        <el-icon><Plus /></el-icon>
        <span>免费发布商品</span>
      </el-button>
    </div>

    <!-- 搜索 + 筛选 -->
    <div class="hall-toolbar">
      <div class="hall-search">
        <el-icon class="hall-search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="hall-search-input"
          placeholder="搜索商品名称、产地、农户…"
        />
      </div>
      <div class="hall-filters">
        <span class="f-label">价格</span>
        <el-input-number v-model="priceMin" :min="0" :precision="2" placeholder="最低"
          controls-position="right" class="price-input" />
        <span class="f-sep">—</span>
        <el-input-number v-model="priceMax" :min="0" :precision="2" placeholder="最高"
          controls-position="right" class="price-input" />
        <span class="f-label">农户</span>
        <el-input v-model="sellerName" placeholder="农户名" clearable class="seller-input"
          :disabled="mode === 'mine'" />
        <el-select v-model="sortOrder" placeholder="默认排序" clearable class="sort-select">
          <el-option label="价格升序" value="asc" />
          <el-option label="价格降序" value="desc" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-loading="loading" class="hall-grid">
      <ProductCard
        v-for="item in filteredProducts"
        :key="item.id"
        :item="item"
        :show-cart="false"
        manage-mode
        :can-manage="mode === 'mine' || isMine(item)"
        @view="viewDetail"
        @edit="openEditDialog"
        @delete="deleteProduct"
      />
    </div>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品数据，点击上方按钮发布你的第一个商品">
      <el-button type="primary" @click="openPublishDialog">立即发布</el-button>
    </el-empty>
    <el-empty v-else-if="!loading && filteredProducts.length === 0" description="没有符合条件的商品">
      <el-button @click="resetFilters">清空筛选条件</el-button>
    </el-empty>

    <!-- 发布弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '发布农产品'" width="550px" destroy-on-close>
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
        <el-button type="primary" :loading="submitting || uploadingImage" @click="submitPublish">{{ editingId ? '保存修改' : '确认发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.hall { max-width: 1280px; margin: 0 auto; }

/* ===== 顶栏 ===== */
.hall-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.hall-tabs {
  display: inline-flex;
  gap: 4px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 5px;
}
.hall-tab {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}
.hall-tab:hover { color: var(--green); }
.hall-tab.active {
  color: #fff;
  background: linear-gradient(135deg, var(--green), var(--green-mid));
  box-shadow: 0 2px 8px rgba(27, 67, 50, 0.18);
}
.hall-publish {
  height: 42px;
  padding: 0 22px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-weight: 600;
}

/* ===== 工具栏：搜索 + 筛选 ===== */
.hall-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}
.hall-search {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 220px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 0 12px 0 38px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.hall-search:focus-within {
  border-color: var(--green-light);
  box-shadow: 0 0 0 3px rgba(82, 183, 136, 0.15);
}
.hall-search-icon {
  position: absolute;
  left: 12px;
  font-size: 17px;
  color: var(--text-muted);
}
.hall-search-input {
  flex: 1;
  height: 38px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text);
}
.hall-search-input::placeholder { color: var(--text-muted); }

.hall-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.f-label { font-size: 13px; color: var(--text-muted); }
.f-sep { color: var(--text-muted); }
.price-input { width: 104px; }
.seller-input { width: 130px; }
.sort-select { width: 124px; }

/* ===== 网格 ===== */
.hall-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  min-height: 120px;
}
@media (max-width: 1100px) { .hall-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 820px)  { .hall-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 520px)  { .hall-grid { grid-template-columns: 1fr; } }

/* ===== 图片上传（发布弹窗） ===== */
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
.image-preview-actions { position: absolute; top: 8px; right: 8px; }
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
