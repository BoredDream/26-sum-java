import type { PageQuery } from './api'

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
  teacherName?: string
  teacherNo?: string
  status: number
  statusName?: string
  statusText?: string
  openStatus: number
  openStatusName?: string
  openStatusText?: string
  createTime?: string
  updateTime?: string
  files?: TopicFileVO[]
  reviews?: TopicReviewVO[]
}

export interface TopicCreateDTO {
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
  status: number
}

export interface TopicUpdateDTO {
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
  modifyReason?: string
}

/**
 * 题目审核请求参数
 * reviewResult 取值与后端 TopicReviewDTO 保持一致：
 * 2 = 审核通过，3 = 退回修改，4 = 不予通过
 */
export interface TopicReviewDTO {
  reviewResult: number
  reviewComment: string
}

export interface TopicFileVO {
  fileId: number
  topicId: number
  fileName: string
  fileType?: string
  fileSize?: number
  fileSizeText?: string
  fileDesc?: string
  uploadUserName?: string
  uploadTime?: string
}

export interface TopicFileUploadForm {
  file: File
  fileDesc?: string
}

export interface TopicReviewVO {
  reviewId: number
  topicId: number
  adminName?: string
  reviewResult: number
  reviewResultText?: string
  reviewComment?: string
  reviewTime?: string
}

export interface TopicQuery extends PageQuery {
  topicType?: string
  difficulty?: string
  status?: number
  openStatus?: number
  teacherId?: number
  startTime?: string
  endTime?: string
}
