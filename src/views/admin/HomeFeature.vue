<template>
  <div class="feature-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">主页内容管理</h2>
        <p class="page-sub">为首页各板块挑选展示内容并排序；未挑选的板块仍显示默认内容</p>
      </div>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header-row">
          <el-tabs v-model="activeSection" class="section-tabs">
            <el-tab-pane v-for="s in SECTIONS" :key="s.key" :label="s.label" :name="s.key" />
          </el-tabs>
        </div>
      </template>

      <!-- 已选内容（可排序） -->
      <div class="block">
        <div class="block-head">
          <span class="block-title">已选内容（拖动顺序）</span>
          <span class="block-tip">已选 {{ selected.length }} 项 · 首页按此顺序展示</span>
        </div>
        <div class="selected-list">
          <div v-for="(rid, idx) in selected" :key="rid" class="selected-item">
            <span class="sel-idx">{{ idx + 1 }}</span>
            <div class="sel-info">
              <div class="sel-title">{{ titleFor(rid) }}</div>
              <div class="sel-sub">{{ subFor(rid) }}</div>
            </div>
            <div class="sel-actions">
              <el-button size="small" link :disabled="idx === 0" @click="moveUp(idx)">上移</el-button>
              <el-button size="small" link :disabled="idx === selected.length - 1" @click="moveDown(idx)">下移</el-button>
              <el-button size="small" link type="danger" @click="removeAt(idx)">移除</el-button>
            </div>
          </div>
          <el-empty v-if="!loading && selected.length === 0" description="尚未选择内容，从下方候选中添加" :image-size="56" />
        </div>
      </div>

      <el-divider />

      <!-- 候选内容 -->
      <div class="block">
        <div class="block-head">
          <span class="block-title">候选内容</span>
          <el-input v-model="keyword" placeholder="搜索标题" clearable class="search-input" />
        </div>
        <el-table :data="filteredCandidates" v-loading="loading" stripe max-height="440">
          <el-table-column label="内容" min-width="280">
            <template #default="{ row }">
              <div class="cand-title">{{ meta.titleOf(row) }}</div>
              <div class="cand-sub">{{ meta.subOf(row) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link :disabled="isSelected(row)" @click="add(row)">
                {{ isSelected(row) ? '已添加' : '添加' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && filteredCandidates.length === 0" description="暂无候选内容" :image-size="56" />
      </div>

      <div class="footer-bar">
        <span class="save-tip">{{ dirty ? '有未保存的改动' : '改动已保存' }}</span>
        <el-button type="primary" :loading="saving" :disabled="!dirty" @click="save">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeatureSectionAPI, saveFeatureSectionAPI } from '../../api/homeFeature'

// 板块定义（顺序与首页一致）
const SECTIONS = [
  { key: 'finance', label: '金融产品' },
  { key: 'expert', label: '农业专家' },
  { key: 'answer', label: '热门问答' },
  { key: 'knowledge', label: '农业知识' },
  { key: 'goods', label: '助农电商' }
]

// 各板块：主键字段、标题/副标题取值
const SECTION_META = {
  finance: {
    refKey: 'id',
    titleOf: (p) => p.name || '金融产品',
    subOf: (p) => [p.bankName, p.rate != null ? `年利率 ${p.rate}%` : null].filter(Boolean).join(' · ')
  },
  expert: {
    refKey: 'userName',
    titleOf: (e) => e.realName || e.userName,
    subOf: (e) => [e.profession, e.belong].filter(Boolean).join(' · ')
  },
  answer: {
    refKey: 'id',
    titleOf: (q) => q.title || (q.question ? String(q.question).slice(0, 30) : '问答'),
    subOf: (q) => [q.plantName, q.expertName ? `专家：${q.expertName}` : null].filter(Boolean).join(' · ')
  },
  knowledge: {
    refKey: 'knowledgeId',
    titleOf: (k) => k.title || '农业知识',
    subOf: (k) => k.ownName || ''
  },
  goods: {
    refKey: 'goodsId',
    titleOf: (g) => g.title || '商品',
    subOf: (g) => g.price != null ? `¥${g.price}` : '面议'
  }
}

const activeSection = ref('expert')
const candidates = ref([])
const selected = ref([]) // refId 数组，顺序即展示顺序
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const dirty = ref(false)

const meta = computed(() => SECTION_META[activeSection.value])

// refId -> 候选对象
const candidateMap = computed(() => {
  const map = {}
  const key = meta.value.refKey
  candidates.value.forEach((row) => {
    if (row && row[key] != null) map[String(row[key])] = row
  })
  return map
})

const filteredCandidates = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return candidates.value
  return candidates.value.filter((row) => String(meta.value.titleOf(row)).toLowerCase().includes(kw))
})

function refIdOf(row) {
  return String(row[meta.value.refKey])
}
function isSelected(row) {
  return selected.value.includes(refIdOf(row))
}
function titleFor(rid) {
  const row = candidateMap.value[rid]
  return row ? meta.value.titleOf(row) : rid
}
function subFor(rid) {
  const row = candidateMap.value[rid]
  return row ? meta.value.subOf(row) : ''
}

function add(row) {
  const rid = refIdOf(row)
  if (!selected.value.includes(rid)) {
    selected.value.push(rid)
    dirty.value = true
  }
}
function removeAt(idx) {
  selected.value.splice(idx, 1)
  dirty.value = true
}
function moveUp(idx) {
  if (idx <= 0) return
  const arr = selected.value
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
  dirty.value = true
}
function moveDown(idx) {
  const arr = selected.value
  if (idx >= arr.length - 1) return
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
  dirty.value = true
}

async function loadSection(section) {
  loading.value = true
  keyword.value = ''
  // 先清空，避免切换板块瞬间用旧数据套用新板块字段渲染
  candidates.value = []
  selected.value = []
  dirty.value = false
  try {
    const res = await getFeatureSectionAPI(section)
    if (res.code === 200 && res.data) {
      candidates.value = res.data.candidates || []
      // selected 已按 sort_order 升序返回
      selected.value = (res.data.selected || []).map((it) => it.refId)
    }
  } catch {
    candidates.value = []
    selected.value = []
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const items = selected.value.map((refId, idx) => ({ refId, sortOrder: idx }))
    const res = await saveFeatureSectionAPI(activeSection.value, items)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dirty.value = false
    }
  } catch {
    /* handled in interceptor */
  } finally {
    saving.value = false
  }
}

watch(activeSection, (s) => { if (s) loadSection(s) })
onMounted(() => loadSection(activeSection.value))
</script>

<style scoped>
.feature-page { max-width: 1200px; }
.page-head { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: var(--green-deep); margin: 0 0 4px; }
.page-sub { font-size: 14px; color: var(--text-muted); margin: 0; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.section-tabs { --el-tabs-header-height: 46px; }
.section-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }
.section-tabs :deep(.el-tabs__active-bar) { background-color: var(--green); }
.section-tabs :deep(.el-tabs__item.is-active) { color: var(--green); }
.section-tabs :deep(.el-tabs__item:hover) { color: var(--green); }

.block { padding: 4px 0; }
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.block-title { font-size: 15px; font-weight: 700; color: var(--green-deep); }
.block-tip { font-size: 13px; color: var(--text-muted); }
.search-input { width: 240px; }

/* 已选列表 */
.selected-list { display: flex; flex-direction: column; gap: 8px; }
.selected-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--green-mist);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
}
.sel-idx {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--green); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.sel-info { flex: 1; min-width: 0; }
.sel-title { font-size: 14px; font-weight: 600; color: var(--green-deep); }
.sel-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.sel-actions { display: flex; gap: 4px; flex-shrink: 0; }

/* 候选表格 */
.cand-title { font-size: 14px; font-weight: 600; color: var(--text); }
.cand-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.footer-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  margin-top: 22px;
}
.save-tip { font-size: 13px; color: var(--text-muted); }
</style>
