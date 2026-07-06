<template>
  <div class="backup-page">
    <page-header title="数据备份">
      <template #extra>
        <div class="toolbar-group">
          <el-button type="primary" :icon="Plus" :loading="creating" @click="handleCreate">创建备份</el-button>
          <el-button type="danger" :icon="Delete" @click="handleCleanup">清理旧备份</el-button>
        </div>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && backups.length === 0 && !error" description="暂无备份" />

    <el-table v-loading="loading" :data="backups" border class="data-table">
      <el-table-column prop="backupId" label="备份ID" width="90" />
      <el-table-column prop="backupTime" label="备份时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as BackupVO).backupTime)
        }}</template>
      </el-table-column>
      <el-table-column prop="filePath" label="文件路径" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="文件大小" width="120" />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            text
            size="small"
            @click="handleDownload(scope.row as BackupVO)"
          >
            下载
          </el-button>
          <el-button
            type="warning"
            text
            size="small"
            :loading="restoreId === (scope.row as BackupVO).backupId"
            @click="handleRestore(scope.row as BackupVO)"
          >
            恢复
          </el-button>
          <el-button type="danger" text size="small" @click="handleDelete(scope.row as BackupVO)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadBackups"
        @current-change="loadBackups"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { BackupVO } from '@/types/info'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const backups = ref<BackupVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const creating = ref(false)
const restoreId = ref(0)

async function loadBackups() {
  loading.value = true
  error.value = ''
  try {
    const res = await infoApi.queryBackupPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    backups.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载备份列表失败'
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  try {
    await ElMessageBox.confirm('确认立即创建一份新的数据备份？', '操作确认', { type: 'warning' })
  } catch {
    return
  }
  creating.value = true
  try {
    await infoApi.createBackup()
    ElMessage.success('备份创建成功')
    loadBackups()
  } catch (err: any) {
    ElMessage.error(err?.message || '备份创建失败')
  } finally {
    creating.value = false
  }
}

function handleDownload(row: BackupVO) {
  infoApi.downloadBackup(row.backupId)
}

async function handleRestore(row: BackupVO) {
  try {
    await ElMessageBox.confirm(
      `确认使用备份 ${row.backupId} 恢复数据？当前数据可能被覆盖，请谨慎操作。`,
      '恢复确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  restoreId.value = row.backupId
  try {
    await infoApi.restoreBackup(row.backupId)
    ElMessage.success('备份恢复成功')
    loadBackups()
  } catch (err: any) {
    ElMessage.error(err?.message || '备份恢复失败')
  } finally {
    restoreId.value = 0
  }
}

async function handleDelete(row: BackupVO) {
  try {
    await ElMessageBox.confirm('确认删除该备份？删除后不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await infoApi.deleteBackup(row.backupId)
    ElMessage.success('删除成功')
    loadBackups()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

async function handleCleanup() {
  try {
    await ElMessageBox.confirm('确认清理过期备份？此操作不可恢复。', '清理确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await infoApi.cleanupBackups()
    ElMessage.success('清理完成')
    loadBackups()
  } catch (err: any) {
    ElMessage.error(err?.message || '清理失败')
  }
}

onMounted(loadBackups)
</script>

<style scoped lang="scss">
.backup-page {
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
