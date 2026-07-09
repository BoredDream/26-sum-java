<template>
  <div class="topic-browse-page">
    <page-header title="题目浏览">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索题目名称/内容"
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

    <el-empty v-if="!loading && topics.length === 0 && !error" description="暂无可选题目" />

    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="topic in topics" :key="topic.topicId" :xs="24" :sm="12" :md="8" class="mb-4">
        <el-card shadow="hover" class="topic-card">
          <template #header>
            <div class="card-header">
              <span class="title" :title="topic.topicName">{{ topic.topicName }}</span>
              <status-tag category="topicOpen" :value="topic.openStatus" />
            </div>
          </template>
          <div class="topic-meta">
            <p><strong>指导教师：</strong>{{ topic.teacherName || '-' }}</p>
            <p><strong>题目类型：</strong>{{ topic.topicType }}</p>
            <p><strong>难度：</strong>{{ topic.difficulty }}</p>
            <p><strong>限选人数：</strong>{{ topic.studentLimit }}</p>
          </div>
          <template #footer>
            <div class="card-footer">
              <el-button type="primary" text @click="openDetail(topic)">查看详情</el-button>
              <el-button
                v-if="auth.isStudent"
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

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[9, 12, 24]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadTopics"
        @current-change="loadTopics"
      />
    </div>

    <!-- 题目详情弹窗 -->
    <el-dialog v-model="detailVisible" v-loading="detailLoading" title="题目详情" width="720px">
      <template v-if="currentTopic">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题目名称" :span="2">{{
            currentTopic.topicName
          }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{
            currentTopic.teacherName || '-'
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
        <h4 class="section-title">题目内容</h4>
        <div class="content-block">{{ currentTopic.topicContent }}</div>
        <h4 class="section-title">技术路线</h4>
        <div class="content-block">{{ currentTopic.technicalRoute }}</div>
        <h4 v-if="currentTopic.developTools" class="section-title">开发工具</h4>
        <div v-if="currentTopic.developTools" class="content-block">
          {{ currentTopic.developTools }}
        </div>

        <h4 class="section-title">资料文件</h4>
        <el-empty
          v-if="!currentTopic.files || currentTopic.files.length === 0"
          description="暂无资料文件"
        />
        <el-table v-else :data="currentTopic.files" border class="data-table">
          <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
          <el-table-column prop="fileSizeText" label="大小" width="120" />
          <el-table-column prop="uploadUserName" label="上传人" width="120" />
          <el-table-column prop="uploadTime" label="上传时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleDownload(scope.row as TopicFileVO)"
                >下载</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <!-- 申请选题弹窗 -->
    <el-dialog v-model="applyVisible" title="提交选题申请" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="题目名称">
          <el-input :model-value="applyTopic?.topicName" disabled />
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
import { useAuthStore } from '@/stores/auth'
import * as topicApi from '@/api/topic'
import * as selectionApi from '@/api/selection'
import type { TopicListVO, TopicDetailVO, TopicFileVO } from '@/types/topic'
import type { TeamVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'
import { downloadByUrl } from '@/utils/download'

const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const keyword = ref('')
const topics = ref<TopicListVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(9)

// 我的团队（用于申请时选择）
const myTeams = ref<TeamVO[]>([])
const myTeamsLoading = ref(false)

async function loadTopics() {
  loading.value = true
  error.value = ''
  try {
    const res = await topicApi.queryTopicPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      openStatus: 1,
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

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentTopic = ref<TopicDetailVO | null>(null)

// 申请
const applyVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref<FormInstance>()
const applyTopic = ref<TopicListVO | null>(null)
const applyForm = ref<{
  topicId: number
  teamId?: number
  selectionReason: string
}>({
  topicId: 0,
  teamId: undefined,
  selectionReason: '',
})

const applyRules: FormRules = {
  teamId: [{ required: true, message: '请选择申请团队', trigger: 'change' }],
  selectionReason: [{ required: true, message: '请输入选题说明', trigger: 'blur' }],
}

async function openDetail(topic: TopicListVO) {
  detailVisible.value = true
  detailLoading.value = true
  currentTopic.value = null
  try {
    currentTopic.value = await topicApi.getTopicDetail(topic.topicId)
  } catch (err: any) {
    ElMessage.error(err?.message || '加载题目详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function handleDownload(row: TopicFileVO) {
  const url = topicApi.getTopicFileDownloadUrl(row.fileId)
  downloadByUrl(url, {}, row.fileName)
}

async function openApply(topic: TopicListVO) {
  applyTopic.value = topic
  applyForm.value = { topicId: topic.topicId, teamId: undefined, selectionReason: '' }
  applyVisible.value = true
  // 加载用户团队列表
  myTeamsLoading.value = true
  try {
    myTeams.value = await selectionApi.getMyTeams()
    if (myTeams.value.length === 1) {
      applyForm.value.teamId = myTeams.value[0].id
    }
    if (myTeams.value.length === 0) {
      ElMessage.warning('请先创建或加入团队，再申请选题')
    }
  } catch (err: any) {
    myTeams.value = []
    ElMessage.error(err?.message || '加载团队列表失败')
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
  if (applyForm.value.teamId == null) {
    ElMessage.warning('请选择申请团队')
    return
  }
  try {
    await ElMessageBox.confirm('确认提交该选题申请？', '提交确认', { type: 'warning' })
  } catch {
    return
  }
  submitting.value = true
  try {
    await selectionApi.submitSelection({
      topicId: applyForm.value.topicId,
      teamId: applyForm.value.teamId,
      selectionReason: applyForm.value.selectionReason,
    })
    ElMessage.success('申请已提交')
    applyVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.message || '提交选题申请失败')
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

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
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

  .data-table {
    margin-top: 8px;
  }
}
</style>
