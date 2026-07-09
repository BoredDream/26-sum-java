<template>
  <div class="documents-feedback-page">
    <page-header title="过程文档反馈" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && documents.length === 0 && !error" description="暂无可反馈文档" />

    <el-table v-loading="loading" :data="documents" border class="data-table">
      <el-table-column prop="documentName" label="文档名称" show-overflow-tooltip />
      <el-table-column prop="teamId" label="所属团队ID" />
      <el-table-column prop="documentType" label="文档类型" />
      <el-table-column prop="projectStage" label="项目阶段" />
      <el-table-column label="审核状态" width="120">
        <template #default="scope">
          <status-tag category="document" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="teacherFeedback" label="现有反馈" show-overflow-tooltip />
      <el-table-column prop="originalFilename" label="文件名" show-overflow-tooltip />
      <el-table-column prop="uploadTime" label="上传时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <span class="action-btns">
            <el-button type="primary" link size="small" @click="handleDownload(scope.row as ProcessDocumentVO)">下载</el-button>
            <el-button type="warning" link size="small" @click="openFeedback(scope.row as ProcessDocumentVO)">反馈</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="文档反馈" width="520px">
      <el-form :model="feedbackForm" label-width="100px">
        <el-form-item label="文档名称">
          <el-input :model-value="currentRow?.documentName" disabled />
        </el-form-item>
        <el-form-item label="所属团队">
          <el-input :model-value="currentRow?.teamId" disabled />
        </el-form-item>
        <el-form-item label="反馈意见">
          <el-input
            v-model="feedbackForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入反馈意见"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="是否退回">
          <el-switch v-model="feedbackForm.returned" active-text="退回" inactive-text="通过" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFeedback">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as selectionApi from '@/api/selection'
import type { ProcessDocumentVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const loading = ref(false)
const error = ref('')
const documents = ref<ProcessDocumentVO[]>([])

async function loadDocuments() {
  loading.value = true
  error.value = ''
  try {
    documents.value = await selectionApi.listDocuments()
  } catch (err: any) {
    error.value = err?.message || '加载文档失败'
  } finally {
    loading.value = false
  }
}

function handleDownload(row: ProcessDocumentVO) {
  const url = selectionApi.getDocumentDownloadUrl(row.id)
  downloadByUrl(url, {}, row.originalFilename)
}

// 反馈
const feedbackVisible = ref(false)
const submitting = ref(false)
const currentRow = ref<ProcessDocumentVO | null>(null)
const feedbackForm = ref({
  feedback: '',
  returned: false,
})

function openFeedback(row: ProcessDocumentVO) {
  currentRow.value = row
  feedbackForm.value = { feedback: '', returned: false }
  feedbackVisible.value = true
}

async function handleFeedback() {
  if (!currentRow.value) return
  if (!feedbackForm.value.feedback.trim()) {
    ElMessage.warning('请输入反馈意见')
    return
  }
  submitting.value = true
  try {
    await selectionApi.feedbackDocument(currentRow.value.id, feedbackForm.value)
    ElMessage.success('反馈已提交')
    feedbackVisible.value = false
    loadDocuments()
  } finally {
    submitting.value = false
  }
}

onMounted(loadDocuments)
</script>

<style scoped lang="scss">
.documents-feedback-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .action-btns {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
