<template>
  <div class="my-documents-page">
    <page-header title="我的过程文档">
      <template #extra>
        <el-button type="primary" @click="openUpload">上传文档</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 阶段任务（进行中） -->
    <el-card v-if="activeStageTasks.length > 0" class="stage-task-section mb-4">
      <template #header>
        <span>📋 阶段任务</span>
        <el-tag type="success" size="small" style="margin-left: 8px">{{ activeStageTasks.length }}个进行中</el-tag>
      </template>
      <div v-for="task in activeStageTasks" :key="task.stageId" class="stage-task-item">
        <div class="task-header">
          <span class="task-name">{{ task.stageName }}</span>
          <el-tag v-if="isTaskSubmitted(task.stageId)" type="info" size="small">已提交</el-tag>
          <el-tag v-else type="warning" size="small">待提交</el-tag>
        </div>
        <div class="task-body">
          <div class="task-meta">
            <span>📅 截止：{{ formatDateTime(task.endTime) }}</span>
            <span>📦 交付物：{{ task.deliverables }}</span>
            <span>⚖️ 权重：{{ task.weight }}%</span>
          </div>
          <div class="task-desc">{{ task.stageDesc }}</div>
        </div>
        <el-button
          type="primary"
          size="small"
          @click="openStageSubmit(task)"
        >
          {{ isTaskSubmitted(task.stageId) ? '再次提交' : '提交' }}
        </el-button>
      </div>
    </el-card>

    <el-empty v-if="!loading && documents.length === 0 && !error" description="暂无过程文档" />

    <el-table v-loading="loading" :data="documents" border class="data-table">
      <el-table-column prop="documentName" label="文档名称" show-overflow-tooltip />
      <el-table-column prop="documentType" label="文档类型" />
      <el-table-column prop="projectStage" label="项目阶段" />
      <el-table-column label="审核状态" width="120">
        <template #default="scope">
          <status-tag category="document" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="teacherFeedback" label="反馈意见" show-overflow-tooltip />
      <el-table-column prop="originalFilename" label="文件名" show-overflow-tooltip />
      <el-table-column prop="uploadTime" label="上传时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <span class="action-btns">
            <el-button type="primary" link size="small" @click="handleDownload(scope.row as ProcessDocumentVO)">下载</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传文档弹窗 -->
    <el-dialog v-model="uploadVisible" :title="stageSubmitTask ? '提交阶段任务' : '上传过程文档'" width="520px">
      <div v-if="stageSubmitTask" class="stage-submit-hint mb-4">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            正在提交：<strong>{{ stageSubmitTask.stageName }}</strong>
            &nbsp;|&nbsp;截止时间：{{ formatDateTime(stageSubmitTask.endTime) }}
          </template>
        </el-alert>
      </div>
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="所属团队" prop="teamId">
          <el-select
            v-model="uploadForm.teamId"
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
        <el-form-item label="文档名称" prop="documentName">
          <el-input v-model="uploadForm.documentName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="文档类型" prop="documentType">
          <el-select
            v-model="uploadForm.documentType"
            placeholder="请选择文档类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in documentTypeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目阶段" prop="projectStage">
          <el-select
            v-model="uploadForm.projectStage"
            placeholder="请选择项目阶段"
            style="width: 100%"
            :disabled="!!stageSubmitTask"
          >
            <el-option
              v-for="item in projectStageOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="uploadForm.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="选填，提交说明"
          />
        </el-form-item>
        <el-form-item label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            action=""
            accept=".doc,.docx,.pdf,.xls,.xlsx,.zip,.rar"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as selectionApi from '@/api/selection'
import * as scoreApi from '@/api/score'
import type { ProcessDocumentVO, TeamVO } from '@/types/selection'
import type { StageTaskVO } from '@/types/score'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const loading = ref(false)
const error = ref('')
const documents = ref<ProcessDocumentVO[]>([])

// 我的团队（用于上传时选择）
const myTeams = ref<TeamVO[]>([])
const myTeamsLoading = ref(false)

// 阶段任务
const activeStageTasks = ref<StageTaskVO[]>([])
const stageSubmitTask = ref<StageTaskVO | null>(null)
const submittedStageIds = computed(() => {
  return new Set(documents.value.filter((d) => d.stageId != null).map((d) => d.stageId!))
})

function isTaskSubmitted(stageId: number): boolean {
  return submittedStageIds.value.has(stageId)
}

async function loadStageTasks() {
  try {
    const res = await scoreApi.queryStageTaskPage({ pageNum: 1, pageSize: 50 })
    // 只显示进行中（status=1）的阶段任务
    activeStageTasks.value = res.records.filter((t) => t.status === 1)
  } catch {
    activeStageTasks.value = []
  }
}

function openStageSubmit(task: StageTaskVO) {
  stageSubmitTask.value = task
  uploadForm.value = {
    teamId: 0,
    documentName: task.stageName,
    documentType: '设计文档',
    projectStage: task.stageName,
    file: null,
    remark: '',
  }
  uploadVisible.value = true
  myTeamsLoading.value = true
  // 加载团队列表
  selectionApi.getMyTeams().then((teams) => {
    myTeams.value = teams
    if (teams.length === 1) {
      uploadForm.value.teamId = teams[0].id
    }
  }).catch(() => {
    myTeams.value = []
  }).finally(() => {
    myTeamsLoading.value = false
  })
  nextTick(() => {
    uploadRef.value?.clearFiles?.()
    uploadFormRef.value?.clearValidate()
  })
}

const documentTypeOptions = ['需求文档', '设计文档', '代码文档', '测试文档', '用户手册', '会议纪要']
const projectStageOptions = ['需求分析', '概要设计', '详细设计', '编码实现', '系统测试', '项目验收']
const allowedExtensions = ['doc', 'docx', 'pdf', 'xls', 'xlsx', 'zip', 'rar']

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

// 上传
const uploadVisible = ref(false)
const submitting = ref(false)
const uploadFormRef = ref<FormInstance>()
const uploadRef = ref()
const uploadForm = ref({
  teamId: 0,
  documentName: '',
  documentType: '',
  projectStage: '',
  file: null as File | null,
  remark: '',
})

const uploadRules: FormRules = {
  teamId: [{ required: true, message: '请选择所属团队', trigger: 'change' }],
  documentName: [{ required: true, message: '请输入文档名称', trigger: 'blur' }],
  documentType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
  projectStage: [{ required: true, message: '请选择项目阶段', trigger: 'change' }],
  file: [{ required: true, message: '请选择文件', trigger: 'change' }],
}

async function openUpload() {
  stageSubmitTask.value = null
  uploadForm.value = {
    teamId: 0,
    documentName: '',
    documentType: '',
    projectStage: '',
    file: null,
    remark: '',
  }
  uploadVisible.value = true
  // 加载团队列表
  myTeamsLoading.value = true
  try {
    myTeams.value = await selectionApi.getMyTeams()
    if (myTeams.value.length === 1) {
      uploadForm.value.teamId = myTeams.value[0].id
    }
  } catch {
    myTeams.value = []
  } finally {
    myTeamsLoading.value = false
  }
  nextTick(() => {
    uploadRef.value?.clearFiles?.()
    uploadFormRef.value?.clearValidate()
  })
}

function handleFileChange(uploadFile: UploadFile) {
  const file = uploadFile.raw || null
  const extension = file?.name.split('.').pop()?.toLowerCase()
  if (!file || !extension || !allowedExtensions.includes(extension)) {
    uploadForm.value.file = null
    uploadRef.value?.clearFiles?.()
    ElMessage.warning('仅支持 doc、docx、pdf、xls、xlsx、zip、rar 格式文件')
    return
  }
  uploadForm.value.file = file
}

function handleFileRemove() {
  uploadForm.value.file = null
}

async function handleUpload() {
  if (!uploadFormRef.value) return
  try {
    await uploadFormRef.value.validate()
  } catch {
    return
  }
  if (!uploadForm.value.file) {
    ElMessage.warning('请选择文件')
    return
  }
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('teamId', String(uploadForm.value.teamId))
    formData.append('documentName', uploadForm.value.documentName)
    formData.append('documentType', uploadForm.value.documentType)
    formData.append('projectStage', uploadForm.value.projectStage)
    formData.append('file', uploadForm.value.file)
    if (stageSubmitTask.value) {
      formData.append('stageId', String(stageSubmitTask.value.stageId))
    }
    if (uploadForm.value.remark) {
      formData.append('remark', uploadForm.value.remark)
    }
    await selectionApi.uploadDocument(formData)
    ElMessage.success('上传成功')
    uploadVisible.value = false
    loadDocuments()
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDocuments()
  loadStageTasks()
})
</script>

<style scoped lang="scss">
.my-documents-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .stage-task-section {
    .stage-task-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      .task-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .task-name {
          font-weight: 600;
          font-size: 15px;
          color: #303133;
        }
      }

      .task-body {
        flex: 1;
        margin-right: 16px;

        .task-meta {
          display: flex;
          gap: 16px;
          font-size: 13px;
          color: #909399;
          margin-bottom: 4px;
        }

        .task-desc {
          font-size: 13px;
          color: #606266;
        }
      }
    }
  }

  .stage-submit-hint {
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
