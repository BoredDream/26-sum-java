<template>
  <div class="progress-page">
    <page-header title="过程评价">
      <template #extra>
        <el-button type="primary" @click="openEvaluate()">提交阶段评价</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="团队">
        <el-input v-model="queryForm.teamId" placeholder="团队ID" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty
      v-if="!loading && progressList.length === 0 && !error"
      description="暂无可评价的团队进度"
    />

    <el-table v-loading="loading" :data="progressList" border class="data-table">
      <el-table-column prop="teamName" label="团队名称" show-overflow-tooltip />
      <el-table-column prop="topicName" label="选题" show-overflow-tooltip />
      <el-table-column label="阶段完成度" width="160">
        <template #default="scope">
          {{ (scope.row as ProgressVO).evaluatedStageCount }} /
          {{ (scope.row as ProgressVO).totalStageCount }}
        </template>
      </el-table-column>
      <el-table-column label="平均阶段得分" width="140">
        <template #default="scope">
          {{ (scope.row as ProgressVO).averageStageScore ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="openEvaluate(scope.row as ProgressVO)"
            >评价</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" title="提交阶段评价" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="阶段任务" prop="stageId">
          <el-select v-model="form.stageId" placeholder="请选择阶段任务" style="width: 100%">
            <el-option
              v-for="task in stageTaskOptions"
              :key="task.stageId"
              :label="task.stageName"
              :value="task.stageId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="团队" prop="teamId">
          <el-select v-model="form.teamId" placeholder="请选择团队" style="width: 100%">
            <el-option
              v-for="item in progressList"
              :key="item.teamId"
              :label="item.teamName"
              :value="item.teamId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档质量得分" prop="docScore">
          <el-input-number
            v-model="form.docScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="完成度得分" prop="completionScore">
          <el-input-number
            v-model="form.completionScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="创新性得分">
          <el-input-number
            v-model="form.innovationScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="技术难度得分">
          <el-input-number
            v-model="form.techScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="是否迟交">
          <el-radio-group v-model="form.isLate">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.isLate === 1" label="迟交天数">
          <el-input-number v-model="form.lateDays" :min="0" :precision="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="评价结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择评价结果" style="width: 100%">
            <el-option label="通过" :value="1" />
            <el-option label="需修改" :value="2" />
            <el-option label="不通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价意见" prop="comment">
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as scoreApi from '@/api/score'
import type { ProgressVO, StageTaskVO, StageEvaluationSubmitDTO } from '@/types/score'

const loading = ref(false)
const error = ref('')
const progressList = ref<ProgressVO[]>([])
const stageTaskOptions = ref<StageTaskVO[]>([])

const queryForm = reactive({
  teamId: '',
})

async function loadProgress() {
  loading.value = true
  error.value = ''
  try {
    const teamId = queryForm.teamId ? Number(queryForm.teamId) : undefined
    const res = await scoreApi.queryProgress(teamId)
    progressList.value = res
  } catch (err: any) {
    error.value = err?.message || '加载进度失败'
  } finally {
    loading.value = false
  }
}

async function loadStageTasks() {
  try {
    const res = await scoreApi.queryStageTaskPage({ pageNum: 1, pageSize: 100 })
    stageTaskOptions.value = res.records
  } catch {
    stageTaskOptions.value = []
  }
}

function handleSearch() {
  loadProgress()
}

function resetQuery() {
  queryForm.teamId = ''
  loadProgress()
}

const formVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<StageEvaluationSubmitDTO>({
  stageId: undefined as unknown as number,
  teamId: undefined as unknown as number,
  docScore: 0,
  completionScore: 0,
  innovationScore: undefined,
  techScore: undefined,
  comment: '',
  result: 1,
  isLate: 0,
  lateDays: 0,
})

const rules: FormRules = {
  stageId: [{ required: true, message: '请选择阶段任务', trigger: 'change' }],
  teamId: [{ required: true, message: '请选择团队', trigger: 'change' }],
  docScore: [{ required: true, message: '请输入文档质量得分', trigger: 'change' }],
  completionScore: [{ required: true, message: '请输入完成度得分', trigger: 'change' }],
  result: [{ required: true, message: '请选择评价结果', trigger: 'change' }],
  comment: [{ required: true, message: '请输入评价意见', trigger: 'blur' }],
}

function resetForm() {
  form.stageId = undefined as unknown as number
  form.teamId = undefined as unknown as number
  form.docScore = 0
  form.completionScore = 0
  form.innovationScore = undefined
  form.techScore = undefined
  form.comment = ''
  form.result = 1
  form.isLate = 0
  form.lateDays = 0
}

function openEvaluate(row?: ProgressVO) {
  resetForm()
  if (row) {
    form.teamId = row.teamId
  }
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload: StageEvaluationSubmitDTO = {
      ...form,
      innovationScore: form.innovationScore === undefined ? 0 : form.innovationScore,
      techScore: form.techScore === undefined ? 0 : form.techScore,
      isLate: form.isLate ?? 0,
      lateDays: form.lateDays ?? 0,
    }
    await scoreApi.submitStageEvaluation(payload)
    ElMessage.success('评价提交成功')
    formVisible.value = false
    loadProgress()
  } catch (err: any) {
    ElMessage.error(err?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadStageTasks()
  await loadProgress()
})
</script>

<style scoped lang="scss">
.progress-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .query-form {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
