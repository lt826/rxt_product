<template>
  <div>
    <h2 class="page-title">收购需求</h2>
    <el-card style="margin-bottom:20px">
      <template #header><span>发布收购需求</span></template>
      <el-form inline>
        <el-form-item label="农产品"><el-input placeholder="名称" /></el-form-item>
        <el-form-item label="需求量"><el-input placeholder="数量" style="width:120px" /></el-form-item>
        <el-form-item label="单位"><el-select style="width:90px"><el-option label="kg" value="kg" /><el-option label="吨" value="ton" /></el-select></el-form-item>
        <el-form-item label="价格"><el-input placeholder="最高单价" style="width:120px" /></el-form-item>
        <el-form-item><el-button type="primary">发布</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card><template #header><span>我的收购需求</span></template><el-table :data="demands" border stripe><el-table-column prop="product" label="农产品" /><el-table-column prop="amount" label="需求量" /><el-table-column prop="price" label="最高价" /><el-table-column prop="status" label="状态"><template #default="{row}"><el-tag :type="row.status==='已匹配'?'success':'warning'">{{row.status}}</el-tag></template></el-table-column><el-table-column label="操作"><el-button text type="primary" size="small">查看匹配</el-button></el-table-column></el-table></el-card>
  </div>
</template>
<script setup>
const demands = [
  { product: '有机大米', amount: '1000kg', price: '6元/kg', status: '匹配中' },
  { product: '土鸡蛋', amount: '500盒', price: '35元/盒', status: '已匹配' }
]
</script>
<style scoped>
.page-title { font-size:20px;font-weight:600;color:#303133;margin:0 0 20px }
</style>
