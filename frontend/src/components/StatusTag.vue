<template>
  <el-tag :type="config.type" :effect="config.effect" size="small">
    {{ config.label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Category =
  | 'topic'
  | 'topicOpen'
  | 'selection'
  | 'joinRequest'
  | 'attendanceTask'
  | 'attendanceSign'
  | 'makeup'
  | 'document'
  | 'log'
  | 'score'

const props = defineProps<{
  category: Category
  value: string | number
}>()

interface TagConfig {
  label: string
  type: 'success' | 'warning' | 'danger' | 'info' | 'primary'
  effect: 'light' | 'dark' | 'plain'
}

const configs: Record<Category, Record<string | number, TagConfig>> = {
  topic: {
    0: { label: '草稿', type: 'info', effect: 'plain' },
    1: { label: '待审核', type: 'warning', effect: 'plain' },
    2: { label: '通过', type: 'success', effect: 'dark' },
    3: { label: '退回', type: 'danger', effect: 'plain' },
    4: { label: '不通过', type: 'danger', effect: 'dark' },
  },
  topicOpen: {
    0: { label: '未开放', type: 'info', effect: 'plain' },
    1: { label: '已开放', type: 'success', effect: 'dark' },
    2: { label: '已关闭', type: 'danger', effect: 'plain' },
  },
  selection: {
    PENDING: { label: '待审核', type: 'warning', effect: 'plain' },
    APPROVED: { label: '通过', type: 'success', effect: 'dark' },
    REJECTED: { label: '驳回', type: 'danger', effect: 'dark' },
    WITHDRAWN: { label: '已撤回', type: 'info', effect: 'plain' },
  },
  joinRequest: {
    0: { label: '待审核', type: 'warning', effect: 'plain' },
    1: { label: '通过', type: 'success', effect: 'dark' },
    2: { label: '驳回', type: 'danger', effect: 'dark' },
  },
  attendanceTask: {
    0: { label: '未开始', type: 'info', effect: 'plain' },
    1: { label: '进行中', type: 'warning', effect: 'plain' },
    2: { label: '已结束', type: 'success', effect: 'dark' },
  },
  attendanceSign: {
    0: { label: '缺勤', type: 'danger', effect: 'dark' },
    1: { label: '正常', type: 'success', effect: 'dark' },
    2: { label: '迟到', type: 'warning', effect: 'dark' },
    3: { label: '补签', type: 'info', effect: 'dark' },
  },
  makeup: {
    0: { label: '待审核', type: 'warning', effect: 'plain' },
    1: { label: '通过', type: 'success', effect: 'dark' },
    2: { label: '驳回', type: 'danger', effect: 'dark' },
  },
  document: {
    0: { label: '待审核', type: 'warning', effect: 'plain' },
    1: { label: '通过', type: 'success', effect: 'dark' },
    2: { label: '需修改', type: 'danger', effect: 'plain' },
  },
  log: {
    0: { label: '待审核', type: 'warning', effect: 'plain' },
    1: { label: '通过', type: 'success', effect: 'dark' },
    2: { label: '需修改', type: 'danger', effect: 'plain' },
  },
  score: {
    0: { label: '草稿', type: 'info', effect: 'plain' },
    1: { label: '已确认', type: 'success', effect: 'dark' },
    2: { label: '已锁定', type: 'danger', effect: 'dark' },
  },
}

const config = computed(() => {
  const map = configs[props.category]
  return (
    map?.[props.value] || {
      label: String(props.value),
      type: 'info',
      effect: 'plain',
    }
  )
})
</script>
