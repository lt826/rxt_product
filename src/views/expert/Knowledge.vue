<template>
  <div class="knowledge-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">知识管理</h2>
        <p class="page-sub">发布和管理农业知识文章</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon style="margin-right:4px"><Plus /></el-icon> 发布新文章
      </el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <el-image v-if="row.picPath" :src="row.picPath" style="width:50px;height:36px;border-radius:4px" fit="cover" />
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑文章' : '发布文章'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" placeholder="请输入文章标题" /></el-form-item>
        <el-form-item label="封面图片">
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
                <div v-if="uploadingImage" class="uploading-text">封面图片上传中...</div>
                <div v-else>点击上传封面图片</div>
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
        <el-form-item label="正文" prop="content"><el-input v-model="form.content" type="textarea" :rows="9" placeholder="请输入正文内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadingImage" @click="submit">{{ editing ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { listMyKnowledge, publishKnowledge, updateKnowledge, deleteKnowledge } from '../../api/knowledge'
import { uploadImageAPI } from '../../api/upload'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const formRef = ref(null)
const form = reactive({ knowledgeId: null, title: '', content: '', picPath: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 封面图片：本地上传 / 图片链接 两种方式
const uploadingImage = ref(false)
const imagePreview = ref('')
const imageUrlInput = ref('')

async function loadList() {
  loading.value = true
  try { const res = await listMyKnowledge(); list.value = res.data || [] } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { knowledgeId: null, title: '', content: '', picPath: '' })
  imagePreview.value = ''
  imageUrlInput.value = ''
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  Object.assign(form, { knowledgeId: row.knowledgeId, title: row.title, content: row.content, picPath: row.picPath || '' })
  // 仅当封面是完整 URL 时直接预览（本地上传得到的也是完整 URL）
  imagePreview.value = (form.picPath && /^https?:\/\//i.test(form.picPath)) ? form.picPath : ''
  imageUrlInput.value = ''
  dialogVisible.value = true
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
  uploadingImage.value = true
  try {
    // 上传到后端，拿到可访问的图片 URL（不再存 base64，避免超过数据库字段长度）
    const res = await uploadImageAPI(file.raw)
    if (res.code === 200) {
      form.picPath = res.data
      imagePreview.value = res.data
      ElMessage.success('封面图片上传成功')
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch {
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploadingImage.value = false
  }
  return false
}

function removeImage() {
  form.picPath = ''
  imagePreview.value = ''
  imageUrlInput.value = ''
}

function useImageUrl() {
  if (imageUrlInput.value.trim()) {
    form.picPath = imageUrlInput.value.trim()
    imagePreview.value = imageUrlInput.value.trim()
    ElMessage.success('图片链接已设置')
  }
}

async function submit() {
  if (!formRef.value) return
  if (uploadingImage.value) return ElMessage.warning('封面图片正在上传，请稍候')
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { title: form.title, content: form.content, picPath: form.picPath || null }
  if (editing.value) { payload.knowledgeId = form.knowledgeId; await updateKnowledge(payload); ElMessage.success('修改成功') }
  else { await publishKnowledge(payload); ElMessage.success('发布成功') }
  dialogVisible.value = false; loadList()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
  await deleteKnowledge(row.knowledgeId); ElMessage.success('删除成功'); loadList()
}

loadList()
</script>

<style scoped>
.knowledge-page { max-width: 1200px; }
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.muted { color: var(--text-muted); font-size: 13px; }

/* 封面图片：上传 / 链接 */
.image-upload-container { width: 100%; }
.image-uploader { width: 100%; }
.upload-trigger {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 100%; min-height: 160px;
  border: 2px dashed var(--border); border-radius: var(--radius-sm);
  cursor: pointer; transition: all 0.3s; background: var(--bg);
}
.upload-trigger:hover { border-color: var(--green); background: var(--green-mist); }
.upload-trigger .el-icon { color: var(--text-muted); margin-bottom: 8px; }
.upload-trigger > div { font-size: 14px; color: var(--text-secondary); }
.upload-hint { font-size: 12px; color: var(--text-muted) !important; margin-top: 2px; }
.uploading-text { color: var(--green); font-weight: 500; }
.image-preview {
  position: relative; width: 100%; height: 200px;
  border-radius: var(--radius-sm); overflow: hidden; background: var(--bg);
}
.image-preview img { width: 100%; height: 100%; object-fit: contain; }
.image-preview-actions { position: absolute; top: 8px; right: 8px; }
.url-input-section { margin-top: 16px; }
.url-divider {
  text-align: center; font-size: 12px; color: var(--text-muted);
  position: relative; margin-bottom: 12px;
}
.url-divider::before {
  content: ''; position: absolute; top: 50%; left: 0;
  width: 40%; height: 1px; background: var(--border);
}
.url-divider::after {
  content: ''; position: absolute; top: 50%; right: 0;
  width: 40%; height: 1px; background: var(--border);
}
.url-input-row { display: flex; gap: 8px; }
</style>
