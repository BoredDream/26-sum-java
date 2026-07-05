<template>
  <div class="logs-feedback-page">
    <page-header title="开发日志反馈" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && logs.length === 0 && !error" description="暂无可反馈日志" />

    <el-timeline v-loading="loading">
      <el-timeline-item
        v-for="log in logs"
        :key="log.logId"
        :timestamp="formatDate(log.logDate)"
        placement="top"
      >
        <el-card shadow="hover">
          <template #header>
            <div class="log-header">
              <span class="title">{{ log.title }}</span>
              <div class="header-actions">
                <status-tag category="log" :value="log.completionStatus" />
                <el-button type="warning" text size="small" @click="openFeedback(log)"
                  >反馈</el-button
                >
              </div>
            </div>
          </template>
          <p><strong>所属团队：</strong>{{ log.teamName }}</p>
          <p><strong>提交人：</strong>{{ log.studentName }}</p>
          <p><strong>工作内容：</strong>{{ log.workContent }}</p>
          <p v-if="log.problemDescription">
            <strong>问题描述：</strong>{{ log.problemDescription }}
          </p>
          <p v-if="log.nextPlan"><strong>下一步计划：</strong>{{ log.nextPlan }}</p>
          <p v-if="log.feedback" class="feedback-text">
            <strong>现有反馈：</strong>{{ log.feedback }}
          </p>
          <p class="log-meta">提交时间：{{ formatDateTime(log.createTime) }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <!-- 反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="日志反馈" width="520px">
      <el-form :model="feedbackForm" label-width="100px">
        <el-form-item label="日志标题">
          <el-input :model-value="currentRow?.title" disabled />
        </el-form-item>
        <el-form-item label="所属团队">
          <el-input :model-value="currentRow?.teamName" disabled />
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
import type { DevelopmentLogVO } from '@/types/selection'
import { formatDate, formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const logs = ref<DevelopmentLogVO[]>([])

async function loadLogs() {
  loading.value = true
  error.value = ''
  try {
    logs.value = await selectionApi.listLogs()
  } catch (err: any) {
    error.value = err?.message || '加载日志失败'
  } finally {
    loading.value = false
  }
}

// 反馈
const feedbackVisible = ref(false)
const submitting = ref(false)
const currentRow = ref<DevelopmentLogVO | null>(null)
const feedbackForm = ref({
  feedback: '',
})

function openFeedback(row: DevelopmentLogVO) {
  currentRow.value = row
  feedbackForm.value = { feedback: '' }
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
    await selectionApi.feedbackLog(currentRow.value.logId, feedbackForm.value)
    ElMessage.success('反馈已提交')
    feedbackVisible.value = false
    loadLogs()
  } finally {
    submitting.value = false
  }
}

onMounted(loadLogs)
</script>

<style scoped lang="scss">
.logs-feedback-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .log-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title {
      font-size: 16px;
      font-weight: 600;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  p {
    margin: 8px 0;
    color: #606266;
    line-height: 1.6;
  }

  .feedback-text {
    color: #e6a23c;
  }

  .log-meta {
    font-size: 13px;
    color: #909399;
    margin-top: 12px;
  }
}
</style>
