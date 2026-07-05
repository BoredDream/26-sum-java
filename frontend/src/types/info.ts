import type { PageQuery } from './api'

export interface NoticeVO {
  noticeId: number
  title: string
  content: string
  type: string
  attachPath?: string
  topFlag: number
  publisherId: number
  publisherName?: string
  createTime: string
  updateTime?: string
}

export interface NoticeCreateDTO {
  title: string
  content: string
  type: string
  topFlag?: number
}

export interface NoticeUpdateDTO {
  title?: string
  content?: string
  type?: string
  topFlag?: number
}

export interface NoticeQuery extends PageQuery {
  type?: string
}

export interface StudentVO {
  studentId: number
  studentNo: string
  studentName: string
  major: string
  className: string
  phone?: string
  email?: string
  status: number
  statusText?: string
  createTime: string
}

export interface StudentCreateDTO {
  studentNo: string
  studentName: string
  major: string
  className: string
  phone?: string
  email?: string
  password?: string
}

export interface StudentUpdateDTO {
  studentNo: string
  studentName: string
  major: string
  className: string
  phone?: string
  email?: string
}

export type StudentImportResult = string

export interface TeacherVO {
  teacherId: number
  teacherNo: string
  teacherName: string
  office?: string
  title?: string
  phone?: string
  email?: string
  role: string
  roleText?: string
  createTime: string
}

export interface TeacherCreateDTO {
  teacherNo: string
  teacherName: string
  office?: string
  title?: string
  phone?: string
  email?: string
  password?: string
}

export interface TeacherUpdateDTO {
  teacherNo: string
  teacherName: string
  office?: string
  title?: string
  phone?: string
  email?: string
}

export interface BackupVO {
  backupId: number
  backupTime: string
  filePath: string
  fileSize?: string
  operatorId?: number
  operatorName?: string
}

export interface BackupCreateDTO {
  description?: string
}

export interface OperateLogVO {
  logId: number
  operateUserId?: number
  operateUserName?: string
  operateType: string
  operateContent?: string
  operateTime: string
}

export interface OperateLogQuery extends PageQuery {
  operateType?: string
}

export interface DashboardStatisticsVO {
  studentCount?: number
  teacherCount?: number
  noticeCount?: number
  backupCount?: number
  logCount?: number
  topicCount?: number
  teamCount?: number
}

export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
}
