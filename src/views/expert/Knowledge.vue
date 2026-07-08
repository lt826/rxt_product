<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listMyKnowledge,
  publishKnowledge,
  updateKnowledge,
  deleteKnowledge
} from '../../api/knowledge'

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

async function loadList() {
  loading.value = true
  try {
    const res = await listMyKnowledge()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { knowledgeId: null, title: '', content: '', picPath: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, {
    knowledgeId: row.knowledgeId,
    title: row.title,
    content: row.content,
    picPath: row.picPath || ''
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = {
    title: form.title,
    content: form.content,
    picPath: form.picPath || null
  }
  if (editing.value) {
    payload.knowledgeId = form.knowledgeId
    await updateKnowledge(payload)
    ElMessage.success('修改成功')
  } else {
    await publishKnowledge(payload)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  loadList()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteKnowledge(row.knowledgeId)
  ElMessage.success('删除成功')
  loadList()
}

loadList()
</script>

<template>
  <div>
    <div class="page-head">
      <h2 class="page-title">知识管理</h2>
      <el-button type="primary" :icon="'Plus'" @click="openCreate">发布新文章</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe style="width:100%">
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image
            v-if="row.picPath"
            :src="row.picPath"
            style="width:50px;height:36px;border-radius:4px"
            fit="cover"
          />
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑文章' : '发布文章'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="form.picPath" placeholder="图片 URL（可选）" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="9" placeholder="请输入正文内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">{{ editing ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.muted {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
