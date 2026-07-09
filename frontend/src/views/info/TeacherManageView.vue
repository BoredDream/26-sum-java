<template>
  <div class="teacher-manage-page">
    <page-header title="教师管理">
      <template #extra>
        <div class="toolbar-group">
          <el-input
            v-model="keyword"
            placeholder="搜索工号/姓名"
            clearable
            style="width: 200px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <div class="toolbar-group">
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
          <el-button type="primary" :icon="Plus" @click="openCreate">新增教师</el-button>
        </div>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && teachers.length === 0 && !error" description="暂无教师" />

    <el-table v-loading="loading" :data="teachers" border class="data-table">
      <el-table-column prop="teacherNo" label="工号" width="120" />
      <el-table-column prop="teacherName" label="姓名" width="120" />
      <el-table-column prop="office" label="教研室" width="140" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
      <el-table-column label="角色" width="120">
        <template #default="scope">
          <el-tag
            :type="(scope.row as TeacherVO).role === 'ADMIN' ? 'danger' : 'primary'"
            size="small"
          >
            {{ (scope.row as TeacherVO).roleText || (scope.row as TeacherVO).role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as TeacherVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <span class="action-btns">
            <el-button type="primary" link size="small" @click="openEdit(scope.row as TeacherVO)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row as TeacherVO)">删除</el-button>
            <el-button type="warning" link size="small" :loading="actionId === (scope.row as TeacherVO).teacherId" @click="handleToggleRole(scope.row as TeacherVO)">切换角色</el-button>
            <el-button type="info" link size="small" @click="openPassword(scope.row as TeacherVO)">重置密码</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadTeachers"
        @current-change="loadTeachers"
      />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑教师' : '新增教师'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="teacherName">
          <el-input v-model="form.teacherName" />
        </el-form-item>
        <el-form-item label="教研室">
          <el-input v-model="form.office" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="不填则使用默认密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <p>
        确认重置教师 <strong>{{ currentRow?.teacherName }}</strong> 的密码？
      </p>
      <p class="tip-text">重置后将恢复为系统默认密码。此操作不可撤销。</p>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleResetPassword"
          >确认重置</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Search, Download } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { TeacherVO, TeacherCreateDTO, TeacherUpdateDTO } from '@/types/info'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const teachers = ref<TeacherVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const actionId = ref(0)

async function loadTeachers() {
  loading.value = true
  error.value = ''
  try {
    const res = await infoApi.queryTeacherPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: 1,
    })
    teachers.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载教师列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadTeachers()
}

function handleExport() {
  infoApi.exportTeachers()
}

// 表单
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref<TeacherCreateDTO>({
  teacherNo: '',
  teacherName: '',
  office: '',
  title: '',
  phone: '',
  email: '',
  password: '',
})

const rules: FormRules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

function resetForm() {
  form.value = {
    teacherNo: '',
    teacherName: '',
    office: '',
    title: '',
    phone: '',
    email: '',
    password: '',
  }
}

function openCreate() {
  isEdit.value = false
  currentId.value = 0
  resetForm()
  formVisible.value = true
}

function openEdit(row: TeacherVO) {
  isEdit.value = true
  currentId.value = row.teacherId
  form.value = {
    teacherNo: row.teacherNo,
    teacherName: row.teacherName,
    office: row.office || '',
    title: row.title || '',
    phone: row.phone || '',
    email: row.email || '',
    password: '',
  }
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { password, ...dto } = form.value
      await infoApi.updateTeacher(currentId.value, dto as TeacherUpdateDTO)
    } else {
      const dto: TeacherCreateDTO = { ...form.value }
      if (dto.password === '') {
        dto.password = undefined
      }
      await infoApi.createTeacher(dto)
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    loadTeachers()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

// 切换角色
async function handleToggleRole(row: TeacherVO) {
  try {
    await ElMessageBox.confirm('确认切换该教师角色？', '操作确认', { type: 'warning' })
  } catch {
    return
  }
  actionId.value = row.teacherId
  try {
    await infoApi.toggleTeacherRole(row.teacherId)
    ElMessage.success('角色切换成功')
    loadTeachers()
  } catch (err: any) {
    ElMessage.error(err?.message || '角色切换失败')
  } finally {
    actionId.value = 0
  }
}

// 删除
async function handleDelete(row: TeacherVO) {
  try {
    await ElMessageBox.confirm('确认删除该教师？删除后不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await infoApi.deleteTeacher(row.teacherId)
    ElMessage.success('删除成功')
    loadTeachers()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

// 重置密码
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const currentRow = ref<TeacherVO | null>(null)

function openPassword(row: TeacherVO) {
  currentRow.value = row
  pwdVisible.value = true
}

async function handleResetPassword() {
  if (!currentRow.value) return
  pwdLoading.value = true
  try {
    await infoApi.resetTeacherPassword(currentRow.value.teacherId)
    ElMessage.success('密码重置成功')
    pwdVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.message || '密码重置失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(loadTeachers)
</script>

<style scoped lang="scss">
.teacher-manage-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .toolbar-group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .data-table {
    margin-top: 8px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .action-btns {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .tip-text {
    margin-top: 8px;
    font-size: 13px;
    color: #f56c6c;
  }
}
</style>
