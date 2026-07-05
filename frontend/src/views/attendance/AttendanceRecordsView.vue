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
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            type="primary"
            text
            size="small"
            @click="openEdit(scope.row as AttendanceRecordVO)"
          >
            修改状态
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
        @size-change="loadRecords"
        @current-change="loadRecords"
      />
    </div>

    <!-- 修改考勤状态 -->
    <el-dialog v-model="editVisible" title="修改考勤状态" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="学生">
          <span>{{ currentRecord?.studentName }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <status-tag category="attendanceSign" :value="currentRecord?.signStatus ?? 0" />
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="editForm.signStatus" placeholder="请选择状态" style="width: 100%">
            <el-option label="缺勤" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="补签" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

// 修改考勤状态
const editVisible = ref(false)
const editSubmitting = ref(false)
const currentRecord = ref<AttendanceRecordVO | null>(null)
const editForm = reactive({
  signStatus: 0,
  remark: '',
})

function openEdit(row: AttendanceRecordVO) {
  currentRecord.value = row
  editForm.signStatus = row.signStatus
  editForm.remark = row.remark || ''
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!currentRecord.value) return
  try {
    await ElMessageBox.confirm('确认保存修改？', '保存确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  editSubmitting.value = true
  try {
    await attendanceApi.updateAttendanceRecordStatus(currentRecord.value.recordId, {
      signStatus: editForm.signStatus,
      remark: editForm.remark,
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadRecords()
  } catch (err: any) {
    if (err?.response?.status === 404) {
      ElMessage.warning('该功能暂不可用')
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    editSubmitting.value = false
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
