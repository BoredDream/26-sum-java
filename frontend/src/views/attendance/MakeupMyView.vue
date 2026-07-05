<template>
  <div class="makeup-my-page">
    <page-header title="我的补签">
      <template #extra>
        <el-button type="primary" @click="openApply">申请补签</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && applications.length === 0 && !error" description="暂无补签申请" />

    <el-table v-loading="loading" :data="applications" border class="data-table">
      <el-table-column prop="taskTitle" label="签到任务" show-overflow-tooltip />
      <el-table-column prop="applyReason" label="补签原因" show-overflow-tooltip />
      <el-table-column label="证明材料" width="220" show-overflow-tooltip>
        <template #default="scope">
          <el-tooltip
            v-if="(scope.row as MakeupApplyVO).proofFilePath"
            effect="dark"
            :content="(scope.row as MakeupApplyVO).proofFilePath"
            placement="top"
          >
            <span class="proof-path">{{ (scope.row as MakeupApplyVO).proofFilePath }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <status-tag category="makeup" :value="(scope.row as MakeupApplyVO).auditStatus" />
        </template>
      </el-table-column>
      <el-table-column prop="auditTeacherName" label="审核人" width="120" />
      <el-table-column prop="auditComment" label="审核意见" show-overflow-tooltip />
      <el-table-column label="申请时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as MakeupApplyVO).createTime)
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
        @size-change="loadApplications"
        @current-change="loadApplications"
      />
    </div>

    <!-- 申请补签 -->
    <el-dialog v-model="applyVisible" title="申请补签" width="550px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="签到任务" prop="taskId">
          <el-select
            v-model="applyForm.taskId"
            placeholder="请选择签到任务"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="task in taskOptions"
              :key="task.taskId"
              :label="task.taskTitle"
              :value="task.taskId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="补签原因" prop="applyReason">
          <el-input
            v-model="applyForm.applyReason"
            type="textarea"
            :rows="4"
            placeholder="请填写补签原因（5-500字）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.png,.jpg,.jpeg,.doc,.docx"
          >
            <el-button type="primary" text>选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 pdf、png、jpg、doc、docx，不超过 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadInstance } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type { MakeupApplyVO, MakeupCreateDTO, AttendanceTaskVO } from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const applications = ref<MakeupApplyVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const taskOptions = ref<AttendanceTaskVO[]>([])

async function loadApplications() {
  loading.value = true
  error.value = ''
  try {
    const res = await attendanceApi.queryMakeupPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    applications.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载补签申请失败'
  } finally {
    loading.value = false
  }
}

async function loadTaskOptions() {
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    taskOptions.value = res.records
  } catch {
    // 非核心
  }
}

// 申请
const applyVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | undefined>(undefined)
const applyForm = reactive<MakeupCreateDTO>({
  taskId: 0,
  recordId: undefined,
  applyReason: '',
  proofFilePath: undefined,
})

const applyRules: FormRules = {
  taskId: [{ required: true, message: '请选择签到任务', trigger: 'change' }],
  applyReason: [
    { required: true, message: '请填写补签原因', trigger: 'blur' },
    { min: 5, max: 500, message: '长度为5-500字符', trigger: 'blur' },
  ],
}

function openApply() {
  applyForm.taskId = 0
  applyForm.recordId = undefined
  applyForm.applyReason = ''
  applyForm.proofFilePath = undefined
  selectedFile.value = undefined
  uploadRef.value?.clearFiles()
  applyVisible.value = true
}

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = undefined
}

async function handleSubmit() {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let proofPath = applyForm.proofFilePath
      if (selectedFile.value) {
        proofPath = await attendanceApi.uploadMakeupProof(selectedFile.value)
      }
      await attendanceApi.applyMakeup({
        taskId: applyForm.taskId,
        recordId: applyForm.recordId,
        applyReason: applyForm.applyReason,
        proofFilePath: proofPath,
      })
      ElMessage.success('申请提交成功')
      applyVisible.value = false
      loadApplications()
    } catch (err: any) {
      ElMessage.error(err?.message || '申请提交失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadApplications()
  loadTaskOptions()
})
</script>

<style scoped lang="scss">
.makeup-my-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .proof-path {
    display: inline-block;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
    vertical-align: middle;
  }
}
</style>
