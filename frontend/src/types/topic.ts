import type { PageQuery } from './api'

export interface TopicListVO {
  topicId: number
  topicName: string
  topicType: string
  difficulty: string
  teacherId: number
  teacherName?: string
  studentLimit: number
  teamLimit?: number
  status: number
  statusText?: string
  openStatus: number
  openStatusText?: string
  createTime?: string
  updateTime?: string
}

export interface TopicDetailVO extends TopicListVO {
  teacherNo?: string
  topicContent: string
  developTools?: string
  technicalRoute: string
  selectionStartTime?: string
  selectionEndTime?: string
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
 * 1 = 通过，2 = 退回修改，3 = 不予通过
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
export interface TopicAiSuggestionDTO {
  topicName?: string
  topicType?: string
  difficulty?: string
  studentLimit?: number
  teamLimit?: number
  topicContent?: string
  developTools?: string
  technicalRoute?: string
}

export interface TopicAiSuggestionVO {
  suggestion: string
}
