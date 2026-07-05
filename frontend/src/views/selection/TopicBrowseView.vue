<template>
  <div class="topic-browse-page">
    <page-header title="可选课题">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索课题名称/内容"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && topics.length === 0 && !error" description="暂无可选课题" />

    <el-row v-loading="loading" :gutter="16">
      <el-col
        v-for="topic in topics"
        :key="topic.topicId"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="8"
        class="mb-4"
      >
        <el-card shadow="hover" class="topic-card">
          <template #header>
            <div class="card-header">
              <span class="title" :title="topic.topicName">{{ topic.topicName }}</span>
              <status-tag category="topicOpen" :value="topic.openStatus" />
            </div>
          </template>
          <div class="topic-meta">
            <p><strong>指导教师：</strong>{{ topic.teacherName }}</p>
            <p><strong>题目类型：</strong>{{ topic.topicType }}</p>
            <p><strong>难度：</strong>{{ topic.difficulty }}</p>
            <p><strong>限选人数：</strong>{{ topic.studentLimit }}</p>
          </div>
          <template #footer>
            <div class="card-footer">
              <el-button type="primary" text @click="openDetail(topic)">查看详情</el-button>
              <el-button
                type="primary"
                :disabled="topic.openStatus !== 1"
                @click="openApply(topic)"
              >
                申请选题
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课题详情弹窗 -->
    <el-dialog v-model="detailVisible" title="课题详情" width="700px">
      <template v-if="currentTopic">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课题名称" :span="2">{{
            currentTopic.topicName
          }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{
            currentTopic.teacherName
          }}</el-descriptions-item>
          <el-descriptions-item label="题目类型">{{ currentTopic.topicType }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ currentTopic.difficulty }}</el-descriptions-item>
          <el-descriptions-item label="限选人数">{{
            currentTopic.studentLimit
          }}</el-descriptions-item>
          <el-descriptions-item label="开放状态">
            <status-tag category="topicOpen" :value="currentTopic.openStatus" />
          </el-descriptions-item>
          <el-descriptions-item label="选题开始">{{
            formatDateTime(currentTopic.selectionStartTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="选题结束">{{
            formatDateTime(currentTopic.selectionEndTime)
          }}</el-descriptions-item>
        </el-descriptions>
        <h4 class="section-title">课题内容</h4>
        <div class="content-block">{{ currentTopic.topicContent }}</div>
        <h4 class="section-title">技术路线</h4>
        <div class="content-block">{{ currentTopic.technicalRoute }}</div>
        <h4 v-if="currentTopic.developTools" class="section-title">开发工具</h4>
        <div v-if="currentTopic.developTools" class="content-block">
          {{ currentTopic.developTools }}
        </div>
      </template>
    </el-dialog>

    <!-- 申请选题弹窗 -->
    <el-dialog v-model="applyVisible" title="提交选题申请" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="课题名称">
          <el-input :model-value="currentTopic?.topicName" disabled />
        </el-form-item>
        <el-form-item label="选题说明" prop="selectionReason">
          <el-input
            v-model="applyForm.selectionReason"
            type="textarea"
            :rows="4"
            placeholder="请填写选题说明"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as selectionApi from '@/api/selection'
import type { TopicVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const keyword = ref('')
const topics = ref<TopicVO[]>([])

async function loadTopics() {
  loading.value = true
  error.value = ''
  try {
    topics.value = await selectionApi.listSelectableTopics(keyword.value || undefined)
  } catch (err: any) {
    error.value = err?.message || '加载课题失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadTopics()
}

// 详情
const detailVisible = ref(false)
const currentTopic = ref<TopicVO | null>(null)

function openDetail(topic: TopicVO) {
  currentTopic.value = topic
  detailVisible.value = true
}

// 申请
const applyVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref<FormInstance>()
const applyForm = ref({
  topicId: 0,
  selectionReason: '',
})

const applyRules: FormRules = {
  selectionReason: [{ required: true, message: '请输入选题说明', trigger: 'blur' }],
}

function openApply(topic: TopicVO) {
  currentTopic.value = topic
  applyForm.value = { topicId: topic.topicId, selectionReason: '' }
  applyVisible.value = true
}

async function handleApply() {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await ElMessageBox.confirm('确认提交该选题申请？', '提交确认', { type: 'warning' })
    } catch {
      return
    }
    submitting.value = true
    try {
      await selectionApi.submitSelection(applyForm.value)
      ElMessage.success('申请已提交')
      applyVisible.value = false
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadTopics)
</script>

<style scoped lang="scss">
.topic-browse-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .topic-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;

      .title {
        font-size: 16px;
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .topic-meta {
      p {
        margin: 8px 0;
        color: #606266;
      }
    }

    .card-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }

  .section-title {
    margin: 20px 0 12px;
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
}
</style>
