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
            <span class="label">用户：</span>{{ detail.userName }}
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
              msg.senderType === 'MERCHANT' ? 'msg-right' : 'msg-left',
            ]"
          >
            <el-avatar
              v-if="msg.senderType !== 'MERCHANT'"
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
                  v-if="msg.senderType === 'MERCHANT'"
                  class="msg-read-status"
                  >{{ msg.isRead === 1 ? "已读" : "未读" }}</span
                >
                <div class="msg-bubble">
                  <div class="msg-content">{{ msg.content }}</div>
                </div>
              </div>
            </div>
            <el-avatar
              v-if="msg.senderType === 'MERCHANT'"
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getCustomizeRequestDetail,
  updateCustomizeRequestStatus,
  quoteCustomizeRequest,
  listCustomizeMessages,
  sendCustomizeMessage,
  markMessagesRead,
  markCancelledRead,
} from "@/api/customize";
import { getImageUrl } from "@/config/api";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();
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

let pollTimer = null;

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
    router.push(`/merchant/customize`);
  }
};

const goBack = () => {
  router.push("/merchant/customize");
};

const previewImage = (url) => {
  window.open(getImageUrl(url), "_blank");
};

onMounted(async () => {
  await fetchDetail();
  await fetchMessages();
  // 如果是已取消状态，商家标记已读消除红点
  if (detail.value.status === 'CANCELLED') {
    markCancelledRead(requestId).catch(() => {});
  }
  // 标记消息已读
  markMessagesRead(requestId).catch(() => {});
  pollTimer = setInterval(fetchMessages, 5000);
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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
</style>
