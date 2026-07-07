<template>
  <div class="my-team-page">
    <page-header title="我的团队">
      <template #extra>
        <el-button type="primary" @click="openCreateDialog">创建团队</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 我的团队卡片区 -->
    <div v-if="teams.length > 0" v-loading="loading" class="team-cards">
      <el-card v-for="t in teams" :key="t.id" class="team-card mb-4">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="team-name">{{ t.teamName }}</span>
              <el-tag size="small" :type="t.status === 'SELECTED' ? 'success' : 'info'" class="ml-2">
                {{ t.status === 'SELECTED' ? '已选题' : '组建中' }}
              </el-tag>
              <el-tag v-if="isLeaderOfTeam(t)" size="small" type="warning" class="ml-1">队长</el-tag>
            </div>
            <div class="header-right">
              <span class="member-count">{{ t.memberCount }}/{{ t.maxSize }} 人</span>
              <el-button
                v-if="!isLeaderOfTeam(t)"
                type="danger"
                text
                size="small"
                :loading="leavingTeamId === t.id"
                @click="handleRequestLeave(t.id)"
              >
                申请离队
              </el-button>
            </div>
          </div>
        </template>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="队长ID">{{ t.leaderId }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(t.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="团队介绍" :span="2">{{ t.introduction || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">成员列表</h4>
        <el-table :data="t.members" border size="small">
          <el-table-column prop="studentId" label="学生ID" />
          <el-table-column label="角色">
            <template #default="{ row }">
              {{ row.memberRole === 'LEADER' ? '队长' : '成员' }}
            </template>
          </el-table-column>
          <el-table-column prop="workContent" label="分工" show-overflow-tooltip />
          <el-table-column v-if="isLeaderOfTeam(t)" label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="scope.row.studentId !== t.leaderId"
                type="primary"
                text
                size="small"
                @click="openWorkDialog(t, scope.row as TeamMemberVO)"
              >
                编辑分工
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 队长审核区 -->
        <template v-if="isLeaderOfTeam(t)">
          <el-divider />
          <el-tabs>
            <el-tab-pane>
              <template #label>
                入队申请
                <el-badge v-if="joinRequestCounts[t.id]" :value="joinRequestCounts[t.id]" class="tab-badge" />
              </template>
              <el-empty v-if="!joinRequestsByTeam[t.id]?.length" description="暂无待审核入队申请" />
              <el-table v-else :data="joinRequestsByTeam[t.id]" border size="small">
                <el-table-column prop="applicantId" label="申请人ID" />
                <el-table-column prop="applyMessage" label="留言" show-overflow-tooltip />
                <el-table-column label="状态">
                  <template #default="{ row }">
                    <status-tag category="joinRequest" :value="row.status" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button v-if="row.status === 'PENDING'" type="success" text size="small" @click="auditJoin(row.id, true)">通过</el-button>
                    <el-button v-if="row.status === 'PENDING'" type="danger" text size="small" @click="auditJoin(row.id, false)">驳回</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane>
              <template #label>
                离队申请
                <el-badge v-if="leaveRequestCounts[t.id]" :value="leaveRequestCounts[t.id]" class="tab-badge" />
              </template>
              <el-empty v-if="!leaveRequestsByTeam[t.id]?.length" description="暂无待审核离队申请" />
              <el-table v-else :data="leaveRequestsByTeam[t.id]" border size="small">
                <el-table-column prop="applicantId" label="申请人ID" />
                <el-table-column prop="leaveMessage" label="离队原因" show-overflow-tooltip />
                <el-table-column label="状态">
                  <template #default="{ row }">
                    <status-tag category="leaveRequest" :value="row.status" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button v-if="row.status === 'PENDING'" type="success" text size="small" @click="auditLeave(row.id, true)">通过</el-button>
                    <el-button v-if="row.status === 'PENDING'" type="danger" text size="small" @click="auditLeave(row.id, false)">驳回</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </template>
      </el-card>
    </div>

    <!-- 无团队时也显示加入/创建入口 -->
    <el-empty v-if="!loading && teams.length === 0 && !error" description="暂无团队，请创建或加入团队" />

    <!-- 可加入团队 -->
    <el-card v-loading="teamsLoading" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>可加入团队</span>
          <el-input v-model="teamKeyword" :prefix-icon="Search" clearable placeholder="按团队名筛选" class="team-search" />
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
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              type="primary" text size="small"
              :disabled="row.memberCount >= row.maxSize"
              :loading="joinLoadingTeamId === row.id"
              @click="handleJoin(row.id)"
            >
              申请入队
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建团队弹窗 -->
    <el-dialog v-model="createVisible" title="创建团队" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="createForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="团队介绍" prop="introduction">
          <el-input v-model="createForm.introduction" type="textarea" :rows="3" placeholder="请输入团队介绍" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxSize">
          <el-input-number v-model="createForm.maxSize" :min="2" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">创建团队</el-button>
      </template>
    </el-dialog>

    <!-- 编辑分工弹窗 -->
    <el-dialog v-model="workDialogVisible" title="编辑成员分工" width="500px">
      <el-form :model="workForm" label-width="80px">
        <el-form-item label="所属团队">{{ currentTeam?.teamName }}</el-form-item>
        <el-form-item label="成员ID">{{ currentMember?.studentId }}</el-form-item>
        <el-form-item label="分工内容">
          <el-input v-model="workForm.workContent" type="textarea" :rows="4" placeholder="请输入分工内容" />
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
import { Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as selectionApi from '@/api/selection'
import type { TeamVO, TeamMemberVO, JoinRequestVO, LeaveRequestVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const auth = useAuthStore()

// ── 我的团队 ──
const loading = ref(false)
const error = ref('')
const teams = ref<TeamVO[]>([])

// ── 可加入团队 ──
const availableTeams = ref<TeamVO[]>([])
const teamsLoading = ref(false)
const teamKeyword = ref('')
const joinLoadingTeamId = ref<number | null>(null)
const joinForm = reactive({ applyMessage: '' })

const filteredTeams = computed(() => {
  const keyword = teamKeyword.value.trim().toLowerCase()
  if (!keyword) return availableTeams.value
  return availableTeams.value.filter((item) => {
    return item.teamName.toLowerCase().includes(keyword) ||
      String(item.id).includes(keyword) ||
      String(item.leaderId).includes(keyword)
  })
})

// ── 队长审核数据 ──
const joinRequestsByTeam = ref<Record<number, JoinRequestVO[]>>({})
const leaveRequestsByTeam = ref<Record<number, LeaveRequestVO[]>>({})
const joinRequestCounts = computed(() => {
  const counts: Record<number, number> = {}
  for (const [teamId, reqs] of Object.entries(joinRequestsByTeam.value)) {
    counts[Number(teamId)] = (reqs as JoinRequestVO[]).filter(r => r.status === 'PENDING').length
  }
  return counts
})
const leaveRequestCounts = computed(() => {
  const counts: Record<number, number> = {}
  for (const [teamId, reqs] of Object.entries(leaveRequestsByTeam.value)) {
    counts[Number(teamId)] = (reqs as LeaveRequestVO[]).filter(r => r.status === 'PENDING').length
  }
  return counts
})

// ── 创建团队 ──
const createVisible = ref(false)
const submitting = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({ teamName: '', introduction: '', maxSize: 5 })
const createRules: FormRules = {
  teamName: [{ required: true, message: '请输入团队名称', trigger: 'blur' }],
}

// ── 离队 ──
const leavingTeamId = ref<number | null>(null)

// ── 编辑分工 ──
const workDialogVisible = ref(false)
const workLoading = ref(false)
const currentTeam = ref<TeamVO | null>(null)
const currentMember = ref<TeamMemberVO | null>(null)
const workForm = reactive({ workContent: '' })

// ── Helpers ──
function isLeaderOfTeam(team: TeamVO): boolean {
  return team.leaderId === auth.user?.relatedId
}

// ── 数据加载 ──
async function loadTeams() {
  loading.value = true
  error.value = ''
  try {
    teams.value = await selectionApi.getMyTeams()
    // 加载每个团队的审核数据
    for (const t of teams.value) {
      if (isLeaderOfTeam(t)) {
        loadJoinRequestsForTeam(t.id)
        loadLeaveRequestsForTeam(t.id)
      }
    }
  } catch (err: any) {
    teams.value = []
    error.value = err?.message || '加载团队信息失败'
  } finally {
    loading.value = false
    loadAvailableTeams()
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

async function loadJoinRequestsForTeam(teamId: number) {
  try {
    joinRequestsByTeam.value[teamId] = await selectionApi.listJoinRequests(teamId)
  } catch { /* ignore */ }
}

async function loadLeaveRequestsForTeam(teamId: number) {
  try {
    leaveRequestsByTeam.value[teamId] = await selectionApi.listLeaveRequests(teamId)
  } catch { /* ignore */ }
}

// ── 创建团队 ──
function openCreateDialog() {
  createForm.teamName = ''
  createForm.introduction = ''
  createForm.maxSize = 5
  createVisible.value = true
  createFormRef.value?.clearValidate()
}

async function handleCreate() {
  if (!createFormRef.value) return
  try { await createFormRef.value.validate() } catch { return }
  submitting.value = true
  try {
    await selectionApi.createTeam({
      teamName: createForm.teamName,
      introduction: createForm.introduction,
      maxSize: createForm.maxSize,
    })
    ElMessage.success('创建成功')
    createVisible.value = false
    await loadTeams()
  } catch { /* 错误已由拦截器展示 */ } finally {
    submitting.value = false
  }
}

// ── 加入团队 ──
async function handleJoin(teamId: number) {
  joinLoadingTeamId.value = teamId
  try {
    await selectionApi.joinTeam(teamId, { applyMessage: joinForm.applyMessage })
    ElMessage.success('申请已提交')
    await loadAvailableTeams()
  } catch { /* 错误已由拦截器展示 */ } finally {
    joinLoadingTeamId.value = null
  }
}

// ── 离队 ──
async function handleRequestLeave(teamId: number) {
  try {
    const { value: message } = await ElMessageBox.prompt('请输入离队原因（选填）', '申请离队', {
      inputType: 'textarea',
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
    })
    leavingTeamId.value = teamId
    await selectionApi.requestLeave(teamId, { leaveMessage: message || '' })
    ElMessage.success('离队申请已提交，请等待队长审核')
    await loadTeams()
  } catch { /* 取消或出错 */ } finally {
    leavingTeamId.value = null
  }
}

// ── 审核入队 ──
async function auditJoin(requestId: number, approved: boolean) {
  try {
    let opinion = ''
    if (approved) {
      await ElMessageBox.confirm('确认通过该入队申请？', '审核确认', { type: 'warning' })
    } else {
      const { value } = await ElMessageBox.prompt('请输入驳回理由', '驳回入队申请', {
        inputType: 'textarea',
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputValidator: (val) => val && val.trim() ? true : '驳回理由不能为空',
      })
      opinion = value || ''
    }
    await selectionApi.auditJoinRequest(requestId, { approved, opinion })
    ElMessage.success('审核完成')
    await loadTeams()
  } catch { /* 取消 */ }
}

// ── 审核离队 ──
async function auditLeave(requestId: number, approved: boolean) {
  try {
    let opinion = ''
    if (approved) {
      await ElMessageBox.confirm('确认通过该离队申请？通过后该成员将从团队中移除。', '审核确认', { type: 'warning' })
    } else {
      const { value } = await ElMessageBox.prompt('请输入驳回理由', '驳回离队申请', {
        inputType: 'textarea',
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputValidator: (val) => val && val.trim() ? true : '驳回理由不能为空',
      })
      opinion = value || ''
    }
    await selectionApi.auditLeaveRequest(requestId, { approved, opinion })
    ElMessage.success('审核完成')
    await loadTeams()
  } catch { /* 取消 */ }
}

// ── 编辑分工 ──
function openWorkDialog(team: TeamVO, member: TeamMemberVO) {
  currentTeam.value = team
  currentMember.value = member
  workForm.workContent = member.workContent || ''
  workDialogVisible.value = true
}

async function handleSaveWork() {
  if (!currentTeam.value || !currentMember.value) return
  workLoading.value = true
  try {
    await selectionApi.updateMemberWork(currentTeam.value.id, currentMember.value.studentId, {
      workContent: workForm.workContent,
    })
    ElMessage.success('保存成功')
    workDialogVisible.value = false
    loadTeams()
  } catch { /* 错误已由拦截器展示 */ } finally {
    workLoading.value = false
  }
}

onMounted(loadTeams)
</script>

<style scoped lang="scss">
.my-team-page {
  .mb-4 { margin-bottom: 16px; }
  .mt-4 { margin-top: 20px; }
  .ml-1 { margin-left: 4px; }
  .ml-2 { margin-left: 8px; }

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    justify-content: space-between;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .team-name { font-size: 18px; font-weight: 600; }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    .member-count { color: #909399; font-size: 14px; }
  }

  .team-search { width: 280px; }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .tab-badge {
    margin-left: 6px;
  }
}
</style>
