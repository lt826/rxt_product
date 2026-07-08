<template>
  <div>
    <h2 class="page-title">知识管理</h2>
    <div style="margin-bottom:16px"><el-button type="primary" @click="dialogVisible=true">发布新文章</el-button></div>
    <el-table :data="articles" border stripe><el-table-column prop="title" label="标题" min-width="200" /><el-table-column prop="category" label="分类" width="100" /><el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="row.status==='已发布'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column><el-table-column prop="views" label="阅读量" width="80" /><el-table-column prop="date" label="发布时间" width="120" /><el-table-column label="操作" width="150"><template #default><el-button size="small" type="primary" link>编辑</el-button><el-button size="small" type="danger" link>删除</el-button></template></el-table-column></el-table>

    <el-dialog v-model="dialogVisible" title="发布文章" width="700px">
      <el-form label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category"><el-option label="种植技术" value="种植" /><el-option label="养殖技术" value="养殖" /><el-option label="金融知识" value="金融" /></el-select></el-form-item>
        <el-form-item label="内容"><el-input type="textarea" :rows="6" v-model="form.content" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary">发布</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const dialogVisible = ref(false)
const form = ref({ title:'', category:'', content:'' })
const articles = ref([
  { title:'水稻高产种植技术要点', category:'种植', status:'已发布', views:156, date:'2026-07-05' },
  { title:'有机蔬菜病虫害防治指南', category:'种植', status:'已发布', views:98, date:'2026-07-03' },
  { title:'春季养鸡注意事项', category:'养殖', status:'草稿', views:0, date:'-' }
])
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
