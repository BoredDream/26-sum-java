import { get, post, put, patch, uploadFile } from '@/utils/request'
import type {
  CreateTeamDTO,
  TeamVO,
  JoinTeamDTO,
  JoinRequestVO,
  UpdateMemberWorkDTO,
  SelectableTopicVO,
  SubmitSelectionDTO,
  SelectionVO,
  ProcessDocumentVO,
  DocumentFeedbackDTO,
  DevelopmentLogVO,
  CreateDevelopmentLogDTO,
  LogFeedbackDTO,
} from '@/types/selection'
import type { AuditDTO } from '@/types/api'

// 团队管理
export function createTeam(data: CreateTeamDTO) {
  return post<TeamVO>('/selection/teams', data)
}

export function listJoinableTeams() {
  return get<TeamVO[]>('/selection/teams')
}

export function getMyTeam() {
  return get<TeamVO>('/selection/teams/my')
}

export function getTeam(teamId: number) {
  return get<TeamVO>(`/selection/teams/${teamId}`)
}

export function joinTeam(teamId: number, data: JoinTeamDTO) {
  return post<JoinRequestVO>(`/selection/teams/${teamId}/join-requests`, data)
}

export function listJoinRequests(teamId: number) {
  return get<JoinRequestVO[]>(`/selection/teams/${teamId}/join-requests`)
}

export function auditJoinRequest(requestId: number, data: AuditDTO) {
  return patch<JoinRequestVO>(`/selection/teams/join-requests/${requestId}/audit`, data)
}

export function updateMemberWork(teamId: number, studentId: number, data: UpdateMemberWorkDTO) {
  return put<void>(`/selection/teams/${teamId}/members/${studentId}/work-content`, data)
}

// 选题申请
export function listSelectableTopics(keyword?: string) {
  return get<SelectableTopicVO[]>('/selection/topics', { params: { keyword } })
}

export function getSelectableTopic(topicId: number) {
  return get<SelectableTopicVO>(`/selection/topics/${topicId}`)
}

export function submitSelection(data: SubmitSelectionDTO) {
  return post<SelectionVO>('/selection/applications', data)
}

export function getMySelection() {
  return get<SelectionVO[]>('/selection/applications/my')
}

export function listPendingSelections() {
  return get<SelectionVO[]>('/selection/applications/pending')
}

export function auditSelection(selectionId: number, data: AuditDTO) {
  return patch<SelectionVO>(`/selection/applications/${selectionId}/audit`, data)
}

// 过程文档
export function uploadDocument(formData: FormData) {
  return uploadFile<ProcessDocumentVO>('/selection/documents', formData)
}

export function listDocuments() {
  return get<ProcessDocumentVO[]>('/selection/documents')
}

export function feedbackDocument(documentId: number, data: DocumentFeedbackDTO) {
  return patch<ProcessDocumentVO>(`/selection/documents/${documentId}/feedback`, data)
}

export function getDocumentDownloadUrl(documentId: number) {
  return `/selection/documents/${documentId}/download`
}

// 开发日志
export function createLog(data: CreateDevelopmentLogDTO) {
  return post<DevelopmentLogVO>('/selection/logs', data)
}

export function listLogs() {
  return get<DevelopmentLogVO[]>('/selection/logs')
}

export function feedbackLog(logId: number, data: LogFeedbackDTO) {
  return patch<DevelopmentLogVO>(`/selection/logs/${logId}/feedback`, data)
}
