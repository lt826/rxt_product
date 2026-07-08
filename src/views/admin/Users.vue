<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersAPI, getUserDetailAPI, createUserAPI, updateUserAPI, deleteUserAPI } from '../../api/admin'

const users = ref([])
const loading = ref(false)

const searchForm = ref({ keyword: '', role: '', status: '' })
const dialogVisible = ref(false)
const editingUser = ref({ userName: '', nickName: '', phone: '', role: '', realName: '' })
const isEdit = ref(false)

// 用户详情抽屉
const detailVisible = ref(false)
const detailUser = ref(null)
const detailLoading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUsersAPI()
    if (res.code === 200) {
      users.value = (res.data || []).map(u => ({
        userName: u.userName,
        name: u.nickName || u.userName,
        role: u.role,
        phone: u.phone || '-',
        realName: u.realName || '-',
        status: '正常',
        regDate: u.createTime ? u.createTime.substring(0, 10) : '-',
        _raw: u
      }))
    }
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

async function showDetail(row) {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getUserDetailAPI(row.userName)
    if (res.code === 200) {
      detailUser.value = res.data
    }
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  } finally {
    detailLoading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingUser.value = { userName: '', password: '', nickName: '', phone: '', role: 'farmer', realName: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingUser.value = {
    userName: row.userName,
    nickName: row.name,
    phone: row.phone,
    role: row.role,
    realName: row.realName
  }
  dialogVisible.value = true
}

async function saveUser() {
  if (!editingUser.value.userName) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    if (isEdit.value) {
      const res = await updateUserAPI(editingUser.value.userName, editingUser.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
      }
    } else {
      if (!editingUser.value.password) {
        ElMessage.warning('请输入密码')
        return
      }
      const res = await createUserAPI(editingUser.value)
      if (res.code === 200) {
        ElMessage.success('创建成功')
      }
    }
    dialogVisible.value = false
    await loadUsers()
  } catch (e) {
    // 已在拦截器中处理
  }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(`确定禁用用户 ${row.name} 吗？`, '提示', { type: 'warning' })
    const res = await deleteUserAPI(row.userName)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      await loadUsers()
    }
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadUsers)
</script>

<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>用户列表</span>
          <el-button type="primary" size="small" @click="openCreate">新增用户</el-button>
        </div>
      </template>
      <el-table :data="users" v-loading="loading" border stripe @row-click="showDetail">
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="name" label="昵称" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column label="角色">
          <template #default="{row}"><el-tag>{{ row.role }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="regDate" label="注册时间" width="120" />
        <el-table-column label="操作" width="150" @click.stop>
          <template #default="{row}">
            <el-button size="small" type="primary" link @click.stop="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click.stop="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="400px" v-loading="detailLoading">
      <template v-if="detailUser">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ detailUser.userName }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ detailUser.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ detailUser.role }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ detailUser.integral ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="信用分">{{ detailUser.credit ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detailUser.identityNum || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detailUser.createTime ? detailUser.createTime.substring(0, 10) : '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="editingUser.userName" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="editingUser.password" type="password" :placeholder="isEdit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editingUser.nickName" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editingUser.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editingUser.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editingUser.role">
            <el-option label="农户" value="farmer" />
            <el-option label="买家" value="buyer" />
            <el-option label="银行" value="bank" />
            <el-option label="专家" value="expert" />
            <el-option label="管理员" value="admin" />
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

<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
