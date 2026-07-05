<template>
  <div class="topic-review-page">
    <page-header title="题目审核" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && topics.length === 0 && !error" description="暂无待审核题目" />

    <el-table v-loading="loading" :data="topics" border class="data-table">
      <el-table-column prop="topicName" label="题目名称" show-overflow-tooltip />
      <el-table-column prop="teacherName" label="出题教师" />
      <el-table-column prop="topicType" label="题目类型" width="120" />
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column label="审核状态" width="120">
        <template #default="scope">
          <status-tag category="topic" :value="(scope.row as TopicVO).status" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as TopicVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="viewDetail(scope.row as TopicVO)"
            >查看详情</el-button
          >
          <el-button type="success" text size="small" @click="openAudit(scope.row as TopicVO, 2)"
            >通过</el-button
          >
          <el-button type="warning" text size="small" @click="openAudit(scope.row as TopicVO, 3)"
            >退回</el-button
          >
          <el-button type="danger" text size="small" @click="openAudit(scope.row as TopicVO, 4)"
            >不通过</el-button
          >
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
        @size-change="loadTopics"
        @current-change="loadTopics"
      />
    </div>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核题目" width="520px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="题目名称">
          <el-input :model-value="currentRow?.topicName" disabled />
        </el-form-item>
        <el-form-item label="出题教师">
          <el-input :model-value="currentRow?.teacherName" disabled />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="auditResultType">{{ auditResultLabel }}</el-tag>
        </el-form-item>
        <el-form-item label="审核意见" required>
          <el-input
            v-model="auditForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请填写审核意见"
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as topicApi from '@/api/topic'
import type { TopicVO } from '@/types/topic'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const topics = ref<TopicVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const auditVisible = ref(false)
const submitting = ref(false)
const currentRow = ref<TopicVO | null>(null)
const auditForm = ref({
  reviewResult: 2,
  reviewComment: '',
})

const auditResultLabel = computed(() => {
  const map: Record<number, string> = {
    2: '审核通过',
    3: '退回修改',
    4: '不予通过',
  }
  return map[auditForm.value.reviewResult] || '审核'
})

const auditResultType = computed(() => {
  const map: Record<number, 'success' | 'warning' | 'danger'> = {
    2: 'success',
    3: 'warning',
    4: 'danger',
  }
  return map[auditForm.value.reviewResult] || 'info'
})

async function loadTopics() {
  loading.value = true
  error.value = ''
  try {
    const res = await topicApi.queryTopicPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: 1,
    })
    topics.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载题目失败'
  } finally {
    loading.value = false
  }
}

function viewDetail(row: TopicVO) {
  router.push(`/topic/${row.topicId}`)
}

function openAudit(row: TopicVO, result: number) {
  currentRow.value = row
  auditForm.value = { reviewResult: result, reviewComment: '' }
  auditVisible.value = true
}

async function handleAudit() {
  if (!currentRow.value) return
  if (!auditForm.value.reviewComment.trim()) {
    ElMessage.warning('请填写审核意见')
    return
  }
  try {
    await ElMessageBox.confirm(`确认${auditResultLabel.value}该题目？`, '审核确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    await topicApi.reviewTopic(currentRow.value.topicId, auditForm.value)
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadTopics()
  } catch (err: any) {
    // 全局拦截器已提示后端错误，此处兜底避免未处理的 Promise 拒绝
    ElMessage.error('审核题目失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(loadTopics)
</script>

<style scoped lang="scss">
.topic-review-page {
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
}
</style>
