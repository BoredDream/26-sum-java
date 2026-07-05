<template>
  <div class="my-documents-page">
    <page-header title="我的过程文档">
      <template #extra>
        <el-button type="primary" @click="openUpload">上传文档</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && documents.length === 0 && !error" description="暂无过程文档" />

    <el-table v-loading="loading" :data="documents" border class="data-table">
      <el-table-column prop="documentName" label="文档名称" show-overflow-tooltip />
      <el-table-column prop="documentType" label="文档类型" />
      <el-table-column prop="projectStage" label="项目阶段" />
      <el-table-column prop="versionNo" label="版本号" />
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
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            text
            size="small"
            @click="handleDownload(scope.row as ProcessDocumentVO)"
            >下载</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传文档弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传过程文档" width="520px">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
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
          >
            <el-option
              v-for="item in projectStageOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="uploadForm.versionNo" placeholder="仅展示，上传时不提交" disabled />
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
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as selectionApi from '@/api/selection'
import type { ProcessDocumentVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const loading = ref(false)
const error = ref('')
const documents = ref<ProcessDocumentVO[]>([])

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
  documentName: '',
  documentType: '',
  projectStage: '',
  versionNo: 'v1.0',
  file: null as File | null,
})

const uploadRules: FormRules = {
  documentName: [{ required: true, message: '请输入文档名称', trigger: 'blur' }],
  documentType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
  projectStage: [{ required: true, message: '请选择项目阶段', trigger: 'change' }],
  file: [{ required: true, message: '请选择文件', trigger: 'change' }],
}

function openUpload() {
  uploadForm.value = {
    documentName: '',
    documentType: '',
    projectStage: '',
    versionNo: 'v1.0',
    file: null,
  }
  uploadVisible.value = true
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
  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!uploadForm.value.file) {
      ElMessage.warning('请选择文件')
      return
    }
    submitting.value = true
    try {
      const formData = new FormData()
      formData.append('documentName', uploadForm.value.documentName)
      formData.append('documentType', uploadForm.value.documentType)
      formData.append('projectStage', uploadForm.value.projectStage)
      formData.append('file', uploadForm.value.file)
      await selectionApi.uploadDocument(formData)
      ElMessage.success('上传成功')
      uploadVisible.value = false
      loadDocuments()
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadDocuments)
</script>

<style scoped lang="scss">
.my-documents-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
