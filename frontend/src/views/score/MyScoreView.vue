<template>
  <div class="my-score-page">
    <page-header title="我的成绩" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && !scoreOverview && !error" description="暂无成绩信息" />

    <template v-if="scoreOverview">
      <el-card v-loading="loading" class="score-overview">
        <template #header>
          <div class="overview-header">
            <span>成绩总览</span>
            <status-tag category="score" :value="scoreOverview.status ?? 0" />
          </div>
        </template>
        <el-row :gutter="40">
          <el-col :span="8">
            <div class="score-item">
              <div class="score-value">{{ scoreOverview.totalScore ?? '-' }}</div>
              <div class="score-label">总成绩</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="score-item">
              <div class="score-value">{{ scoreOverview.grade ?? '-' }}</div>
              <div class="score-label">成绩等级</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="score-item">
              <div class="score-value">{{ scoreOverview.teamName ?? '-' }}</div>
              <div class="score-label">所属团队</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <h4 class="section-title">各阶段得分</h4>
      <el-empty v-if="stageEvaluations.length === 0" description="暂无阶段评价记录" />
      <el-table v-else :data="stageEvaluations" border class="data-table">
        <el-table-column prop="stageName" label="阶段名称" show-overflow-tooltip />
        <el-table-column prop="docScore" label="文档质量" width="110" />
        <el-table-column prop="completionScore" label="完成度" width="110" />
        <el-table-column prop="innovationScore" label="创新性" width="110">
          <template #default="scope">{{
            (scope.row as StageEvaluationVO).innovationScore ?? '-'
          }}</template>
        </el-table-column>
        <el-table-column prop="techScore" label="技术难度" width="110">
          <template #default="scope">{{
            (scope.row as StageEvaluationVO).techScore ?? '-'
          }}</template>
        </el-table-column>
        <el-table-column label="阶段综合得分" width="130">
          <template #default="scope">
            <strong>{{ (scope.row as StageEvaluationVO).totalScore }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="是否迟交" width="100">
          <template #default="scope">{{
            (scope.row as StageEvaluationVO).isLate === 1 ? '是' : '否'
          }}</template>
        </el-table-column>
        <el-table-column prop="comment" label="教师评语" show-overflow-tooltip />
      </el-table>

      <h4 class="section-title">个人成绩明细</h4>
      <el-empty v-if="studentScores.length === 0" description="暂无个人成绩明细" />
      <el-table v-else :data="studentScores" border class="data-table">
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="contributionFactor" label="贡献系数" width="120" />
        <el-table-column prop="personalScore" label="个人成绩" width="120" />
        <el-table-column prop="grade" label="等级" width="100" />
        <el-table-column prop="teacherComment" label="评语" show-overflow-tooltip />
      </el-table>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as scoreApi from '@/api/score'
import type { MyScoreVO, StageEvaluationVO, StudentScoreVO } from '@/types/score'

const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const scoreOverview = ref<MyScoreVO | null>(null)
const stageEvaluations = ref<StageEvaluationVO[]>([])
const studentScores = ref<StudentScoreVO[]>([])

async function loadMyScore() {
  const studentId = auth.user?.relatedId
  if (!studentId) {
    error.value = '无法获取当前学生信息'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const [studentScoreList, myScoreData] = await Promise.all([
      scoreApi.queryStudentScore(studentId),
      scoreApi.getMyScore(studentId).catch(() => null),
    ])

    studentScores.value = studentScoreList

    if (myScoreData) {
      scoreOverview.value = myScoreData
      stageEvaluations.value = myScoreData.stageEvaluations || []
      return
    }

    if (studentScoreList.length > 0) {
      const first = studentScoreList[0]
      const teamId = first.teamId
      const teamScoreRes = await scoreApi.queryScorePage({ pageNum: 1, pageSize: 1, teamId })
      const teamScoreRecord = teamScoreRes.records[0]

      scoreOverview.value = {
        studentId,
        studentName: first.studentName,
        studentNo: first.studentNo,
        teamId: first.teamId,
        teamName: first.teamName,
        scoreId: first.scoreId,
        totalScore: first.personalScore,
        grade: first.grade,
        status: teamScoreRecord?.status ?? 0,
        stageEvaluations: [],
        studentScores: studentScoreList,
      }

      try {
        const progressList = await scoreApi.queryProgress(teamId)
        stageEvaluations.value = progressList.map((p): StageEvaluationVO => ({
          evaluationId: 0,
          stageId: 0,
          stageName: '阶段评价',
          teamId,
          teamName: p.teamName,
          docScore: p.averageStageScore ?? 0,
          completionScore: p.averageStageScore ?? 0,
          totalScore: p.averageStageScore ?? 0,
          comment: '',
          result: 1,
          isLate: 0,
          createTime: '',
        }))
      } catch {
        stageEvaluations.value = []
      }
    }
  } catch (err: any) {
    error.value = err?.message || '加载我的成绩失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadMyScore)
</script>

<style scoped lang="scss">
.my-score-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .score-overview {
    margin-bottom: 24px;

    .overview-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 16px;
      font-weight: 600;
    }

    .score-item {
      text-align: center;
      padding: 16px 0;

      .score-value {
        font-size: 28px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 8px;
      }

      .score-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .data-table {
    margin-top: 8px;
  }
}
</style>
