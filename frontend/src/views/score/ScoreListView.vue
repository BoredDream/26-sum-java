<template>
  <div class="score-list-page">
    <page-header title="成绩管理" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="团队">
        <el-input v-model="queryForm.teamId" placeholder="团队ID" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="草稿" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已锁定" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryForm.keyword" placeholder="团队名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="!loading && scores.length === 0 && !error" description="暂无可管理团队" />

    <el-table v-loading="loading" :data="scores" border class="data-table score-table">
      <el-table-column prop="teamName" label="团队名称" width="140" show-overflow-tooltip>
        <template #default="scope">
          <span class="team-name-cell">{{ (scope.row as ScoreVO).teamName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="topicName" label="选题" min-width="220" show-overflow-tooltip>
        <template #default="scope">{{ (scope.row as ScoreVO).topicName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="teacherName" label="评分教师" width="120" />
      <el-table-column label="过程评价" width="220">
        <template #default="scope">
          <div class="process-cell">
            <span>{{ stageProgressText(scope.row as ScoreVO) }}</span>
            <span>加权 {{ formatScore((scope.row as ScoreVO).processScore) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文档成绩" width="110">
        <template #default="scope">{{ formatScore((scope.row as ScoreVO).docScore) }}</template>
      </el-table-column>
      <el-table-column label="考勤成绩" width="110">
        <template #default="scope">{{ formatScore((scope.row as ScoreVO).attendanceScore) }}</template>
      </el-table-column>
      <el-table-column label="系统实现" width="110">
        <template #default="scope">{{ formatScore((scope.row as ScoreVO).systemScore) }}</template>
      </el-table-column>
      <el-table-column label="答辩成绩" width="110">
        <template #default="scope">{{ formatScore((scope.row as ScoreVO).defenseScore) }}</template>
      </el-table-column>
      <el-table-column label="总分" width="110">
        <template #default="scope">
          <strong>{{ formatScore((scope.row as ScoreVO).totalScore) }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag v-if="!(scope.row as ScoreVO).scoreId" type="info" effect="plain" size="small">
            未生成
          </el-tag>
          <status-tag v-else category="score" :value="(scope.row as ScoreVO).status" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="scope">{{
          (scope.row as ScoreVO).updateTime ? formatDateTime((scope.row as ScoreVO).updateTime) : '-'
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="scope">
          <div class="table-actions">
            <el-button type="primary" text size="small" @click="openDetail(scope.row as ScoreVO)"
              >详情</el-button
            >
            <el-button type="primary" text size="small" @click="openEdit(scope.row as ScoreVO)"
              >编辑</el-button
            >
            <el-button
              v-if="(scope.row as ScoreVO).scoreId && (scope.row as ScoreVO).status !== 2"
              type="success"
              text
              size="small"
              :loading="confirmingId === (scope.row as ScoreVO).scoreId"
              @click="handleConfirm(scope.row as ScoreVO)"
            >
              确认
            </el-button>
          </div>
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
        @size-change="loadScores"
        @current-change="loadScores"
      />
    </div>

    <!-- 成绩详情 -->
    <el-dialog v-model="detailVisible" title="成绩详情" width="700px">
      <template v-if="currentScore">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团队名称">{{ currentScore.teamName }}</el-descriptions-item>
          <el-descriptions-item label="选题">{{ currentScore.topicName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分教师">{{
            currentScore.teacherName
          }}</el-descriptions-item>
          <el-descriptions-item label="过程评价">{{
            stageProgressText(currentScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="过程加权得分">{{
            formatScore(currentScore.processScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="过程建议分">{{
            processSuggestionText(currentScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="文档成绩">{{
            formatScore(currentScore.docScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="考勤成绩">{{
            formatScore(currentScore.attendanceScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="系统实现与测试">{{
            formatScore(currentScore.systemScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="答辩成绩">{{
            formatScore(currentScore.defenseScore)
          }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <strong>{{ formatScore(currentScore.totalScore) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="!currentScore.scoreId" type="info" effect="plain" size="small">未生成</el-tag>
            <status-tag v-else category="score" :value="currentScore.status" />
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="currentScore.teacherComment" class="comment-block">
          <div class="comment-label">教师总评</div>
          <div class="comment-content">{{ currentScore.teacherComment }}</div>
        </div>
        <h4 class="section-title">个人成绩</h4>
        <el-empty
          v-if="!currentScore.scoreId || currentScore.studentScores.length === 0"
          description="保存团队成绩后生成个人成绩明细"
        />
        <el-table v-else :data="currentScore.studentScores" border>
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="contributionFactor" label="贡献系数" width="110" />
          <el-table-column prop="personalScore" label="个人成绩" width="110" />
          <el-table-column prop="grade" label="等级" width="100" />
          <el-table-column prop="teacherComment" label="评语" show-overflow-tooltip />
        </el-table>
      </template>
    </el-dialog>

    <!-- 编辑成绩 -->
    <el-dialog v-model="formVisible" title="编辑成绩" width="860px" class="score-edit-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="score-form">
        <el-form-item label="团队" prop="teamId">
          <el-select v-model="form.teamId" placeholder="请选择团队" style="width: 100%" disabled>
            <el-option
              v-for="item in teamOptions"
              :key="item.teamId"
              :label="item.teamName"
              :value="item.teamId"
            />
          </el-select>
        </el-form-item>
        <div v-if="currentEditingScore" class="process-summary">
          <div class="summary-grid">
            <div>
              <span class="summary-label">过程评价</span>
              <strong>{{ stageProgressText(currentEditingScore) }}</strong>
            </div>
            <div>
              <span class="summary-label">过程加权</span>
              <strong>{{ formatScore(currentEditingScore.processScore) }}</strong>
            </div>
            <div>
              <span class="summary-label">建议文档</span>
              <strong>{{ formatScore(currentEditingScore.suggestedDocScore) }} / 15</strong>
            </div>
            <div>
              <span class="summary-label">建议系统</span>
              <strong>{{ formatScore(currentEditingScore.suggestedSystemScore) }} / 50</strong>
            </div>
          </div>
          <el-button size="small" type="primary" plain @click="syncProcessScores">
            同步过程评价分
          </el-button>
        </div>
        <el-form-item label="文档编写成绩" prop="docScore">
          <div class="score-input-row">
            <el-input-number v-model="form.docScore" :min="0" :max="15" :precision="2" />
            <span class="form-tip">/ 15</span>
          </div>
        </el-form-item>
        <el-form-item label="考勤成绩" prop="attendanceScore">
          <div class="score-input-row">
            <el-input-number v-model="form.attendanceScore" :min="0" :max="15" :precision="2" />
            <span class="form-tip">/ 15</span>
          </div>
        </el-form-item>
        <el-form-item label="系统实现与测试" prop="systemScore">
          <div class="score-input-row">
            <el-input-number v-model="form.systemScore" :min="0" :max="50" :precision="2" />
            <span class="form-tip">/ 50</span>
          </div>
        </el-form-item>
        <el-form-item label="答辩成绩" prop="defenseScore">
          <div class="score-input-row">
            <el-input-number v-model="form.defenseScore" :min="0" :max="20" :precision="2" />
            <span class="form-tip">/ 20</span>
          </div>
        </el-form-item>
        <el-form-item label="当前总分">
          <strong>{{ formatScore(formTotal) }}</strong>
        </el-form-item>
        <el-form-item label="教师总评">
          <el-input
            v-model="form.teacherComment"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="个人成绩调整">
          <el-table :data="form.studentScores" border size="small" class="student-score-table">
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="studentNo" label="学号" width="150" />
            <el-table-column label="贡献系数" width="170">
              <template #default="scope">
                <el-input-number
                  v-model="(scope.row as StudentScoreFormItem).contributionFactor"
                  :min="0"
                  :max="2"
                  :precision="2"
                  class="factor-input"
                />
              </template>
            </el-table-column>
            <el-table-column label="评语">
              <template #default="scope">
                <el-input
                  v-model="(scope.row as StudentScoreFormItem).teacherComment"
                  maxlength="200"
                  show-word-limit
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存成绩</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as scoreApi from '@/api/score'
import * as selectionApi from '@/api/selection'
import type {
  ScoreVO,
  ScoreListVO,
  ScoreSaveDTO,
  StudentScoreSaveDTO,
  StudentScoreVO,
} from '@/types/score'
import type { TeamVO, TeamMemberVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

interface StudentScoreFormItem {
  studentId: number
  studentName?: string
  studentNo?: string
  contributionFactor?: number
  teacherComment?: string
}

const loading = ref(false)
const error = ref('')
const scores = ref<ScoreListVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  teamId: '',
  status: undefined as number | undefined,
  keyword: '',
})

function formatScore(value?: number | null) {
  if (value === undefined || value === null || Number.isNaN(Number(value))) {
    return '0.00'
  }
  return Number(value).toFixed(2)
}

function stageProgressText(score: Pick<ScoreVO, 'evaluatedStageCount' | 'totalStageCount' | 'evaluatedStageWeight' | 'totalStageWeight'>) {
  const evaluatedCount = score.evaluatedStageCount ?? 0
  const totalCount = score.totalStageCount ?? 0
  const evaluatedWeight = score.evaluatedStageWeight ?? 0
  const totalWeight = score.totalStageWeight ?? 0
  return `${evaluatedCount}/${totalCount} 阶段，权重 ${formatScore(evaluatedWeight)}/${formatScore(totalWeight)}`
}

function processSuggestionText(score: ScoreVO) {
  return `文档 ${formatScore(score.suggestedDocScore)} / 15，系统 ${formatScore(
    score.suggestedSystemScore
  )} / 50`
}

async function loadScores() {
  loading.value = true
  error.value = ''
  try {
    const res = await scoreApi.queryScorePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      teamId: queryForm.teamId ? Number(queryForm.teamId) : undefined,
      status: queryForm.status,
      keyword: queryForm.keyword || undefined,
    })
    scores.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载成绩列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadScores()
}

function resetQuery() {
  queryForm.teamId = ''
  queryForm.status = undefined
  queryForm.keyword = ''
  pageNum.value = 1
  loadScores()
}

const detailVisible = ref(false)
const currentScore = ref<(ScoreVO & { studentScores: StudentScoreVO[] }) | null>(null)

async function openDetail(row: ScoreVO) {
  detailVisible.value = true
  currentScore.value = { ...row, studentScores: [] }
  if (!row.scoreId) {
    return
  }
  const scoreId = row.scoreId
  try {
    const team = await selectionApi.getTeam(row.teamId)
    const members = team.members ?? []
    const studentScores = await Promise.all(
      members.map((m: TeamMemberVO) =>
        scoreApi.queryStudentScore(m.studentId).then((list) => {
          const matched = list.find((item) => item.scoreId === scoreId)
          return (
            matched || {
              studentScoreId: 0,
              scoreId,
              teamId: row.teamId,
              studentId: m.studentId,
              studentName: `学生${m.studentId}`,
              studentNo: '-',
              contributionFactor: 1,
              personalScore: row.totalScore,
              grade: '-',
              teacherComment: '',
            }
          )
        })
      )
    )
    if (currentScore.value) {
      currentScore.value.studentScores = studentScores
    }
  } catch (err: any) {
    ElMessage.warning(err?.message || '加载个人成绩明细失败')
  }
}

const teamOptions = ref<{ teamId: number; teamName: string }[]>([])

async function loadTeamOptions() {
  try {
    const res = await scoreApi.queryScorePage({ pageNum: 1, pageSize: 1000 })
    teamOptions.value = res.records.map((item) => ({
      teamId: item.teamId,
      teamName: item.teamName,
    }))
  } catch {
    teamOptions.value = []
  }
}

const formVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const currentEditingScore = ref<ScoreVO | null>(null)
const form = reactive<ScoreSaveDTO & { studentScores: StudentScoreFormItem[] }>({
  teamId: undefined as unknown as number,
  docScore: 0,
  attendanceScore: 0,
  systemScore: 0,
  defenseScore: 0,
  teacherComment: '',
  studentScores: [],
})

const formTotal = computed(
  () =>
    Number(form.docScore || 0) +
    Number(form.attendanceScore || 0) +
    Number(form.systemScore || 0) +
    Number(form.defenseScore || 0)
)

const rules: FormRules = {
  teamId: [{ required: true, message: '请选择团队', trigger: 'change' }],
  docScore: [{ required: true, message: '请输入文档编写成绩', trigger: 'change' }],
  attendanceScore: [{ required: true, message: '请输入考勤成绩', trigger: 'change' }],
  systemScore: [{ required: true, message: '请输入系统实现与测试成绩', trigger: 'change' }],
  defenseScore: [{ required: true, message: '请输入答辩成绩', trigger: 'change' }],
}

async function openEdit(row: ScoreVO) {
  await loadTeamOptions()
  currentEditingScore.value = row
  form.teamId = row.teamId
  form.docScore = row.docScore
  form.attendanceScore = row.attendanceScore
  form.systemScore = row.systemScore
  form.defenseScore = row.defenseScore
  form.teacherComment = row.teacherComment || ''
  form.studentScores = []

  try {
    const team: TeamVO = await selectionApi.getTeam(row.teamId)
    const members = team.members ?? []
    const scoreId = row.scoreId
    const existingScores = scoreId
      ? await Promise.all(
          members.map((m: TeamMemberVO) =>
            scoreApi
              .queryStudentScore(m.studentId)
              .then((list) => list.find((item) => item.scoreId === scoreId))
          )
        )
      : []
    form.studentScores = members.map((m: TeamMemberVO, index: number) => {
      const existing = existingScores[index]
      return {
        studentId: m.studentId,
        studentName: `学生${m.studentId}`,
        studentNo: '-',
        contributionFactor: existing?.contributionFactor ?? 1,
        teacherComment: existing?.teacherComment || '',
      }
    })
  } catch (err: any) {
    ElMessage.warning(err?.message || '加载团队成员失败')
  }

  formVisible.value = true
}

function syncProcessScores() {
  if (!currentEditingScore.value) return
  form.docScore = Number(currentEditingScore.value.suggestedDocScore ?? 0)
  form.systemScore = Number(currentEditingScore.value.suggestedSystemScore ?? 0)
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
    const payload: ScoreSaveDTO = {
      teamId: form.teamId,
      docScore: form.docScore,
      attendanceScore: form.attendanceScore,
      systemScore: form.systemScore,
      defenseScore: form.defenseScore,
      teacherComment: form.teacherComment,
      studentScores: form.studentScores.map((item): StudentScoreSaveDTO => ({
        studentId: item.studentId,
        contributionFactor: item.contributionFactor,
        teacherComment: item.teacherComment,
      })),
    }
    await scoreApi.saveTeamScore(payload)
    ElMessage.success('保存成功')
    formVisible.value = false
    loadScores()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const confirmingId = ref(0)
async function handleConfirm(row: ScoreVO) {
  if (!row.scoreId) {
    ElMessage.warning('请先保存团队成绩，再进行确认')
    return
  }
  const scoreId = row.scoreId
  try {
    await ElMessageBox.confirm('确认后成绩将变为已确认状态，是否继续？', '确认成绩', {
      type: 'warning',
    })
  } catch {
    return
  }
  confirmingId.value = scoreId
  try {
    await scoreApi.confirmScore(scoreId)
    ElMessage.success('成绩已确认')
    loadScores()
  } catch (err: any) {
    ElMessage.error(err?.message || '确认失败')
  } finally {
    confirmingId.value = 0
  }
}

onMounted(() => {
  loadScores()
  loadTeamOptions()
})
</script>

<style scoped lang="scss">
.score-list-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .query-form {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }

  .score-table {
    :deep(.el-table__header th) {
      background: #f8fafc;
      color: #606266;
      font-weight: 600;
    }
  }

  .team-name-cell {
    display: inline-block;
    max-width: 100%;
    color: #303133;
    font-weight: 500;
    white-space: nowrap;
  }

  .process-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
    line-height: 1.35;

    span {
      white-space: nowrap;
    }
  }

  .table-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    white-space: nowrap;

    :deep(.el-button) {
      margin-left: 0;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .comment-block {
    margin-top: 16px;

    .comment-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }

    .comment-content {
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

  .score-form {
    :deep(.el-form-item__label) {
      white-space: nowrap;
    }
  }

  .process-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin: 0 0 16px 140px;
    padding: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #f8fafc;
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(100px, 1fr));
    gap: 12px;
    flex: 1;

    > div {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
  }

  .score-input-row {
    display: flex;
    align-items: center;
    gap: 8px;

    :deep(.el-input-number) {
      width: 220px;
    }
  }

  .student-score-table {
    width: 100%;

    .factor-input {
      width: 130px;
    }
  }

  .summary-label,
  .form-tip {
    color: #909399;
    font-size: 13px;
  }

  .form-tip {
    min-width: 36px;
  }
}
</style>
