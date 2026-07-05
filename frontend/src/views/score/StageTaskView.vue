<template>
  <div class="stage-task-page">
    <page-header title="阶段任务">
      <template #extra>
        <el-button type="primary" @click="openCreate">新增阶段任务</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="任务状态">
        <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="未开始" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryForm.keyword" placeholder="阶段名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && tasks.length === 0 && !error" description="暂无阶段任务" />

    <el-table v-loading="loading" :data="tasks" border class="data-table">
      <el-table-column prop="stageName" label="阶段名称" show-overflow-tooltip />
      <el-table-column prop="stageDesc" label="阶段描述" show-overflow-tooltip />
      <el-table-column label="开始时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as StageTaskVO).startTime)
        }}</template>
      </el-table-column>
      <el-table-column label="截止时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as StageTaskVO).endTime)
        }}</template>
      </el-table-column>
      <el-table-column label="权重" width="100">
        <template #default="scope">{{ (scope.row as StageTaskVO).weight }}%</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <status-tag category="stageTask" :value="(scope.row as StageTaskVO).status" />
        </template>
      </el-table-column>
      <el-table-column prop="teacherName" label="发布人" width="120" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="openEdit(scope.row as StageTaskVO)"
            >编辑</el-button
          >
          <el-button type="danger" text size="small" @click="handleDelete(scope.row as StageTaskVO)"
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
        @size-change="loadTasks"
        @current-change="loadTasks"
      />
    </div>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑阶段任务' : '新增阶段任务'"
      width="650px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="阶段名称" prop="stageName">
          <el-input v-model="form.stageName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="阶段描述" prop="stageDesc">
          <el-input
            v-model="form.stageDesc"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="请选择开始时间"
                value-format="YYYY-MM-DD[T]HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="请选择截止时间"
                value-format="YYYY-MM-DD[T]HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权重" prop="weight">
          <el-input-number
            v-model="form.weight"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
          <span class="form-tip">%</span>
        </el-form-item>
        <el-form-item label="交付物要求" prop="deliverables">
          <el-input
            v-model="form.deliverables"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="评分标准" prop="scoringCriteria">
          <el-input
            v-model="form.scoringCriteria"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{
          isEdit ? '保存' : '创建'
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as scoreApi from '@/api/score'
import type { StageTaskVO, StageTaskCreateDTO, StageTaskUpdateDTO } from '@/types/score'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const tasks = ref<StageTaskVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  status: undefined as number | undefined,
  keyword: '',
})

async function loadTasks() {
  loading.value = true
  error.value = ''
  try {
    const res = await scoreApi.queryStageTaskPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: queryForm.status,
      keyword: queryForm.keyword || undefined,
    })
    tasks.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载阶段任务失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadTasks()
}

function resetQuery() {
  queryForm.status = undefined
  queryForm.keyword = ''
  pageNum.value = 1
  loadTasks()
}

const formVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const formRef = ref<FormInstance>()
const form = reactive<StageTaskCreateDTO>({
  stageName: '',
  stageDesc: '',
  startTime: '',
  endTime: '',
  deliverables: '',
  scoringCriteria: '',
  weight: 0,
  status: 0,
})

const rules: FormRules = {
  stageName: [{ required: true, message: '请输入阶段名称', trigger: 'blur' }],
  stageDesc: [{ required: true, message: '请输入阶段描述', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!value || !form.startTime) {
          callback()
          return
        }
        if (new Date(value) <= new Date(form.startTime)) {
          callback(new Error('截止时间必须晚于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  weight: [{ required: true, message: '请输入权重', trigger: 'change' }],
  deliverables: [{ required: true, message: '请输入交付物要求', trigger: 'blur' }],
  scoringCriteria: [{ required: true, message: '请输入评分标准', trigger: 'blur' }],
}

function resetForm() {
  form.stageName = ''
  form.stageDesc = ''
  form.startTime = ''
  form.endTime = ''
  form.deliverables = ''
  form.scoringCriteria = ''
  form.weight = 0
  form.status = 0
}

function openCreate() {
  isEdit.value = false
  editingId.value = 0
  resetForm()
  formVisible.value = true
}

function openEdit(row: StageTaskVO) {
  isEdit.value = true
  editingId.value = row.stageId
  form.stageName = row.stageName
  form.stageDesc = row.stageDesc
  form.startTime = row.startTime
  form.endTime = row.endTime
  form.deliverables = row.deliverables
  form.scoringCriteria = row.scoringCriteria
  form.weight = row.weight
  form.status = row.status
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        const payload: StageTaskUpdateDTO = { ...form }
        await scoreApi.updateStageTask(editingId.value, payload)
        ElMessage.success('保存成功')
      } else {
        await scoreApi.createStageTask({ ...form })
        ElMessage.success('创建成功')
      }
      formVisible.value = false
      loadTasks()
    } catch (err: any) {
      ElMessage.error(err?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row: StageTaskVO) {
  try {
    await ElMessageBox.confirm('确认删除该阶段任务？删除后不可恢复。', '删除确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await scoreApi.deleteStageTask(row.stageId)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(loadTasks)
</script>

<style scoped lang="scss">
.stage-task-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .query-form {
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

  .form-tip {
    margin-left: 8px;
    color: #909399;
  }
}
</style>
