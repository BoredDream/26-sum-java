import { get, post, downloadFile } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  StageTaskVO,
  StageTaskCreateDTO,
  StageTaskQuery,
  StageEvaluationSubmitDTO,
  ContributionUpdateDTO,
  ScoreListVO,
  ScoreQuery,
  StudentScoreVO,
  ScoreSaveDTO,
  ProgressVO,
} from '@/types/score'

// 阶段任务
export function createStageTask(data: StageTaskCreateDTO) {
  return post<void>('/score/stage-task', data)
}

export function queryStageTaskPage(query: StageTaskQuery) {
  return get<PageResult<StageTaskVO>>('/score/stage-task/page', { params: query })
}

// 过程评价
export function submitStageEvaluation(data: StageEvaluationSubmitDTO) {
  return post<void>('/score/stage-evaluation', data)
}

// 贡献度
export function recordContribution(data: ContributionUpdateDTO) {
  return post<void>('/score/contribution', data)
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

export function confirmScore(scoreId: number) {
  return post<void>(`/score/${scoreId}/confirm`)
}

export function exportScores() {
  return downloadFile('/score/export')
}

// 学生成绩
export function queryStudentScore(studentId: number) {
  return get<StudentScoreVO[]>(`/score/student/${studentId}`)
}
