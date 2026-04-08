<template>
  <div class="profile">
    <div class="profile-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <div class="info-layout">
            <div class="info-left">
              <div class="avatar-wrapper">
                <el-avatar
                  :size="100"
                  :src="userInfo.avatar || ''"
                  class="profile-avatar"
                >
                  {{ userInfo.name?.charAt(0) || "U" }}
                </el-avatar>
                <div class="avatar-mask">
                  <el-upload
                    class="avatar-uploader-trigger"
                    :action="UPLOAD_URL"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    :headers="uploadHeaders"
                    name="file"
                  >
                    <el-icon><Camera /></el-icon>
                    <span>更换头像</span>
                  </el-upload>
                </div>
              </div>
              <div class="user-role-badge">{{ roleText }}</div>
            </div>

            <div class="info-right">
              <h3 class="section-title">基本信息</h3>
              <el-form
                :model="form"
                :rules="rules"
                ref="formRef"
                label-position="top"
                class="compact-form"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="账号" prop="account">
                      <el-input v-model="form.account" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="名称" prop="name">
                      <el-input v-model="form.name" placeholder="请输入名称" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="证件号" prop="certificateNumber">
                      <el-input
                        v-model="form.certificateNumber"
                        disabled
                        placeholder="未认证"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="注册时间">
                      <el-input
                        :value="formatTime(userInfo.registerTime)"
                        disabled
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item style="margin-top: 10px">
                  <el-button
                    type="primary"
                    @click="handleUpdateProfile"
                    :loading="updating"
                    class="submit-btn"
                    >保存修改</el-button
                  >
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="收货地址" name="address">
          <div class="address-section">
            <div class="address-toolbar">
              <el-button type="primary" @click="openAddAddress"
                >新增地址</el-button
              >
            </div>

            <el-skeleton v-if="addressLoading" :rows="5" animated />

            <el-empty
              v-else-if="addresses.length === 0"
              description="暂无地址"
            />

            <div v-else class="address-list">
              <div
                v-for="item in pagedAddresses"
                :key="item.id"
                class="address-item"
              >
                <div class="address-item-main">
                  <div class="address-item-title">
                    <span class="address-item-name">{{
                      item.receiverName
                    }}</span>
                    <span class="address-item-phone">{{
                      item.receiverPhone
                    }}</span>
                    <el-tag
                      v-if="item.isDefault === 1"
                      type="success"
                      size="small"
                      >默认</el-tag
                    >
                  </div>
                  <div class="address-item-detail">
                    {{ formatAddressLine(item) }}
                  </div>
                </div>

                <div class="address-item-actions">
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    round
                    @click="openEditAddress(item)"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    round
                    @click="handleDeleteAddress(item)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                  <el-button
                    size="small"
                    type="success"
                    plain
                    round
                    :disabled="item.isDefault === 1"
                    @click="handleSetDefaultAddress(item)"
                  >
                    设为默认
                  </el-button>
                </div>
              </div>

              <div class="pagination-container">
                <AppPagination
                  v-model:current-page="addressCurrentPage"
                  v-model:page-size="addressPageSize"
                  :total="addresses.length"
                />
              </div>
            </div>

            <el-dialog
              v-model="addressDialogVisible"
              :title="addressDialogTitle"
              width="520px"
              @closed="handleAddressDialogClosed"
            >
              <el-form
                ref="addressFormRef"
                :model="addressForm"
                :rules="addressRules"
                label-width="90px"
              >
                <el-form-item label="收货人" prop="receiverName">
                  <el-input
                    v-model="addressForm.receiverName"
                    placeholder="请输入收货人姓名"
                  />
                </el-form-item>
                <el-form-item label="手机号" prop="receiverPhone">
                  <el-input
                    v-model="addressForm.receiverPhone"
                    placeholder="请输入收货人手机号"
                  />
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
                  <el-input
                    :model-value="addressForm.regionNamePath"
                    disabled
                  />
                </el-form-item>
                <el-form-item label="详细地址" prop="detailAddress">
                  <el-input
                    v-model="addressForm.detailAddress"
                    placeholder="请输入详细地址"
                  />
                </el-form-item>
                <el-form-item>
                  <el-checkbox v-model="addressForm.isDefault"
                    >设为默认地址</el-checkbox
                  >
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="addressDialogVisible = false"
                  >取消</el-button
                >
                <el-button
                  type="primary"
                  :loading="addressSaving"
                  @click="submitAddress"
                  >保存</el-button
                >
              </template>
            </el-dialog>
          </div>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="password">
          <div class="password-section">
            <el-form
              :model="passwordForm"
              :rules="passwordRules"
              ref="passwordFormRef"
              label-position="top"
              class="single-column-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item style="margin-top: 20px">
                <el-button
                  type="primary"
                  @click="handleUpdatePassword"
                  :loading="updatingPassword"
                  class="submit-btn"
                  >修改密码</el-button
                >
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="实名认证" name="certificate">
          <div class="certificate-section">
            <el-form
              :model="certificateForm"
              :rules="certificateRules"
              ref="certificateFormRef"
              label-position="top"
              class="single-column-form"
            >
              <el-form-item label="证件号" prop="certificateNumber">
                <el-input
                  v-model="certificateForm.certificateNumber"
                  placeholder="请输入证件号"
                  :disabled="isCertified"
                />
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input
                  v-model="certificateForm.name"
                  placeholder="请输入真实姓名"
                  :disabled="isCertified"
                />
              </el-form-item>
              <el-form-item v-if="!isCertified" style="margin-top: 20px">
                <el-button
                  type="primary"
                  @click="handleVerifyCertificate"
                  :loading="verifying"
                  class="submit-btn"
                  >提交认证</el-button
                >
              </el-form-item>
            </el-form>
            <div
              class="certificate-status-wrapper"
              v-if="isCertified || certificateInfo"
            >
              <el-alert
                v-if="isCertified"
                :title="`认证状态：${certificateStatusText}`"
                type="success"
                :closable="false"
                show-icon
              />
              <el-descriptions
                v-if="certificateInfo"
                :column="1"
                border
                style="margin-top: 20px"
              >
                <el-descriptions-item label="证件号">{{
                  certificateInfo.certificateNumber
                }}</el-descriptions-item>
                <el-descriptions-item label="真实姓名">{{
                  certificateInfo.name
                }}</el-descriptions-item>
                <el-descriptions-item label="角色">{{
                  certificateInfo.role === "ADMIN"
                    ? "管理员"
                    : certificateInfo.role === "MERCHANT"
                      ? "商家"
                      : "普通用户"
                }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import { useUserStore } from "@/stores/user";
import { ElMessage, ElMessageBox } from "element-plus";
import AppPagination from "@/components/AppPagination.vue";
import {
  getProfile,
  updateProfile,
  updatePassword,
  verifyCertificate,
  getCertificateInfo,
  createAddress,
  updateAddress,
  deleteAddress,
  listAddresses,
  setDefaultAddress,
} from "@/api/auth";
import { regionData, CodeToText, TextToCode } from "element-china-area-data";
import { Camera, Edit, Delete } from "@element-plus/icons-vue";
import { UPLOAD_URL, getAvatarUrl } from "@/config/api.js";

const userStore = useUserStore();

const activeTab = ref("info");
const formRef = ref(null);
const passwordFormRef = ref(null);
const certificateFormRef = ref(null);
const addressFormRef = ref(null);
const updating = ref(false);
const updatingPassword = ref(false);
const verifying = ref(false);

const userInfo = reactive({
  id: null,
  account: "",
  name: "",
  role: "",
  avatar: "",
  certificateNumber: "",
  registerTime: null,
});

const form = reactive({
  account: "",
  name: "",
  avatar: "",
  certificateNumber: "",
});

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const certificateForm = reactive({
  certificateNumber: "",
  name: "",
});

const certificateInfo = ref(null);
const isCertified = computed(() => !!userInfo.certificateNumber);
const certificateStatusText = computed(() => {
  if (userInfo.certificateNumber) {
    return "已认证";
  }
  return "未认证";
});

const roleText = computed(() => {
  const roleMap = {
    ADMIN: "管理员",
    MERCHANT: "商家",
    USER: "普通用户",
  };
  return roleMap[userInfo.role] || userInfo.role;
});

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`,
}));

const rules = {
  name: [
    { required: true, message: "请输入名称", trigger: "blur" },
    { min: 2, max: 20, message: "名称长度在2-20个字符", trigger: "blur" },
  ],
};

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在6-20个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
};

const certificateRules = {
  certificateNumber: [
    { required: true, message: "请输入证件号", trigger: "blur" },
  ],
  name: [
    { required: true, message: "请输入真实姓名", trigger: "blur" },
    { min: 2, max: 20, message: "姓名长度在2-20个字符", trigger: "blur" },
  ],
};

const formatTime = (time) => {
  if (!time) return "";
  return new Date(time).toLocaleString("zh-CN");
};

const addresses = ref([]);
const addressLoading = ref(false);
const addressCurrentPage = ref(1);
const addressPageSize = ref(5);

const pagedAddresses = computed(() => {
  const start = (addressCurrentPage.value - 1) * addressPageSize.value;
  const end = start + addressPageSize.value;
  return addresses.value.slice(start, end);
});

const addressDialogVisible = ref(false);
const addressSaving = ref(false);
const editingAddressId = ref(null);
const addressDialogTitle = computed(() =>
  editingAddressId.value ? "编辑地址" : "新增地址",
);

const addressForm = reactive({
  receiverName: "",
  receiverPhone: "",
  regionNamePath: "",
  regionCodePath: "",
  regionDepth: 0,
  province: "",
  city: "",
  district: "",
  detailAddress: "",
  regionCodes: [],
  isDefault: false,
});

const addressRules = {
  receiverName: [
    { required: true, message: "请输入收货人姓名", trigger: "blur" },
  ],
  receiverPhone: [
    { required: true, message: "请输入收货人手机号", trigger: "blur" },
  ],
  regionCodes: [
    {
      validator: (rule, value, callback) => {
        if (Array.isArray(value) && value.length > 0) {
          callback();
          return;
        }
        if (addressForm.regionNamePath) {
          callback();
          return;
        }
        callback(new Error("请选择地区"));
      },
      trigger: "change",
    },
  ],
  detailAddress: [
    { required: true, message: "请输入详细地址", trigger: "blur" },
  ],
};

const regionOptions = regionData;

const fetchUserInfo = async () => {
  try {
    const res = await getProfile();
    if (res.code === 200) {
      Object.assign(userInfo, res.data);
      form.account = res.data.account;
      form.name = res.data.name;
      form.certificateNumber = res.data.certificateNumber;
      const avatar = res.data.avatar;
      form.avatar = getAvatarUrl(avatar);
      userInfo.avatar = getAvatarUrl(avatar);

      if (res.data.certificateNumber) {
        await fetchCertificateInfo();
      }
    }
  } catch (error) {
    ElMessage.error("获取用户信息失败");
  }
};

const fetchCertificateInfo = async () => {
  try {
    const res = await getCertificateInfo();
    if (res.code === 200) {
      certificateInfo.value = res.data;
    }
  } catch (error) {
    console.error("获取认证信息失败", error);
  }
};

const fetchAddresses = async () => {
  addressLoading.value = true;
  try {
    const res = await listAddresses();
    addresses.value = res.data || [];
  } catch (error) {
    ElMessage.error("获取地址失败");
  } finally {
    addressLoading.value = false;
  }
};

const resetAddressForm = () => {
  addressForm.receiverName = "";
  addressForm.receiverPhone = "";
  addressForm.regionNamePath = "";
  addressForm.regionCodePath = "";
  addressForm.regionDepth = 0;
  addressForm.province = "";
  addressForm.city = "";
  addressForm.district = "";
  addressForm.detailAddress = "";
  addressForm.regionCodes = [];
  addressForm.isDefault = false;
};

const handleAddressDialogClosed = async () => {
  resetAddressForm();
  editingAddressId.value = null;
  await nextTick();
  addressFormRef.value?.clearValidate();
};

const toRegionCodes = (province, city, district) => {
  const p = TextToCode?.[province];
  const pCode = p?.code;
  const c = p?.[city];
  const cCode = c?.code;
  const d = c?.[district];
  const dCode = d?.code;
  if (pCode && cCode && dCode) {
    return [pCode, cCode, dCode];
  }
  return [];
};

const syncRegionText = () => {
  const codes = addressForm.regionCodes;
  if (Array.isArray(codes) && codes.length > 0) {
    const names = codes.map((c) => CodeToText[c]).filter(Boolean);
    addressForm.regionCodePath = codes.join("/");
    addressForm.regionNamePath = names.join("/");
    addressForm.regionDepth = names.length;
    addressForm.province = names[0] || "";
    addressForm.city = names[1] || "";
    addressForm.district = names[2] || "";
    return;
  }

  addressForm.regionCodePath = "";
  if (addressForm.regionNamePath) {
    const parts = addressForm.regionNamePath
      .split("/")
      .map((p) => p.trim())
      .filter(Boolean);
    addressForm.regionNamePath = parts.join("/");
    addressForm.regionDepth = parts.length;
    addressForm.province = parts[0] || "";
    addressForm.city = parts[1] || "";
    addressForm.district = parts[2] || "";
    return;
  }

  addressForm.regionDepth = 0;
  addressForm.province = "";
  addressForm.city = "";
  addressForm.district = "";
};

const handleRegionChange = () => {
  syncRegionText();
};

const openAddAddress = () => {
  editingAddressId.value = null;
  resetAddressForm();
  addressDialogVisible.value = true;
  nextTick(() => {
    addressFormRef.value?.clearValidate();
  });
};

const openEditAddress = (item) => {
  editingAddressId.value = item.id;
  addressForm.receiverName = item.receiverName || "";
  addressForm.receiverPhone = item.receiverPhone || "";
  addressForm.regionNamePath = item.regionNamePath || "";
  addressForm.regionCodePath = item.regionCodePath || "";
  addressForm.regionDepth = item.regionDepth || 0;
  addressForm.province = item.province || "";
  addressForm.city = item.city || "";
  addressForm.district = item.district || "";
  addressForm.detailAddress = item.detailAddress || "";
  if (addressForm.regionCodePath) {
    addressForm.regionCodes = addressForm.regionCodePath
      .split("/")
      .filter(Boolean);
  } else {
    addressForm.regionCodes = toRegionCodes(
      addressForm.province,
      addressForm.city,
      addressForm.district,
    );
  }
  syncRegionText();
  addressForm.isDefault = item.isDefault === 1;
  addressDialogVisible.value = true;
  nextTick(() => {
    addressFormRef.value?.clearValidate();
  });
};

const formatAddressLine = (item) => {
  const region =
    item.regionNamePath ||
    `${item.province || ""}${item.city || ""}${item.district || ""}`;
  return `${region}${item.detailAddress || ""}`;
};

const submitAddress = async () => {
  if (!addressFormRef.value) return;

  await addressFormRef.value.validate(async (valid) => {
    if (!valid) return;

    addressSaving.value = true;
    try {
      syncRegionText();
      const payload = {
        receiverName: addressForm.receiverName,
        receiverPhone: addressForm.receiverPhone,
        regionNamePath: addressForm.regionNamePath,
        regionCodePath: addressForm.regionCodePath || null,
        regionDepth: addressForm.regionDepth || null,
        detailAddress: addressForm.detailAddress,
        isDefault: addressForm.isDefault,
      };

      if (editingAddressId.value) {
        await updateAddress(editingAddressId.value, payload);
        if (addressForm.isDefault) {
          await setDefaultAddress(editingAddressId.value);
        }
        ElMessage.success("地址更新成功");
      } else {
        await createAddress(payload);
        ElMessage.success("地址新增成功");
      }

      addressDialogVisible.value = false;
      await fetchAddresses();
    } catch (error) {
      ElMessage.error(error.response?.data?.message || "保存地址失败");
    } finally {
      addressSaving.value = false;
    }
  });
};

const handleDeleteAddress = async (item) => {
  try {
    await ElMessageBox.confirm("确认删除该地址吗？", "提示", {
      type: "warning",
    });
    await deleteAddress(item.id);
    ElMessage.success("删除成功");
    await fetchAddresses();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
    }
  }
};

const handleSetDefaultAddress = async (item) => {
  try {
    await setDefaultAddress(item.id);
    ElMessage.success("设置默认地址成功");
    await fetchAddresses();
  } catch (error) {
    ElMessage.error("设置默认地址失败");
  }
};

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过2MB");
    return false;
  }
  return true;
};

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    const avatarUrl = getAvatarUrl(response.data);
    form.avatar = avatarUrl;
    userInfo.avatar = avatarUrl;

    try {
      await updateProfile({
        name: form.name,
        avatar: avatarUrl,
      });
      userStore.setUserInfo({
        ...userStore.userInfo,
        avatar: avatarUrl,
      });
      ElMessage.success("头像上传成功");
    } catch (error) {
      ElMessage.error("头像保存失败");
    }
  } else {
    ElMessage.error(response.message || "头像上传失败");
  }
};

const handleUpdateProfile = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    updating.value = true;
    try {
      await updateProfile({
        name: form.name,
        avatar: form.avatar,
      });
      userStore.setUserInfo({
        ...userStore.userInfo,
        name: form.name,
        avatar: form.avatar,
      });
      ElMessage.success("个人信息更新成功");
    } catch (error) {
      ElMessage.error("个人信息更新失败");
    } finally {
      updating.value = false;
    }
  });
};

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;

    updatingPassword.value = true;
    try {
      await updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword,
      });
      ElMessage.success("密码修改成功，请重新登录");
      userStore.logout();
      setTimeout(() => {
        window.location.href = "/login";
      }, 1500);
    } catch (error) {
      ElMessage.error(error.response?.data?.message || "密码修改失败");
    } finally {
      updatingPassword.value = false;
    }
  });
};

const handleVerifyCertificate = async () => {
  if (!certificateFormRef.value) return;

  await certificateFormRef.value.validate(async (valid) => {
    if (!valid) return;

    verifying.value = true;
    try {
      await verifyCertificate({
        certificateNumber: certificateForm.certificateNumber,
        name: certificateForm.name,
      });
      ElMessage.success("实名认证提交成功");
      await fetchUserInfo();
      certificateForm.certificateNumber = "";
      certificateForm.name = "";
    } catch (error) {
      ElMessage.error(error.response?.data?.message || "实名认证失败");
    } finally {
      verifying.value = false;
    }
  });
};

onMounted(() => {
  fetchUserInfo();
  fetchAddresses();
});
</script>

<style scoped>
.profile {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--bg-page);
  transition: background-color 0.3s ease;
}

.profile-container {
  max-width: 900px;
  margin: 0 auto;
  background: var(--card-bg);
  padding: 32px 40px;
  border-radius: var(--app-radius);
  border: 1px solid var(--card-border);
  min-height: 600px;
  box-shadow: var(--card-shadow);
  transition:
    background-color 0.3s ease,
    border-color 0.3s ease;
}

/* Info Layout */
.info-layout {
  display: flex;
  gap: 60px;
  padding: 20px 0;
}

.info-left {
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color-base);
  padding-right: 40px;
}

.info-right {
  flex: 1;
  min-width: 0;
}

/* Avatar Styling */
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 16px;
  cursor: pointer;
  border: 4px solid var(--card-bg);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.profile-avatar {
  width: 100%;
  height: 100%;
  font-size: 36px;
  background: var(--primary-color);
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-uploader-trigger {
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  gap: 4px;
}

.user-role-badge {
  padding: 4px 12px;
  background: var(--btn-plain-primary-bg);
  color: var(--btn-plain-primary-text);
  border-radius: 99px;
  font-size: 13px;
  font-weight: 500;
}

/* Section Title */
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color-lighter);
}

/* Forms */
.compact-form :deep(.el-form-item__label),
.single-column-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-color-regular);
  padding-bottom: 8px;
}

.single-column-form {
  max-width: 420px;
  margin: 0 auto;
  padding-top: 20px;
}

.submit-btn {
  width: 100%;
  height: 40px;
  font-size: 15px;
  letter-spacing: 1px;
}

.certificate-status-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.password-section,
.certificate-section {
  padding: 20px 0;
}

/* Address Styling */
.address-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color-base);
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--border-color-base);
  border-radius: var(--app-radius);
  background: var(--card-bg);
  transition: all 0.2s;
}

.address-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
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
  font-size: 15px;
  color: var(--text-color-primary);
}

.address-item-phone {
  color: var(--text-color-secondary);
}

.address-item-detail {
  color: var(--text-color-secondary);
  word-break: break-all;
  line-height: 1.5;
}

.address-item-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.address-item-actions .el-button {
  min-width: 60px;
  font-weight: 500;
}

.address-item-actions .el-button--primary.is-plain {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.1) !important;
  border-color: rgba(64, 158, 255, 0.4) !important;
}

.address-item-actions .el-button--primary.is-plain:hover {
  background-color: rgba(64, 158, 255, 0.2) !important;
  border-color: #409eff !important;
}

.address-item-actions .el-button--danger.is-plain {
  color: #f56c6c !important;
  background-color: rgba(245, 108, 108, 0.1) !important;
  border-color: rgba(245, 108, 108, 0.4) !important;
}

.address-item-actions .el-button--danger.is-plain:hover {
  background-color: rgba(245, 108, 108, 0.2) !important;
  border-color: #f56c6c !important;
}

.address-item-actions .el-button--success.is-plain {
  color: #67c23a !important;
  background-color: rgba(103, 194, 58, 0.1) !important;
  border-color: rgba(103, 194, 58, 0.4) !important;
}

.address-item-actions .el-button--success.is-plain:hover {
  background-color: rgba(103, 194, 58, 0.2) !important;
  border-color: #67c23a !important;
}

/* Dark mode button enhancements */
html.dark .address-item-actions .el-button--primary.is-plain {
  color: #79bbff !important;
  background-color: rgba(64, 158, 255, 0.15) !important;
  border-color: rgba(64, 158, 255, 0.5) !important;
}

html.dark .address-item-actions .el-button--danger.is-plain {
  color: #f89898 !important;
  background-color: rgba(245, 108, 108, 0.15) !important;
  border-color: rgba(245, 108, 108, 0.5) !important;
}

html.dark .address-item-actions .el-button--success.is-plain {
  color: #95d475 !important;
  background-color: rgba(103, 194, 58, 0.15) !important;
  border-color: rgba(103, 194, 58, 0.5) !important;
}

/* Responsive */
@media (max-width: 768px) {
  .info-layout {
    flex-direction: column;
    gap: 30px;
  }

  .info-left {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--border-color-base);
    padding-right: 0;
    padding-bottom: 20px;
  }

  .profile-container {
    padding: 20px;
  }
}
</style>
