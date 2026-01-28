<template>
  <div class="users">
    <h1 class="page-title">用户管理</h1>
    
    <el-card class="table-card">
      <div class="table-header">
        <div class="search-filters">
          <el-input
            v-model="searchName"
            placeholder="按名称筛选"
            class="search-input"
            clearable
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="searchRole"
            placeholder="按角色筛选"
            class="role-select"
            clearable
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option label="全部" value="" />
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
      
      <el-table :data="users" style="width: 100%" v-loading="loading" :row-class-name="getRowClassName">
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="250" align="center">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :size="50" :src="getAvatarUrl(row.avatar)">
                {{ row.name?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ row.name }}</div>
                <div class="user-account">账号：{{ row.account }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="large">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateNumber" label="证件号" min-width="180" align="center" />
        <el-table-column prop="password" label="密码" min-width="150" align="center">
          <template #default="{ row }">
            {{ row.password ? '******' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="账号" prop="account" v-if="isAdd">
          <el-input v-model="form.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="isAdd">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="角色" prop="role" v-if="isAdd">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="getAvatarUrl(form.avatar)">
              {{ form.name?.charAt(0) || 'U' }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/api/file/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
              name="file"
            >
              <el-button type="primary" size="small">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, deleteUser, register, updateUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const users = ref([])
const searchName = ref('')
const searchRole = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const isAdd = ref(true)
const formRef = ref(null)
const submitting = ref(false)

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

const form = ref({
  account: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: 'USER',
  avatar: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (isAdd.value && value !== form.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { 
      validator: (rule, value, callback) => {
        if (isAdd.value && !value) {
          callback(new Error('请输入密码'))
        } else if (value && (value.length < 6 || value.length > 20)) {
          callback(new Error('密码长度在6-20个字符'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { 
      validator: (rule, value, callback) => {
        if (isAdd.value && !value) {
          callback(new Error('请再次输入密码'))
        } else if (form.value.password && value !== form.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 20, message: '名称长度在2-20个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const getRowClassName = ({ row }) => {
  if (row.role === 'ADMIN') {
    return 'admin-row'
  }
  return ''
}

const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'MERCHANT': 'warning',
    'USER': 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role) => {
  const textMap = {
    'ADMIN': '管理员',
    'MERCHANT': '商家',
    'USER': '普通用户'
  }
  return textMap[role] || role
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  return avatar.startsWith('http') ? avatar : `http://localhost:8080${avatar}`
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value,
      name: searchName.value,
      role: searchRole.value
    })
    if (res.code === 200) {
      users.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

const handleAdd = () => {
  dialogTitle.value = '添加用户'
  isAdd.value = true
  form.value = {
    account: '',
    password: '',
    confirmPassword: '',
    name: '',
    role: 'USER',
    avatar: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  isAdd.value = false
  form.value = {
    id: row.id,
    name: row.name,
    password: '',
    avatar: row.avatar
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    const avatarUrl = response.data.startsWith('http') ? response.data : `http://localhost:8080${response.data}`
    form.value.avatar = avatarUrl
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isAdd.value) {
        await register({
          account: form.value.account,
          password: form.value.password,
          confirmPassword: form.value.confirmPassword,
          name: form.value.name,
          role: form.value.role
        })
        ElMessage.success('添加成功')
      } else {
        const updateData = {
          name: form.value.name,
          avatar: form.value.avatar
        }
        if (form.value.password) {
          updateData.password = form.value.password
        }
        await updateUser(form.value.id, updateData)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchUsers()
    } catch (error) {
      ElMessage.error(isAdd.value ? '添加失败' : '更新失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 14px;
  flex-wrap: wrap;
}

.search-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
  max-width: 100%;
}

.role-select {
  width: 180px;
  max-width: 100%;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-account {
  font-size: 12px;
  color: #909399;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: inline-block;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-table__row.admin-row) {
  background-color: #f0f0f0;
}

:deep(.el-table__row.admin-row:hover) {
  background-color: #e0e0e0;
}

:deep(.el-tag) {
  font-weight: 500;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
