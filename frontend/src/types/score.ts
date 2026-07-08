import type { PageQuery } from './api'

export interface StageTaskVO {
  stageId: number
  stageName: string
  stageDesc: string
  startTime: string
  endTime: string
  deliverables: string
  scoringCriteria: string
  weight: number
  teacherId: number
  teacherName: string
  status: number
  statusText?: string
  createTime: string
  updateTime?: string
}

export interface StageTaskCreateDTO {
  stageName: string
  stageDesc: string
  startTime: string
  endTime: string
  deliverables: string
  scoringCriteria: string
  weight: number
  status?: number
}

export interface StageTaskUpdateDTO {
  stageName?: string
  stageDesc?: string
  startTime?: string
  endTime?: string
  deliverables?: string
  scoringCriteria?: string
  weight?: number
  status?: number
}

export interface StageTaskQuery extends PageQuery {
  teamId?: number
  teacherId?: number
  status?: number
  keyword?: string
}

export interface StageEvaluationVO {
  evaluationId: number
  stageId: number
  stageName?: string
  teamId: number
  teamName?: string
  relatedDocumentId?: number
  docScore: number
  completionScore: number
  innovationScore?: number
  techScore?: number
  totalScore: number
  comment: string
  result: number
  resultText?: string
  teacherId?: number
  teacherName?: string
  isLate: number
  lateDays?: number
  createTime: string
  updateTime?: string
}

export interface StageEvaluationSubmitDTO {
  stageId: number
  teamId: number
  relatedDocumentId?: number
  docScore: number
  completionScore: number
  innovationScore?: number
  techScore?: number
  comment: string
  result: number
  isLate?: number
  lateDays?: number
}

export interface ContributionVO {
  contributionId: number
  teamId: number
  teamName?: string
  stageId?: number
  stageName?: string
  studentId: number
  studentName?: string
  studentNo?: string
  evaluatorId: number
  evaluatorName?: string
  evaluationType: number
  evaluationTypeName?: string
  contributionScore: number
  workloadRatio: number
  workDescription: string
  comment?: string
  createTime: string
}

export interface ContributionUpdateDTO {
  teamId: number
  stageId?: number
  studentId: number
  evaluationType: number
  contributionScore: number
  workloadRatio: number
  workDescription: string
  comment?: string
}

export interface ContributionQuery extends PageQuery {
  teamId?: number
  studentId?: number
  evaluationType?: number
}

export interface ScoreVO {
  scoreId: number
  teamId: number
  teamName: string
  teacherId: number
  teacherName?: string
  aiReportId?: number
  docScore: number
  attendanceScore: number
  systemScore: number
  defenseScore: number
  totalScore: number
  teacherComment?: string
  status: number
  statusText?: string
  createTime: string
  updateTime?: string
}

export interface ScoreListVO extends ScoreVO {}

export interface ScoreQuery extends PageQuery {
  teamId?: number
  teacherId?: number
  status?: number
  keyword?: string
}

export interface StudentScoreVO {
  studentScoreId: number
  scoreId: number
  teamId: number
  teamName?: string
  studentId: number
  studentName?: string
  studentNo?: string
  contributionFactor: number
  personalScore: number
  grade?: string
  teacherComment?: string
  createTime?: string
}

export interface ScoreDetailVO extends ScoreVO {
  studentScores: StudentScoreVO[]
}

export interface ScoreSaveDTO {
  teamId: number
  aiReportId?: number
  docScore: number
  attendanceScore: number
  systemScore: number
  defenseScore: number
  teacherComment?: string
  studentScores?: StudentScoreSaveDTO[]
}

export interface StudentScoreSaveDTO {
  studentId: number
  contributionFactor?: number
  teacherComment?: string
}

export interface MyScoreVO {
  studentId: number
  studentName?: string
  studentNo?: string
  teamId?: number
  teamName?: string
  scoreId?: number
  totalScore?: number
  grade?: string
  status?: number
  statusText?: string
  stageEvaluations: StageEvaluationVO[]
  studentScores: StudentScoreVO[]
}

export interface ProgressVO {
  teamId: number
  teamName: string
  topicId?: number
  topicName?: string
  totalStageCount: number
  evaluatedStageCount: number
  averageStageScore?: number
}

export interface ProgressStageVO {
  stageId: number
  stageName: string
  weight: number
  status: number
  statusText?: string
  evaluated: boolean
  evaluation?: StageEvaluationVO
}
