<template>
  <div class="detail-page">
    <div class="section-container">
      <el-card class="detail-card" shadow="never" v-loading="loading">
        <el-tag
          v-if="detail && detail.isTop === 1"
          type="danger"
          size="small"
          class="top-tag"
          >置顶</el-tag
        >
        <div class="back-bar" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <template v-if="detail">
          <div class="detail-header">
            <h1 class="detail-title">{{ detail.title }}</h1>
            <div class="detail-meta">
              <span class="detail-time"
                >发布时间：{{ formatTime(detail.createTime) }}</span
              >
              <span
                v-if="
                  detail.updateTime && detail.updateTime !== detail.createTime
                "
                class="detail-time"
                >最近更新：{{ formatTime(detail.updateTime) }}</span
              >
            </div>
          </div>
          <el-divider />
          <div
            class="detail-content rich-text-content"
            v-html="detail.content"
          ></div>
        </template>
        <el-empty v-else-if="!loading" description="公告不存在" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";
import { getAnnouncementDetail } from "@/api/info";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const detail = ref(null);

const loadDetail = async () => {
  const id = route.params.id;
  if (!id) return;

  loading.value = true;
  try {
    const res = await getAnnouncementDetail(id);
    if (res.code === 200) {
      detail.value = res.data;
    }
  } catch (e) {
    // ignore
  } finally {
    loading.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return "";
  return time.replace("T", " ").substring(0, 16);
};

const goBack = () => {
  const tab = route.query.tab || "announcement";
  const page = route.query.page || 1;
  const size = route.query.size || 10;
  router.push({ path: "/info", query: { tab, page, size } });
};

onMounted(() => {
  loadDetail();
});
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 60px);
  background-color: var(--bg-page);
}

.section-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px;
}

.detail-card {
  border-radius: 8px;
  position: relative;
}

.top-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1;
}

.detail-header {
  text-align: center;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 12px 0 8px;
  padding-right: 50px;
}

.detail-meta {
  font-size: 13px;
  color: var(--text-placeholder);
}

.detail-time {
  margin-right: 12px;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular);
  word-break: break-word;
}

.rich-text-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 12px 0;
}

.rich-text-content :deep(p) {
  margin: 8px 0;
}

.back-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
  transition: color 0.2s;
  width: fit-content;
}

.back-bar:hover {
  color: var(--el-color-primary);
}
</style>
