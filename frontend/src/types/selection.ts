import type { AuditDTO } from './api'

export interface CreateTeamDTO {
  teamName: string
  introduction?: string
  maxSize?: number
}

export interface JoinTeamDTO {
  applyMessage?: string
}

export interface UpdateMemberWorkDTO {
  workContent: string
}

export interface TeamMemberVO {
  memberId: number
  studentId: number
  studentName: string
  studentNo: string
  memberRole: number
  memberRoleName: string
  workContent?: string
  status: number
}

export interface JoinRequestVO {
  requestId: number
  teamId: number
  applicantId: number
  applicantName: string
  applicantNo: string
  applyMessage?: string
  auditStatus: number
  auditStatusName: string
  createTime: string
}

export interface TeamVO {
  id?: number
  teamId?: number
  teamName: string
  leaderId: number
  leaderName: string
  introduction?: string
  teamStatus: number
  teamStatusName: string
  members: TeamMemberVO[]
  createTime: string
}

export interface TopicVO {
  topicId: number
  topicName: string
  topicType: string
  difficulty: string
  studentLimit: number
  teamLimit?: number
  topicContent: string
  developTools?: string
  technicalRoute: string
  selectionStartTime?: string
  selectionEndTime?: string
  teacherId: number
  teacherName: string
  status: number
  openStatus: number
  statusName: string
  openStatusName: string
}

export interface SubmitSelectionDTO {
  topicId: number
  selectionReason: string
}

export interface SelectionVO {
  selectionId: number
  teamId: number
  teamName: string
  topicId: number
  topicName: string
  selectionReason: string
  auditStatus: string
  auditStatusName: string
  auditOpinion?: string
  auditTeacherName?: string
  auditTime?: string
  createTime: string
}

export interface ProcessDocumentVO {
  documentId: number
  documentName: string
  documentType: string
  projectStage: string
  versionNo?: string
  filePath?: string
  fileName?: string
  auditStatus: number
  auditStatusName: string
  feedback?: string
  returned: boolean
  teamId: number
  teamName: string
  createTime: string
  updateTime: string
}

export interface UploadDocumentForm {
  documentName: string
  documentType: string
  projectStage: string
  versionNo: string
  file: File
}

export interface DocumentFeedbackDTO {
  feedback: string
  returned: boolean
}

export interface DevelopmentLogVO {
  logId: number
  title: string
  logDate: string
  workContent: string
  completionStatus: string
  completionStatusName: string
  problemDescription?: string
  nextPlan?: string
  feedback?: string
  studentId: number
  studentName: string
  teamId: number
  teamName: string
  createTime: string
}

export interface CreateDevelopmentLogDTO {
  title: string
  logDate: string
  workContent: string
  completionStatus: string
  problemDescription?: string
  nextPlan?: string
}

export interface LogFeedbackDTO {
  feedback: string
}

export { AuditDTO }
