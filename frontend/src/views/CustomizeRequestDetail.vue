<template>
  <div class="detail-page">
    <div class="detail-container">
      <!-- 工单信息 -->
      <el-card class="info-card" v-loading="loading">
        <div class="info-header">
          <div>
            <h3>{{ detail.title }}</h3>
            <el-tag
              :type="statusType(detail.status)"
              size="small"
              style="margin-left: 8px"
              >{{ statusLabel(detail.status) }}</el-tag
            >
          </div>
          <div class="header-actions">
            <el-button @click="goBack">返回列表</el-button>
            <el-button
              v-if="detail.orderId"
              type="primary"
              link
              @click="goOrder"
              >查看订单</el-button
            >
          </div>
        </div>
        <div class="info-body">
          <div class="info-row">
            <span class="label">商家：</span>{{ detail.merchantName }}
          </div>
          <div class="info-row">
            <span class="label">需求：</span>{{ detail.description }}
          </div>
          <div class="info-row" v-if="detail.quotedPrice">
            <span class="label">报价：</span>¥{{ detail.quotedPrice }}
          </div>
          <div
            class="info-row"
            v-if="detail.imageUrls && detail.imageUrls.length"
          >
            <span class="label">参考图片：</span>
            <div class="detail-images">
              <img
                v-for="(url, idx) in detail.imageUrls"
                :key="idx"
                :src="getImageUrl(url)"
                class="ai-preview"
                @click="previewImage(url)"
              />
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions" v-if="detail.status">
          <template v-if="isUser">
            <el-button
              v-if="detail.status === 'QUOTED'"
              type="primary"
              @click="showConfirmDialog = true"
              >确认报价</el-button
            >
            <el-button
              v-if="detail.status === 'PENDING' || detail.status === 'QUOTED'"
              @click="handleCancel"
              >取消定制</el-button
            >
          </template>
          <template v-if="isMerchant">
            <el-button
              v-if="detail.status === 'PENDING'"
              type="primary"
              @click="showQuoteDialog = true"
              >填写报价</el-button
            >
            <el-button
              v-if="detail.status === 'CONFIRMED'"
              type="success"
              @click="handleComplete"
              >标记完成</el-button
            >
            <el-button
              v-if="detail.status === 'PENDING' || detail.status === 'QUOTED'"
              @click="handleCancel"
              >取消</el-button
            >
          </template>
        </div>
      </el-card>

      <!-- 消息沟通区 -->
      <el-card class="chat-card">
        <div class="chat-messages" ref="messagesRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="[
              'msg-row',
              msg.senderType === 'USER' ? 'msg-right' : 'msg-left',
            ]"
          >
            <el-avatar
              v-if="msg.senderType !== 'USER'"
              :size="32"
              :src="getImageUrl(msg.senderAvatar)"
              class="msg-avatar"
              >{{ msg.senderName?.charAt(0) }}</el-avatar
            >
            <div class="msg-body">
              <div class="msg-sender">
                {{ msg.senderName }}
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="msg-bubble-row">
                <span
                  v-if="msg.senderId === userStore.userInfo.id"
                  class="msg-read-status"
                  >{{ msg.isRead === 1 ? "已读" : "未读" }}</span
                >
                <div class="msg-bubble">
                  <div class="msg-content">{{ msg.content }}</div>
                </div>
              </div>
            </div>
            <el-avatar
              v-if="msg.senderType === 'USER'"
              :size="32"
              :src="getImageUrl(msg.senderAvatar)"
              class="msg-avatar"
              >{{ msg.senderName?.charAt(0) }}</el-avatar
            >
          </div>
          <el-empty
            v-if="messages.length === 0"
            description="暂无消息"
            :image-size="60"
          />
        </div>
        <div class="chat-input">
          <el-input
            v-model="msgContent"
            placeholder="输入消息..."
            @keyup.enter="handleSend"
            :disabled="sending"
          />
          <el-button type="primary" @click="handleSend" :loading="sending"
            >发送</el-button
          >
        </div>
      </el-card>
    </div>

    <!-- 报价弹窗 -->
    <el-dialog v-model="showQuoteDialog" title="填写报价" width="400px">
      <el-form>
        <el-form-item label="报价金额（元）">
          <el-input-number
            v-model="quotePrice"
            :min="0.01"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuoteDialog = false">取消</el-button>
        <el-button type="primary" @click="handleQuote">确认报价</el-button>
      </template>
    </el-dialog>

    <!-- 确认报价弹窗（选地址） -->
    <el-dialog
      v-model="showConfirmDialog"
      title="确认报价 - 选择收件地址"
      width="540px"
    >
      <div v-loading="addressesLoading" class="address-radio-group">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
          :class="{ 'address-item-selected': selectedAddressId === addr.id }"
          @click="selectedAddressId = addr.id"
        >
          <el-radio :value="addr.id" v-model="selectedAddressId"
            >&nbsp;</el-radio
          >
          <div class="address-item-main">
            <div class="address-item-title">
              <span class="address-item-name">{{ addr.receiverName }}</span>
              <span class="address-item-phone">{{ addr.receiverPhone }}</span>
              <el-tag v-if="addr.isDefault === 1" type="success" size="small"
                >默认</el-tag
              >
            </div>
            <div class="address-item-detail">
              {{ addr.regionNamePath }} {{ addr.detailAddress }}
            </div>
          </div>
        </div>
      </div>
      <div
        v-if="!addressesLoading && addresses.length === 0"
        class="no-address-tip"
      >
        <p>暂无收件地址</p>
      </div>

      <div class="address-add-row">
        <el-button type="primary" @click="openAddAddressDialog"
          ><el-icon><Plus /></el-icon> 添加地址</el-button
        >
      </div>

      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedAddressId"
          @click="handleConfirm"
          >确认下单</el-button
        >
      </template>
    </el-dialog>

    <!-- 新增地址弹窗 -->
    <el-dialog
      v-model="showAddAddressForm"
      title="新增地址"
      width="520px"
      append-to-body
      @closed="resetAddressForm"
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
          <el-input :model-value="addressForm.regionNamePath" disabled />
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
        <el-button @click="showAddAddressForm = false">取消</el-button>
        <el-button
          type="primary"
          :loading="addingAddress"
          @click="handleAddAddress"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import {
  getCustomizeRequestDetail,
  updateCustomizeRequestStatus,
  quoteCustomizeRequest,
  confirmCustomizeRequest,
  listCustomizeMessages,
  sendCustomizeMessage,
  markMessagesRead,
} from "@/api/customize";
import { getImageUrl } from "@/config/api";
import { listAddresses, createAddress } from "@/api/auth";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { regionData, CodeToText } from "element-china-area-data";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const requestId = route.params.id;

const loading = ref(false);
const detail = ref({});
const messages = ref([]);
const msgContent = ref("");
const sending = ref(false);
const messagesRef = ref(null);

// 报价
const showQuoteDialog = ref(false);
const quotePrice = ref(null);

// 确认
const showConfirmDialog = ref(false);
const addresses = ref([]);
const addressesLoading = ref(false);
const selectedAddressId = ref(null);

// 内联添加地址
const showAddAddressForm = ref(false);
const addingAddress = ref(false);
const addressFormRef = ref(null);
const regionOptions = regionData;
const addressForm = ref({
  receiverName: "",
  receiverPhone: "",
  regionCodes: [],
  regionNamePath: "",
  regionCodePath: "",
  regionDepth: 0,
  province: "",
  city: "",
  district: "",
  detailAddress: "",
  isDefault: false,
});
const addressRules = {
  receiverName: [
    { required: true, message: "请输入收货人姓名", trigger: "blur" },
  ],
  receiverPhone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  regionCodes: [
    {
      validator: (rule, value, callback) => {
        if (Array.isArray(value) && value.length > 0) {
          callback();
        } else {
          callback(new Error("请选择地区"));
        }
      },
      trigger: "change",
    },
  ],
  detailAddress: [
    { required: true, message: "请输入详细地址", trigger: "blur" },
  ],
};

const handleRegionChange = (codes) => {
  const form = addressForm.value;
  form.province = codes[0] ? CodeToText[codes[0]] : "";
  form.city = codes[1] ? CodeToText[codes[1]] : "";
  form.district = codes[2] ? CodeToText[codes[2]] : "";
  form.regionNamePath = [form.province, form.city, form.district]
    .filter(Boolean)
    .join("/");
  form.regionCodePath = codes.join(",");
  form.regionDepth = codes.length;
};

const handleAddAddress = async () => {
  const formEl = addressFormRef.value;
  if (!formEl) return;
  await formEl.validate();
  addingAddress.value = true;
  try {
    const form = addressForm.value;
    await createAddress({
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      regionNamePath: form.regionNamePath,
      regionCodePath: form.regionCodePath,
      regionDepth: form.regionDepth,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      isDefault: form.isDefault,
    });
    ElMessage.success("地址添加成功");
    showAddAddressForm.value = false;
    // 重新加载地址列表
    await fetchAddresses();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "添加地址失败");
  } finally {
    addingAddress.value = false;
  }
};

const openAddAddressDialog = () => {
  resetAddressForm();
  showAddAddressForm.value = true;
};

const resetAddressForm = () => {
  addressForm.value = {
    receiverName: "",
    receiverPhone: "",
    regionCodes: [],
    regionNamePath: "",
    regionCodePath: "",
    regionDepth: 0,
    province: "",
    city: "",
    district: "",
    detailAddress: "",
    isDefault: false,
  };
};

let pollTimer = null;

const isUser = computed(() => userStore.userInfo?.role !== "MERCHANT");
const isMerchant = computed(() => userStore.userInfo?.role === "MERCHANT");

const statusType = (s) =>
  ({
    PENDING: "info",
    QUOTED: "warning",
    CONFIRMED: "",
    COMPLETED: "success",
    CANCELLED: "danger",
  })[s] || "info";
const statusLabel = (s) =>
  ({
    PENDING: "待报价",
    QUOTED: "已报价",
    CONFIRMED: "已确认",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
  })[s] || s;
const formatTime = (t) => {
  if (!t) return "";
  return t.replace("T", " ").substring(11, 19);
};

const fetchDetail = async () => {
  try {
    const res = await getCustomizeRequestDetail(requestId);
    detail.value = res?.data || {};
  } catch (e) {
    ElMessage.error("获取工单失败");
  }
};

const fetchMessages = async () => {
  try {
    const res = await listCustomizeMessages(requestId);
    messages.value = res?.data || [];
    await nextTick();
    scrollToBottom();
  } catch (e) {
    /* silent */
  }
};

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const handleSend = async () => {
  if (!msgContent.value.trim()) return;
  sending.value = true;
  try {
    await sendCustomizeMessage(requestId, { content: msgContent.value.trim() });
    msgContent.value = "";
    await fetchMessages();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "发送失败");
  } finally {
    sending.value = false;
  }
};

const handleQuote = async () => {
  if (!quotePrice.value || quotePrice.value <= 0) {
    ElMessage.warning("请输入有效报价");
    return;
  }
  try {
    await quoteCustomizeRequest(requestId, { quotedPrice: quotePrice.value });
    ElMessage.success("报价成功");
    showQuoteDialog.value = false;
    await fetchDetail();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "报价失败");
  }
};

const handleConfirm = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning("请选择收件地址");
    return;
  }
  try {
    const res = await confirmCustomizeRequest(requestId, {
      addressId: selectedAddressId.value,
    });
    ElMessage.success("确认成功，已创建订单");
    showConfirmDialog.value = false;
    await fetchDetail();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "确认失败");
  }
};

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm("确定要取消该定制工单吗？", "提示", {
      type: "warning",
    });
    await updateCustomizeRequestStatus(requestId, { status: "CANCELLED" });
    ElMessage.success("已取消");
    await fetchDetail();
  } catch (e) {
    /* cancelled */
  }
};

const handleComplete = async () => {
  try {
    await ElMessageBox.confirm("确定标记为已完成？", "提示", {
      type: "success",
    });
    await updateCustomizeRequestStatus(requestId, { status: "COMPLETED" });
    ElMessage.success("已标记完成");
    await fetchDetail();
  } catch (e) {
    /* cancelled */
  }
};

const goOrder = () => {
  if (detail.value.orderId) {
    router.push(`/orders`);
  }
};

const goBack = () => {
  router.push("/customize/requests");
};

const previewImage = (url) => {
  window.open(getImageUrl(url), "_blank");
};

const fetchAddresses = async () => {
  addressesLoading.value = true;
  try {
    const res = await listAddresses();
    addresses.value = res?.data || [];
  } catch (e) {
    /* silent */
  } finally {
    addressesLoading.value = false;
  }
};

onMounted(async () => {
  await fetchDetail();
  await fetchMessages();
  // 标记消息已读
  markMessagesRead(requestId).catch(() => {});
  // 轮询消息
  pollTimer = setInterval(fetchMessages, 5000);
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

// 确认弹窗打开时加载地址
import { watch } from "vue";
watch(showConfirmDialog, (val) => {
  if (val) {
    fetchAddresses();
    showAddAddressForm.value = false;
  }
});
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.detail-container {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.info-card {
  flex: 0 0 380px;
  min-width: 0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-header h3 {
  display: inline;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-body {
  margin-top: 16px;
}

.info-row {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.info-row .label {
  color: var(--el-text-color-secondary);
  min-width: 80px;
  display: inline-block;
}

.detail-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.ai-preview {
  max-width: 160px;
  max-height: 120px;
  border-radius: 6px;
  cursor: pointer;
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.chat-card {
  flex: 1;
  min-width: 0;
}

.chat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 520px;
  padding: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.msg-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-left {
  justify-content: flex-start;
}

.msg-right {
  justify-content: flex-end;
}

.msg-body {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.msg-right .msg-body {
  align-items: flex-end;
}

.msg-left .msg-body {
  align-items: flex-start;
}

.msg-bubble-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
}

.msg-right .msg-bubble {
  background: var(--el-color-primary-light-9);
}

.msg-sender {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
  padding: 0 4px;
}

.msg-read-status {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  padding: 0 4px;
}

.msg-time {
  margin-left: 8px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.msg-content {
  font-size: 14px;
  color: var(--el-text-color-primary);
  word-break: break-all;
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.address-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.address-item:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
}

.address-item-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.address-item-main {
  flex: 1;
  min-width: 0;
}

.address-item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.address-item-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

.address-item-phone {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.address-item-detail {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.no-address-tip {
  text-align: center;
  padding: 20px 0;
  color: var(--el-text-color-secondary);
}

.no-address-tip p {
  margin: 0 0 8px;
}

.address-add-row {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}
</style>
