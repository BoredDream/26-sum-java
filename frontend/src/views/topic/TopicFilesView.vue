<template>
  <div class="topic-files-page">
    <page-header title="题目资料管理">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-card class="upload-card">
      <template #header>上传新资料</template>
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
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
        <el-form-item label="文件说明">
          <el-input
            v-model="uploadForm.fileDesc"
            placeholder="请输入文件说明（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="!uploadForm.file"
            @click="handleUpload"
            >上传</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="loading">
      <template #header>资料列表</template>
      <el-empty v-if="!loading && files.length === 0 && !error" description="暂无资料文件" />
      <el-table v-if="files.length > 0" :data="files" border class="data-table">
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="fileSizeText" label="大小" width="120" />
        <el-table-column prop="fileDesc" label="说明" show-overflow-tooltip />
        <el-table-column prop="uploadUserName" label="上传人" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="scope">{{
            formatDateTime((scope.row as TopicFileVO).uploadTime)
          }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              text
              size="small"
              @click="handleDownload(scope.row as TopicFileVO)"
              >下载</el-button
            >
            <el-button
              type="danger"
              text
              size="small"
              @click="handleDelete(scope.row as TopicFileVO)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadInstance, UploadFile } from 'element-plus'
import * as topicApi from '@/api/topic'
import type { TopicFileVO } from '@/types/topic'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const route = useRoute()
const topicId = Number(route.params.topicId)

const loading = ref(false)
const error = ref('')
const files = ref<TopicFileVO[]>([])
const uploading = ref(false)
const uploadRef = ref<UploadInstance>()
const uploadForm = reactive({
  file: undefined as File | undefined,
  fileDesc: '',
})

async function loadFiles() {
  if (!topicId) return
  loading.value = true
  error.value = ''
  try {
    files.value = await topicApi.listTopicFiles(topicId)
  } catch (err: any) {
    error.value = err?.message || '加载资料失败'
  } finally {
    loading.value = false
  }
}

function handleFileChange(uploadFile: UploadFile) {
  if (uploadFile.raw) {
    uploadForm.file = uploadFile.raw
  }
}

function handleFileRemove() {
  uploadForm.file = undefined
}

async function handleUpload() {
  if (!uploadForm.file) {
    ElMessage.warning('请选择文件')
    return
  }
  const formData = new FormData()
  formData.append('file', uploadForm.file)
  if (uploadForm.fileDesc) {
    formData.append('fileDesc', uploadForm.fileDesc)
  }
  uploading.value = true
  try {
    await topicApi.uploadTopicFile(topicId, formData)
    ElMessage.success('上传成功')
    uploadForm.file = undefined
    uploadForm.fileDesc = ''
    uploadRef.value?.clearFiles()
    loadFiles()
  } catch (err: any) {
    ElMessage.error(err?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function handleDownload(row: TopicFileVO) {
  const url = topicApi.getTopicFileDownloadUrl(row.fileId)
  downloadByUrl(url, {}, row.fileName)
}

async function handleDelete(row: TopicFileVO) {
  try {
    await ElMessageBox.confirm('确认删除该资料文件？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await topicApi.deleteTopicFile(row.fileId)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(loadFiles)
</script>

<style scoped lang="scss">
.topic-files-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .upload-card {
    margin-bottom: 20px;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
