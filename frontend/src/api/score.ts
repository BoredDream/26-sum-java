import { get, post, put, del, downloadFile } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  StageTaskVO,
  StageTaskCreateDTO,
  StageTaskUpdateDTO,
  StageTaskQuery,
  StageEvaluationVO,
  StageEvaluationSubmitDTO,
  ContributionVO,
  ContributionUpdateDTO,
  ContributionQuery,
  ScoreListVO,
  ScoreQuery,
  ScoreDetailVO,
  StudentScoreVO,
  MyScoreVO,
  ScoreSaveDTO,
  ProgressVO,
} from '@/types/score'

// 阶段任务
export function createStageTask(data: StageTaskCreateDTO) {
  return post<void>('/score/stage-task', data)
}

export function updateStageTask(stageId: number, data: StageTaskUpdateDTO) {
  return put<void>(`/score/stage-tasks/${stageId}`, data)
}

export function deleteStageTask(stageId: number) {
  return del<void>(`/score/stage-tasks/${stageId}`)
}

export function queryStageTaskPage(query: StageTaskQuery) {
  return get<PageResult<StageTaskVO>>('/score/stage-task/page', { params: query })
}

// 过程评价
export function submitStageEvaluation(data: StageEvaluationSubmitDTO) {
  return post<void>('/score/stage-evaluation', data)
}

export function queryStageEvaluationPage(query: StageTaskQuery) {
  return get<PageResult<StageEvaluationVO>>('/score/evaluations', { params: query })
}

// 贡献度
export function recordContribution(data: ContributionUpdateDTO) {
  return post<void>('/score/contribution', data)
}

export function queryContributionPage(query: ContributionQuery) {
  return get<PageResult<ContributionVO>>('/score/contributions', { params: query })
}

export function updateContribution(contributionId: number, data: ContributionUpdateDTO) {
  return put<void>(`/score/contributions/${contributionId}`, data)
}

// 进度
export function queryProgress(teamId?: number) {
  return get<ProgressVO[]>('/score/progress', { params: { teamId } })
}

// 成绩
export function saveTeamScore(data: ScoreSaveDTO) {
  return post<void>('/score', data)
}

export function queryScorePage(query: ScoreQuery) {
  return get<PageResult<ScoreListVO>>('/score/page', { params: query })
}

export function getScoreDetail(scoreId: number) {
  return get<ScoreDetailVO>(`/score/${scoreId}`)
}

export function confirmScore(scoreId: number) {
  return post<void>(`/score/${scoreId}/confirm`)
}

export function lockScore(scoreId: number) {
  return post<void>(`/score/${scoreId}/lock`)
}

export function exportScores() {
  return downloadFile('/score/export')
}

// 学生成绩
export function queryStudentScore(studentId: number) {
  return get<StudentScoreVO[]>(`/score/student/${studentId}`)
}

export function getMyScore(studentId: number) {
  return get<MyScoreVO>('/score/my', { params: { studentId } })
}
