<template>
  <div class="notice-page">
    <page-header title="公告管理">
      <template v-if="isAdminOrTeacher" #extra>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增公告</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && notices.length === 0 && !error" description="暂无公告" />

    <el-table v-loading="loading" :data="notices" border class="data-table">
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column label="置顶" width="90">
        <template #default="scope">
          <el-tag v-if="(scope.row as NoticeVO).topFlag === 1" type="danger" size="small"
            >置顶</el-tag
          >
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column prop="createTime" label="发布时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as NoticeVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="openDetail(scope.row as NoticeVO)"
            >查看</el-button
          >
          <template v-if="isAdminOrTeacher">
            <el-button type="primary" text size="small" @click="openEdit(scope.row as NoticeVO)"
              >编辑</el-button
            >
            <el-button
              type="warning"
              text
              size="small"
              :loading="topId === (scope.row as NoticeVO).noticeId"
              @click="handleToggleTop(scope.row as NoticeVO)"
            >
              {{ (scope.row as NoticeVO).topFlag === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(scope.row as NoticeVO)"
              >删除</el-button
            >
          </template>
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
        @size-change="loadNotices"
        @current-change="loadNotices"
      />
    </div>

    <!-- 公告详情 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="700px">
      <template v-if="currentNotice">
        <h3 class="notice-title">{{ currentNotice.title }}</h3>
        <div class="notice-meta">
          <span>类型：{{ currentNotice.type }}</span>
          <span>发布人：{{ currentNotice.publisherName || '-' }}</span>
          <span>发布时间：{{ formatDateTime(currentNotice.createTime) }}</span>
          <el-tag v-if="currentNotice.topFlag === 1" type="danger" size="small">置顶</el-tag>
        </div>
        <div class="notice-content">{{ currentNotice.content }}</div>
        <div v-if="currentNotice.attachPath" class="notice-attachment">
          <el-button type="primary" text @click="infoApi.downloadNoticeAttachment(currentNotice.noticeId)">
            下载附件
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增/编辑公告 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑公告' : '新增公告'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="通知" value="通知" />
            <el-option label="公告" value="公告" />
            <el-option label="重要" value="重要" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary" text>选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { NoticeVO, NoticeCreateDTO, NoticeUpdateDTO } from '@/types/info'
import { formatDateTime } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const isAdminOrTeacher = computed(() => auth.isAdmin || auth.isTeacher)

const loading = ref(false)
const error = ref('')
const notices = ref<NoticeVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

async function loadNotices() {
  loading.value = true
  error.value = ''
  try {
    const res = await infoApi.queryNoticePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    notices.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载公告失败'
  } finally {
    loading.value = false
  }
}

// 详情
const detailVisible = ref(false)
const currentNotice = ref<NoticeVO | null>(null)

function openDetail(row: NoticeVO) {
  currentNotice.value = row
  detailVisible.value = true
}

// 表单
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | undefined>(undefined)
const submitting = ref(false)

const form = ref<NoticeCreateDTO>({
  title: '',
  content: '',
  type: '',
  topFlag: 0,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

function resetForm() {
  form.value = { title: '', content: '', type: '', topFlag: 0 }
  selectedFile.value = undefined
  uploadRef.value?.clearFiles()
}

function openCreate() {
  isEdit.value = false
  currentId.value = 0
  resetForm()
  formVisible.value = true
}

function openEdit(row: NoticeVO) {
  isEdit.value = true
  currentId.value = row.noticeId
  form.value = {
    title: row.title,
    content: row.content,
    type: row.type,
    topFlag: row.topFlag,
  }
  selectedFile.value = undefined
  uploadRef.value?.clearFiles()
  formVisible.value = true
}

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = undefined
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        const dto: NoticeUpdateDTO = { ...form.value }
        await infoApi.updateNotice(currentId.value, dto, selectedFile.value)
      } else {
        await infoApi.createNotice(form.value, selectedFile.value)
      }
      ElMessage.success('保存成功')
      formVisible.value = false
      loadNotices()
    } catch (err: any) {
      ElMessage.error(err?.message || '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除
async function handleDelete(row: NoticeVO) {
  try {
    await ElMessageBox.confirm('确认删除该公告？删除后不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await infoApi.deleteNotice(row.noticeId)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

// 置顶
const topId = ref(0)
async function handleToggleTop(row: NoticeVO) {
  try {
    await ElMessageBox.confirm(
      row.topFlag === 1 ? '确认取消置顶该公告？' : '确认置顶该公告？',
      '操作确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  topId.value = row.noticeId
  try {
    await infoApi.toggleNoticeTop(row.noticeId)
    ElMessage.success('操作成功')
    loadNotices()
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    topId.value = 0
  }
}

onMounted(loadNotices)
</script>

<style scoped lang="scss">
.notice-page {
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

  .notice-title {
    margin: 0 0 12px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .notice-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #606266;
  }

  .notice-content {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .notice-attachment {
    margin-top: 16px;
  }
}
</style>
