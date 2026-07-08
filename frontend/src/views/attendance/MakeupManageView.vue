<template>
  <div class="makeup-manage-page">
    <page-header title="补签审核" />

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
      <el-form-item label="审核状态">
        <el-select
          v-model="queryForm.auditStatus"
          placeholder="全部状态"
          clearable
          style="width: 140px"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="通过" :value="1" />
          <el-option label="驳回" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && applications.length === 0 && !error" description="暂无补签申请" />

    <el-table v-loading="loading" :data="applications" border class="data-table">
      <el-table-column prop="taskTitle" label="签到任务" show-overflow-tooltip />
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="applyReason" label="补签原因" show-overflow-tooltip />
      <el-table-column label="证明材料" width="220" show-overflow-tooltip>
        <template #default="scope">
          <el-tooltip
            v-if="(scope.row as MakeupApplyVO).proofFilePath"
            effect="dark"
            :content="(scope.row as MakeupApplyVO).proofFilePath"
            placement="top"
          >
            <span class="proof-path">{{ (scope.row as MakeupApplyVO).proofFilePath }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <status-tag category="makeup" :value="(scope.row as MakeupApplyVO).auditStatus" />
        </template>
      </el-table-column>
      <el-table-column prop="auditTeacherName" label="审核人" width="120" />
      <el-table-column prop="auditComment" label="审核意见" show-overflow-tooltip />
      <el-table-column label="申请时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as MakeupApplyVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <template v-if="(scope.row as MakeupApplyVO).auditStatus === 0">
            <el-button
              type="success"
              text
              size="small"
              :loading="auditId === (scope.row as MakeupApplyVO).applyId && auditAction === 1"
              @click="openAudit(scope.row as MakeupApplyVO, 1)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              text
              size="small"
              :loading="auditId === (scope.row as MakeupApplyVO).applyId && auditAction === 2"
              @click="openAudit(scope.row as MakeupApplyVO, 2)"
            >
              驳回
            </el-button>
          </template>
          <el-button
            type="primary"
            text
            size="small"
            @click="openDetail(scope.row as MakeupApplyVO)"
          >
            详情
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
        @size-change="loadApplications"
        @current-change="loadApplications"
      />
    </div>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="auditVisible"
      :title="auditAction === 1 ? '通过补签申请' : '驳回补签申请'"
      width="450px"
    >
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="80px">
        <el-form-item label="学生">
          <span>{{ currentApply?.studentName }}（{{ currentApply?.studentNo }}）</span>
        </el-form-item>
        <el-form-item label="补签原因">
          <div class="reason-text">{{ currentApply?.applyReason }}</div>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditComment">
          <el-input
            v-model="auditForm.auditComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="handleAudit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="补签申请详情" width="550px">
      <template v-if="currentApply">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="签到任务" :span="2">{{
            currentApply.taskTitle
          }}</el-descriptions-item>
          <el-descriptions-item label="学生">{{ currentApply.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentApply.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <status-tag category="makeup" :value="currentApply.auditStatus" />
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{
            currentApply.auditTeacherName || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{
            formatDateTime(currentApply.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="审核时间" :span="2">{{
            formatDateTime(currentApply.auditTime)
          }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <div class="detail-label">补签原因</div>
          <div class="detail-content">{{ currentApply.applyReason }}</div>
        </div>
        <div v-if="currentApply.auditComment" class="detail-section">
          <div class="detail-label">审核意见</div>
          <div class="detail-content">{{ currentApply.auditComment }}</div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type { MakeupApplyVO, MakeupQuery, AttendanceTaskVO } from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const applications = ref<MakeupApplyVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const taskOptions = ref<AttendanceTaskVO[]>([])

const queryForm = reactive({
  taskId: undefined as number | undefined,
  auditStatus: undefined as number | undefined,
})

async function loadApplications() {
  loading.value = true
  error.value = ''
  try {
    const query: MakeupQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      taskId: queryForm.taskId,
      auditStatus: queryForm.auditStatus,
    }
    const res = await attendanceApi.queryMakeupPage(query)
    applications.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载补签申请失败'
  } finally {
    loading.value = false
  }
}

async function loadTaskOptions() {
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    taskOptions.value = res.records
  } catch {
    // 非核心
  }
}

function handleSearch() {
  pageNum.value = 1
  loadApplications()
}

function resetQuery() {
  queryForm.taskId = undefined
  queryForm.auditStatus = undefined
  pageNum.value = 1
  loadApplications()
}

// 审核
const auditVisible = ref(false)
const auditLoading = ref(false)
const auditId = ref(0)
const auditAction = ref(1)
const currentApply = ref<MakeupApplyVO | null>(null)
const auditFormRef = ref<FormInstance>()
const auditForm = reactive({
  auditComment: '',
})

const auditRules: FormRules = {
  auditComment: [
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (auditAction.value === 2 && (!value || !value.trim())) {
          callback(new Error('驳回时必须填写审核意见'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function openAudit(row: MakeupApplyVO, action: number) {
  currentApply.value = row
  auditAction.value = action
  auditForm.auditComment = ''
  auditVisible.value = true
}

async function handleAudit() {
  if (!auditFormRef.value || !currentApply.value) return
  try {
    await auditFormRef.value.validate()
  } catch {
    return
  }
  const confirmText = auditAction.value === 1 ? '确认通过该补签申请？' : '确认驳回该补签申请？'
  try {
    await ElMessageBox.confirm(confirmText, '审核确认', { type: 'warning' })
  } catch {
    return
  }

  auditLoading.value = true
  const applyId = currentApply.value!.applyId
  auditId.value = applyId
  try {
    await attendanceApi.reviewMakeup(applyId, {
      auditStatus: auditAction.value,
      auditComment: auditForm.auditComment,
    })
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadApplications()
  } catch (err: any) {
    ElMessage.error(err?.message || '审核失败')
  } finally {
    auditLoading.value = false
    auditId.value = 0
  }
}

// 详情
const detailVisible = ref(false)

function openDetail(row: MakeupApplyVO) {
  currentApply.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadApplications()
  loadTaskOptions()
})
</script>

<style scoped lang="scss">
.makeup-manage-page {
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

  .reason-text {
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  .proof-path {
    display: inline-block;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
    vertical-align: middle;
  }

  .detail-section {
    margin-top: 16px;

    .detail-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }

    .detail-content {
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      color: #606266;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }
}
</style>
