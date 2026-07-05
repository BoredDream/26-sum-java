<template>
  <div class="topic-create-page">
    <page-header title="新增题目">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </page-header>

    <el-card>
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
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as topicApi from '@/api/topic'
import type { TopicCreateDTO } from '@/types/topic'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const topicTypeOptions = ['理论研究', '应用开发', '工程设计', '数据分析', '其他']
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
  await formRef.value.validate(async (valid) => {
    if (!valid) return
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
  })
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
  .topic-form {
    max-width: 800px;
    margin: 0 auto;
  }
}
</style>
