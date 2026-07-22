<template>
  <div class="carousel-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">首页轮播图管理</h2>
        <p class="page-sub">上传首页轮播图片并调整顺序；首页每张图片展示 3 秒，点击可放大查看大图</p>
      </div>
    </div>

    <!-- 上传 -->
    <el-card shadow="never" class="upload-card">
      <template #header><strong>添加轮播图</strong></template>
      <div class="upload-row">
        <el-upload :show-file-list="false" :http-request="customUpload" accept="image/*">
          <el-button type="primary" :loading="uploading">
            <el-icon><Upload /></el-icon>&nbsp;选择图片上传
          </el-button>
        </el-upload>
        <el-input
          v-model="linkInput"
          placeholder="跳转链接（可选，如 https://...）"
          class="link-input"
          clearable
        />
      </div>
      <p class="upload-tip">支持 jpg / png / gif / webp，单张不超过 10MB。先填写链接再上传，可将该图片设为可点击跳转。</p>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" class="list-card" v-loading="loading">
      <template #header>
        <div class="card-header-row">
          <strong>轮播图列表</strong>
          <span class="block-tip">共 {{ list.length }} 张 · 首页按此顺序展示</span>
        </div>
      </template>
      <div class="banner-list">
        <div v-for="(item, idx) in list" :key="item.id" class="banner-item">
          <span class="b-idx">{{ idx + 1 }}</span>
          <div class="b-thumb">
            <img :src="item.imageUrl" :alt="'轮播图 ' + (idx + 1)" />
          </div>
          <div class="b-info">
            <div class="b-url" :title="item.imageUrl">{{ item.imageUrl }}</div>
            <div v-if="item.link" class="b-link">跳转：{{ item.link }}</div>
          </div>
          <div class="b-actions">
            <el-button size="small" link :disabled="idx === 0" @click="move(idx, -1)">上移</el-button>
            <el-button size="small" link :disabled="idx === list.length - 1" @click="move(idx, 1)">下移</el-button>
            <el-button size="small" link type="danger" @click="remove(item)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无轮播图，请上传图片" :image-size="64" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCarouselListAPI, addCarouselAPI, deleteCarouselAPI, reorderCarouselAPI } from '../../api/carousel'
import { uploadImageAPI } from '../../api/upload'

const list = ref([])
const loading = ref(false)
const uploading = ref(false)
const linkInput = ref('')

async function load() {
  loading.value = true
  try {
    const res = await getCarouselListAPI()
    if (res.code === 200) list.value = res.data || []
  } catch { /* handled in interceptor */ } finally {
    loading.value = false
  }
}

// el-upload 自定义上传：先上传图片拿到 URL，再写入轮播图表（带上可选链接）
async function customUpload(options) {
  const file = options.file
  uploading.value = true
  try {
    const upRes = await uploadImageAPI(file)
    if (upRes.code !== 200 || !upRes.data) {
      uploading.value = false
      return
    }
    const addRes = await addCarouselAPI({ imageUrl: upRes.data, link: linkInput.value.trim() || undefined })
    if (addRes.code === 200) {
      ElMessage.success('已添加轮播图')
      linkInput.value = ''
      await load()
    }
  } catch { /* handled in interceptor */ } finally {
    uploading.value = false
  }
}

async function move(idx, delta) {
  const j = idx + delta
  if (j < 0 || j >= list.value.length) return
  const ordered = list.value.slice()
  ;[ordered[idx], ordered[j]] = [ordered[j], ordered[idx]]
  await persistOrder(ordered)
}

async function remove(item) {
  try {
    await ElMessageBox.confirm('确定删除这张轮播图吗？', '提示', { type: 'warning' })
  } catch { return }
  try {
    const res = await deleteCarouselAPI(item.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      await load()
    }
  } catch { /* handled in interceptor */ }
}

async function persistOrder(ordered) {
  const ids = ordered.map(i => i.id)
  try {
    const res = await reorderCarouselAPI(ids)
    if (res.code === 200) {
      list.value = ordered
    }
  } catch { /* handled in interceptor */ }
}

onMounted(load)
</script>

<style scoped>
.carousel-page { max-width: 1200px; margin: 0 auto; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }

.upload-card { border-radius: var(--radius-md); }
.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.link-input { flex: 1; min-width: 260px; }
.upload-tip { font-size: 13px; color: var(--text-muted); margin: 12px 0 0; }

.list-card { margin-top: 20px; border-radius: var(--radius-md); }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.block-tip { font-size: 13px; color: var(--text-muted); }

.banner-list { display: flex; flex-direction: column; gap: 12px; }
.banner-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--green-mist);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
}
.b-idx {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--green); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.b-thumb {
  width: 120px; height: 68px; border-radius: var(--radius-sm);
  overflow: hidden; flex-shrink: 0; background: var(--bg);
}
.b-thumb img { width: 100%; height: 100%; object-fit: cover; display: block; }
.b-info { flex: 1; min-width: 0; }
.b-url {
  font-size: 13px; color: var(--text-secondary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.b-link { font-size: 12px; color: var(--text-muted); margin-top: 4px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.b-actions { display: flex; gap: 4px; flex-shrink: 0; }

@media (max-width: 640px) {
  .banner-item { flex-wrap: wrap; }
  .b-info { width: 100%; }
}
</style>
