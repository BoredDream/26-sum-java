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

    <el-empty v-if="!loading && scores.length === 0 && !error" description="暂无成绩记录" />

    <el-table v-loading="loading" :data="scores" border class="data-table">
      <el-table-column prop="teamName" label="团队名称" show-overflow-tooltip />
      <el-table-column prop="teacherName" label="评分教师" width="120" />
      <el-table-column label="文档成绩" width="110">
        <template #default="scope">{{ (scope.row as ScoreVO).docScore }}</template>
      </el-table-column>
      <el-table-column label="考勤成绩" width="110">
        <template #default="scope">{{ (scope.row as ScoreVO).attendanceScore }}</template>
      </el-table-column>
      <el-table-column label="系统实现" width="110">
        <template #default="scope">{{ (scope.row as ScoreVO).systemScore }}</template>
      </el-table-column>
      <el-table-column label="答辩成绩" width="110">
        <template #default="scope">{{ (scope.row as ScoreVO).defenseScore }}</template>
      </el-table-column>
      <el-table-column label="总分" width="110">
        <template #default="scope">
          <strong>{{ (scope.row as ScoreVO).totalScore }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <status-tag category="score" :value="(scope.row as ScoreVO).status" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as ScoreVO).updateTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="openDetail(scope.row as ScoreVO)"
            >详情</el-button
          >
          <el-button type="primary" text size="small" @click="openEdit(scope.row as ScoreVO)"
            >编辑</el-button
          >
          <el-button
            v-if="(scope.row as ScoreVO).status !== 2"
            type="success"
            text
            size="small"
            :loading="confirmingId === (scope.row as ScoreVO).scoreId"
            @click="handleConfirm(scope.row as ScoreVO)"
          >
            确认
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
        @size-change="loadScores"
        @current-change="loadScores"
      />
    </div>

    <!-- 成绩详情 -->
    <el-dialog v-model="detailVisible" title="成绩详情" width="700px">
      <template v-if="currentScore">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团队名称">{{ currentScore.teamName }}</el-descriptions-item>
          <el-descriptions-item label="评分教师">{{
            currentScore.teacherName
          }}</el-descriptions-item>
          <el-descriptions-item label="文档成绩">{{ currentScore.docScore }}</el-descriptions-item>
          <el-descriptions-item label="考勤成绩">{{
            currentScore.attendanceScore
          }}</el-descriptions-item>
          <el-descriptions-item label="系统实现与测试">{{
            currentScore.systemScore
          }}</el-descriptions-item>
          <el-descriptions-item label="答辩成绩">{{
            currentScore.defenseScore
          }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <strong>{{ currentScore.totalScore }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <status-tag category="score" :value="currentScore.status" />
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="currentScore.teacherComment" class="comment-block">
          <div class="comment-label">教师总评</div>
          <div class="comment-content">{{ currentScore.teacherComment }}</div>
        </div>
        <h4 class="section-title">个人成绩</h4>
        <el-empty v-if="currentScore.studentScores.length === 0" description="暂无个人成绩明细" />
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
    <el-dialog v-model="formVisible" title="编辑成绩" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
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
        <el-form-item label="文档编写成绩" prop="docScore">
          <el-input-number
            v-model="form.docScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="考勤成绩" prop="attendanceScore">
          <el-input-number
            v-model="form.attendanceScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="系统实现与测试" prop="systemScore">
          <el-input-number
            v-model="form.systemScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="答辩成绩" prop="defenseScore">
          <el-input-number
            v-model="form.defenseScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 200px"
          />
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
          <el-table :data="form.studentScores" border size="small">
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="studentNo" label="学号" width="140" />
            <el-table-column label="贡献系数" width="160">
              <template #default="scope">
                <el-input-number
                  v-model="(scope.row as StudentScoreFormItem).contributionFactor"
                  :min="0"
                  :max="2"
                  :precision="2"
                  style="width: 120px"
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
import { ref, reactive, onMounted } from 'vue'
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
  try {
    const team = await selectionApi.getTeam(row.teamId)
    const studentScores = await Promise.all(
      team.members.map((m: TeamMemberVO) =>
        scoreApi.queryStudentScore(m.studentId).then((list) => {
          const matched = list.find((item) => item.scoreId === row.scoreId)
          return (
            matched || {
              studentScoreId: 0,
              scoreId: row.scoreId,
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
const form = reactive<ScoreSaveDTO & { studentScores: StudentScoreFormItem[] }>({
  teamId: undefined as unknown as number,
  docScore: 0,
  attendanceScore: 0,
  systemScore: 0,
  defenseScore: 0,
  teacherComment: '',
  studentScores: [],
})

const rules: FormRules = {
  teamId: [{ required: true, message: '请选择团队', trigger: 'change' }],
  docScore: [{ required: true, message: '请输入文档编写成绩', trigger: 'change' }],
  attendanceScore: [{ required: true, message: '请输入考勤成绩', trigger: 'change' }],
  systemScore: [{ required: true, message: '请输入系统实现与测试成绩', trigger: 'change' }],
  defenseScore: [{ required: true, message: '请输入答辩成绩', trigger: 'change' }],
}

async function openEdit(row: ScoreVO) {
  await loadTeamOptions()
  form.teamId = row.teamId
  form.docScore = row.docScore
  form.attendanceScore = row.attendanceScore
  form.systemScore = row.systemScore
  form.defenseScore = row.defenseScore
  form.teacherComment = row.teacherComment || ''
  form.studentScores = []

  try {
    const team: TeamVO = await selectionApi.getTeam(row.teamId)
    const existingScores = await Promise.all(
      team.members.map((m: TeamMemberVO) =>
        scoreApi
          .queryStudentScore(m.studentId)
          .then((list) => list.find((item) => item.scoreId === row.scoreId))
      )
    )
    form.studentScores = team.members.map((m: TeamMemberVO, index: number) => {
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

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
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
  })
}

const confirmingId = ref(0)
async function handleConfirm(row: ScoreVO) {
  try {
    await ElMessageBox.confirm('确认后成绩将变为已确认状态，是否继续？', '确认成绩', {
      type: 'warning',
    })
  } catch {
    return
  }
  confirmingId.value = row.scoreId
  try {
    await scoreApi.confirmScore(row.scoreId)
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
}
</style>
