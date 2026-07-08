<template>
  <div class="my-attendance-page">
    <page-header title="我的考勤" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 待签到任务 -->
    <el-card v-loading="tasksLoading" class="mb-4">
      <template #header><span>待签到任务</span></template>
      <el-empty v-if="pendingTasks.length === 0" description="暂无待签到任务" />
      <el-row v-else :gutter="16">
        <el-col v-for="task in pendingTasks" :key="task.taskId" :span="8" class="task-card-col">
          <el-card shadow="hover">
            <div class="task-card-title">{{ task.taskTitle }}</div>
            <div class="task-card-meta">
              <div>签到类型：{{ task.taskTypeName }}</div>
              <div>适用范围：{{ task.scopeDisplayName }}</div>
              <div>开始时间：{{ formatDateTime(task.startTime) }}</div>
              <div>结束时间：{{ formatDateTime(task.endTime) }}</div>
            </div>
            <el-button
              type="primary"
              :loading="signingId === task.taskId"
              style="width: 100%"
              @click="handleSign(task.taskId)"
            >
              立即签到
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 考勤记录 -->
    <el-card v-loading="loading">
      <template #header><span>考勤记录</span></template>
      <el-empty v-if="!loading && records.length === 0 && !error" description="暂无考勤记录" />
      <el-table v-else :data="records" border>
        <el-table-column prop="taskTitle" label="签到任务" show-overflow-tooltip />
        <el-table-column label="签到时间" width="170">
          <template #default="scope">{{
            formatDateTime((scope.row as AttendanceRecordVO).signTime)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <status-tag
              category="attendanceSign"
              :value="(scope.row as AttendanceRecordVO).signStatus"
            />
          </template>
        </el-table-column>
        <el-table-column label="补签" width="90">
          <template #default="scope">
            <el-tag v-if="(scope.row as AttendanceRecordVO).isMakeup === 1" type="info" size="small"
              >是</el-tag
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 签到备注 -->
    <el-dialog v-model="signVisible" title="签到确认" width="400px">
      <el-form :model="signForm" label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="signForm.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signVisible = false">取消</el-button>
        <el-button type="primary" :loading="signLoading" @click="confirmSign">确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type { AttendanceRecordVO, AttendanceTaskVO } from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const tasksLoading = ref(false)
const error = ref('')
const records = ref<AttendanceRecordVO[]>([])
const tasks = ref<AttendanceTaskVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const pendingTasks = computed(() => tasks.value.filter((t) => t.status === 1))

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const res = await attendanceApi.queryAttendanceRecordPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    records.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载考勤记录失败'
  } finally {
    loading.value = false
  }
}

async function loadTasks() {
  tasksLoading.value = true
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    tasks.value = res.records
  } catch (err: any) {
    error.value = err?.message || '加载签到任务失败'
  } finally {
    tasksLoading.value = false
  }
}

// 签到
const signVisible = ref(false)
const signLoading = ref(false)
const signingId = ref(0)
const currentTaskId = ref(0)
const signForm = reactive({
  remark: '',
})

function handleSign(taskId: number) {
  currentTaskId.value = taskId
  signForm.remark = ''
  signVisible.value = true
}

async function confirmSign() {
  signLoading.value = true
  signingId.value = currentTaskId.value
  try {
    await attendanceApi.signAttendance({
      taskId: currentTaskId.value,
      remark: signForm.remark,
    })
    ElMessage.success('签到成功')
    signVisible.value = false
    loadRecords()
    loadTasks()
  } catch (err: any) {
    ElMessage.error(err?.message || '签到失败')
  } finally {
    signLoading.value = false
    signingId.value = 0
  }
}

onMounted(() => {
  loadRecords()
  loadTasks()
})
</script>

<style scoped lang="scss">
.my-attendance-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .task-card-col {
    margin-bottom: 16px;
  }

  .task-card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .task-card-meta {
    font-size: 13px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 16px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
