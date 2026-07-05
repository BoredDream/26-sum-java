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
  studentId: number
  memberRole: string
  workContent?: string
  joinTime?: string
}

export interface JoinRequestVO {
  id: number
  teamId: number
  applicantId: number
  applyMessage?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  reviewerId?: number
  reviewOpinion?: string
  applyTime: string
  reviewTime?: string
}

export interface TeamVO {
  id: number
  teamName: string
  leaderId: number
  introduction?: string
  status: 'BUILDING' | 'SELECTED'
  selectedTopicId?: number
  maxSize: number
  memberCount: number
  members: TeamMemberVO[]
  createTime: string
}

export interface TopicVO {
  id: number
  title: string
  description?: string
  direction?: string
  difficulty: string
  teacherId: number
  minMembers?: number
  maxMembers?: number
  status: 'OPEN' | 'SELECTED'
  selectionStart?: string
  selectionEnd?: string
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
  topicTitle: string
  selectionReason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  auditTeacherId?: number
  auditOpinion?: string
  applyTime: string
  auditTime?: string
}

export interface ProcessDocumentVO {
  id: number
  teamId: number
  topicId: number
  documentName: string
  documentType: string
  projectStage: string
  versionNo?: string
  originalFilename?: string
  uploaderId?: number
  status: 'SUBMITTED' | 'REVIEWED' | 'RETURNED'
  teacherFeedback?: string
  feedbackTeacherId?: number
  uploadTime?: string
  feedbackTime?: string
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
  id: number
  teamId: number
  studentId: number
  title: string
  logDate: string
  workContent: string
  completionStatus: string
  problemDescription?: string
  nextPlan?: string
  teacherFeedback?: string
  feedbackTeacherId?: number
  createTime: string
  updateTime?: string
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
