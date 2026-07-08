<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listExperts } from '../../api/expert'
import { applyReserve, listMyReserves } from '../../api/reserve'

const activeTab = ref('book')
const loading = ref(false)
const submitting = ref(false)
const experts = ref([])
const myReserves = ref([])

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  expertName: '',
  expertRealName: '',
  area: '',
  address: '',
  plantName: '',
  soilCondition: '',
  plantCondition: '',
  plantDetail: '',
  message: ''
})

const rules = {
  expertName: [{ required: true, message: '请选择专家', trigger: 'change' }],
  area: [{ required: true, message: '请输入种植面积', trigger: 'blur' }],
  address: [{ required: true, message: '请输入土地地址', trigger: 'blur' }],
  plantName: [{ required: true, message: '请输入作物名称', trigger: 'blur' }],
  plantCondition: [{ required: true, message: '请描述作物当前状况', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const [e, r] = await Promise.all([listExperts(), listMyReserves()])
    experts.value = e.data || []
    myReserves.value = r.data || []
  } finally {
    loading.value = false
  }
}

function openBook(expert) {
  Object.assign(form, {
    expertName: expert.userName,
    expertRealName: expert.realName,
    area: '',
    address: '',
    plantName: '',
    soilCondition: '',
    plantCondition: '',
    plantDetail: '',
    message: ''
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await applyReserve({ ...form })
    ElMessage.success('出诊预约已提交，等待专家确认')
    dialogVisible.value = false
    myReserves.value = (await listMyReserves()).data || []
    activeTab.value = 'mine'
  } finally {
    submitting.value = false
  }
}

function statusInfo(s) {
  if (s === 1) return { text: '已接受', type: 'success' }
  if (s === 2) return { text: '已拒绝', type: 'info' }
  return { text: '待处理', type: 'warning' }
}

loadData()
</script>

<template>
  <div v-loading="loading">
    <h2 class="page-title">专家预约</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="预约专家" name="book">
        <el-empty v-if="!experts.length" description="暂无可预约的专家" />
        <div v-for="e in experts" :key="e.userName" class="expert-card">
          <div class="expert-avatar"><el-icon size="30"><User /></el-icon></div>
          <div class="expert-body">
            <div class="expert-name">{{ e.realName }}</div>
            <div class="expert-field">{{ e.profession }}<span v-if="e.position"> · {{ e.position }}</span><span v-if="e.belong"> · {{ e.belong }}</span></div>
          </div>
          <el-button type="primary" size="small" @click="openBook(e)">预约出诊</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`我的预约（${myReserves.length}）`" name="mine">
        <el-empty v-if="!myReserves.length" description="暂无预约记录" />
        <div v-for="r in myReserves" :key="r.id" class="reserve-item">
          <div class="ri-head">
            <span class="ri-expert">专家：{{ r.expertName }}</span>
            <el-tag :type="statusInfo(r.status).type">{{ statusInfo(r.status).text }}</el-tag>
          </div>
          <div class="ri-line"><el-tag size="small" type="info">{{ r.plantName }}</el-tag><span>面积：{{ r.area }}</span></div>
          <div class="ri-line muted">{{ r.address }}</div>
          <div class="ri-line muted" v-if="r.plantCondition">作物状况：{{ r.plantCondition }}</div>
          <div class="ri-answer" v-if="r.answer"><strong>专家回复：</strong>{{ r.answer }}</div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="`预约 ${form.expertRealName} 出诊`" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="作物名称" prop="plantName">
          <el-input v-model="form.plantName" placeholder="如：五常大米" />
        </el-form-item>
        <el-form-item label="种植面积" prop="area">
          <el-input v-model="form.area" placeholder="如：50亩" />
        </el-form-item>
        <el-form-item label="土地地址" prop="address">
          <el-input v-model="form.address" placeholder="土地所在详细地址" />
        </el-form-item>
        <el-form-item label="土壤条件">
          <el-input v-model="form.soilCondition" placeholder="如：黑土地，近期积水较重（可选）" />
        </el-form-item>
        <el-form-item label="作物状况" prop="plantCondition">
          <el-input v-model="form.plantCondition" type="textarea" :rows="2" placeholder="作物当前表现，如叶片发黄、根部腐烂等" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="form.plantDetail" type="textarea" :rows="3" placeholder="更多细节，帮助专家判断（可选）" />
        </el-form-item>
        <el-form-item label="给专家留言">
          <el-input v-model="form.message" type="textarea" :rows="2" placeholder="如希望专家尽快上门的时间要求（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 20px; }
.expert-card { display: flex; align-items: center; gap: 14px; padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; }
.expert-avatar { width: 48px; height: 48px; border-radius: 50%; background: #f0f2f5; display: flex; align-items: center; justify-content: center; color: #764ba2; flex-shrink: 0; }
.expert-body { flex: 1; }
.expert-name { font-weight: 600; color: #303133; font-size: 15px; }
.expert-field { font-size: 13px; color: #909399; margin-top: 4px; }
.reserve-item { padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; }
.ri-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.ri-expert { font-weight: 500; color: #303133; }
.ri-line { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #606266; margin-bottom: 4px; }
.muted { color: #909399; font-size: 13px; }
.ri-answer { margin-top: 8px; background: #f0f9eb; padding: 10px; border-radius: 6px; color: #529b2e; font-size: 14px; line-height: 1.6; }
</style>
