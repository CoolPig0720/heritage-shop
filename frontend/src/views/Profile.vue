<template>
  <div class="profile">
    <div class="profile-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <div class="info-section">
            <div class="avatar-section">
              <el-avatar :size="120" :src="userInfo.avatar || ''">
                {{ userInfo.name?.charAt(0) || 'U' }}
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
                <el-button type="primary" size="small">更换头像</el-button>
              </el-upload>
            </div>
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
              <el-form-item label="账号" prop="account">
                <el-input v-model="form.account" disabled />
              </el-form-item>
              <el-form-item label="名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入名称" />
              </el-form-item>
              <el-form-item label="角色" prop="role">
                <el-input v-model="roleText" disabled />
              </el-form-item>
              <el-form-item label="证件号" prop="certificateNumber">
                <el-input v-model="form.certificateNumber" disabled />
              </el-form-item>
              <el-form-item label="注册时间">
                <el-input :value="formatTime(userInfo.registerTime)" disabled />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="收货地址" name="address">
          <div class="address-section">
            <div class="address-toolbar">
              <el-button type="primary" @click="openAddAddress">新增地址</el-button>
            </div>

            <el-skeleton v-if="addressLoading" :rows="5" animated />

            <el-empty v-else-if="addresses.length === 0" description="暂无地址" />

            <div v-else class="address-list">
              <div v-for="item in addresses" :key="item.id" class="address-item">
                <div class="address-item-main">
                  <div class="address-item-title">
                    <span class="address-item-name">{{ item.receiverName }}</span>
                    <span class="address-item-phone">{{ item.receiverPhone }}</span>
                    <el-tag v-if="item.isDefault === 1" type="success" size="small">默认</el-tag>
                  </div>
                  <div class="address-item-detail">
                    {{ formatAddressLine(item) }}
                  </div>
                </div>

                <div class="address-item-actions">
                  <el-button size="small" @click="openEditAddress(item)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteAddress(item)">删除</el-button>
                  <el-button
                    size="small"
                    type="success"
                    :disabled="item.isDefault === 1"
                    @click="handleSetDefaultAddress(item)"
                  >
                    设为默认
                  </el-button>
                </div>
              </div>
            </div>

            <el-dialog v-model="addressDialogVisible" :title="addressDialogTitle" width="520px" @closed="handleAddressDialogClosed">
              <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="90px">
                <el-form-item label="收货人" prop="receiverName">
                  <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
                </el-form-item>
                <el-form-item label="手机号" prop="receiverPhone">
                  <el-input v-model="addressForm.receiverPhone" placeholder="请输入收货人手机号" />
                </el-form-item>
                <el-form-item label="地区" prop="regionCodes">
                  <el-cascader
                    v-model="addressForm.regionCodes"
                    :options="regionOptions"
                    placeholder="请选择地区"
                    style="width: 100%"
                    @change="handleRegionChange"
                  />
                </el-form-item>
                <el-form-item label="已选地区">
                  <el-input :model-value="addressForm.regionNamePath" disabled />
                </el-form-item>
                <el-form-item label="详细地址" prop="detailAddress">
                  <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址" />
                </el-form-item>
                <el-form-item>
                  <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="addressDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="addressSaving" @click="submitAddress">保存</el-button>
              </template>
            </el-dialog>
          </div>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="password">
          <div class="password-section">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdatePassword" :loading="updatingPassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="实名认证" name="certificate">
          <div class="certificate-section">
            <el-form :model="certificateForm" :rules="certificateRules" ref="certificateFormRef" label-width="120px">
              <el-form-item label="证件号" prop="certificateNumber">
                <el-input v-model="certificateForm.certificateNumber" placeholder="请输入证件号" :disabled="isCertified" />
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="certificateForm.name" placeholder="请输入真实姓名" :disabled="isCertified" />
              </el-form-item>
              <el-form-item v-if="!isCertified">
                <el-button type="primary" @click="handleVerifyCertificate" :loading="verifying">提交认证</el-button>
              </el-form-item>
            </el-form>
            <el-alert
              v-if="isCertified"
              :title="`认证状态：${certificateStatusText}`"
              type="success"
              :closable="false"
              style="margin-top: 20px"
            />
            <el-alert
              v-if="certificateInfo"
              title="认证信息"
              type="info"
              :closable="false"
              style="margin-top: 20px"
            >
              <p>证件号：{{ certificateInfo.certificateNumber }}</p>
              <p>真实姓名：{{ certificateInfo.name }}</p>
              <p>角色：{{ certificateInfo.role === 'ADMIN' ? '管理员' : certificateInfo.role === 'MERCHANT' ? '商家' : '普通用户' }}</p>
            </el-alert>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProfile, updateProfile, updatePassword, verifyCertificate, getCertificateInfo, createAddress, updateAddress, deleteAddress, listAddresses, setDefaultAddress } from '@/api/auth'
import { regionData, CodeToText, TextToCode } from 'element-china-area-data'

const userStore = useUserStore()

const activeTab = ref('info')
const formRef = ref(null)
const passwordFormRef = ref(null)
const certificateFormRef = ref(null)
const addressFormRef = ref(null)
const updating = ref(false)
const updatingPassword = ref(false)
const verifying = ref(false)

const userInfo = reactive({
  id: null,
  account: '',
  name: '',
  role: '',
  avatar: '',
  certificateNumber: '',
  registerTime: null
})

const form = reactive({
  account: '',
  name: '',
  avatar: '',
  certificateNumber: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const certificateForm = reactive({
  certificateNumber: '',
  name: ''
})

const certificateInfo = ref(null)
const isCertified = computed(() => !!userInfo.certificateNumber)
const certificateStatusText = computed(() => {
  if (userInfo.certificateNumber) {
    return '已认证'
  }
  return '未认证'
})

const roleText = computed(() => {
  const roleMap = {
    'ADMIN': '管理员',
    'MERCHANT': '商家',
    'USER': '普通用户'
  }
  return roleMap[userInfo.role] || userInfo.role
})

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 20, message: '名称长度在2-20个字符', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const certificateRules = {
  certificateNumber: [
    { required: true, message: '请输入证件号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const addresses = ref([])
const addressLoading = ref(false)
const addressDialogVisible = ref(false)
const addressSaving = ref(false)
const editingAddressId = ref(null)
const addressDialogTitle = computed(() => (editingAddressId.value ? '编辑地址' : '新增地址'))

const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  regionNamePath: '',
  regionCodePath: '',
  regionDepth: 0,
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  regionCodes: [],
  isDefault: false
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入收货人手机号', trigger: 'blur' }],
  regionCodes: [
    {
      validator: (rule, value, callback) => {
        if (Array.isArray(value) && value.length > 0) {
          callback()
          return
        }
        if (addressForm.regionNamePath) {
          callback()
          return
        }
        callback(new Error('请选择地区'))
      },
      trigger: 'change'
    }
  ],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const regionOptions = regionData

const fetchUserInfo = async () => {
  try {
    const res = await getProfile()
    if (res.code === 200) {
      Object.assign(userInfo, res.data)
      form.account = res.data.account
      form.name = res.data.name
      form.certificateNumber = res.data.certificateNumber
      const avatar = res.data.avatar
      form.avatar = avatar?.startsWith('http') ? avatar : `http://localhost:8080${avatar}`
      userInfo.avatar = avatar?.startsWith('http') ? avatar : `http://localhost:8080${avatar}`
      
      if (res.data.certificateNumber) {
        await fetchCertificateInfo()
      }
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const fetchCertificateInfo = async () => {
  try {
    const res = await getCertificateInfo()
    if (res.code === 200) {
      certificateInfo.value = res.data
    }
  } catch (error) {
    console.error('获取认证信息失败', error)
  }
}

const fetchAddresses = async () => {
  addressLoading.value = true
  try {
    const res = await listAddresses()
    addresses.value = res.data || []
  } catch (error) {
    ElMessage.error('获取地址失败')
  } finally {
    addressLoading.value = false
  }
}

const resetAddressForm = () => {
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.regionNamePath = ''
  addressForm.regionCodePath = ''
  addressForm.regionDepth = 0
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detailAddress = ''
  addressForm.regionCodes = []
  addressForm.isDefault = false
}

const handleAddressDialogClosed = async () => {
  resetAddressForm()
  editingAddressId.value = null
  await nextTick()
  addressFormRef.value?.clearValidate()
}

const toRegionCodes = (province, city, district) => {
  const p = TextToCode?.[province]
  const pCode = p?.code
  const c = p?.[city]
  const cCode = c?.code
  const d = c?.[district]
  const dCode = d?.code
  if (pCode && cCode && dCode) {
    return [pCode, cCode, dCode]
  }
  return []
}

const syncRegionText = () => {
  const codes = addressForm.regionCodes
  if (Array.isArray(codes) && codes.length > 0) {
    const names = codes.map((c) => CodeToText[c]).filter(Boolean)
    addressForm.regionCodePath = codes.join('/')
    addressForm.regionNamePath = names.join('/')
    addressForm.regionDepth = names.length
    addressForm.province = names[0] || ''
    addressForm.city = names[1] || ''
    addressForm.district = names[2] || ''
    return
  }

  addressForm.regionCodePath = ''
  if (addressForm.regionNamePath) {
    const parts = addressForm.regionNamePath.split('/').map((p) => p.trim()).filter(Boolean)
    addressForm.regionNamePath = parts.join('/')
    addressForm.regionDepth = parts.length
    addressForm.province = parts[0] || ''
    addressForm.city = parts[1] || ''
    addressForm.district = parts[2] || ''
    return
  }

  addressForm.regionDepth = 0
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
}

const handleRegionChange = () => {
  syncRegionText()
}

const openAddAddress = () => {
  editingAddressId.value = null
  resetAddressForm()
  addressDialogVisible.value = true
  nextTick(() => {
    addressFormRef.value?.clearValidate()
  })
}

const openEditAddress = (item) => {
  editingAddressId.value = item.id
  addressForm.receiverName = item.receiverName || ''
  addressForm.receiverPhone = item.receiverPhone || ''
  addressForm.regionNamePath = item.regionNamePath || ''
  addressForm.regionCodePath = item.regionCodePath || ''
  addressForm.regionDepth = item.regionDepth || 0
  addressForm.province = item.province || ''
  addressForm.city = item.city || ''
  addressForm.district = item.district || ''
  addressForm.detailAddress = item.detailAddress || ''
  if (addressForm.regionCodePath) {
    addressForm.regionCodes = addressForm.regionCodePath.split('/').filter(Boolean)
  } else {
    addressForm.regionCodes = toRegionCodes(addressForm.province, addressForm.city, addressForm.district)
  }
  syncRegionText()
  addressForm.isDefault = item.isDefault === 1
  addressDialogVisible.value = true
  nextTick(() => {
    addressFormRef.value?.clearValidate()
  })
}

const formatAddressLine = (item) => {
  const region = item.regionNamePath || `${item.province || ''}${item.city || ''}${item.district || ''}`
  return `${region}${item.detailAddress || ''}`
}

const submitAddress = async () => {
  if (!addressFormRef.value) return

  await addressFormRef.value.validate(async (valid) => {
    if (!valid) return

    addressSaving.value = true
    try {
      syncRegionText()
      const payload = {
        receiverName: addressForm.receiverName,
        receiverPhone: addressForm.receiverPhone,
        regionNamePath: addressForm.regionNamePath,
        regionCodePath: addressForm.regionCodePath || null,
        regionDepth: addressForm.regionDepth || null,
        detailAddress: addressForm.detailAddress,
        isDefault: addressForm.isDefault
      }

      if (editingAddressId.value) {
        await updateAddress(editingAddressId.value, payload)
        if (addressForm.isDefault) {
          await setDefaultAddress(editingAddressId.value)
        }
        ElMessage.success('地址更新成功')
      } else {
        await createAddress(payload)
        ElMessage.success('地址新增成功')
      }

      addressDialogVisible.value = false
      await fetchAddresses()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '保存地址失败')
    } finally {
      addressSaving.value = false
    }
  })
}

const handleDeleteAddress = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该地址吗？', '提示', { type: 'warning' })
    await deleteAddress(item.id)
    ElMessage.success('删除成功')
    await fetchAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSetDefaultAddress = async (item) => {
  try {
    await setDefaultAddress(item.id)
    ElMessage.success('设置默认地址成功')
    await fetchAddresses()
  } catch (error) {
    ElMessage.error('设置默认地址失败')
  }
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

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    const avatarUrl = response.data.startsWith('http') ? response.data : `http://localhost:8080${response.data}`
    form.avatar = avatarUrl
    userInfo.avatar = avatarUrl
    
    try {
      await updateProfile({
        name: form.name,
        avatar: avatarUrl
      })
      userStore.setUserInfo({
        ...userStore.userInfo,
        avatar: avatarUrl
      })
      ElMessage.success('头像上传成功')
    } catch (error) {
      ElMessage.error('头像保存失败')
    }
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const handleUpdateProfile = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    updating.value = true
    try {
      await updateProfile({
        name: form.name,
        avatar: form.avatar
      })
      userStore.setUserInfo({
        ...userStore.userInfo,
        name: form.name,
        avatar: form.avatar
      })
      ElMessage.success('个人信息更新成功')
    } catch (error) {
      ElMessage.error('个人信息更新失败')
    } finally {
      updating.value = false
    }
  })
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    updatingPassword.value = true
    try {
      await updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      userStore.logout()
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '密码修改失败')
    } finally {
      updatingPassword.value = false
    }
  })
}

const handleVerifyCertificate = async () => {
  if (!certificateFormRef.value) return

  await certificateFormRef.value.validate(async (valid) => {
    if (!valid) return

    verifying.value = true
    try {
      await verifyCertificate({
        certificateNumber: certificateForm.certificateNumber,
        name: certificateForm.name
      })
      ElMessage.success('实名认证提交成功')
      await fetchUserInfo()
      certificateForm.certificateNumber = ''
      certificateForm.name = ''
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '实名认证失败')
    } finally {
      verifying.value = false
    }
  })
}

onMounted(() => {
  fetchUserInfo()
  fetchAddresses()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #EBEEF5;
}

.avatar-uploader {
  display: inline-block;
}

.info-section,
.address-section,
.password-section,
.certificate-section {
  padding: 20px 0;
}

.address-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
}

.address-item-main {
  flex: 1;
  min-width: 0;
}

.address-item-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.address-item-name {
  font-weight: 600;
}

.address-item-phone {
  color: #606266;
}

.address-item-detail {
  color: #606266;
  word-break: break-all;
}

.address-item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}
</style>
