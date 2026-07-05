<template>
  <div class="pending-applications-page">
    <page-header title="选题审核" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty
      v-if="!loading && applications.length === 0 && !error"
      description="暂无待审核选题申请"
    />

    <el-table v-loading="loading" :data="applications" border class="data-table">
      <el-table-column prop="topicTitle" label="课题名称" show-overflow-tooltip />
      <el-table-column prop="teamName" label="申请团队" />
      <el-table-column prop="selectionReason" label="选题说明" show-overflow-tooltip />
      <el-table-column label="审核状态" width="120">
        <template #default="scope">
          <status-tag category="selection" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'PENDING'"
            type="success"
            text
            size="small"
            @click="openAudit(scope.row as SelectionVO, true)"
            >通过</el-button
          >
          <el-button
            v-if="scope.row.status === 'PENDING'"
            type="danger"
            text
            size="small"
            @click="openAudit(scope.row as SelectionVO, false)"
            >驳回</el-button
          >
          <span v-else class="handled-text">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核选题申请" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="课题名称">
          <el-input :model-value="currentRow?.topicTitle" disabled />
        </el-form-item>
        <el-form-item label="申请团队">
          <el-input :model-value="currentRow?.teamName" disabled />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.approved ? 'success' : 'danger'">{{
            auditForm.approved ? '通过' : '驳回'
          }}</el-tag>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（选填）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
    applications.value = await selectionApi.listPendingSelections()
  } catch (err: any) {
    error.value = err?.message || '加载申请记录失败'
  } finally {
    loading.value = false
  }
}

// 审核
const auditVisible = ref(false)
const submitting = ref(false)
const currentRow = ref<SelectionVO | null>(null)
const auditForm = ref({
  approved: true,
  opinion: '',
})

function openAudit(row: SelectionVO, approved: boolean) {
  currentRow.value = row
  auditForm.value = { approved, opinion: '' }
  auditVisible.value = true
}

async function handleAudit() {
  if (!currentRow.value) return
  try {
    await ElMessageBox.confirm(
      `确认${auditForm.value.approved ? '通过' : '驳回'}该选题申请？`,
      '审核确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  submitting.value = true
  try {
    await selectionApi.auditSelection(currentRow.value.selectionId, auditForm.value)
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadApplications()
  } finally {
    submitting.value = false
  }
}

onMounted(loadApplications)
</script>

<style scoped lang="scss">
.pending-applications-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }

  .handled-text {
    color: #909399;
    font-size: 13px;
  }
}
</style>
