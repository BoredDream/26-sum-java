<template>
  <div class="attendance-records-page">
    <page-header title="考勤记录">
      <template #extra>
        <el-button type="primary" :loading="exporting" @click="handleExport">导出考勤</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="签到任务">
        <el-select v-model="queryForm.taskId" placeholder="全部任务" clearable style="width: 180px">
          <el-option
            v-for="task in taskOptions"
            :key="task.taskId"
            :label="task.taskTitle"
            :value="task.taskId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学生">
        <el-input
          v-model="queryForm.keyword"
          placeholder="姓名/学号"
          clearable
          style="width: 160px"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="queryForm.signStatus"
          placeholder="全部状态"
          clearable
          style="width: 140px"
        >
          <el-option label="缺勤" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="迟到" :value="2" />
          <el-option label="补签" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && records.length === 0 && !error" description="暂无考勤记录" />

    <el-table v-loading="loading" :data="records" border class="data-table">
      <el-table-column prop="taskTitle" label="签到任务" show-overflow-tooltip />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="className" label="班级" width="120" />
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

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type {
  AttendanceRecordVO,
  AttendanceRecordQuery,
  AttendanceTaskVO,
} from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const records = ref<AttendanceRecordVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const exporting = ref(false)

const taskOptions = ref<AttendanceTaskVO[]>([])
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive({
  taskId: undefined as number | undefined,
  keyword: '',
  signStatus: undefined as number | undefined,
})

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const query: AttendanceRecordQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      taskId: queryForm.taskId,
      keyword: queryForm.keyword || undefined,
      signStatus: queryForm.signStatus,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }
    const res = await attendanceApi.queryAttendanceRecordPage(query)
    records.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载考勤记录失败'
  } finally {
    loading.value = false
  }
}

async function loadTaskOptions() {
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    taskOptions.value = res.records
  } catch {
    // 任务筛选非核心功能，失败不阻断页面
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRecords()
}

function resetQuery() {
  queryForm.taskId = undefined
  queryForm.keyword = ''
  queryForm.signStatus = undefined
  dateRange.value = null
  pageNum.value = 1
  loadRecords()
}

async function handleExport() {
  exporting.value = true
  try {
    const query: AttendanceRecordQuery = {
      taskId: queryForm.taskId,
      keyword: queryForm.keyword || undefined,
      signStatus: queryForm.signStatus,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }
    const res = await attendanceApi.exportAttendanceRecords(query)
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '考勤记录.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (err: any) {
    ElMessage.error(err?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadRecords()
  loadTaskOptions()
})
</script>

<style scoped lang="scss">
.attendance-records-page {
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
}
</style>
