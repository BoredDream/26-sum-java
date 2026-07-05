<template>
  <div class="student-manage-page">
    <page-header title="学生管理">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索学号/姓名"
          clearable
          style="width: 220px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filterStatus"
          placeholder="账号状态"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button :icon="Search" @click="handleSearch" />
        <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        <el-upload
          ref="uploadRef"
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleImport"
          style="display: inline-block; margin: 0 12px"
        >
          <el-button type="warning" :icon="Upload" :loading="importing">导入</el-button>
        </el-upload>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增学生</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && students.length === 0 && !error" description="暂无学生" />

    <el-table v-loading="loading" :data="students" border class="data-table">
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="major" label="专业" width="140" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag v-if="(scope.row as StudentVO).status === 1" type="success" size="small"
            >正常</el-tag
          >
          <el-tag v-else type="danger" size="small">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="scope">{{
          formatDateTime((scope.row as StudentVO).createTime)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button type="primary" text size="small" @click="openEdit(scope.row as StudentVO)"
            >编辑</el-button
          >
          <el-button
            type="warning"
            text
            size="small"
            :loading="actionId === (scope.row as StudentVO).studentId"
            @click="handleToggleStatus(scope.row as StudentVO)"
          >
            {{ (scope.row as StudentVO).status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="info" text size="small" @click="openPassword(scope.row as StudentVO)"
            >重置密码</el-button
          >
          <el-button type="danger" text size="small" @click="handleDelete(scope.row as StudentVO)"
            >删除</el-button
          >
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
        @size-change="loadStudents"
        @current-change="loadStudents"
      />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑学生' : '新增学生'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="form.studentName" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="form.className" />
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
        确认重置学生 <strong>{{ currentRow?.studentName }}</strong> 的密码？
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
import type { FormInstance, FormRules, UploadFile, UploadInstance } from 'element-plus'
import { Plus, Search, Download, Upload } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { StudentVO, StudentCreateDTO, StudentUpdateDTO } from '@/types/info'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const students = ref<StudentVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const actionId = ref(0)

async function loadStudents() {
  loading.value = true
  error.value = ''
  try {
    const res = await infoApi.queryStudentPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: filterStatus.value,
    })
    students.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载学生列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadStudents()
}

function handleExport() {
  infoApi.exportStudents()
}

const importing = ref(false)
const uploadRef = ref<UploadInstance>()
async function handleImport(file: UploadFile) {
  if (!file.raw) return
  importing.value = true
  try {
    const res = await infoApi.importStudents(file.raw)
    ElMessage.success(res.message)
    loadStudents()
  } catch (err: any) {
    ElMessage.error(err?.message || '导入失败')
  } finally {
    importing.value = false
    uploadRef.value?.clearFiles()
  }
}

// 表单
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref<StudentCreateDTO>({
  studentNo: '',
  studentName: '',
  major: '',
  className: '',
  phone: '',
  email: '',
  password: '',
})

const rules: FormRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
}

function resetForm() {
  form.value = {
    studentNo: '',
    studentName: '',
    major: '',
    className: '',
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

function openEdit(row: StudentVO) {
  isEdit.value = true
  currentId.value = row.studentId
  form.value = {
    studentNo: row.studentNo,
    studentName: row.studentName,
    major: row.major,
    className: row.className,
    phone: row.phone || '',
    email: row.email || '',
  }
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        const dto: StudentUpdateDTO = { ...form.value }
        await infoApi.updateStudent(currentId.value, dto)
      } else {
        await infoApi.createStudent(form.value)
      }
      ElMessage.success('保存成功')
      formVisible.value = false
      loadStudents()
    } catch (err: any) {
      ElMessage.error(err?.message || '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

// 状态切换
async function handleToggleStatus(row: StudentVO) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该学生账号？`, '操作确认', { type: 'warning' })
  } catch {
    return
  }
  actionId.value = row.studentId
  try {
    await infoApi.toggleStudentStatus(row.studentId)
    ElMessage.success(`${action}成功`)
    loadStudents()
  } catch (err: any) {
    ElMessage.error(err?.message || `${action}失败`)
  } finally {
    actionId.value = 0
  }
}

// 删除
async function handleDelete(row: StudentVO) {
  try {
    await ElMessageBox.confirm('确认删除该学生？删除后不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await infoApi.deleteStudent(row.studentId)
    ElMessage.success('删除成功')
    loadStudents()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

// 重置密码
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const currentRow = ref<StudentVO | null>(null)

function openPassword(row: StudentVO) {
  currentRow.value = row
  pwdVisible.value = true
}

async function handleResetPassword() {
  if (!currentRow.value) return
  pwdLoading.value = true
  try {
    await infoApi.resetStudentPassword(currentRow.value.studentId)
    ElMessage.success('密码重置成功')
    pwdVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.message || '密码重置失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(loadStudents)
</script>

<style scoped lang="scss">
.student-manage-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .data-table {
    margin-top: 8px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .tip-text {
    margin-top: 8px;
    font-size: 13px;
    color: #f56c6c;
  }
}
</style>
