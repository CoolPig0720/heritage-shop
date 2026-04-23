<template>
  <div class="requests-page">
    <div class="section-container">
      <el-card class="list-card table-card" shadow="never">
        <div class="filter-row">
          <el-tabs
            v-model="activeStatus"
            @tab-change="handleTabChange"
            class="status-tabs"
          >
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane name="PENDING">
              <template #label>待报价</template>
            </el-tab-pane>
            <el-tab-pane name="QUOTED">
              <template #label
                >已报价<el-badge
                  v-if="unreadCounts.quotedCount > 0"
                  :value="unreadCounts.quotedCount"
                  :max="99"
                  class="tab-badge"
              /></template>
            </el-tab-pane>
            <el-tab-pane label="已确认" name="CONFIRMED" />
            <el-tab-pane name="COMPLETED">
              <template #label
                >已完成<el-badge
                  v-if="unreadCounts.completedCount > 0"
                  :value="unreadCounts.completedCount"
                  :max="99"
                  class="tab-badge"
              /></template>
            </el-tab-pane>
            <el-tab-pane label="已取消" name="CANCELLED" />
          </el-tabs>
          <div class="filter-spacer" />
          <el-input
            v-model="keyword"
            placeholder="搜索定制标题"
            clearable
            style="width: 200px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <span class="result-count">共 {{ total }} 条</span>
        </div>

        <el-table v-loading="loading" :data="requests" style="width: 100%">
          <el-table-column
            label="序号"
            width="80"
            :index="indexMethod"
            type="index"
            align="center"
            header-align="center"
          />
          <el-table-column
            label="定制标题"
            min-width="200"
            align="center"
            header-align="center"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <el-link
                type="primary"
                :underline="false"
                @click="goDetail(row.id)"
                >{{ row.title }}</el-link
              >
            </template>
          </el-table-column>
          <el-table-column
            prop="merchantName"
            label="商家"
            width="120"
            align="center"
            header-align="center"
          />
          <el-table-column
            label="状态"
            width="120"
            align="center"
            header-align="center"
          >
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{
                statusLabel(row.status)
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="报价"
            width="110"
            align="center"
            header-align="center"
          >
            <template #default="{ row }">
              <span v-if="row.quotedPrice">¥{{ row.quotedPrice }}</span>
              <span v-else class="text-muted">--</span>
            </template>
          </el-table-column>
          <el-table-column
            label="创建时间"
            width="180"
            align="center"
            header-align="center"
          >
            <template #default="{ row }"
              ><span class="time-text">{{
                formatTime(row.createTime)
              }}</span></template
            >
          </el-table-column>
          <el-table-column
            label="操作"
            width="120"
            align="center"
            header-align="center"
          >
            <template #default="{ row }">
              <div class="row-actions">
                <el-badge
                  :value="row.unreadCount"
                  :hidden="!row.unreadCount"
                  :max="99"
                >
                  <el-button
                    type="primary"
                    plain
                    round
                    size="small"
                    @click="goDetail(row.id)"
                    >查看详情</el-button
                  >
                </el-badge>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <AppPagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { pageMyRequests, getCustomizeUnreadCount } from "@/api/customize";
import AppPagination from "@/components/AppPagination.vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";

const router = useRouter();

const loading = ref(false);
const requests = ref([]);
const page = ref(1);
const size = ref(5);
const total = ref(0);
const activeStatus = ref("all");
const keyword = ref("");
const unreadCounts = ref({
  pendingCount: 0,
  quotedCount: 0,
  confirmedCount: 0,
});

const statusType = (s) => {
  const map = {
    PENDING: "info",
    QUOTED: "warning",
    CONFIRMED: "",
    COMPLETED: "success",
    CANCELLED: "danger",
  };
  return map[s] || "info";
};

const statusLabel = (s) => {
  const map = {
    PENDING: "待报价",
    QUOTED: "已报价",
    CONFIRMED: "已确认",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
  };
  return map[s] || s;
};

const formatTime = (t) => {
  if (!t) return "--";
  return t.replace("T", " ").substring(0, 19);
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = { page: page.value, size: size.value };
    if (activeStatus.value !== "all") {
      params.status = activeStatus.value;
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim();
    }
    const res = await pageMyRequests(params);
    requests.value = res?.data?.records || [];
    total.value = res?.data?.total || 0;
  } catch (e) {
    ElMessage.error("获取定制列表失败");
  } finally {
    loading.value = false;
  }
};

const fetchUnread = async () => {
  try {
    const res = await getCustomizeUnreadCount();
    if (res.code === 200 && res.data) {
      unreadCounts.value = res.data;
    }
  } catch {
    // ignore
  }
};

const handleTabChange = () => {
  page.value = 1;
  fetchData();
};

const handleSearch = () => {
  page.value = 1;
  fetchData();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchData();
};

const handleCurrentChange = () => {
  fetchData();
};

const indexMethod = (index) => (page.value - 1) * size.value + index + 1;

const goDetail = (id) => {
  router.push(`/customize/requests/${id}`);
};

onMounted(() => {
  fetchData();
  fetchUnread();
});
</script>

<style scoped>
.requests-page {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--app-bg);
}

.section-container {
  max-width: var(--app-max-width);
  margin: 0 auto;
}

.list-card {
  border-radius: var(--app-radius);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.filter-spacer {
  flex: 1;
}

.result-count {
  color: #909399;
  font-size: 13px;
}

.status-tabs {
  flex-shrink: 0;
  --el-tabs-header-height: 40px;
}

.status-tabs :deep(.el-tabs__nav-wrap) {
  background-color: transparent !important;
  border-radius: 8px;
  padding: 4px;
}

.status-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 0;
}

.status-tabs :deep(.el-tabs__item) {
  transition: all 0.3s ease !important;
  border-radius: 6px !important;
  margin: 0 !important;
  padding: 0 16px !important;
  color: #606266 !important;
  font-weight: normal !important;
  position: relative !important;
  border: none !important;
  background: transparent !important;
  height: 40px !important;
  line-height: 40px !important;
}

/* 暗色模式下的 tab 文字颜色 */
html.dark .status-tabs :deep(.el-tabs__item) {
  color: #c0c4cc !important;
}

/* 激活状态的 tab */
.status-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff !important;
  font-weight: 500 !important;
  background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 暗色模式下激活 tab */
html.dark .status-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.08) !important;
}

/* hover 状态 */
.status-tabs :deep(.el-tabs__item:hover) {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.03) !important;
}

/* 暗色模式下 hover */
html.dark .status-tabs :deep(.el-tabs__item:hover) {
  background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 底部指示线 */
.status-tabs :deep(.el-tabs__active-bar) {
  display: none !important;
}

.status-tabs :deep(.el-tabs__item.is-active)::after {
  content: "" !important;
  position: absolute !important;
  bottom: 0 !important;
  left: 0 !important;
  transform: none !important;
  width: 100% !important;
  height: 1px !important;
  background-color: #409eff !important;
  border-radius: 1px !important;
  opacity: 0.7 !important;
}

.status-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.status-tabs :deep(.el-tabs__content) {
  display: none;
}

.text-muted {
  color: var(--el-text-color-placeholder);
}

.row-actions {
  display: flex;
  justify-content: center;
}

:deep(.el-table__body td .cell) {
  overflow: visible;
}

:deep(.el-badge) {
  overflow: visible;
}

.time-text {
  white-space: nowrap;
}

:deep(.el-table__header th .cell) {
  white-space: nowrap;
}

:deep(.el-table__body .el-tag) {
  white-space: nowrap;
}

.tab-badge {
  margin-left: 6px;
}

.tab-badge :deep(.el-badge__content) {
  position: relative;
  top: 0;
  right: 0;
  transform: none;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 600;
  border: none;
}
</style>
