<template>
  <div class="my-team-page">
    <page-header title="我的团队" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 无团队：创建表单 -->
    <el-card v-if="!team" v-loading="loading">
      <template #header>
        <span>创建团队</span>
      </template>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="createForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="团队介绍" prop="introduction">
          <el-input
            v-model="createForm.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入团队介绍"
          />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxSize">
          <el-input-number v-model="createForm.maxSize" :min="2" :max="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleCreate">创建团队</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 有团队：团队信息 -->
    <template v-else>
      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>{{ team.teamName }}</span>
            <el-tag size="small">{{ team.teamStatusName }}</el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="队长">{{ team.leaderName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{
            formatDateTime(team.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="团队介绍" :span="2">{{
            team.introduction || '-'
          }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">成员列表</h4>
        <el-table :data="team.members" border>
          <el-table-column prop="studentName" label="姓名" />
          <el-table-column prop="studentNo" label="学号" />
          <el-table-column prop="memberRoleName" label="角色" />
          <el-table-column prop="workContent" label="分工" show-overflow-tooltip />
          <el-table-column v-if="isLeader" label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="scope.row.studentId !== team.leaderId"
                type="primary"
                text
                size="small"
                @click="openWorkDialog(scope.row as TeamMemberVO)"
              >
                编辑分工
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 入队申请入口 -->
        <el-divider />
        <h4 class="section-title">申请加入其他团队</h4>
        <el-form :model="joinForm" inline>
          <el-form-item label="团队ID">
            <el-input-number v-model="joinForm.teamId" :min="1" placeholder="团队ID" />
          </el-form-item>
          <el-form-item label="申请留言">
            <el-input v-model="joinForm.applyMessage" placeholder="选填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="joinLoading" @click="handleJoin"
              >申请入队</el-button
            >
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 队长：入队申请审核 -->
      <el-card v-if="isLeader" v-loading="requestsLoading" class="mt-4">
        <template #header>入队申请审核</template>
        <el-empty v-if="joinRequests.length === 0" description="暂无待审核申请" />
        <el-table v-else :data="joinRequests" border>
          <el-table-column prop="applicantName" label="申请人" />
          <el-table-column prop="applicantNo" label="学号" />
          <el-table-column prop="applyMessage" label="留言" show-overflow-tooltip />
          <el-table-column prop="auditStatusName" label="状态" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button
                v-if="row.auditStatus === 0"
                type="success"
                text
                size="small"
                @click="auditRequest(row.requestId, true)"
                >通过</el-button
              >
              <el-button
                v-if="row.auditStatus === 0"
                type="danger"
                text
                size="small"
                @click="auditRequest(row.requestId, false)"
                >驳回</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 编辑分工弹窗 -->
    <el-dialog v-model="workDialogVisible" title="编辑成员分工" width="500px">
      <el-form :model="workForm" label-width="80px">
        <el-form-item label="分工内容">
          <el-input
            v-model="workForm.workContent"
            type="textarea"
            :rows="4"
            placeholder="请输入分工内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="workLoading" @click="handleSaveWork">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import * as selectionApi from '@/api/selection'
import type { TeamVO, TeamMemberVO, JoinRequestVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const team = ref<TeamVO | null>(null)

const isLeader = computed(() => {
  return team.value?.leaderId === auth.user?.relatedId
})

const createFormRef = ref<FormInstance>()
const createForm = reactive({
  teamName: '',
  introduction: '',
  maxSize: 5,
})

const createRules: FormRules = {
  teamName: [{ required: true, message: '请输入团队名称', trigger: 'blur' }],
}

async function loadTeam() {
  loading.value = true
  error.value = ''
  try {
    team.value = await selectionApi.getMyTeam()
    if (isLeader.value) {
      loadJoinRequests()
    }
  } catch (err: any) {
    const status = err?.response?.status
    const msg = err?.message || ''
    const noTeam = status === 404 || msg.includes('暂无团队')
    if (!noTeam) {
      error.value = msg || '加载团队信息失败'
    }
    team.value = null
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await selectionApi.createTeam({
        teamName: createForm.teamName,
        introduction: createForm.introduction,
        maxSize: createForm.maxSize,
      })
      ElMessage.success('创建成功')
      loadTeam()
    } finally {
      submitting.value = false
    }
  })
}

// 入队申请
const joinForm = reactive({
  teamId: undefined as number | undefined,
  applyMessage: '',
})
const joinLoading = ref(false)

async function handleJoin() {
  if (!joinForm.teamId) {
    ElMessage.warning('请输入团队ID')
    return
  }
  joinLoading.value = true
  try {
    await selectionApi.joinTeam(joinForm.teamId, { applyMessage: joinForm.applyMessage })
    ElMessage.success('申请已提交')
    joinForm.teamId = undefined
    joinForm.applyMessage = ''
  } finally {
    joinLoading.value = false
  }
}

// 入队申请审核
const joinRequests = ref<JoinRequestVO[]>([])
const requestsLoading = ref(false)

async function loadJoinRequests() {
  if (!team.value?.id && !team.value?.teamId) return
  requestsLoading.value = true
  try {
    const teamId = team.value?.id || team.value?.teamId || 0
    joinRequests.value = await selectionApi.listJoinRequests(teamId)
  } finally {
    requestsLoading.value = false
  }
}

async function auditRequest(requestId: number, approved: boolean) {
  try {
    await ElMessageBox.confirm(
      approved ? '确认通过该入队申请？' : '确认驳回该入队申请？',
      '审核确认',
      { type: 'warning' }
    )
    await selectionApi.auditJoinRequest(requestId, { approved, opinion: '' })
    ElMessage.success('审核完成')
    loadJoinRequests()
    loadTeam()
  } catch {
    // 取消
  }
}

// 编辑分工
const workDialogVisible = ref(false)
const workLoading = ref(false)
const currentMember = ref<TeamMemberVO | null>(null)
const workForm = reactive({ workContent: '' })

function openWorkDialog(member: TeamMemberVO) {
  currentMember.value = member
  workForm.workContent = member.workContent || ''
  workDialogVisible.value = true
}

async function handleSaveWork() {
  if (!team.value || !currentMember.value) return
  workLoading.value = true
  try {
    const teamId = team.value.id || team.value.teamId || 0
    await selectionApi.updateMemberWork(teamId, currentMember.value.studentId, {
      workContent: workForm.workContent,
    })
    ElMessage.success('保存成功')
    workDialogVisible.value = false
    loadTeam()
  } finally {
    workLoading.value = false
  }
}

onMounted(loadTeam)
</script>

<style scoped lang="scss">
.my-team-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .mt-4 {
    margin-top: 20px;
  }
}
</style>
