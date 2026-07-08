<template>
  <div class="my-logs-page">
    <page-header title="我的开发日志">
      <template #extra>
        <el-button type="primary" @click="openCreate">新增日志</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && logs.length === 0 && !error" description="暂无开发日志" />

    <el-timeline v-loading="loading">
      <el-timeline-item
        v-for="log in logs"
        :key="log.id"
        :timestamp="formatDate(log.logDate)"
        placement="top"
      >
        <el-card shadow="hover">
          <template #header>
            <div class="log-header">
              <span class="title">{{ log.title }}</span>
              <status-tag category="logCompletion" :value="log.completionStatus" />
            </div>
          </template>
          <p><strong>工作内容：</strong>{{ log.workContent }}</p>
          <p v-if="log.problemDescription">
            <strong>问题描述：</strong>{{ log.problemDescription }}
          </p>
          <p v-if="log.nextPlan"><strong>下一步计划：</strong>{{ log.nextPlan }}</p>
          <p v-if="log.teacherFeedback"><strong>教师反馈：</strong>{{ log.teacherFeedback }}</p>
          <p class="log-meta">
            提交人ID：{{ log.studentId }} · 提交时间：{{ formatDateTime(log.createTime) }}
          </p>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <!-- 新增日志弹窗 -->
    <el-dialog v-model="createVisible" title="新增开发日志" width="600px">
      <el-form ref="logFormRef" :model="logForm" :rules="logRules" label-width="100px">
        <el-form-item label="所属团队" prop="teamId">
          <el-select
            v-model="logForm.teamId"
            placeholder="请选择团队"
            style="width: 100%"
            :loading="myTeamsLoading"
          >
            <el-option
              v-for="team in myTeams"
              :key="team.id"
              :label="team.teamName"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日志标题" prop="title">
          <el-input v-model="logForm.title" placeholder="请输入日志标题" />
        </el-form-item>
        <el-form-item label="日志日期" prop="logDate">
          <el-date-picker
            v-model="logForm.logDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择日志日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="完成情况" prop="completionStatus">
          <el-select
            v-model="logForm.completionStatus"
            placeholder="请选择完成情况"
            style="width: 100%"
          >
            <el-option
              v-for="item in completionStatusOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工作内容" prop="workContent">
          <el-input
            v-model="logForm.workContent"
            type="textarea"
            :rows="4"
            placeholder="请输入工作内容"
            maxlength="3000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="logForm.problemDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入遇到的问题（选填）"
            maxlength="3000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="下一步计划">
          <el-input
            v-model="logForm.nextPlan"
            type="textarea"
            :rows="3"
            placeholder="请输入下一步计划（选填）"
            maxlength="3000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as selectionApi from '@/api/selection'
import type { DevelopmentLogVO, TeamVO } from '@/types/selection'
import { formatDate, formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const logs = ref<DevelopmentLogVO[]>([])
const completionStatusOptions = ['未开始', '进行中', '已完成', '已阻塞']

// 我的团队（用于新增日志时选择）
const myTeams = ref<TeamVO[]>([])
const myTeamsLoading = ref(false)

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

// 新增日志
const createVisible = ref(false)
const submitting = ref(false)
const logFormRef = ref<FormInstance>()
const logForm = ref({
  teamId: 0,
  title: '',
  logDate: '',
  workContent: '',
  completionStatus: '',
  problemDescription: '',
  nextPlan: '',
})

const logRules: FormRules = {
  teamId: [{ required: true, message: '请选择所属团队', trigger: 'change' }],
  title: [{ required: true, message: '请输入日志标题', trigger: 'blur' }],
  logDate: [{ required: true, message: '请选择日志日期', trigger: 'change' }],
  completionStatus: [{ required: true, message: '请选择完成情况', trigger: 'change' }],
  workContent: [{ required: true, message: '请输入工作内容', trigger: 'blur' }],
}

async function openCreate() {
  logForm.value = {
    teamId: 0,
    title: '',
    logDate: formatDate(new Date()),
    workContent: '',
    completionStatus: '',
    problemDescription: '',
    nextPlan: '',
  }
  createVisible.value = true
  // 加载团队列表
  myTeamsLoading.value = true
  try {
    myTeams.value = await selectionApi.getMyTeams()
    if (myTeams.value.length === 1) {
      logForm.value.teamId = myTeams.value[0].id
    }
  } catch {
    myTeams.value = []
  } finally {
    myTeamsLoading.value = false
  }
  logFormRef.value?.clearValidate()
}

async function handleCreate() {
  if (!logFormRef.value) return
  try {
    await logFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await selectionApi.createLog(logForm.value)
    ElMessage.success('提交成功')
    createVisible.value = false
    loadLogs()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadLogs)
</script>

<style scoped lang="scss">
.my-logs-page {
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
  }

  p {
    margin: 8px 0;
    color: #606266;
    line-height: 1.6;
  }

  .log-meta {
    font-size: 13px;
    color: #909399;
    margin-top: 12px;
  }
}
</style>
