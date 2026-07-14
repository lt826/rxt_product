<template>
  <div class="users-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-sub">管理平台所有用户</p>
      </div>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header-row">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="所有用户" name="all" />
            <el-tab-pane :label="`待审批 (${pendingCount})`" name="pending" />
          </el-tabs>
          <el-button type="primary" size="small" @click="openCreate">新增用户</el-button>
        </div>
      </template>

      <div v-if="activeTab === 'pending'">
        <el-table :data="pendingUsers" v-loading="pendingLoading" stripe>
          <el-table-column prop="userName" label="用户名" width="150" />
          <el-table-column prop="name" label="昵称" />
          <el-table-column prop="realName" label="真实姓名" />
          <el-table-column label="角色" width="120"><template #default="{row}"><el-tag size="small">{{ row.role }}</el-tag></template></el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="regDate" label="注册时间" width="120" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="success" @click="handleApprove(row, true)">通过</el-button>
              <el-button size="small" type="danger" @click="handleApprove(row, false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!pendingLoading && pendingUsers.length === 0" description="暂无待审批用户" />
      </div>

      <div v-else>
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="用户名 / 昵称" clearable class="search-input"
            @keyup.enter="onSearch" @clear="onSearch" />
          <el-select v-model="roleFilter" placeholder="全部角色" clearable class="role-select"
            @change="onSearch" @clear="onSearch">
            <el-option label="农户" value="farmer" />
            <el-option label="买家" value="buyer" />
            <el-option label="银行" value="bank" />
            <el-option label="专家" value="expert" />
            <el-option label="管理员" value="admin" />
          </el-select>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>

        <el-table :data="users" v-loading="loading" stripe @row-click="showDetail">
          <el-table-column prop="userName" label="用户名" />
          <el-table-column prop="name" label="昵称" />
          <el-table-column prop="realName" label="真实姓名" />
          <el-table-column label="角色"><template #default="{row}"><el-tag size="small">{{ row.role }}</el-tag></template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag></template></el-table-column>
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="regDate" label="注册时间" width="120" />
          <el-table-column label="操作" width="150" @click.stop>
            <template #default="{row}">
              <el-button size="small" type="primary" link @click.stop="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" link @click.stop="deleteUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && users.length === 0" description="暂无用户" />

        <!-- 分页 -->
        <div class="pagination-row">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="onSizeChange"
            @current-change="onPageChange"
          />
        </div>
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="用户详情" size="400px" v-loading="detailLoading">
      <template v-if="detailUser">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ detailUser.userName }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ detailUser.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ detailUser.role }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="getStatusType(detailUser.status)">{{ getStatusText(detailUser.status) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="积分">{{ detailUser.integral ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="信用分">{{ detailUser.credit ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detailUser.identityNum || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detailUser.createTime ? detailUser.createTime.substring(0, 10) : '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="用户名" required><el-input v-model="editingUser.userName" :disabled="isEdit" /></el-form-item>
        <el-form-item label="密码" :required="!isEdit"><el-input v-model="editingUser.password" type="password" :placeholder="isEdit ? '留空不修改' : '请输入密码'" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="editingUser.nickName" /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="editingUser.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editingUser.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editingUser.role" style="width:100%">
            <el-option label="农户" value="farmer" /><el-option label="买家" value="buyer" /><el-option label="银行" value="bank" /><el-option label="专家" value="expert" /><el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersAPI, getUserDetailAPI, createUserAPI, updateUserAPI, deleteUserAPI, getPendingUsersAPI, approveUserAPI } from '../../api/admin'

const users = ref([])
const loading = ref(false)
const activeTab = ref('all')
const dialogVisible = ref(false)
const editingUser = ref({ userName: '', nickName: '', phone: '', role: '', realName: '' })
const isEdit = ref(false)
const pendingUsers = ref([])
const pendingLoading = ref(false)
const detailVisible = ref(false)
const detailUser = ref(null)
const detailLoading = ref(false)

// 分页 + 搜索
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const roleFilter = ref('')

const pendingCount = computed(() => pendingUsers.value.length)

async function loadUsers() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (roleFilter.value) params.role = roleFilter.value
    const res = await getUsersAPI(params)
    if (res.code === 200 && res.data) {
      users.value = (res.data.records || []).map(u => ({
        userName: u.userName, name: u.nickName || u.userName, role: u.role, phone: u.phone || '-',
        realName: u.realName || '-', status: u.status || 'approved',
        statusText: getStatusText(u.status), regDate: u.createTime ? u.createTime.substring(0, 10) : '-', _raw: u
      }))
      total.value = res.data.total || 0
    }
  } catch { ElMessage.error('获取用户列表失败') } finally { loading.value = false }
}

function onSearch() {
  page.value = 1
  loadUsers()
}

function resetSearch() {
  keyword.value = ''
  roleFilter.value = ''
  page.value = 1
  loadUsers()
}

function onPageChange(p) {
  page.value = p
  loadUsers()
}

function onSizeChange(s) {
  size.value = s
  page.value = 1
  loadUsers()
}

async function loadPendingUsers() {
  pendingLoading.value = true
  try {
    const res = await getPendingUsersAPI()
    if (res.code === 200) pendingUsers.value = (res.data || []).map(u => ({
      userName: u.userName, name: u.nickName || u.userName, role: u.role, phone: u.phone || '-',
      realName: u.realName || '-', regDate: u.createTime ? u.createTime.substring(0, 10) : '-', _raw: u
    }))
  } catch { ElMessage.error('获取待审批用户失败') } finally { pendingLoading.value = false }
}

function getStatusText(s) { const m = { pending: '待审批', approved: '已通过', rejected: '已拒绝', normal: '已通过' }; return m[s] || (s || '已通过') }
function getStatusType(s) { const m = { pending: 'warning', approved: 'success', rejected: 'danger' }; return m[s] || 'info' }

async function showDetail(row) {
  detailLoading.value = true; detailVisible.value = true
  try { const res = await getUserDetailAPI(row.userName); if (res.code === 200) detailUser.value = res.data } catch { ElMessage.error('获取用户详情失败') } finally { detailLoading.value = false }
}

function openCreate() { isEdit.value = false; editingUser.value = { userName: '', password: '', nickName: '', phone: '', role: 'farmer', realName: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; editingUser.value = { userName: row.userName, nickName: row.name, phone: row.phone, role: row.role, realName: row.realName }; dialogVisible.value = true }

async function saveUser() {
  if (!editingUser.value.userName) return ElMessage.warning('请输入用户名')
  try {
    if (isEdit.value) { const res = await updateUserAPI(editingUser.value.userName, editingUser.value); if (res.code === 200) ElMessage.success('更新成功') }
    else {
      if (!editingUser.value.password) return ElMessage.warning('请输入密码')
      const res = await createUserAPI(editingUser.value)
      if (res.code === 200) ElMessage.success('创建成功')
    }
    dialogVisible.value = false; await loadUsers(); await loadPendingUsers()
  } catch { /* handled in interceptor */ }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${row.name} 吗？`, '提示', { type: 'warning' })
    const res = await deleteUserAPI(row.userName)
    if (res.code === 200) { ElMessage.success('操作成功'); await loadUsers() }
  } catch { /* cancelled */ }
}

async function handleApprove(row, approved) {
  try {
    const action = approved ? '通过' : '拒绝'
    await ElMessageBox.confirm(`确定${action}用户 ${row.name} 的申请吗？`, '审批确认', { type: approved ? 'success' : 'warning' })
    const res = await approveUserAPI(row.userName, approved)
    if (res.code === 200) { ElMessage.success(`已${action}该用户申请`); await loadPendingUsers(); await loadUsers() }
  } catch { /* cancelled */ }
}

function handleTabChange() { if (activeTab.value === 'pending') loadPendingUsers() }
onMounted(() => { loadUsers(); loadPendingUsers() })
</script>

<style scoped>
.users-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.search-input { width: 220px; }
.role-select { width: 140px; }

/* 分页 */
.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
