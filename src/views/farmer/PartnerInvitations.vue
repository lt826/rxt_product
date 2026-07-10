<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReceivedInvitationsAPI,
  getSentInvitationsAPI,
  replyInvitationAPI,
  clearReceivedInvitationsAPI,
  clearSentInvitationsAPI
} from '../../api/finance'

const activeTab = ref('received')
const receivedList = ref([])
const sentList = ref([])
const loading = ref(false)

const replyDialogVisible = ref(false)
const currentInvitation = ref(null)
const replyForm = ref({
  status: 1,
  remark: ''
})

const statusMap = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '已同意', type: 'success' },
  2: { text: '已拒绝', type: 'danger' }
}

// 加载收到的邀请
async function loadReceivedInvitations() {
  loading.value = true
  try {
    const res = await getReceivedInvitationsAPI()
    if (res.code === 200) {
      receivedList.value = res.data || []
    }
  } catch {
    ElMessage.error('获取邀请失败')
  } finally {
    loading.value = false
  }
}

// 加载发送的邀请
async function loadSentInvitations() {
  loading.value = true
  try {
    const res = await getSentInvitationsAPI()
    if (res.code === 200) {
      sentList.value = res.data || []
    }
  } catch {
    ElMessage.error('获取邀请失败')
  } finally {
    loading.value = false
  }
}

// 切换标签
function handleTabChange() {
  if (activeTab.value === 'received') {
    loadReceivedInvitations()
  } else {
    loadSentInvitations()
  }
}

// 打开回复对话框
function openReplyDialog(invitation) {
  currentInvitation.value = invitation
  replyForm.value = {
    status: 1,
    remark: ''
  }
  replyDialogVisible.value = true
}

// 回复邀请
async function handleReply() {
  if (!currentInvitation.value) return

  try {
    const res = await replyInvitationAPI(
      currentInvitation.value.id,
      replyForm.value.status,
      replyForm.value.remark
    )
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      await loadReceivedInvitations()
    }
  } catch {
    // handled in interceptor
  }
}

// 清除所有收到的邀请
async function clearReceived() {
  try {
    await ElMessageBox.confirm('确定清除所有收到的邀请吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定清除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await clearReceivedInvitationsAPI()
    if (res.code === 200) {
      ElMessage.success(res.message || '已清除')
      await loadReceivedInvitations()
    }
  } catch {
    // 错误由拦截器处理
  }
}

// 清除所有发送的邀请
async function clearSent() {
  try {
    await ElMessageBox.confirm('确定清除所有发送的邀请吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定清除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await clearSentInvitationsAPI()
    if (res.code === 200) {
      ElMessage.success(res.message || '已清除')
      await loadSentInvitations()
    }
  } catch {
    // 错误由拦截器处理
  }
}

// 删除邀请（仅发送的）
function removeInvitation(invitation, index) {
  ElMessageBox.confirm(
    '确定要移除这条邀请记录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    sentList.value.splice(index, 1)
    ElMessage.success('已移除')
  }).catch(() => {
    // 用户取消
  })
}

// 格式化时间
function formatTime(timeStr) {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ')
}

onMounted(() => {
  loadReceivedInvitations()
})
</script>

<template>
  <div class="invitations-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">联合贷款人邀请</h2>
        <p class="page-sub">管理您收到和发送的联合贷款邀请</p>
      </div>
    </div>

    <el-card shadow="never" class="main-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 收到的邀请 -->
        <el-tab-pane label="收到的邀请" name="received">
          <div class="tab-toolbar">
            <el-button
              type="danger"
              plain
              size="small"
              :disabled="!receivedList.length"
              @click="clearReceived"
            >
              清除全部
            </el-button>
          </div>
          <el-table :data="receivedList" v-loading="loading" stripe>
            <el-table-column prop="applicantRealName" label="邀请人" width="120" />
            <el-table-column prop="applicantPhone" label="联系电话" width="130" />
            <el-table-column prop="projectDescription" label="项目描述" min-width="200" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusMap[row.status]?.type" size="small">
                  {{ statusMap[row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="邀请时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="replyTime" label="回复时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.replyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  text
                  type="primary"
                  size="small"
                  @click="openReplyDialog(row)"
                >
                  回复
                </el-button>
                <el-text v-else-if="row.replyRemark" type="info" size="small">
                  {{ row.replyRemark }}
                </el-text>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && receivedList.length === 0" description="暂无收到的邀请" />
        </el-tab-pane>

        <!-- 发送的邀请 -->
        <el-tab-pane label="发送的邀请" name="sent">
          <div class="tab-toolbar">
            <el-button
              type="danger"
              plain
              size="small"
              :disabled="!sentList.length"
              @click="clearSent"
            >
              清除全部
            </el-button>
          </div>
          <el-table :data="sentList" v-loading="loading" stripe>
            <el-table-column prop="applicantRealName" label="被邀请人" width="120" />
            <el-table-column prop="applicantPhone" label="联系电话" width="130" />
            <el-table-column prop="projectDescription" label="项目描述" min-width="200" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusMap[row.status]?.type" size="small">
                  {{ statusMap[row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发送时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="replyTime" label="回复时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.replyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row, $index }">
                <el-button
                  text
                  type="danger"
                  size="small"
                  @click="removeInvitation(row, $index)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && sentList.length === 0" description="暂无发送的邀请" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复联合贷款邀请" width="500px">
      <el-form label-width="100px">
        <el-form-item label="邀请人">
          <span>{{ currentInvitation?.applicantRealName }}</span>
        </el-form-item>
        <el-form-item label="项目描述">
          <div class="dialog-desc">{{ currentInvitation?.projectDescription }}</div>
        </el-form-item>
        <el-form-item label="您的回复" required>
          <el-radio-group v-model="replyForm.status">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="replyForm.remark"
            placeholder="请填写备注说明（可选）"
            :rows="3"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.invitations-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--green-deep);
  margin: 0 0 4px;
}
.page-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.main-card {
  border-radius: var(--radius-md);
}

.tab-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.dialog-desc {
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
}
</style>
