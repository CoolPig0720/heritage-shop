<template>
  <div class="info-manage">
    <el-card class="table-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 公告管理 Tab -->
        <el-tab-pane label="公告管理" name="announcement">
          <div class="table-header">
            <div class="search-filters">
              <el-select
                v-model="announcementStatus"
                placeholder="状态"
                class="type-select"
                clearable
                @change="loadAnnouncements"
              >
                <el-option label="已发布" :value="1" />
                <el-option label="草稿" :value="0" />
              </el-select>
              <el-input
                v-model="announcementKeyword"
                placeholder="搜索公告标题"
                class="search-input"
                clearable
                @keyup.enter="loadAnnouncements"
                @clear="loadAnnouncements"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="loadAnnouncements">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </div>
            <el-button type="primary" @click="openAnnouncementDialog()">
              <el-icon><Plus /></el-icon>
              新增公告
            </el-button>
          </div>

          <el-table
            v-loading="announcementLoading"
            :data="announcementList"
            style="width: 100%"
          >
            <el-table-column label="序号" width="80" align="center">
              <template #default="{ $index }">{{
                (announcementPage - 1) * announcementSize + $index + 1
              }}</template>
            </el-table-column>
            <el-table-column
              prop="title"
              label="标题"
              min-width="200"
              align="center"
              show-overflow-tooltip
            />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'info'"
                  size="small"
                  >{{ row.status === 1 ? "已发布" : "草稿" }}</el-tag
                >
              </template>
            </el-table-column>
            <el-table-column label="置顶" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.isTop === 1"
                  inline-prompt
                  @change="(val) => toggleAnnouncementTop(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="170" align="center">
              <template #default="{ row }">{{
                formatTime(row.createTime)
              }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button
                    type="primary"
                    plain
                    round
                    size="small"
                    @click="openAnnouncementDialog(row)"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    plain
                    round
                    size="small"
                    @click="handleDeleteAnnouncement(row.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <AppPagination
            v-model:current-page="announcementPage"
            v-model:page-size="announcementSize"
            :total="announcementTotal"
            @size-change="handleAnnouncementSizeChange"
            @current-change="handleAnnouncementCurrentChange"
          />
        </el-tab-pane>

        <!-- 非遗故事管理 Tab -->
        <el-tab-pane label="非遗故事管理" name="story">
          <div class="table-header">
            <div class="search-filters">
              <el-select
                v-model="storyStatus"
                placeholder="状态"
                class="type-select"
                clearable
                @change="loadStories"
              >
                <el-option label="已发布" :value="1" />
                <el-option label="草稿" :value="0" />
              </el-select>
              <el-input
                v-model="storyKeyword"
                placeholder="搜索故事标题"
                class="search-input"
                clearable
                @keyup.enter="loadStories"
                @clear="loadStories"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="loadStories">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </div>
            <el-button type="primary" @click="openStoryDialog()">
              <el-icon><Plus /></el-icon>
              新增故事
            </el-button>
          </div>

          <el-table
            v-loading="storyLoading"
            :data="storyList"
            style="width: 100%"
          >
            <el-table-column label="序号" width="80" align="center">
              <template #default="{ $index }">{{
                (storyPage - 1) * storySize + $index + 1
              }}</template>
            </el-table-column>
            <el-table-column
              prop="title"
              label="标题"
              min-width="200"
              align="center"
              show-overflow-tooltip
            />
            <el-table-column
              prop="heritageProjectName"
              label="关联项目"
              width="160"
              show-overflow-tooltip
            >
              <template #default="{ row }">{{
                row.heritageProjectName || "-"
              }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'info'"
                  size="small"
                  >{{ row.status === 1 ? "已发布" : "草稿" }}</el-tag
                >
              </template>
            </el-table-column>
            <el-table-column label="置顶" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.isTop === 1"
                  inline-prompt
                  @change="(val) => toggleStoryTop(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="170" align="center">
              <template #default="{ row }">{{
                formatTime(row.createTime)
              }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button
                    type="primary"
                    plain
                    round
                    size="small"
                    @click="openStoryDialog(row)"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    plain
                    round
                    size="small"
                    @click="handleDeleteStory(row.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <AppPagination
            v-model:current-page="storyPage"
            v-model:page-size="storySize"
            :total="storyTotal"
            @size-change="handleStorySizeChange"
            @current-change="handleStoryCurrentChange"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 公告新增/编辑弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="announcementForm.id ? '编辑公告' : '新增公告'"
      fullscreen
      destroy-on-close
    >
      <el-form :model="announcementForm" label-width="80px">
        <div class="form-top-row">
          <el-form-item label="标题" required class="form-title-item">
            <el-input
              v-model="announcementForm.title"
              placeholder="请输入公告标题"
              maxlength="200"
            />
          </el-form-item>
          <el-form-item label="状态" class="form-inline-item">
            <el-radio-group v-model="announcementForm.status">
              <el-radio :value="1">发布</el-radio>
              <el-radio :value="0">草稿</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="置顶" class="form-inline-item">
            <el-switch
              v-model="announcementForm.isTop"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </div>
        <el-form-item label="内容" required>
          <RichTextEditor v-model="announcementForm.content" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="announcementDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="announcementSubmitting"
          @click="submitAnnouncement"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <!-- 非遗故事新增/编辑弹窗 -->
    <el-dialog
      v-model="storyDialogVisible"
      :title="storyForm.id ? '编辑非遗故事' : '新增非遗故事'"
      fullscreen
      destroy-on-close
    >
      <el-form :model="storyForm" label-width="100px">
        <div class="form-top-row">
          <el-form-item label="标题" required class="form-title-item">
            <el-input
              v-model="storyForm.title"
              placeholder="请输入故事标题"
              maxlength="200"
            />
          </el-form-item>
          <el-form-item label="状态" class="form-inline-item">
            <el-radio-group v-model="storyForm.status">
              <el-radio :value="1">发布</el-radio>
              <el-radio :value="0">草稿</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="置顶" class="form-inline-item">
            <el-switch
              v-model="storyForm.isTop"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </div>
        <el-form-item label="内容" required>
          <RichTextEditor v-model="storyForm.content" />
        </el-form-item>
        <el-form-item label="关联非遗项目">
          <el-select
            v-model="storyForm.heritageProjectId"
            placeholder="选择关联非遗项目（可选）"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="p in heritageProjectOptions"
              :key="p.id"
              :label="p.name"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="storySubmitting"
          @click="submitStory"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search } from "@element-plus/icons-vue";
import AppPagination from "@/components/AppPagination.vue";
import RichTextEditor from "@/components/RichTextEditor.vue";
import {
  pageAdminAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  pageAdminHeritageStories,
  createHeritageStory,
  updateHeritageStory,
  deleteHeritageStory,
} from "@/api/adminInfo";
import { pageHeritageProjects } from "@/api/heritage";

const activeTab = ref("announcement");

// ============ 公告管理 ============
const announcementLoading = ref(false);
const announcementList = ref([]);
const announcementTotal = ref(0);
const announcementPage = ref(1);
const announcementSize = ref(10);
const announcementKeyword = ref("");
const announcementStatus = ref(null);

const announcementDialogVisible = ref(false);
const announcementSubmitting = ref(false);
const announcementForm = ref({
  id: null,
  title: "",
  content: "",
  status: 1,
  isTop: 0,
});

const loadAnnouncements = async () => {
  announcementLoading.value = true;
  try {
    const params = {
      page: announcementPage.value,
      size: announcementSize.value,
      keyword: announcementKeyword.value || undefined,
    };
    if (announcementStatus.value !== null && announcementStatus.value !== "")
      params.status = announcementStatus.value;
    const res = await pageAdminAnnouncements(params);
    if (res.code === 200) {
      announcementList.value = res.data.records || [];
      announcementTotal.value = res.data.total || 0;
    }
  } catch (e) {
    /* ignore */
  } finally {
    announcementLoading.value = false;
  }
};

const openAnnouncementDialog = (row = null) => {
  if (row) {
    announcementForm.value = {
      id: row.id,
      title: row.title,
      content: row.content,
      status: row.status,
      isTop: row.isTop,
    };
  } else {
    announcementForm.value = {
      id: null,
      title: "",
      content: "",
      status: 1,
      isTop: 0,
    };
  }
  announcementDialogVisible.value = true;
};

const submitAnnouncement = async () => {
  const form = announcementForm.value;
  if (!form.title.trim()) {
    ElMessage.warning("请输入标题");
    return;
  }
  if (!form.content.trim()) {
    ElMessage.warning("请输入内容");
    return;
  }

  announcementSubmitting.value = true;
  try {
    const data = {
      title: form.title.trim(),
      content: form.content,
      status: form.status,
      isTop: form.isTop,
    };
    let res;
    if (form.id) {
      res = await updateAnnouncement(form.id, data);
    } else {
      res = await createAnnouncement(data);
    }
    if (res.code === 200) {
      ElMessage.success(form.id ? "修改成功" : "新增成功");
      announcementDialogVisible.value = false;
      loadAnnouncements();
    } else {
      ElMessage.error(res.message || "操作失败");
    }
  } catch (e) {
    ElMessage.error("操作失败");
  } finally {
    announcementSubmitting.value = false;
  }
};

const handleDeleteAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除该公告？", "提示", { type: "warning" });
    const res = await deleteAnnouncement(id);
    if (res.code === 200) {
      ElMessage.success("删除成功");
      loadAnnouncements();
    } else {
      ElMessage.error(res.message || "删除失败");
    }
  } catch (e) {
    /* cancel */
  }
};

const toggleAnnouncementTop = async (row, val) => {
  try {
    const res = await updateAnnouncement(row.id, { isTop: val ? 1 : 0 });
    if (res.code === 200) {
      ElMessage.success(val ? "已置顶" : "已取消置顶");
      loadAnnouncements();
    }
  } catch (e) {
    /* ignore */
  }
};

// ============ 非遗故事管理 ============
const storyLoading = ref(false);
const storyList = ref([]);
const storyTotal = ref(0);
const storyPage = ref(1);
const storySize = ref(10);
const storyKeyword = ref("");
const storyStatus = ref(null);

const storyDialogVisible = ref(false);
const storySubmitting = ref(false);
const storyForm = ref({
  id: null,
  title: "",
  content: "",
  heritageProjectId: null,
  status: 1,
  isTop: 0,
});
const heritageProjectOptions = ref([]);

const loadStories = async () => {
  storyLoading.value = true;
  try {
    const params = {
      page: storyPage.value,
      size: storySize.value,
      keyword: storyKeyword.value || undefined,
    };
    if (storyStatus.value !== null && storyStatus.value !== "")
      params.status = storyStatus.value;
    const res = await pageAdminHeritageStories(params);
    if (res.code === 200) {
      storyList.value = res.data.records || [];
      storyTotal.value = res.data.total || 0;
    }
  } catch (e) {
    /* ignore */
  } finally {
    storyLoading.value = false;
  }
};

const openStoryDialog = (row = null) => {
  if (row) {
    storyForm.value = {
      id: row.id,
      title: row.title,
      content: row.content,
      heritageProjectId: row.heritageProjectId,
      status: row.status,
      isTop: row.isTop,
    };
  } else {
    storyForm.value = {
      id: null,
      title: "",
      content: "",
      heritageProjectId: null,
      status: 1,
      isTop: 0,
    };
  }
  storyDialogVisible.value = true;
};

const submitStory = async () => {
  const form = storyForm.value;
  if (!form.title.trim()) {
    ElMessage.warning("请输入标题");
    return;
  }
  if (!form.content.trim()) {
    ElMessage.warning("请输入内容");
    return;
  }

  storySubmitting.value = true;
  try {
    const data = {
      title: form.title.trim(),
      content: form.content,
      heritageProjectId: form.heritageProjectId,
      status: form.status,
      isTop: form.isTop,
    };
    let res;
    if (form.id) {
      res = await updateHeritageStory(form.id, data);
    } else {
      res = await createHeritageStory(data);
    }
    if (res.code === 200) {
      ElMessage.success(form.id ? "修改成功" : "新增成功");
      storyDialogVisible.value = false;
      loadStories();
    } else {
      ElMessage.error(res.message || "操作失败");
    }
  } catch (e) {
    ElMessage.error("操作失败");
  } finally {
    storySubmitting.value = false;
  }
};

const handleDeleteStory = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除该故事？", "提示", { type: "warning" });
    const res = await deleteHeritageStory(id);
    if (res.code === 200) {
      ElMessage.success("删除成功");
      loadStories();
    } else {
      ElMessage.error(res.message || "删除失败");
    }
  } catch (e) {
    /* cancel */
  }
};

const toggleStoryTop = async (row, val) => {
  try {
    const res = await updateHeritageStory(row.id, { isTop: val ? 1 : 0 });
    if (res.code === 200) {
      ElMessage.success(val ? "已置顶" : "已取消置顶");
      loadStories();
    }
  } catch (e) {
    /* ignore */
  }
};

// ============ 非遗项目选项 ============
const loadHeritageProjectOptions = async () => {
  try {
    const res = await pageHeritageProjects({ page: 1, size: 200 });
    if (res.code === 200) {
      heritageProjectOptions.value = res.data.records || [];
    }
  } catch (e) {
    /* ignore */
  }
};

// ============ 通用 ============
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

const handleTabChange = (tab) => {
  if (tab === "announcement" && announcementList.value.length === 0)
    loadAnnouncements();
  if (tab === "story" && storyList.value.length === 0) loadStories();
};

const formatTime = (time) => {
  if (!time) return "";
  return time.replace("T", " ").substring(0, 16);
};

onMounted(() => {
  loadAnnouncements();
  loadHeritageProjectOptions();
});
</script>

<style scoped>
.info-manage {
  padding: 20px;
}

.type-select {
  width: 120px;
}

.form-top-row {
  display: flex;
  align-items: flex-start;
  gap: 0;
}

.form-top-row .form-title-item {
  flex: 0 0 50%;
}

.form-top-row .form-inline-item {
  flex-shrink: 0;
}

.info-manage :deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
}

.info-manage :deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  height: 46px;
  line-height: 46px;
}
</style>
