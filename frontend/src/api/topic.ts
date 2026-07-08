import { get, post, put, del, uploadFile } from '@/utils/request'
import type {
  TopicListVO,
  TopicDetailVO,
  TopicCreateDTO,
  TopicUpdateDTO,
  TopicReviewDTO,
  TopicFileVO,
  TopicReviewVO,
  TopicQuery,
  TopicAiSuggestionDTO,
  TopicAiSuggestionVO,
} from '@/types/topic'
import type { PageResult } from '@/types/api'

// 题目 CRUD
export function createTopic(data: TopicCreateDTO) {
  return post<void>('/topic', data)
}

export function updateTopic(topicId: number, data: TopicUpdateDTO) {
  return put<void>(`/topic/${topicId}`, data)
}

export function deleteTopic(topicId: number) {
  return del<void>(`/topic/${topicId}`)
}

export function getTopicDetail(topicId: number) {
  return get<TopicDetailVO>(`/topic/${topicId}`)
}

export function queryTopicPage(query: TopicQuery) {
  return get<PageResult<TopicListVO>>('/topic/page', { params: query })
}

// 流程操作
export function submitForReview(topicId: number) {
  return post<void>(`/topic/${topicId}/submit`)
}

export function reviewTopic(topicId: number, data: TopicReviewDTO) {
  return post<void>(`/topic/${topicId}/review`, data)
}

export function openTopic(topicId: number) {
  return post<void>(`/topic/${topicId}/open`)
}

export function closeTopic(topicId: number) {
  return post<void>(`/topic/${topicId}/close`)
}

// 题目资料文件
export function uploadTopicFile(topicId: number, formData: FormData) {
  return uploadFile<void>(`/topic/${topicId}/file`, formData)
}

export function deleteTopicFile(fileId: number) {
  return del<void>(`/topic/file/${fileId}`)
}

export function listTopicFiles(topicId: number) {
  return get<TopicFileVO[]>(`/topic/${topicId}/file`)
}

export function getTopicFileDownloadUrl(fileId: number) {
  return `/topic/file/${fileId}/download`
}

// 审核记录
export function listTopicReviews(topicId: number) {
  return get<TopicReviewVO[]>(`/topic/${topicId}/review`)
}
// AI 出题建议
export function suggestTopic(data: TopicAiSuggestionDTO) {
  return post<TopicAiSuggestionVO>('/topic/ai/suggestion', data, { timeout: 45000 })
}
