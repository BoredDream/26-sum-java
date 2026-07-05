<template>
  <div class="attendance-task-page">
    <page-header title="签到任务">
      <template #extra>
        <el-button type="primary" @click="openCreate">新增签到任务</el-button>
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
        <el-input v-model="queryForm.keyword" placeholder="任务标题" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && tasks.length === 0 && !error" description="暂无签到任务" />

    <el-table v-loading="loading" :data="tasks" border class="data-table">
      <el-table-column prop="taskTitle" label="任务标题" show-overflow-tooltip />
      <el-table-column prop="taskTypeName" label="签到类型" width="120" />
      <el-table-column prop="scopeDisplayName" label="适用范围" width="160" show-overflow-tooltip />
      <el-table-column label="开始时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as AttendanceTaskVO).startTime)
        }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as AttendanceTaskVO).endTime)
        }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <status-tag category="attendanceTask" :value="(scope.row as AttendanceTaskVO).status" />
        </template>
      </el-table-column>
      <el-table-column prop="signedCount" label="签到进度" width="120">
        <template #default="scope">
          {{ (scope.row as AttendanceTaskVO).signedCount || 0 }} /
          {{ (scope.row as AttendanceTaskVO).totalCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="teacherName" label="发布人" width="120" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            text
            size="small"
            @click="openDetail(scope.row as AttendanceTaskVO)"
            >详情</el-button
          >
          <el-button
            v-if="(scope.row as AttendanceTaskVO).status !== 2"
            type="warning"
            text
            size="small"
            :loading="finishId === (scope.row as AttendanceTaskVO).taskId"
            @click="handleFinish(scope.row as AttendanceTaskVO)"
          >
            结束任务
          </el-button>
          <el-button
            type="danger"
            text
            size="small"
            @click="handleDelete(scope.row as AttendanceTaskVO)"
          >
            删除
          </el-button>
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

    <!-- 新增/编辑任务 -->
    <el-dialog v-model="formVisible" title="新增签到任务" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="taskTitle">
          <el-input v-model="form.taskTitle" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="签到类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="请选择签到类型" style="width: 100%">
            <el-option label="日常签到" :value="1" />
            <el-option label="阶段签到" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用范围" prop="scopeType">
          <el-select v-model="form.scopeType" placeholder="请选择适用范围" style="width: 100%">
            <el-option label="班级" :value="1" />
            <el-option label="团队" :value="2" />
            <el-option label="全部" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 1" label="班级" prop="scopeValue">
          <el-input v-model="form.scopeValue" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item v-if="form.scopeType === 2" label="团队" prop="scopeValue">
          <el-input-number
            :model-value="asNumber(form.scopeValue)"
            :min="1"
            placeholder="请输入团队ID"
            style="width: 100%"
            @update:model-value="form.scopeValue = $event || undefined"
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
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="请选择结束时间"
                value-format="YYYY-MM-DD[T]HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布任务</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="700px">
      <template v-if="currentTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题">{{ currentTask.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="签到类型">{{
            currentTask.taskTypeName
          }}</el-descriptions-item>
          <el-descriptions-item label="适用范围">{{
            currentTask.scopeDisplayName
          }}</el-descriptions-item>
          <el-descriptions-item label="发布人">{{ currentTask.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{
            formatDateTime(currentTask.startTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{
            formatDateTime(currentTask.endTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <status-tag category="attendanceTask" :value="currentTask.status" />
          </el-descriptions-item>
          <el-descriptions-item label="签到进度">
            {{ currentTask.signedCount || 0 }} / {{ currentTask.totalCount || 0 }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="currentTask.description" class="task-desc">
          <div class="desc-label">任务说明</div>
          <div class="desc-content">{{ currentTask.description }}</div>
        </div>
        <h4 class="section-title">考勤记录</h4>
        <el-empty
          v-if="!currentTask.records || currentTask.records.length === 0"
          description="暂无考勤记录"
        />
        <el-table v-else :data="currentTask.records" border>
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column label="签到时间" width="170">
            <template #default="scope">{{
              formatDateTime((scope.row as AttendanceRecordVO).signTime)
            }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <status-tag
                category="attendanceSign"
                :value="(scope.row as AttendanceRecordVO).signStatus"
              />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type {
  AttendanceTaskVO,
  AttendanceTaskCreateDTO,
  AttendanceTaskDetailVO,
  AttendanceRecordVO,
} from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const tasks = ref<AttendanceTaskVO[]>([])
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
    const res = await attendanceApi.queryAttendanceTaskPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: queryForm.status,
      keyword: queryForm.keyword || undefined,
    })
    tasks.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载签到任务失败'
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

// 表单
const formVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<AttendanceTaskCreateDTO>({
  taskTitle: '',
  taskType: 1,
  scopeType: 3,
  scopeValue: undefined,
  startTime: '',
  endTime: '',
  description: '',
})

const rules: FormRules = {
  taskTitle: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择签到类型', trigger: 'change' }],
  scopeType: [{ required: true, message: '请选择适用范围', trigger: 'change' }],
  scopeValue: [
    {
      validator: (_rule: any, value: string | number, callback: (error?: Error) => void) => {
        if (form.scopeType === 3) {
          callback()
          return
        }
        if (value === undefined || value === null || value === '') {
          callback(new Error(form.scopeType === 1 ? '请输入班级名称' : '请输入团队ID'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!value || !form.startTime) {
          callback()
          return
        }
        if (new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

function asNumber(value: string | number | undefined): number | undefined {
  return typeof value === 'number' ? value : undefined
}

function openCreate() {
  form.taskTitle = ''
  form.taskType = 1
  form.scopeType = 3
  form.scopeValue = undefined
  form.startTime = ''
  form.endTime = ''
  form.description = ''
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload: AttendanceTaskCreateDTO = {
        ...form,
        scopeValue: form.scopeType === 3 ? undefined : String(form.scopeValue),
      }
      await attendanceApi.createAttendanceTask(payload)
      ElMessage.success('任务发布成功')
      formVisible.value = false
      loadTasks()
    } catch (err: any) {
      ElMessage.error(err?.message || '发布失败')
    } finally {
      submitting.value = false
    }
  })
}

// 结束任务
const finishId = ref(0)
async function handleFinish(row: AttendanceTaskVO) {
  try {
    await ElMessageBox.confirm('确认结束该签到任务？结束后学生将无法签到。', '结束确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  finishId.value = row.taskId
  try {
    await attendanceApi.finishAttendanceTask(row.taskId)
    ElMessage.success('任务已结束')
    loadTasks()
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    finishId.value = 0
  }
}

// 删除任务
async function handleDelete(row: AttendanceTaskVO) {
  try {
    await ElMessageBox.confirm('确认删除该签到任务？删除后不可恢复。', '删除确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await attendanceApi.deleteAttendanceTask(row.taskId)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (err: any) {
    if (err?.response?.status === 404) {
      ElMessage.warning('该功能暂不可用')
    } else {
      ElMessage.error('删除失败')
    }
  }
}

// 详情
const detailVisible = ref(false)
const currentTask = ref<AttendanceTaskDetailVO | null>(null)

async function openDetail(row: AttendanceTaskVO) {
  try {
    currentTask.value = await attendanceApi.getAttendanceTaskDetail(row.taskId)
    detailVisible.value = true
  } catch (err: any) {
    ElMessage.error(err?.message || '加载详情失败')
  }
}

onMounted(loadTasks)
</script>

<style scoped lang="scss">
.attendance-task-page {
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

  .task-desc {
    margin-top: 16px;

    .desc-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }

    .desc-content {
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      color: #606266;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
