<template>
  <div class="info-page">
    <div class="section-container">
      <el-tabs
        v-model="activeTab"
        class="info-tabs"
        @tab-change="handleTabChange"
      >
        <el-tab-pane label="公告" name="announcement">
          <div v-loading="announcementLoading" class="info-list">
            <div
              v-if="announcementList.length === 0 && !announcementLoading"
              class="empty-state"
            >
              <el-empty description="暂无公告" />
            </div>
            <div
              v-for="item in announcementList"
              :key="item.id"
              class="info-card"
              @click="goAnnouncementDetail(item.id)"
            >
              <el-tag
                v-if="item.isTop === 1"
                type="danger"
                size="small"
                class="top-tag"
                >置顶</el-tag
              >
              <div class="info-card-title">{{ item.title }}</div>
              <div class="info-card-summary">
                {{ stripHtml(item.content) }}
              </div>
              <div class="info-card-footer">
                <span class="info-time"
                  >发布：{{ formatTime(item.createTime) }}</span
                >
                <span
                  v-if="item.updateTime && item.updateTime !== item.createTime"
                  class="info-time"
                  >更新：{{ formatTime(item.updateTime) }}</span
                >
              </div>
            </div>
          </div>
          <AppPagination
            v-model:current-page="announcementPage"
            v-model:page-size="announcementSize"
            :total="announcementTotal"
            @size-change="handleAnnouncementSizeChange"
            @current-change="handleAnnouncementCurrentChange"
          />
        </el-tab-pane>

        <el-tab-pane label="非遗故事" name="story">
          <div v-loading="storyLoading" class="info-list">
            <div
              v-if="storyList.length === 0 && !storyLoading"
              class="empty-state"
            >
              <el-empty description="暂无非遗故事" />
            </div>
            <div
              v-for="item in storyList"
              :key="item.id"
              class="info-card"
              @click="goStoryDetail(item.id)"
            >
              <el-tag
                v-if="item.isTop === 1"
                type="danger"
                size="small"
                class="top-tag"
                >置顶</el-tag
              >
              <div class="info-card-title">
                {{ item.title }}
                <el-tag
                  v-if="item.heritageProjectName"
                  type="info"
                  size="small"
                  class="project-tag"
                >
                  {{ item.heritageProjectName }}
                </el-tag>
              </div>
              <div class="info-card-summary">
                {{ stripHtml(item.content) }}
              </div>
              <div class="info-card-footer">
                <span class="info-time"
                  >发布：{{ formatTime(item.createTime) }}</span
                >
                <span
                  v-if="item.updateTime && item.updateTime !== item.createTime"
                  class="info-time"
                  >更新：{{ formatTime(item.updateTime) }}</span
                >
              </div>
            </div>
          </div>
          <AppPagination
            v-model:current-page="storyPage"
            v-model:page-size="storySize"
            :total="storyTotal"
            @size-change="handleStorySizeChange"
            @current-change="handleStoryCurrentChange"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getAnnouncementPage, getHeritageStoryPage } from "@/api/info";
import AppPagination from "@/components/AppPagination.vue";

const router = useRouter();

const activeTab = ref("announcement");

// 公告
const announcementLoading = ref(false);
const announcementList = ref([]);
const announcementTotal = ref(0);
const announcementPage = ref(1);
const announcementSize = ref(10);

// 非遗故事
const storyLoading = ref(false);
const storyList = ref([]);
const storyTotal = ref(0);
const storyPage = ref(1);
const storySize = ref(10);

// 从路由 query 恢复状态
const route = useRoute();
if (route.query.tab) {
  activeTab.value = route.query.tab;
}
if (route.query.page) {
  const p = parseInt(route.query.page);
  if (p > 0) {
    if (route.query.tab === "story") {
      storyPage.value = p;
    } else {
      announcementPage.value = p;
    }
  }
}
if (route.query.size) {
  const s = parseInt(route.query.size);
  if (s > 0) {
    if (route.query.tab === "story") {
      storySize.value = s;
    } else {
      announcementSize.value = s;
    }
  }
}

const loadAnnouncements = async () => {
  announcementLoading.value = true;
  try {
    const res = await getAnnouncementPage({
      page: announcementPage.value,
      size: announcementSize.value,
    });
    if (res.code === 200) {
      announcementList.value = res.data.records || [];
      announcementTotal.value = res.data.total || 0;
    }
  } catch (e) {
    // ignore
  } finally {
    announcementLoading.value = false;
  }
};

const loadStories = async () => {
  storyLoading.value = true;
  try {
    const res = await getHeritageStoryPage({
      page: storyPage.value,
      size: storySize.value,
    });
    if (res.code === 200) {
      storyList.value = res.data.records || [];
      storyTotal.value = res.data.total || 0;
    }
  } catch (e) {
    // ignore
  } finally {
    storyLoading.value = false;
  }
};

const goAnnouncementDetail = (id) => {
  router.push({
    path: `/info/announcements/${id}`,
    query: {
      tab: "announcement",
      page: announcementPage.value,
      size: announcementSize.value,
    },
  });
};

const goStoryDetail = (id) => {
  router.push({
    path: `/info/stories/${id}`,
    query: { tab: "story", page: storyPage.value, size: storySize.value },
  });
};

const handleTabChange = (tab) => {
  if (tab === "story" && storyList.value.length === 0) {
    loadStories();
  }
};

const handleAnnouncementSizeChange = () => {
  announcementPage.value = 1;
  loadAnnouncements();
};
const handleAnnouncementCurrentChange = () => {
  loadAnnouncements();
};
const handleStorySizeChange = () => {
  storyPage.value = 1;
  loadStories();
};
const handleStoryCurrentChange = () => {
  loadStories();
};

const stripHtml = (html) => {
  if (!html) return "";
  return html.replace(/<[^>]+>/g, "").substring(0, 120);
};

const formatTime = (time) => {
  if (!time) return "";
  return time.replace("T", " ").substring(0, 16);
};

onMounted(() => {
  loadAnnouncements();
  if (activeTab.value === "story") {
    loadStories();
  }
});
</script>

<style scoped>
.info-page {
  min-height: calc(100vh - 60px);
  background-color: var(--bg-page);
}

.section-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px;
}

.info-tabs {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 0 20px 20px;
}

.info-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.info-tabs :deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
}

.info-tabs :deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  line-height: 48px;
}

.info-list {
  min-height: 200px;
}

.info-card {
  position: relative;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color-lighter, #ebeef5);
  cursor: pointer;
  transition: background 0.2s;
}

.info-card:hover {
  background: var(--fill-color-light, #f5f7fa);
  border-radius: 8px;
}

.info-card:last-child {
  border-bottom: none;
}

.top-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.info-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 50px;
}

.project-tag {
  flex-shrink: 0;
}

.info-card-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 12px;
}

.info-card-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-time {
  font-size: 12px;
  color: var(--text-placeholder);
}

.empty-state {
  padding: 60px 0;
}
</style>
