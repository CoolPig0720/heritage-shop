<template>
  <div class="heritage">
    <h1 class="page-title">非遗文化管理</h1>
    
    <el-card class="table-card">
      <div class="table-header">
        <div class="search-filters">
          <el-select
            v-model="searchCategoryId"
            placeholder="类型"
            class="type-select"
            clearable
            filterable
            :loading="categoryLoading"
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>

          <el-input
            v-model="searchKeyword"
            placeholder="关键词：项目名称"
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>

        <div class="actions">
          <el-button @click="openInheritorDrawer">
            传承人管理
          </el-button>
          <el-button @click="openCategoryDialog">
            <el-icon><Plus /></el-icon>
            添加类型
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加项目
          </el-button>
        </div>
      </div>
      
      <el-table v-loading="loading" :data="projects" style="width: 100%">
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="类型" width="180" show-overflow-tooltip />
        <el-table-column label="项目名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goProjectDetail(row.id)">
              {{ row.name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="传承人" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="Array.isArray(row.inheritors) && row.inheritors.length > 0">
              <span v-for="(p, idx) in row.inheritors" :key="p.id">
                <el-link type="primary" :underline="false" @click="goInheritorDetail(p.id)">
                  {{ p.name }}
                </el-link>
                <span v-if="idx < row.inheritors.length - 1">、</span>
              </span>
            </template>
            <span v-else>暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyUnit" label="申报单位" min-width="200" show-overflow-tooltip />
        <el-table-column prop="protectUnit" label="保护单位" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @closed="resetDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="类型" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择类型" filterable style="width: 100%">
            <el-option v-for="opt in flatCategoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="介绍" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="申报单位" prop="applyUnit">
          <el-input v-model="form.applyUnit" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="保护单位" prop="protectUnit">
          <el-input v-model="form.protectUnit" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="传承人">
          <el-select
            v-model="form.inheritorIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="搜索并选择传承人"
            :remote-method="searchInheritors"
            :loading="inheritorLoading"
            style="width: 100%"
          >
            <el-option v-for="opt in inheritorOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" title="添加类型" width="520px" @closed="resetCategoryDialog">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="90px">
        <el-form-item label="上级类型" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="可选" clearable filterable style="width: 100%">
            <el-option v-for="opt in flatCategoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySaving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="inheritorDrawerVisible" title="传承人管理" size="75%" :with-header="true">
      <div class="drawer-body">
        <div class="drawer-toolbar">
          <el-input
            v-model="inheritorKeyword"
            placeholder="关键词：姓名"
            class="drawer-search"
            clearable
            @clear="handleInheritorSearch"
            @keyup.enter="handleInheritorSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="openInheritorAdd">
            <el-icon><Plus /></el-icon>
            添加传承人
          </el-button>
        </div>

        <el-table v-loading="inheritorListLoading" :data="inheritorList" style="width: 100%">
          <el-table-column label="序号" width="80" align="center">
            <template #default="{ $index }">
              {{ (inheritorPage - 1) * inheritorPageSize + $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="140" show-overflow-tooltip />
          <el-table-column prop="gender" label="性别" width="90" />
          <el-table-column prop="nation" label="民族" width="120" show-overflow-tooltip />
          <el-table-column prop="birthDate" label="出生日期" width="120" />
          <el-table-column label="照片" width="110" align="center">
            <template #default="{ row }">
              <el-image v-if="row.photo" :src="normalizeUrl(row.photo)" fit="cover" style="width: 56px; height: 56px; border-radius: 6px" />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="简介" min-width="240" show-overflow-tooltip />
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openInheritorEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleInheritorDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="inheritorPage"
            v-model:page-size="inheritorPageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="inheritorTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleInheritorSizeChange"
            @current-change="handleInheritorCurrentChange"
          />
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="inheritorDialogVisible" :title="inheritorDialogTitle" width="720px" @closed="resetInheritorDialog">
      <el-form ref="inheritorFormRef" :model="inheritorForm" :rules="inheritorRules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="inheritorForm.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="性别">
          <el-input v-model="inheritorForm.gender" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="inheritorForm.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="民族">
          <el-input v-model="inheritorForm.nation" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="照片">
          <div class="upload-row">
            <el-upload
              :action="uploadUrl"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUploadInheritorPhoto"
              :on-success="handleInheritorPhotoSuccess"
            >
              <el-button size="small" type="primary">上传照片</el-button>
            </el-upload>

            <div v-if="inheritorForm.photo" class="upload-preview">
              <el-image
                :src="normalizeUrl(inheritorForm.photo)"
                fit="cover"
                style="width: 56px; height: 56px; border-radius: 6px"
              />
              <el-button size="small" @click="clearInheritorPhoto">删除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="inheritorForm.description" type="textarea" :rows="6" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inheritorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="inheritorSaving" @click="saveInheritor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import {
  createAdminHeritageCategory,
  createAdminHeritageProject,
  createAdminHeritageInheritor,
  deleteAdminHeritageInheritor,
  deleteAdminHeritageProject,
  getAdminHeritageCategoryTree,
  getAdminHeritageInheritorDetail,
  getAdminHeritageProjectDetail,
  pageAdminHeritageInheritors,
  pageAdminHeritageProjects,
  updateAdminHeritageInheritor,
  updateAdminHeritageProject,
  updateAdminHeritageProjectInheritors
} from '@/api/heritage'

const userStore = useUserStore()
const router = useRouter()

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const categoryLoading = ref(false)
const categoryTree = ref([])
const searchCategoryId = ref(null)

const projects = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({
  id: null,
  categoryId: null,
  name: '',
  description: '',
  applyUnit: '',
  protectUnit: '',
  inheritorIds: []
})

const rules = {
  categoryId: [{ required: true, message: '请选择类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入介绍', trigger: 'blur' }],
  applyUnit: [{ required: true, message: '请输入申报单位', trigger: 'blur' }],
  protectUnit: [{ required: true, message: '请输入保护单位', trigger: 'blur' }]
}

const flattenCategories = (nodes, depth = 0, out = []) => {
  const list = Array.isArray(nodes) ? nodes : []
  list.forEach((n) => {
    const name = (n?.name ?? '').toString()
    const prefix = depth > 0 ? `${'—'.repeat(depth)} ` : ''
    out.push({ value: n?.id, label: `${prefix}${name}` })
    if (Array.isArray(n?.children) && n.children.length > 0) {
      flattenCategories(n.children, depth + 1, out)
    }
  })
  return out
}

const categoryOptions = computed(() => flattenCategories(categoryTree.value))
const flatCategoryOptions = computed(() =>
  categoryOptions.value.map((x) => ({ value: x.value, label: x.label.replace(/^—+\s/, '') }))
)

const dialogTitle = computed(() => (isEdit.value ? '编辑项目' : '添加项目'))

const inheritorLoading = ref(false)
const inheritorOptions = ref([])

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const uploadUrl = 'http://localhost:8080/api/file/upload'
const uploadHeaders = computed(() => {
  const token = userStore.token || localStorage.getItem('token')
  if (!token) return {}
  return { Authorization: `Bearer ${token}` }
})

const goProjectDetail = (projectId) => {
  if (!projectId) return
  router.push({ path: `/heritage/projects/${projectId}`, query: { from: 'manage' } })
}

const goInheritorDetail = (inheritorId) => {
  if (!inheritorId) return
  router.push({ path: `/heritage/inheritors/${inheritorId}`, query: { from: 'manage' } })
}

const beforeUploadInheritorPhoto = (file) => {
  if (!file?.type?.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const maxSizeMb = 5
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const handleInheritorPhotoSuccess = (response) => {
  if (response?.code === 200) {
    inheritorForm.value.photo = response.data || ''
    ElMessage.success('上传成功')
    return
  }
  ElMessage.error(response?.message || '上传失败')
}

const clearInheritorPhoto = () => {
  inheritorForm.value.photo = ''
}

const mergeInheritorOptions = (base, extra) => {
  const map = new Map()
  const out = []
  const add = (x) => {
    if (!x || x.value == null) return
    if (map.has(x.value)) return
    map.set(x.value, true)
    out.push(x)
  }
  ;(Array.isArray(base) ? base : []).forEach(add)
  ;(Array.isArray(extra) ? extra : []).forEach(add)
  return out
}

const upsertOptionsFromInheritors = (inheritors) => {
  const opts = (Array.isArray(inheritors) ? inheritors : [])
    .filter((x) => x && x.id != null)
    .map((x) => ({ value: x.id, label: x.name || `ID:${x.id}` }))
  inheritorOptions.value = mergeInheritorOptions(inheritorOptions.value, opts)
}

const searchInheritors = async (keyword) => {
  inheritorLoading.value = true
  try {
    const kw = (keyword || '').trim()
    const res = await pageAdminHeritageInheritors({ page: 1, size: 50, keyword: kw })
    const data = res.data || {}
    const records = Array.isArray(data.records) ? data.records : []
    const opts = records
      .filter((x) => x && x.id != null)
      .map((x) => ({ value: x.id, label: x.name || `ID:${x.id}` }))
    inheritorOptions.value = mergeInheritorOptions(opts, inheritorOptions.value)
  } finally {
    inheritorLoading.value = false
  }
}

const inheritorDrawerVisible = ref(false)
const inheritorListLoading = ref(false)
const inheritorList = ref([])
const inheritorKeyword = ref('')
const inheritorPage = ref(1)
const inheritorPageSize = ref(10)
const inheritorTotal = ref(0)

const fetchInheritorList = async () => {
  inheritorListLoading.value = true
  try {
    const params = { page: inheritorPage.value, size: inheritorPageSize.value }
    const kw = (inheritorKeyword.value || '').trim()
    if (kw) params.keyword = kw
    const res = await pageAdminHeritageInheritors(params)
    const data = res.data || {}
    inheritorList.value = Array.isArray(data.records) ? data.records : []
    inheritorTotal.value = Number(data.total ?? 0)
  } finally {
    inheritorListLoading.value = false
  }
}

const openInheritorDrawer = async () => {
  inheritorDrawerVisible.value = true
  inheritorPage.value = 1
  await fetchInheritorList()
}

const handleInheritorSearch = async () => {
  inheritorPage.value = 1
  await fetchInheritorList()
}

const handleInheritorSizeChange = async () => {
  inheritorPage.value = 1
  await fetchInheritorList()
}

const handleInheritorCurrentChange = async () => {
  await fetchInheritorList()
}

const inheritorDialogVisible = ref(false)
const inheritorSaving = ref(false)
const inheritorIsEdit = ref(false)
const inheritorFormRef = ref()
const inheritorForm = ref({
  id: null,
  name: '',
  gender: '',
  birthDate: '',
  nation: '',
  photo: '',
  description: ''
})

const inheritorRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const inheritorDialogTitle = computed(() => (inheritorIsEdit.value ? '编辑传承人' : '添加传承人'))

const openInheritorAdd = () => {
  inheritorIsEdit.value = false
  inheritorDialogVisible.value = true
}

const openInheritorEdit = async (row) => {
  const res = await getAdminHeritageInheritorDetail(row.id)
  const data = res.data || {}
  inheritorForm.value = {
    id: data.id ?? row.id,
    name: data.name ?? row.name ?? '',
    gender: data.gender ?? row.gender ?? '',
    birthDate: data.birthDate ?? row.birthDate ?? '',
    nation: data.nation ?? row.nation ?? '',
    photo: data.photo ?? row.photo ?? '',
    description: data.description ?? row.description ?? ''
  }
  inheritorIsEdit.value = true
  inheritorDialogVisible.value = true
}

const resetInheritorDialog = () => {
  inheritorFormRef.value?.resetFields?.()
  inheritorForm.value = {
    id: null,
    name: '',
    gender: '',
    birthDate: '',
    nation: '',
    photo: '',
    description: ''
  }
  inheritorIsEdit.value = false
}

const saveInheritor = async () => {
  await inheritorFormRef.value?.validate()
  inheritorSaving.value = true
  try {
    const payload = {
      name: inheritorForm.value.name,
      gender: inheritorForm.value.gender,
      birthDate: inheritorForm.value.birthDate || null,
      nation: inheritorForm.value.nation,
      photo: inheritorForm.value.photo,
      description: inheritorForm.value.description
    }
    if (inheritorIsEdit.value && inheritorForm.value.id) {
      await updateAdminHeritageInheritor(inheritorForm.value.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createAdminHeritageInheritor(payload)
      ElMessage.success('添加成功')
    }
    inheritorDialogVisible.value = false
    await fetchInheritorList()
    await fetchProjects()
    await searchInheritors('')
  } finally {
    inheritorSaving.value = false
  }
}

const handleInheritorDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除传承人「${row.name}」吗？删除后将解除与项目的关联。`, '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteAdminHeritageInheritor(row.id)
  ElMessage.success('删除成功')
  await fetchInheritorList()
  await fetchProjects()
  await searchInheritors('')
}

const categoryDialogVisible = ref(false)
const categorySaving = ref(false)
const categoryFormRef = ref()
const categoryForm = ref({
  parentId: null,
  name: '',
  sortOrder: 0
})

const categoryRules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

const fetchCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getAdminHeritageCategoryTree()
    categoryTree.value = Array.isArray(res.data) ? res.data : []
  } finally {
    categoryLoading.value = false
  }
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const kw = (searchKeyword.value || '').trim()
    if (kw) params.keyword = kw
    if (searchCategoryId.value) params.categoryId = searchCategoryId.value
    const res = await pageAdminHeritageProjects(params)
    const data = res.data || {}
    projects.value = Array.isArray(data.records) ? data.records : []
    total.value = Number(data.total ?? 0)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await fetchProjects()
}

const handleSizeChange = async () => {
  currentPage.value = 1
  await fetchProjects()
}

const handleCurrentChange = async () => {
  await fetchProjects()
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    ...form.value,
    id: null,
    categoryId: null,
    name: '',
    description: '',
    applyUnit: '',
    protectUnit: '',
    inheritorIds: []
  }
  inheritorOptions.value = []
  searchInheritors('')
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

const handleEdit = async (row) => {
  const res = await getAdminHeritageProjectDetail(row.id)
  const data = res.data || {}
  const inheritors = Array.isArray(data.inheritors) ? data.inheritors : []
  form.value = {
    id: data.id ?? row.id,
    categoryId: data.categoryId ?? row.categoryId ?? null,
    name: data.name ?? row.name ?? '',
    description: data.description ?? '',
    applyUnit: data.applyUnit ?? row.applyUnit ?? '',
    protectUnit: data.protectUnit ?? row.protectUnit ?? '',
    inheritorIds: inheritors.map((x) => x?.id).filter((x) => x != null)
  }
  upsertOptionsFromInheritors(inheritors)
  isEdit.value = true
  searchInheritors('')
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

const handleSave = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      categoryId: form.value.categoryId,
      name: form.value.name,
      description: form.value.description,
      applyUnit: form.value.applyUnit,
      protectUnit: form.value.protectUnit
    }
    if (isEdit.value && form.value.id) {
      await updateAdminHeritageProject(form.value.id, payload)
      await updateAdminHeritageProjectInheritors(form.value.id, { inheritorIds: form.value.inheritorIds || [] })
      ElMessage.success('保存成功')
    } else {
      const res = await createAdminHeritageProject(payload)
      const newId = res.data
      await updateAdminHeritageProjectInheritors(newId, { inheritorIds: form.value.inheritorIds || [] })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchProjects()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除项目「${row.name}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteAdminHeritageProject(row.id)
  ElMessage.success('删除成功')
  await fetchProjects()
}

const resetDialog = () => {
  formRef.value?.resetFields?.()
  formRef.value?.clearValidate?.()
  form.value = {
    id: null,
    categoryId: null,
    name: '',
    description: '',
    applyUnit: '',
    protectUnit: '',
    inheritorIds: []
  }
  inheritorOptions.value = []
  isEdit.value = false
}

const openCategoryDialog = () => {
  categoryDialogVisible.value = true
}

const saveCategory = async () => {
  await categoryFormRef.value?.validate()
  categorySaving.value = true
  try {
    const payload = {
      name: categoryForm.value.name,
      parentId: categoryForm.value.parentId,
      sortOrder: categoryForm.value.sortOrder
    }
    await createAdminHeritageCategory(payload)
    ElMessage.success('添加成功')
    categoryDialogVisible.value = false
    await fetchCategories()
  } finally {
    categorySaving.value = false
  }
}

const resetCategoryDialog = () => {
  categoryFormRef.value?.resetFields?.()
  categoryForm.value = {
    parentId: null,
    name: '',
    sortOrder: 0
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchProjects()
})
</script>

<style scoped>
.heritage {
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
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.type-select {
  width: 220px;
}

.search-input {
  width: 320px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.drawer-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.drawer-search {
  width: 360px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-preview {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
