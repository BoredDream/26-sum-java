<template>
  <div class="my-application-page">
    <page-header title="我的选题申请" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && applications.length === 0 && !error" description="暂无选题申请" />

    <el-table v-loading="loading" :data="applications" border class="data-table">
      <el-table-column prop="topicTitle" label="课题名称" show-overflow-tooltip />
      <el-table-column prop="teamName" label="申请团队" />
      <el-table-column prop="selectionReason" label="选题说明" show-overflow-tooltip />
      <el-table-column label="审核状态" width="120">
        <template #default="{ row }">
          <status-tag category="selection" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="auditTeacherId" label="审核教师ID" />
      <el-table-column prop="auditOpinion" label="审核意见" show-overflow-tooltip />
      <el-table-column prop="auditTime" label="审核时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.auditTime) }}</template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as selectionApi from '@/api/selection'
import type { SelectionVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const applications = ref<SelectionVO[]>([])

async function loadApplications() {
  loading.value = true
  error.value = ''
  try {
    applications.value = await selectionApi.getMySelection()
  } catch (err: any) {
    error.value = err?.message || '加载申请记录失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadApplications)
</script>

<style scoped lang="scss">
.my-application-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
