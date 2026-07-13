<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyGoodsAPI, publishGoodsAPI, updateGoodsAPI, deleteGoodsAPI } from '../../api/goods'
import { getClaimsByDemandAPI } from '../../api/claim'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const demands = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const form = ref({ title: '', price: null, content: '', address: '', picture: '' })

const claimsVisible = ref(false)
const currentClaims = ref([])
const currentClaimsTitle = ref('')

const dialogTitle = computed(() => editingId.value ? '编辑需求' : '发布需求')
const msgPath = computed(() => userStore.role === 'farmer' ? '/farmer/messages' : '/buyer/messages')

async function loadDemands() {
  loading.value = true
  try {
    const res = await getMyGoodsAPI({ type: '2' })
    if (res.code === 200) {
      const items = res.data || []
      // 并行查询每条需求的接取者
      const enriched = await Promise.all(items.map(async d => {
        let claims = []
        try {
          const cr = await getClaimsByDemandAPI(d.goodsId)
          if (cr && cr.code === 200) {
            claims = (cr.data || []).map(c => ({
              name: c.claimerName,
              time: c.createTime ? String(c.createTime).replace('T', ' ').substring(0, 16) : '-'
            }))
          }
        } catch { /* ignore */ }
        return {
          id: d.goodsId, title: d.title,
          price: d.price ? `¥${d.price}` : '面议',
          rawPrice: d.price != null ? Number(d.price) : null,
          content: d.content, address: d.address, picture: d.picture || '',
          createTime: d.createTime ? d.createTime.substring(0, 10) : '-',
          claims, claimCount: claims.length
        }
      }))
      demands.value = enriched
    }
  } catch (e) { ElMessage.error('获取需求列表失败') }
  finally { loading.value = false }
}

function openCreate() {
  editingId.value = null
  form.value = { title: '', price: null, content: '', address: '', picture: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    price: row.rawPrice,
    content: row.content || '',
    address: row.address || '',
    picture: row.picture || ''
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.title?.trim()) return ElMessage.warning('请输入需求标题')
  submitting.value = true
  const editing = !!editingId.value
  const successMsg = editing ? '修改成功' : '发布成功'
  try {
    const payload = {
      title: form.value.title.trim(),
      price: form.value.price != null ? Number(form.value.price) : null,
      content: form.value.content?.trim() || '',
      address: form.value.address?.trim() || '',
      picture: form.value.picture?.trim() || '',
      type: '2'
    }
    const res = editing
      ? await updateGoodsAPI(editingId.value, payload)
      : await publishGoodsAPI(payload)
    if (res.code === 200) {
      ElMessage.success(successMsg)
      dialogVisible.value = false
      await loadDemands()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除需求「${row.title}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
  } catch (e) { return }
  try {
    const res = await deleteGoodsAPI(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDemands()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function showClaims(row) {
  currentClaimsTitle.value = row.title
  currentClaims.value = row.claims || []
  claimsVisible.value = true
}

function goChat(claimerName) {
  claimsVisible.value = false
  router.push(msgPath.value + '?to=' + encodeURIComponent(claimerName))
}

onMounted(loadDemands)
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 class="page-title" style="margin:0">求购需求管理</h2>
      <el-button type="primary" @click="openCreate">发布需求</el-button>
    </div>
    <el-table :data="demands" v-loading="loading" stripe>
      <el-table-column prop="title" label="需求标题" min-width="200" />
      <el-table-column prop="price" label="期望价格" width="120" />
      <el-table-column prop="address" label="地区" width="150" />
      <el-table-column prop="createTime" label="发布时间" width="120" />
      <el-table-column label="接取" width="100" align="center">
        <template #default="{ row }">
          <el-button v-if="row.claimCount > 0" type="primary" link @click="showClaims(row)">
            {{ row.claimCount }} 人
          </el-button>
          <span v-else style="color:var(--text-muted)">0</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && demands.length === 0" description="暂无需求" />

    <!-- 接取者列表 -->
    <el-dialog v-model="claimsVisible" :title="'接取者列表 — ' + currentClaimsTitle" width="520px">
      <el-table :data="currentClaims" stripe size="small">
        <el-table-column prop="name" label="接取农户" min-width="140" />
        <el-table-column prop="time" label="接取时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goChat(row.name)">去私聊</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="currentClaims.length === 0" description="暂无接取" :image-size="60" />
      <template #footer>
        <el-button @click="claimsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发布/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="需求标题" required>
          <el-input v-model="form.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="期望价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="需求描述">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-title { font-size: 18px; }
</style>
