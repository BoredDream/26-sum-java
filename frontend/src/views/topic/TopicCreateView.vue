<template>
  <div class="topic-create-page">
    <page-header title="新增题目">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </page-header>

    <div class="topic-create-layout">
      <el-card class="topic-form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="topic-form">
        <el-form-item label="题目名称" prop="topicName">
          <el-input
            v-model="form.topicName"
            placeholder="请输入题目名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="题目类型" prop="topicType">
          <el-select v-model="form.topicType" placeholder="请选择题目类型" style="width: 100%">
            <el-option v-for="item in topicTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option v-for="item in difficultyOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="限选人数" prop="studentLimit">
              <el-input-number v-model="form.studentLimit" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队上限">
              <el-input-number v-model="form.teamLimit" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="选题开始" prop="selectionStartTime">
          <el-date-picker
            v-model="form.selectionStartTime"
            type="datetime"
            placeholder="请选择选题开始时间"
            value-format="YYYY-MM-DD[T]HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="选题结束" prop="selectionEndTime">
          <el-date-picker
            v-model="form.selectionEndTime"
            type="datetime"
            placeholder="请选择选题结束时间"
            value-format="YYYY-MM-DD[T]HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="题目内容" prop="topicContent">
          <el-input
            v-model="form.topicContent"
            type="textarea"
            :rows="6"
            placeholder="请输入题目内容及要求"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="技术路线" prop="technicalRoute">
          <el-input
            v-model="form.technicalRoute"
            type="textarea"
            :rows="4"
            placeholder="请输入技术路线"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="开发工具">
          <el-input
            v-model="form.developTools"
            type="textarea"
            :rows="3"
            placeholder="请输入开发工具及环境（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="info" :loading="submitting" @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
          <el-button type="success" :loading="aiLoading" @click="handleAiSuggest">AI 建议</el-button>
        </el-form-item>
      </el-form>
      </el-card>
      <el-card class="ai-suggestion-card">
      <template #header>
        <div class="ai-suggestion-header">
          <span>AI 出题建议</span>
          <el-button link type="primary" :loading="aiLoading" @click="handleAiSuggest">重新生成</el-button>
        </div>
      </template>
      <div v-if="aiSuggestion" class="ai-suggestion-content" v-html="renderedAiSuggestion"></div>
      <el-empty v-else description="填写题目信息后点击 AI 建议" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as topicApi from '@/api/topic'
import type { TopicCreateDTO } from '@/types/topic'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const aiLoading = ref(false)
const aiSuggestion = ref("")

const topicTypeOptions = ['理论研究', '应用开发', '工程设计', '数据分析', '其他']

const renderedAiSuggestion = computed(() => renderMarkdown(aiSuggestion.value))

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderInlineMarkdown(value: string) {
  return value
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

function renderMarkdown(value: string) {
  const lines = escapeHtml(value).split(/\r?\n/)
  const html: string[] = []
  let inList = false

  const closeList = () => {
    if (inList) {
      html.push('</ul>')
      inList = false
    }
  }

  lines.forEach((line) => {
    const trimmed = line.trim()
    if (!trimmed) {
      closeList()
      return
    }

    const heading = /^(#{1,3})\s+(.+)$/.exec(trimmed)
    if (heading) {
      closeList()
      const level = heading[1].length + 2
      html.push(`<h${level}>${renderInlineMarkdown(heading[2])}</h${level}>`)
      return
    }

    const bullet = /^[-*]\s+(.+)$/.exec(trimmed)
    if (bullet) {
      if (!inList) {
        html.push('<ul>')
        inList = true
      }
      html.push(`<li>${renderInlineMarkdown(bullet[1])}</li>`)
      return
    }

    closeList()
    html.push(`<p>${renderInlineMarkdown(trimmed)}</p>`)
  })

  closeList()
  return html.join('')
}
const difficultyOptions = ['简单', '中等', '困难']

const form = reactive<TopicCreateDTO>({
  topicName: '',
  topicType: '',
  difficulty: '',
  studentLimit: 5,
  teamLimit: undefined,
  topicContent: '',
  developTools: '',
  technicalRoute: '',
  selectionStartTime: '',
  selectionEndTime: '',
  status: 0,
})

const rules: FormRules = {
  topicName: [{ required: true, message: '请输入题目名称', trigger: 'blur' }],
  topicType: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  studentLimit: [{ required: true, message: '请填写限选人数', trigger: 'change' }],
  topicContent: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  technicalRoute: [{ required: true, message: '请输入技术路线', trigger: 'blur' }],
  selectionEndTime: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!value) {
          callback()
          return
        }
        if (form.selectionStartTime && new Date(value) <= new Date(form.selectionStartTime)) {
          callback(new Error('选题结束时间必须晚于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

async function submit(status: number) {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload: TopicCreateDTO = { ...form, status }
    await topicApi.createTopic(payload)
    ElMessage.success(status === 0 ? '草稿已保存' : '题目已提交审核')
    router.push('/topic/my-list')
  } catch (err: any) {
    // 全局拦截器已提示后端错误，此处兜底避免未处理的 Promise 拒绝
    ElMessage.error('创建题目失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function handleAiSuggest() {
  if (!form.topicName && !form.topicContent && !form.technicalRoute) {
    ElMessage.warning('请先填写题目名称、题目内容或技术路线中的至少一项')
    return
  }
  aiLoading.value = true
  try {
    const res = await topicApi.suggestTopic({
      topicName: form.topicName,
      topicType: form.topicType,
      difficulty: form.difficulty,
      studentLimit: form.studentLimit,
      teamLimit: form.teamLimit,
      topicContent: form.topicContent,
      technicalRoute: form.technicalRoute,
      developTools: form.developTools,
    })
    aiSuggestion.value = res.suggestion
  } catch (err: any) {
    ElMessage.error(err?.message || '生成出题建议失败')
  } finally {
    aiLoading.value = false
  }
}
function handleSaveDraft() {
  submit(0)
}

function handleSubmit() {
  submit(1)
}
</script>

<style scoped lang="scss">
.topic-create-page {
  .topic-create-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 360px;
    gap: 16px;
    align-items: start;
  }

  .topic-form-card,
  .ai-suggestion-card {
    min-width: 0;
  }

  .topic-form {
    max-width: 800px;
    margin: 0 auto;
  }

  .ai-suggestion-card {
    position: sticky;
    top: 16px;
  }

  .ai-suggestion-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .ai-suggestion-content {
    max-height: calc(100vh - 180px);
    overflow: auto;
    line-height: 1.8;
    white-space: pre-wrap;
  }

  @media (max-width: 1100px) {
    .topic-create-layout {
      grid-template-columns: 1fr;
    }

    .ai-suggestion-card {
      position: static;
    }
  }
}
</style>
