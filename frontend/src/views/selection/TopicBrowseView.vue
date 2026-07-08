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
        :key="topic.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="8"
        class="mb-4"
      >
        <el-card shadow="hover" class="topic-card">
          <template #header>
            <div class="card-header">
              <span class="title" :title="topic.title">{{ topic.title }}</span>
              <status-tag category="topicOpen" :value="topic.status" />
            </div>
          </template>
          <div class="topic-meta">
            <p><strong>指导教师ID：</strong>{{ topic.teacherId }}</p>
            <p><strong>方向：</strong>{{ topic.direction || '-' }}</p>
            <p><strong>难度：</strong>{{ topic.difficulty }}</p>
            <p>
              <strong>人数要求：</strong>{{ topic.minMembers || '-' }} -
              {{ topic.maxMembers || '-' }}
            </p>
          </div>
          <template #footer>
            <div class="card-footer">
              <el-button type="primary" text @click="openDetail(topic)">查看详情</el-button>
              <el-button
                type="primary"
                :disabled="topic.status !== 'OPEN'"
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
    <el-dialog v-model="detailVisible" v-loading="detailLoading" title="课题详情" width="700px">
      <template v-if="currentTopic">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课题名称" :span="2">{{
            currentTopic.title
          }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{ currentTopic.teacherId }}</el-descriptions-item>
          <el-descriptions-item label="方向">{{
            currentTopic.direction || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ currentTopic.difficulty }}</el-descriptions-item>
          <el-descriptions-item label="限选人数"
            >{{ currentTopic.minMembers || '-' }} -
            {{ currentTopic.maxMembers || '-' }}</el-descriptions-item
          >
          <el-descriptions-item label="开放状态">
            <status-tag category="topicOpen" :value="currentTopic.status" />
          </el-descriptions-item>
          <el-descriptions-item label="选题开始">{{
            formatDateTime(currentTopic.selectionStart)
          }}</el-descriptions-item>
          <el-descriptions-item label="选题结束">{{
            formatDateTime(currentTopic.selectionEnd)
          }}</el-descriptions-item>
        </el-descriptions>
        <h4 class="section-title">课题内容</h4>
        <div class="content-block">{{ currentTopic.description || '-' }}</div>
      </template>
    </el-dialog>

    <!-- 申请选题弹窗 -->
    <el-dialog v-model="applyVisible" title="提交选题申请" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="课题名称">
          <el-input :model-value="currentTopic?.title" disabled />
        </el-form-item>
        <el-form-item label="申请团队" prop="teamId">
          <el-select
            v-model="applyForm.teamId"
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
import type { SelectableTopicVO, TeamVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const keyword = ref('')
const topics = ref<SelectableTopicVO[]>([])

// 我的团队（用于申请时选择）
const myTeams = ref<TeamVO[]>([])
const myTeamsLoading = ref(false)

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
const detailLoading = ref(false)
const currentTopic = ref<SelectableTopicVO | null>(null)

async function openDetail(topic: SelectableTopicVO) {
  detailVisible.value = true
  detailLoading.value = true
  currentTopic.value = null
  try {
    currentTopic.value = await selectionApi.getSelectableTopic(topic.id)
  } catch (err: any) {
    ElMessage.error(err?.message || '加载课题详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 申请
const applyVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref<FormInstance>()
const applyForm = ref({
  topicId: 0,
  teamId: 0,
  selectionReason: '',
})

const applyRules: FormRules = {
  teamId: [{ required: true, message: '请选择申请团队', trigger: 'change' }],
  selectionReason: [{ required: true, message: '请输入选题说明', trigger: 'blur' }],
}

async function openApply(topic: SelectableTopicVO) {
  currentTopic.value = topic
  applyForm.value = { topicId: topic.id, teamId: 0, selectionReason: '' }
  applyVisible.value = true
  // 加载用户团队列表
  myTeamsLoading.value = true
  try {
    myTeams.value = await selectionApi.getMyTeams()
  } catch {
    myTeams.value = []
  } finally {
    myTeamsLoading.value = false
  }
}

async function handleApply() {
  if (!applyFormRef.value) return
  try {
    await applyFormRef.value.validate()
  } catch {
    return
  }
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
