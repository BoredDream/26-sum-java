import { get, post, put, patch, del, uploadFile } from '@/utils/request'
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
  RequestLeaveTeamDTO,
  LeaveRequestVO,
} from '@/types/selection'
import type { AuditDTO } from '@/types/api'

// 团队管理
export function createTeam(data: CreateTeamDTO) {
  return post<TeamVO>('/selection/teams', data)
}

export function disbandTeam(teamId: number) {
  return del<void>(`/selection/teams/${teamId}`)
}

export function listJoinableTeams() {
  return get<TeamVO[]>('/selection/teams')
}

export function getMyTeams() {
  return get<TeamVO[]>('/selection/teams/my')
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

// 离队申请
export function requestLeave(teamId: number, data: RequestLeaveTeamDTO) {
  return post<LeaveRequestVO>(`/selection/teams/${teamId}/leave-requests`, data)
}

export function listLeaveRequests(teamId: number) {
  return get<LeaveRequestVO[]>(`/selection/teams/${teamId}/leave-requests`)
}

export function auditLeaveRequest(requestId: number, data: AuditDTO) {
  return patch<LeaveRequestVO>(`/selection/teams/leave-requests/${requestId}/audit`, data)
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

export function getMySelections(teamId?: number) {
  return get<SelectionVO[]>('/selection/applications/my', { params: { teamId } })
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

export function listDocuments(teamId?: number) {
  return get<ProcessDocumentVO[]>('/selection/documents', { params: { teamId } })
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

export function listLogs(teamId?: number) {
  return get<DevelopmentLogVO[]>('/selection/logs', { params: { teamId } })
}

export function feedbackLog(logId: number, data: LogFeedbackDTO) {
  return patch<DevelopmentLogVO>(`/selection/logs/${logId}/feedback`, data)
}
