<template>
  <div class="topic-manage-page">
    <page-header title="题库管理">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索题目名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filterStatus"
          placeholder="审核状态"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option label="草稿" :value="0" />
          <el-option label="待审核" :value="1" />
          <el-option label="通过" :value="2" />
          <el-option label="退回" :value="3" />
          <el-option label="不通过" :value="4" />
        </el-select>
        <el-select
          v-model="filterOpenStatus"
          placeholder="开放状态"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option label="未开放" :value="0" />
          <el-option label="已开放" :value="1" />
          <el-option label="已关闭" :value="2" />
        </el-select>
        <el-button :icon="Search" @click="handleSearch" />
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && topics.length === 0 && !error" description="暂无题目" />

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
      <el-table-column label="开放状态" width="120">
        <template #default="scope">
          <status-tag category="topicOpen" :value="(scope.row as TopicVO).openStatus" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as TopicVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="viewDetail(scope.row as TopicVO)"
            >查看详情</el-button
          >
          <el-button
            v-if="(scope.row as TopicVO).openStatus !== 1"
            type="success"
            text
            size="small"
            :loading="actionId === (scope.row as TopicVO).topicId"
            @click="handleOpen(scope.row as TopicVO)"
          >
            开放
          </el-button>
          <el-button
            v-if="(scope.row as TopicVO).openStatus === 1"
            type="danger"
            text
            size="small"
            :loading="actionId === (scope.row as TopicVO).topicId"
            @click="handleClose(scope.row as TopicVO)"
          >
            关闭
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
import { Search } from '@element-plus/icons-vue'
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
const keyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const filterOpenStatus = ref<number | undefined>(undefined)
const actionId = ref(0)

async function loadTopics() {
  loading.value = true
  error.value = ''
  try {
    const res = await topicApi.queryTopicPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: filterStatus.value,
      openStatus: filterOpenStatus.value,
    })
    topics.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载题目失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadTopics()
}

function viewDetail(row: TopicVO) {
  router.push(`/topic/${row.topicId}`)
}

async function handleOpen(row: TopicVO) {
  try {
    await ElMessageBox.confirm('确认开放该题目？', '操作确认', { type: 'warning' })
  } catch {
    return
  }
  actionId.value = row.topicId
  try {
    await topicApi.openTopic(row.topicId)
    ElMessage.success('题目已开放')
    loadTopics()
  } finally {
    actionId.value = 0
  }
}

async function handleClose(row: TopicVO) {
  try {
    await ElMessageBox.confirm('确认关闭该题目？', '操作确认', { type: 'warning' })
  } catch {
    return
  }
  actionId.value = row.topicId
  try {
    await topicApi.closeTopic(row.topicId)
    ElMessage.success('题目已关闭')
    loadTopics()
  } finally {
    actionId.value = 0
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
.topic-manage-page {
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
