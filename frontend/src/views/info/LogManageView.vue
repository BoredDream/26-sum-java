<template>
  <div class="log-manage-page">
    <page-header title="操作日志">
      <template #extra>
        <div class="toolbar-group">
          <el-input
            v-model="keyword"
            placeholder="搜索操作人/内容"
            clearable
            style="width: 200px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="filterType"
            placeholder="操作类型"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="备份" value="BACKUP" />
            <el-option label="恢复" value="RESTORE" />
          </el-select>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <div class="toolbar-group">
          <el-button type="danger" :icon="Delete" @click="handleClear">清理日志</el-button>
        </div>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && logs.length === 0 && !error" description="暂无操作日志" />

    <el-table v-loading="loading" :data="logs" border class="data-table">
      <el-table-column prop="logId" label="日志ID" width="90" />
      <el-table-column prop="operateType" label="操作类型" width="120" />
      <el-table-column prop="operateUserName" label="操作人" width="120" />
      <el-table-column prop="operateContent" label="操作内容" show-overflow-tooltip />
      <el-table-column prop="operateTime" label="操作时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as OperateLogVO).operateTime)
        }}</template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogs"
        @current-change="loadLogs"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { OperateLogVO } from '@/types/info'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const logs = ref<OperateLogVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const filterType = ref('')

async function loadLogs() {
  loading.value = true
  error.value = ''
  try {
    const res = await infoApi.queryOperateLogPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      operateType: filterType.value || undefined,
    })
    logs.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载操作日志失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadLogs()
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确认清理 90 天前的操作日志？此操作不可恢复。', '清理确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await infoApi.clearOperateLogs(90)
    ElMessage.success('清理完成')
    loadLogs()
  } catch (err: any) {
    ElMessage.error(err?.message || '清理失败')
  }
}

onMounted(loadLogs)
</script>

<style scoped lang="scss">
.log-manage-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .toolbar-group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .data-table {
    margin-top: 8px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
