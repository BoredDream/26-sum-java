<template>
  <div class="topic-list-page">
    <page-header title="我的题目">
      <template #extra>
        <el-button type="primary" @click="$router.push('/topic/create')">新增题目</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && topics.length === 0 && !error" description="暂无题目" />

    <el-table v-loading="loading" :data="topics" border class="data-table">
      <el-table-column prop="topicName" label="题目名称" show-overflow-tooltip />
      <el-table-column prop="topicType" label="题目类型" width="120" />
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column label="审核状态" width="120">
        <template #default="scope">
          <status-tag category="topic" :value="(scope.row as TopicVO).status" />
        </template>
      </el-table-column>
      <el-table-column label="开放状态" width="120">
        <template #default="scope">
          <status-tag category="topicOpen" :value="(scope.row as TopicVO).openStatus" />
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as TopicVO).updateTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="handleEdit(scope.row as TopicVO)"
            >编辑</el-button
          >
          <el-button type="primary" text size="small" @click="handleFiles(scope.row as TopicVO)"
            >资料</el-button
          >
          <el-button
            v-if="(scope.row as TopicVO).status === 0 || (scope.row as TopicVO).status === 3"
            type="success"
            text
            size="small"
            :loading="submittingId === (scope.row as TopicVO).topicId"
            @click="handleSubmit(scope.row as TopicVO)"
          >
            提交审核
          </el-button>
          <el-button type="danger" text size="small" @click="handleDelete(scope.row as TopicVO)"
            >删除</el-button
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
const submittingId = ref(0)

async function loadTopics() {
  loading.value = true
  error.value = ''
  try {
    const res = await topicApi.queryTopicPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    topics.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载题目失败'
  } finally {
    loading.value = false
  }
}

function handleEdit(row: TopicVO) {
  router.push(`/topic/edit/${row.topicId}`)
}

function handleFiles(row: TopicVO) {
  router.push(`/topic/${row.topicId}/files`)
}

async function handleSubmit(row: TopicVO) {
  try {
    await ElMessageBox.confirm('确认提交该题目进行审核？', '提交确认', { type: 'warning' })
  } catch {
    return
  }
  submittingId.value = row.topicId
  try {
    await topicApi.submitForReview(row.topicId)
    ElMessage.success('已提交审核')
    loadTopics()
  } catch {
    // 全局请求拦截器已显示错误提示，这里只需阻止未处理的 Promise 拒绝
  } finally {
    submittingId.value = 0
  }
}

async function handleDelete(row: TopicVO) {
  try {
    await ElMessageBox.confirm('确认删除该题目？删除后不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await topicApi.deleteTopic(row.topicId)
    ElMessage.success('删除成功')
    loadTopics()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(loadTopics)
</script>

<style scoped lang="scss">
.topic-list-page {
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
