<template>
  <div class="topic-detail-page">
    <page-header title="题目详情">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
        <template v-if="auth.isAdmin && topic">
          <el-button
            v-if="topic.openStatus !== 1"
            type="success"
            :loading="actionLoading"
            @click="handleOpen(topic.topicId)"
          >
            开放题目
          </el-button>
          <el-button
            v-if="topic.openStatus === 1"
            type="danger"
            :loading="actionLoading"
            @click="handleClose(topic.topicId)"
          >
            关闭题目
          </el-button>
        </template>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-skeleton v-if="loading" :rows="10" animated />

    <template v-if="!loading && topic">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目名称" :span="2">{{
          topic.topicName
        }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ topic.teacherName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="题目类型">{{ topic.topicType }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ topic.difficulty }}</el-descriptions-item>
        <el-descriptions-item label="限选人数">{{ topic.studentLimit }}</el-descriptions-item>
        <el-descriptions-item label="团队上限">{{ topic.teamLimit ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <status-tag category="topic" :value="topic.status" />
        </el-descriptions-item>
        <el-descriptions-item label="开放状态">
          <status-tag category="topicOpen" :value="topic.openStatus" />
        </el-descriptions-item>
        <el-descriptions-item label="选题开始">{{
          formatDateTime(topic.selectionStartTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="选题结束">{{
          formatDateTime(topic.selectionEndTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          formatDateTime(topic.createTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
          formatDateTime(topic.updateTime)
        }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="section-title">题目内容</h4>
      <div class="content-block">{{ topic.topicContent }}</div>

      <h4 class="section-title">技术路线</h4>
      <div class="content-block">{{ topic.technicalRoute }}</div>

      <template v-if="topic.developTools">
        <h4 class="section-title">开发工具</h4>
        <div class="content-block">{{ topic.developTools }}</div>
      </template>

      <h4 class="section-title">资料文件</h4>
      <el-empty v-if="files.length === 0" description="暂无资料文件" />
      <el-table v-else :data="files" border class="data-table">
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="fileSizeText" label="大小" width="120" />
        <el-table-column prop="uploadUserName" label="上传人" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              text
              size="small"
              @click="handleDownload(scope.row as TopicFileVO)"
              >下载</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <h4 class="section-title">审核记录</h4>
      <el-empty v-if="reviews.length === 0" description="暂无审核记录" />
      <el-timeline v-else class="review-timeline">
        <el-timeline-item
          v-for="review in reviews"
          :key="review.reviewId"
          :timestamp="formatDateTime(review.reviewTime)"
        >
          <p>
            <strong>{{ review.adminName || '管理员' }}：</strong>
            <status-tag category="topic" :value="review.reviewResult" />
          </p>
          <p v-if="review.reviewComment" class="review-comment">{{ review.reviewComment }}</p>
        </el-timeline-item>
      </el-timeline>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import * as topicApi from '@/api/topic'
import type { TopicDetailVO, TopicFileVO, TopicReviewVO } from '@/types/topic'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const error = ref('')
const topic = ref<TopicDetailVO | null>(null)
const files = ref<TopicFileVO[]>([])
const reviews = ref<TopicReviewVO[]>([])
const actionLoading = ref(false)

async function loadData() {
  const topicId = Number(route.params.topicId)
  if (!topicId) {
    ElMessage.error('题目 ID 无效')
    router.push('/topic/browse')
    return
  }
  loading.value = true
  error.value = ''
  try {
    const [detail, fileList, reviewList] = await Promise.all([
      topicApi.getTopicDetail(topicId),
      topicApi.listTopicFiles(topicId),
      topicApi.listTopicReviews(topicId),
    ])
    topic.value = detail
    files.value = fileList
    reviews.value = reviewList
  } catch (err: any) {
    error.value = err?.message || '加载题目详情失败'
  } finally {
    loading.value = false
  }
}

function handleDownload(row: TopicFileVO) {
  const url = topicApi.getTopicFileDownloadUrl(row.fileId)
  downloadByUrl(url, {}, row.fileName)
}

async function handleOpen(topicId: number) {
  try {
    await ElMessageBox.confirm('确认开放该题目？开放后学生可申请选题。', '操作确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  actionLoading.value = true
  try {
    await topicApi.openTopic(topicId)
    ElMessage.success('题目已开放')
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '开放题目失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleClose(topicId: number) {
  try {
    await ElMessageBox.confirm('确认关闭该题目？关闭后学生将无法继续申请。', '操作确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  actionLoading.value = true
  try {
    await topicApi.closeTopic(topicId)
    ElMessage.success('题目已关闭')
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '关闭题目失败')
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.topic-detail-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .section-title {
    margin: 24px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .content-block {
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  .data-table {
    margin-top: 8px;
  }

  .review-timeline {
    margin-top: 8px;

    .review-comment {
      margin: 8px 0 0;
      color: #606266;
      white-space: pre-wrap;
    }
  }
}
</style>
