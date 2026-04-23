<template>
  <div class="product-comment" v-if="productId">
    <el-card class="comment-card" shadow="never">
      <template #header>
        <div class="comment-header">
          <h3>评论区</h3>
          <span class="comment-count" v-if="total > 0"
            >共 {{ total }} 条评论</span
          >
        </div>
      </template>

      <!-- 评分统计和发表评论 -->
      <div class="rating-and-input" v-if="ratingData.ratingCount > 0">
        <!-- 左侧：评分分布 -->
        <div class="rating-summary-left">
          <div class="rating-big-score">{{ ratingData.avgRating }}</div>
          <el-rate
            :model-value="ratingData.avgRating"
            disabled
            :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
            size="small"
          />
          <div class="rating-total">{{ ratingData.ratingCount }}人评价</div>
        </div>

        <!-- 中间：评分分布条形图 -->
        <div class="rating-summary-middle">
          <div
            v-for="star in [5, 4, 3, 2, 1]"
            :key="star"
            class="rating-bar-row"
          >
            <span class="rating-bar-label">{{ star }}星</span>
            <div class="rating-bar-track">
              <div
                class="rating-bar-fill"
                :style="{
                  width:
                    ratingData.ratingCount > 0
                      ? ((ratingData.distribution[star] || 0) /
                          ratingData.ratingCount) *
                          100 +
                        '%'
                      : '0%',
                }"
              ></div>
            </div>
            <span class="rating-bar-count">{{
              ratingData.distribution[star] || 0
            }}</span>
          </div>
        </div>

        <!-- 右侧：发表评论 -->
        <div class="comment-input-section">
          <div class="comment-input-box">
            <el-avatar :size="36" :src="currentUserAvatar" class="comment-avatar">
              <el-icon :size="18"><User /></el-icon>
            </el-avatar>
            <div class="comment-input-wrapper">
              <div class="rating-input-row" v-if="token">
                <span class="rating-input-label">商品评分：</span>
                <el-rate
                  v-model="myRating"
                  :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                  size="default"
                />
              </div>
              <el-input
                v-model="newCommentContent"
                type="textarea"
                :placeholder="token ? '发表评论...' : '请先登录后再发表评论'"
                :rows="2"
                :disabled="!token"
                resize="none"
                @keydown.enter.exact.prevent="submitComment"
                class="comment-text-input"
              />
              <div class="comment-input-actions">
                <el-button
                  v-if="myRating > 0 && !myRatingSubmitted && token"
                  size="small"
                  :loading="ratingSubmitting"
                  @click="submitRatingOnly"
                >
                  提交评分
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :loading="submitting"
                  :disabled="!token || !newCommentContent.trim()"
                  @click="submitComment"
                >
                  发表
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 无评分时显示原来的发表评论 -->
      <div class="rating-and-input no-rating" v-else>
        <div class="comment-input-section full-width">
          <div class="comment-input-box">
            <el-avatar :size="36" :src="currentUserAvatar" class="comment-avatar">
              <el-icon :size="18"><User /></el-icon>
            </el-avatar>
            <div class="comment-input-wrapper">
              <div class="rating-input-row" v-if="token">
                <span class="rating-input-label">商品评分：</span>
                <el-rate
                  v-model="myRating"
                  :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                  size="default"
                />
              </div>
              <el-input
                v-model="newCommentContent"
                type="textarea"
                :placeholder="token ? '发表评论...' : '请先登录后再发表评论'"
                :rows="2"
                :disabled="!token"
                resize="none"
                @keydown.enter.exact.prevent="submitComment"
                class="comment-text-input"
              />
              <div class="comment-input-actions">
                <el-button
                  v-if="myRating > 0 && !myRatingSubmitted && token"
                  size="small"
                  :loading="ratingSubmitting"
                  @click="submitRatingOnly"
                >
                  提交评分
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :loading="submitting"
                  :disabled="!token || !newCommentContent.trim()"
                  @click="submitComment"
                >
                  发表
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list" v-loading="loading">
        <div v-if="!loading && comments.length === 0" class="comment-empty">
          暂无评论，快来抢沙发吧！
        </div>

        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-main">
            <el-avatar
              :size="36"
              :src="normalizeAvatar(comment.userAvatar)"
              class="comment-avatar"
            >
              <el-icon :size="18"><User /></el-icon>
            </el-avatar>
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-username">{{ comment.userName }}</span>
                <span class="comment-time">{{
                  formatTime(comment.createTime)
                }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <span
                  class="action-btn"
                  :class="{ liked: comment.liked }"
                  @click="handleLike(comment)"
                >
                  <el-icon :size="14"><Star /></el-icon>
                  {{ comment.likeCount > 0 ? comment.likeCount : "" }}
                </span>
                <span class="action-btn" @click="toggleReplyInput(comment)">
                  <el-icon :size="14"><ChatDotRound /></el-icon>
                  回复
                </span>
                <span
                  v-if="canDelete(comment)"
                  class="action-btn delete-btn"
                  @click="handleDelete(comment)"
                >
                  <el-icon :size="14"><Delete /></el-icon>
                  删除
                </span>
              </div>

              <!-- 回复列表 -->
              <div
                v-if="comment.replyCount > 0 || replyingTo === comment.id"
                class="replies-section"
              >
                <div
                  v-for="reply in repliesMap[comment.id] || []"
                  :key="reply.id"
                  class="reply-item"
                >
                  <el-avatar
                    :size="28"
                    :src="normalizeAvatar(reply.userAvatar)"
                    class="reply-avatar"
                  >
                    <el-icon :size="14"><User /></el-icon>
                  </el-avatar>
                  <div class="reply-body">
                    <span class="reply-username">{{ reply.userName }}</span>
                    <template v-if="reply.replyToUserName">
                      <span class="reply-text"> 回复 </span>
                      <span class="reply-username"
                        >@{{ reply.replyToUserName }}</span
                      >
                    </template>
                    <span class="reply-text">：{{ reply.content }}</span>
                    <div class="comment-actions reply-actions">
                      <span
                        class="action-btn"
                        :class="{ liked: reply.liked }"
                        @click="handleLike(reply)"
                      >
                        <el-icon :size="14"><Star /></el-icon>
                        {{ reply.likeCount > 0 ? reply.likeCount : "" }}
                      </span>
                      <span
                        class="action-btn"
                        @click="toggleReplyInput(reply, comment)"
                      >
                        <el-icon :size="14"><ChatDotRound /></el-icon>
                        回复
                      </span>
                      <span
                        v-if="canDelete(reply)"
                        class="action-btn delete-btn"
                        @click="handleDelete(reply)"
                      >
                        <el-icon :size="14"><Delete /></el-icon>
                        删除
                      </span>
                    </div>
                  </div>
                </div>
                <div
                  v-if="
                    comment.replyCount > (repliesMap[comment.id] || []).length
                  "
                  class="load-more-replies"
                  @click="loadReplies(comment)"
                >
                  查看更多回复 ({{
                    comment.replyCount - (repliesMap[comment.id] || []).length
                  }})
                </div>

                <!-- 内联回复输入框（放在回复列表内部底部） -->
                <div v-if="replyingTo === comment.id" class="reply-input-area">
                  <el-input
                    ref="replyInputRef"
                    v-model="replyContent"
                    type="textarea"
                    :placeholder="`回复 @${replyToUser?.userName || comment.userName}...`"
                    :rows="2"
                    resize="none"
                    @keydown.enter.exact.prevent="submitReply(comment)"
                  />
                  <div class="reply-input-actions">
                    <el-button size="small" @click="cancelReply"
                      >取消</el-button
                    >
                    <el-button
                      type="primary"
                      size="small"
                      :loading="submitting"
                      :disabled="!replyContent.trim()"
                      @click="submitReply(comment)"
                    >
                      回复
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 顶级评论的回复输入框（没有回复时显示在这里） -->
              <div
                v-if="replyingTo === comment.id && comment.replyCount === 0"
                class="reply-input-area"
              >
                <el-input
                  ref="replyInputRef"
                  v-model="replyContent"
                  type="textarea"
                  :placeholder="`回复 @${comment.userName}...`"
                  :rows="2"
                  resize="none"
                  @keydown.enter.exact.prevent="submitReply(comment)"
                />
                <div class="reply-input-actions">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button
                    type="primary"
                    size="small"
                    :loading="submitting"
                    :disabled="!replyContent.trim()"
                    @click="submitReply(comment)"
                  >
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <el-button text @click="loadMore" :loading="loadingMore"
            >加载更多评论</el-button
          >
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { User, Star, ChatDotRound, Delete } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import { getAvatarUrl } from "@/config/api.js";
import {
  getProductComments,
  getCommentReplies,
  createComment,
  deleteComment,
  toggleCommentLike,
} from "@/api/comment";
import { getProductRating, submitRating, getMyRating } from "@/api/rating";

const props = defineProps({
  productId: {
    type: [Number, String],
    default: null,
  },
});

const userStore = useUserStore();
const token = computed(() => userStore.token);
const currentUserAvatar = computed(() => {
  return normalizeAvatar(userStore.userInfo?.avatar);
});
const currentUserId = computed(() => userStore.userInfo?.id);

const comments = ref([]);
const repliesMap = ref({});
const total = ref(0);
const currentPage = ref(1);
const pageSize = 10;
const loading = ref(false);
const loadingMore = ref(false);
const submitting = ref(false);

const newCommentContent = ref("");
const replyingTo = ref(null);
const replyContent = ref("");
const replyParentComment = ref(null);
const replyToUser = ref(null);

// 评分相关
const ratingData = ref({
  avgRating: 0,
  ratingCount: 0,
  distribution: {},
  myRating: null,
});
const myRating = ref(0);
const myRatingSubmitted = ref(false);
const ratingSubmitting = ref(false);

const hasMore = computed(() => comments.value.length < total.value);

const normalizeAvatar = (url) => {
  if (!url) return "";
  return getAvatarUrl(url);
};

const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "刚刚";
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

const canDelete = (comment) => {
  if (!token.value) return false;
  return (
    comment.userId === currentUserId.value ||
    userStore.userInfo?.role === "ADMIN"
  );
};

const loadComments = async (reset = false) => {
  if (!props.productId) return;
  if (reset) {
    currentPage.value = 1;
    comments.value = [];
    repliesMap.value = {};
  }

  loading.value = reset;
  loadingMore.value = !reset;

  try {
    const res = await getProductComments(props.productId, {
      page: currentPage.value,
      size: pageSize,
    });
    const page = res.data || {};
    total.value = page.total || 0;
    if (reset) {
      comments.value = page.records || [];
    } else {
      comments.value.push(...(page.records || []));
    }
  } catch (e) {
    if (reset) ElMessage.error("获取评论失败");
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

const loadRatingData = async () => {
  if (!props.productId) return;
  try {
    const res = await getProductRating(props.productId);
    const data = res.data || {};
    ratingData.value = data;
    if (data.myRating) {
      myRating.value = data.myRating;
      myRatingSubmitted.value = true;
    }
  } catch (e) {
    // 静默失败
  }
};

const submitRatingOnly = async () => {
  if (!token.value) {
    ElMessage.warning("请先登录");
    return;
  }
  if (myRating.value < 1) return;

  ratingSubmitting.value = true;
  try {
    await submitRating({ productId: props.productId, rating: myRating.value });
    ElMessage.success("评分成功");
    myRatingSubmitted.value = true;
    loadRatingData();
  } catch (e) {
    // 错误已在request拦截器中处理
  } finally {
    ratingSubmitting.value = false;
  }
};

const loadMore = () => {
  currentPage.value++;
  loadComments(false);
};

const loadReplies = async (comment) => {
  try {
    const existingReplies = repliesMap.value[comment.id] || [];
    const res = await getCommentReplies(comment.id, {
      page: 1,
      size: 100,
    });
    const page = res.data || {};
    repliesMap.value[comment.id] = page.records || [];
  } catch (e) {
    ElMessage.error("获取回复失败");
  }
};

const submitComment = async () => {
  if (!token.value) {
    ElMessage.warning("请先登录");
    return;
  }
  if (!newCommentContent.value.trim()) return;

  submitting.value = true;
  try {
    // 如果选择了评分且尚未提交，同步提交评分
    if (myRating.value > 0 && !myRatingSubmitted.value) {
      await submitRating({
        productId: props.productId,
        rating: myRating.value,
      });
      myRatingSubmitted.value = true;
    }
    await createComment({
      productId: props.productId,
      content: newCommentContent.value.trim(),
    });
    ElMessage.success("评论发表成功");
    newCommentContent.value = "";
    loadComments(true);
    loadRatingData();
  } catch (e) {
    // 错误已在request拦截器中处理
  } finally {
    submitting.value = false;
  }
};

const toggleReplyInput = (comment, parentComment = null) => {
  if (!token.value) {
    ElMessage.warning("请先登录");
    return;
  }
  // 如果是顶级评论的回复按钮
  if (!parentComment) {
    if (replyingTo.value === comment.id) {
      replyingTo.value = null;
      replyContent.value = "";
      replyParentComment.value = null;
      replyToUser.value = null;
      return;
    }
    replyingTo.value = comment.id;
    replyContent.value = `@${comment.userName} `;
    replyParentComment.value = comment;
    replyToUser.value = comment;
  } else {
    // 如果是二级回复的回复按钮，输入框仍挂在顶级评论下
    const topComment = parentComment;
    if (
      replyingTo.value === topComment.id &&
      replyToUser.value?.id === comment.id
    ) {
      replyingTo.value = null;
      replyContent.value = "";
      replyParentComment.value = null;
      replyToUser.value = null;
      return;
    }
    replyingTo.value = topComment.id;
    replyContent.value = `@${comment.userName} `;
    replyParentComment.value = topComment;
    replyToUser.value = comment;
  }
};

const cancelReply = () => {
  replyingTo.value = null;
  replyContent.value = "";
  replyParentComment.value = null;
  replyToUser.value = null;
};

const submitReply = async (comment) => {
  if (!token.value) {
    ElMessage.warning("请先登录");
    return;
  }

  const content = replyContent.value.trim();
  if (!content) return;

  const parentComment = replyParentComment.value;
  // replyToUser 是真正被回复的人（可能是顶级评论者，也可能是二级回复者）
  const targetUser = replyToUser.value || comment;

  submitting.value = true;
  try {
    await createComment({
      productId: props.productId,
      parentId: parentComment.id,
      replyToUserId: targetUser.userId,
      content: content,
    });
    ElMessage.success("回复发表成功");
    cancelReply();
    loadComments(true);
    // 同时重新加载该顶级评论的回复
    if (parentComment.id) {
      const res = await getCommentReplies(parentComment.id, {
        page: 1,
        size: 100,
      });
      const page = res.data || {};
      repliesMap.value[parentComment.id] = page.records || [];
    }
  } catch (e) {
    // 错误已在request拦截器中处理
  } finally {
    submitting.value = false;
  }
};

const handleLike = async (comment) => {
  if (!token.value) {
    ElMessage.warning("请先登录");
    return;
  }
  try {
    const res = await toggleCommentLike(comment.id);
    const data = res.data || {};
    comment.liked = data.liked;
    comment.likeCount = data.likeCount;
  } catch (e) {
    // 错误已在request拦截器中处理
  }
};

const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm("确定要删除这条评论吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteComment(comment.id);
    ElMessage.success("评论已删除");
    loadComments(true);
  } catch (e) {
    if (e !== "cancel") {
      // 错误已在request拦截器中处理
    }
  }
};

watch(
  () => props.productId,
  (newVal) => {
    if (newVal) {
      loadComments(true);
      loadRatingData();
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.product-comment {
  margin-top: 20px;
}

.comment-card {
  border-radius: var(--app-radius);
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  box-shadow: var(--card-shadow);
}

.comment-card :deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color-base);
}

.comment-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-color-primary);
}

.comment-count {
  font-size: 14px;
  color: var(--text-color-secondary);
}

/* 评分统计和发表评论 */
.rating-and-input {
  display: flex;
  gap: 24px;
  padding: 20px;
  margin-bottom: 20px;
  background: var(--bg-elevated);
  border-radius: 12px;
  border: 1px solid var(--border-color-base);
  align-items: stretch;
}

.rating-summary-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  padding-right: 20px;
  border-right: 1px solid var(--border-color-lighter);
}

.rating-big-score {
  font-size: 36px;
  font-weight: 700;
  color: #f7ba2a;
  line-height: 1;
  margin-bottom: 8px;
}

.rating-total {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-top: 4px;
}

.rating-summary-middle {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: center;
  padding-right: 20px;
  border-right: 1px solid var(--border-color-lighter);
}

/* 发表评论区域 */
.comment-input-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-width: 400px;
}

.comment-input-section.full-width {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.no-rating {
  justify-content: center;
}

.comment-input-box {
  display: flex;
  gap: 12px;
}

.comment-input-section .rating-input-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-input-section .comment-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.rating-bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-bar-label {
  font-size: 12px;
  color: var(--text-color-secondary);
  width: 28px;
  text-align: right;
}

.rating-bar-track {
  flex: 1;
  height: 8px;
  background: var(--border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.rating-bar-fill {
  height: 100%;
  background: #f7ba2a;
  border-radius: 4px;
  transition: width 0.3s;
}

.rating-bar-count {
  font-size: 12px;
  color: var(--text-color-secondary);
  width: 20px;
  text-align: left;
}

/* 评分输入 */
.rating-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.rating-input-label {
  font-size: 14px;
  color: var(--text-color-secondary);
  white-space: nowrap;
}

.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 发表评论 */
.comment-input-area {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color-lighter);
}

.comment-avatar {
  flex-shrink: 0;
  background: var(--bg-elevated);
}

.comment-input-wrapper {
  flex: 1;
}

.comment-input-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.comment-text-input {
  flex: 1;
}

.comment-text-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: var(--border-color-base);
  background: var(--bg-elevated);
  color: var(--text-color-primary);
}

.comment-text-input :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
}

.rating-submit-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
}

.rating-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.rating-input-label {
  font-size: 14px;
  color: var(--text-color-secondary);
}

.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 评论列表 */
.comment-list {
  min-height: 100px;
}

.comment-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color-lighter);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.comment-content {
  font-size: 14px;
  color: var(--text-color-primary);
  line-height: 1.6;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-color-secondary);
  cursor: pointer;
  transition: color 0.2s;
  user-select: none;
}

.action-btn:hover {
  color: var(--color-primary);
}

.action-btn.liked {
  color: #f56c6c;
}

.action-btn.liked:hover {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f56c6c;
}

/* 内联回复输入框 */
.reply-input-area {
  margin-top: 10px;
  padding: 12px;
  background: var(--bg-elevated);
  border-radius: 8px;
  border: 1px solid var(--border-color-base);
}

.reply-input-area :deep(.el-textarea__inner) {
  border-radius: 6px;
  border-color: var(--border-color-base);
  background: var(--bg-base);
  color: var(--text-color-primary);
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

/* 回复列表 */
.replies-section {
  margin-top: 12px;
  padding: 12px 16px;
  background: var(--bg-elevated);
  border-radius: 8px;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color-lighter);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-avatar {
  flex-shrink: 0;
  background: var(--bg-base);
}

.reply-body {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.reply-username {
  font-weight: 600;
  color: var(--text-color-primary);
  margin-right: 4px;
}

.reply-text {
  color: var(--text-color-secondary);
}

.reply-actions {
  margin-top: 4px;
  gap: 12px;
}

.load-more-replies {
  padding: 8px 0 4px;
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
  user-select: none;
}

.load-more-replies:hover {
  text-decoration: underline;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .comment-card :deep(.el-card__header) {
    padding: 12px 16px;
  }

  .comment-card :deep(.el-card__body) {
    padding: 16px;
  }

  .comment-input-area {
    flex-direction: column;
  }

  .comment-avatar {
    display: none;
  }

  .replies-section {
    padding: 8px 12px;
  }
}
</style>
