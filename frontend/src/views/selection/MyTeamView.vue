<template>
  <div class="my-team-page">
    <page-header title="我的团队" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 无团队：加入或创建 -->
    <el-card v-if="!team" v-loading="teamsLoading" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>可加入团队</span>
          <el-input
            v-model="teamKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="按团队名筛选"
            class="team-search"
          />
        </div>
      </template>
      <el-form :model="joinForm" inline class="mb-4">
        <el-form-item label="申请留言">
          <el-input v-model="joinForm.applyMessage" placeholder="选填" />
        </el-form-item>
      </el-form>
      <el-empty v-if="filteredTeams.length === 0" description="暂无可加入团队" />
      <el-table v-else :data="filteredTeams" border>
        <el-table-column prop="id" label="团队ID" width="100" />
        <el-table-column prop="teamName" label="团队名称" min-width="160" />
        <el-table-column prop="leaderId" label="队长ID" width="100" />
        <el-table-column label="人数" width="120">
          <template #default="{ row }"> {{ row.memberCount }}/{{ row.maxSize }} </template>
        </el-table-column>
        <el-table-column prop="introduction" label="团队介绍" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <span class="action-btns">
              <el-button type="primary" link size="small" :disabled="row.memberCount >= row.maxSize" :loading="joinLoadingTeamId === row.id" @click="handleJoin(row.id)">申请入队</el-button>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
            <el-tag size="small">{{ team.status === 'SELECTED' ? '已选题' : '组建中' }}</el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="队长ID">{{ team.leaderId }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{
            formatDateTime(team.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="团队介绍" :span="2">{{
            team.introduction || '-'
          }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">成员列表</h4>
        <el-table :data="team.members" border>
          <el-table-column prop="studentId" label="学生ID" />
          <el-table-column label="角色">
            <template #default="{ row }">
              {{ row.memberRole === 'LEADER' ? '队长' : '成员' }}
            </template>
          </el-table-column>
          <el-table-column prop="workContent" label="分工" show-overflow-tooltip />
          <el-table-column v-if="isLeader" label="操作" width="100">
            <template #default="scope">
              <span class="action-btns">
                <el-button v-if="scope.row.studentId !== team.leaderId" type="primary" link size="small" @click="openWorkDialog(scope.row as TeamMemberVO)">编辑分工</el-button>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 队长：入队申请审核 -->
      <el-card v-if="isLeader" v-loading="requestsLoading" class="mt-4">
        <template #header>
          <div class="card-header">
            <span>入队申请审核</span>
            <el-button :icon="Refresh" text @click="loadJoinRequests">刷新</el-button>
          </div>
        </template>
        <el-empty v-if="joinRequests.length === 0" description="暂无待审核申请" />
        <el-table v-else :data="joinRequests" border>
          <el-table-column prop="applicantId" label="申请人ID" />
          <el-table-column prop="applyMessage" label="留言" show-overflow-tooltip />
          <el-table-column label="状态">
            <template #default="{ row }">
              <status-tag category="joinRequest" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <span class="action-btns">
                <el-button v-if="row.status === 'PENDING'" type="success" link size="small" @click="auditRequest(row.id, true)">通过</el-button>
                <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click="auditRequest(row.id, false)">驳回</el-button>
              </span>
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
import { Search, Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as selectionApi from '@/api/selection'
import type { TeamVO, TeamMemberVO, JoinRequestVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const team = ref<TeamVO | null>(null)
const joinRequests = ref<JoinRequestVO[]>([])
const requestsLoading = ref(false)
const availableTeams = ref<TeamVO[]>([])
const teamsLoading = ref(false)
const teamKeyword = ref('')
const joinLoadingTeamId = ref<number | null>(null)

const isLeader = computed(() => {
  return team.value?.leaderId === auth.user?.relatedId
})

const filteredTeams = computed(() => {
  const keyword = teamKeyword.value.trim().toLowerCase()
  if (!keyword) return availableTeams.value
  return availableTeams.value.filter((item) => {
    return (
      item.teamName.toLowerCase().includes(keyword) ||
      String(item.id).includes(keyword) ||
      String(item.leaderId).includes(keyword)
    )
  })
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
      await loadJoinRequests()
    }
  } catch (err: any) {
    const status = err?.response?.status
    const msg = err?.message || ''
    const noTeam = status === 404 || msg.includes('暂无团队') || msg.includes('请先创建或加入团队')
    if (!noTeam) {
      error.value = msg || '加载团队信息失败'
    }
    team.value = null
    await loadAvailableTeams()
  } finally {
    loading.value = false
  }
}

async function loadAvailableTeams() {
  teamsLoading.value = true
  try {
    availableTeams.value = await selectionApi.listJoinableTeams()
  } finally {
    teamsLoading.value = false
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
      await loadTeam()
    } finally {
      submitting.value = false
    }
  })
}

const joinForm = reactive({
  applyMessage: '',
})

async function handleJoin(teamId: number) {
  joinLoadingTeamId.value = teamId
  try {
    await selectionApi.joinTeam(teamId, { applyMessage: joinForm.applyMessage })
    ElMessage.success('申请已提交')
    await loadAvailableTeams()
  } finally {
    joinLoadingTeamId.value = null
  }
}

// 入队申请审核
async function loadJoinRequests() {
  if (!team.value?.id) return
  requestsLoading.value = true
  try {
    joinRequests.value = await selectionApi.listJoinRequests(team.value.id)
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
    await loadJoinRequests()
    await loadTeam()
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
    await selectionApi.updateMemberWork(team.value.id, currentMember.value.studentId, {
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
    justify-content: space-between;
  }

  .team-search {
    width: 280px;
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .action-btns {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .mt-4 {
    margin-top: 20px;
  }
}
</style>
