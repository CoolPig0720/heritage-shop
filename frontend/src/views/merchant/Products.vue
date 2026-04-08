<template>
  <div class="products">
    <h1 class="page-title">商品管理</h1>

    <el-card class="table-card">
      <div class="table-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
      </div>

      <el-table v-loading="loading" :data="products" style="width: 100%">
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="280" align="center">
          <template #default="{ row }">
            <div class="product-info-cell">
              <el-image
                :src="getCoverUrl(row)"
                fit="cover"
                class="product-cover"
              />
              <div class="product-details">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-meta">
                  商家：{{ row.merchantName || "-" }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="center">
          <template #default="{ row }"> ¥{{ row.price }} </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="上架"
              inactive-text="下架"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button
                type="primary"
                plain
                round
                size="small"
                @click="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                type="danger"
                plain
                round
                size="small"
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <AppPagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="min(920px, 94vw)"
      top="3vh"
      class="product-edit-dialog"
    >
      <el-form
        ref="formRef"
        class="product-edit-form"
        :model="form"
        :rules="rules"
        label-width="90px"
      >
        <el-row :gutter="10" class="form-grid">
          <el-col :xs="24" :md="15">
            <el-form-item label="商品名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入商品名称"
                class="name-input"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="9">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                :step="10"
                placeholder="不填默认 999"
                class="price-input"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="isEdit" :gutter="10" class="form-grid">
          <el-col :xs="24" :md="15">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="9">
            <el-form-item label="溯源码" prop="traceCode">
              <el-input
                v-model="form.traceCode"
                placeholder="可选"
                class="tracecode-input"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-row v-if="isEdit" :gutter="16" class="form-grid">
          <el-col :xs="24" :md="12">
            <el-form-item label="溯源二维码" prop="traceQrUrl">
              <div class="asset-box">
                <el-upload
                  :action="uploadUrl"
                  name="file"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforeUploadTraceQr"
                  :on-success="handleTraceQrSuccess"
                >
                  <el-button size="small" type="primary">上传二维码</el-button>
                </el-upload>
                <el-button
                  v-if="form.traceQrUrl"
                  size="small"
                  @click="clearTraceQr"
                  >删除</el-button
                >
              </div>
              <div v-if="form.traceQrUrl" class="qr-preview">
                <el-image
                  class="qr-preview-image"
                  :src="normalizeUrl(form.traceQrUrl)"
                  fit="contain"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="3D 模型" prop="model3dUrl">
              <div class="asset-box">
                <el-upload
                  :action="uploadUrl"
                  name="file"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforeUploadModel"
                  :on-success="handleModelSuccess"
                >
                  <el-button size="small" type="primary"
                    >上传 3D 模型</el-button
                  >
                </el-upload>

                <div v-if="form.model3dUrl" class="asset-preview">
                  <el-button size="small" @click="downloadFile(form.model3dUrl)"
                    >下载</el-button
                  >
                  <el-button
                    size="small"
                    @click="modelPreviewVisible = !modelPreviewVisible"
                  >
                    {{ modelPreviewVisible ? "收起预览" : "预览" }}
                  </el-button>
                  <el-button size="small" @click="clearModel">删除</el-button>
                </div>
              </div>

              <div
                v-if="form.model3dUrl && modelPreviewVisible"
                class="model-preview"
              >
                <model-viewer
                  class="model-preview-viewer"
                  :src="normalizeUrl(form.model3dUrl)"
                  :poster="modelPosterUrl"
                  :alt="form.name || '3D 模型预览'"
                  camera-controls
                  auto-rotate
                  shadow-intensity="1"
                  exposure="1"
                  touch-action="pan-y"
                  @load="handleModelPreviewLoad"
                  @error="handleModelPreviewError"
                />
                <div v-if="modelPreviewLoading" class="model-preview-loading">
                  模型加载中...
                </div>
                <div v-if="modelPreviewError" class="model-preview-error">
                  {{ modelPreviewError }}
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="isEdit" label="商品图片" class="images-item">
          <div class="images-panel">
            <div class="images-toolbar">
              <el-upload
                :action="uploadUrl"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                multiple
                :before-upload="beforeUploadProductImage"
                :on-success="handleProductImageUploadSuccess"
              >
                <el-button size="small" type="primary">上传图片</el-button>
              </el-upload>
              <el-button
                size="small"
                :loading="imagesLoading"
                @click="fetchProductImages"
                >刷新</el-button
              >
            </div>

            <el-table
              v-if="productImages.length"
              v-loading="imagesLoading"
              :data="productImages"
              :row-class-name="getImageRowClass"
              size="default"
              class="images-table"
              table-layout="fixed"
              style="width: 100%"
            >
              <el-table-column label="预览" width="92" align="center">
                <template #default="{ row }">
                  <el-image
                    class="image-thumb"
                    :src="normalizeUrl(row.imageUrl)"
                    fit="cover"
                  />
                </template>
              </el-table-column>
              <el-table-column label="封面" width="92" align="center">
                <template #default="{ row }">
                  <el-tag
                    v-if="row.isCover === 1"
                    type="success"
                    size="small"
                    effect="light"
                    >封面</el-tag
                  >
                  <span v-else class="muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="排序" width="140" align="center">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.sortOrder"
                    :min="0"
                    :step="1"
                    controls-position="right"
                    size="small"
                    class="sort-input"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="240" align="center">
                <template #default="{ row }">
                  <div class="image-actions">
                    <el-button
                      link
                      size="small"
                      :disabled="row.isCover === 1"
                      @click="setCoverImage(row)"
                    >
                      设为封面
                    </el-button>
                    <el-button link size="small" @click="saveImageRow(row)"
                      >保存</el-button
                    >
                    <el-button
                      link
                      size="small"
                      @click="downloadFile(row.imageUrl)"
                      >下载</el-button
                    >
                    <el-button
                      link
                      size="small"
                      type="danger"
                      @click="deleteImageRow(row)"
                      >删除</el-button
                    >
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无图片" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { Search, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AppPagination from "@/components/AppPagination.vue";
import {
  addProductImages,
  createProduct,
  deleteProduct,
  deleteProductImage,
  listProductImages,
  pageProducts,
  updateProduct,
  updateProductImage,
  updateProductStatus,
} from "@/api/product";
import { useUserStore } from "@/stores/user";
import { UPLOAD_URL, getImageUrl } from "@/config/api.js";

const userStore = useUserStore();

const searchKeyword = ref("");
const currentPage = ref(1);
const pageSize = ref(5);
const total = ref(0);
const loading = ref(false);

const products = ref([]);

const dialogVisible = ref(false);
const saving = ref(false);
const isEdit = ref(false);
const formRef = ref();
const form = reactive({
  id: null,
  name: "",
  description: "",
  price: null,
  status: 1,
  traceCode: "",
  traceQrUrl: "",
  model3dUrl: "",
});

const rules = {
  name: [{ required: true, message: "商品名称不能为空", trigger: "blur" }],
  description: [
    { required: true, message: "商品描述不能为空", trigger: "blur" },
  ],
};

const dialogTitle = computed(() => (isEdit.value ? "编辑商品" : "添加商品"));

const uploadUrl = UPLOAD_URL;
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`,
}));

const normalizeUrl = (url) => {
  return getImageUrl(url);
};

const downloadFile = (url) => {
  const resolvedUrl = normalizeUrl(url);
  if (!resolvedUrl) return;
  window.open(resolvedUrl, "_blank");
};

const PLACEHOLDER_IMAGE =
  "data:image/svg+xml;charset=utf-8," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
      <rect width="600" height="400" fill="#f5f7fa"/>
      <path d="M160 280l80-100 70 80 60-60 110 140H160z" fill="#dcdfe6"/>
      <circle cx="240" cy="160" r="28" fill="#dcdfe6"/>
      <text x="300" y="330" text-anchor="middle" font-size="18" fill="#909399">暂无图片</text>
    </svg>`,
  );

const getCoverUrl = (row) => {
  const url = normalizeUrl(row?.coverImageUrl);
  return url || PLACEHOLDER_IMAGE;
};

const formatTime = (time) => {
  if (!time) return "";
  return new Date(time).toLocaleString("zh-CN");
};

const getFileName = (url) => {
  if (!url) return "";
  const parts = url.split("/");
  return parts[parts.length - 1] || url;
};

const beforeUploadTraceQr = (file) => {
  if (!file?.type?.startsWith("image/")) {
    ElMessage.error("请上传图片文件");
    return false;
  }
  const maxSizeMb = 5;
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`);
    return false;
  }
  return true;
};

const beforeUploadModel = (file) => {
  const name = (file?.name || "").toLowerCase();
  const is3d = name.endsWith(".glb") || name.endsWith(".gltf");
  if (!is3d) {
    ElMessage.error("请上传 .glb 或 .gltf 文件");
    return false;
  }
  const maxSizeMb = 50;
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${maxSizeMb}MB`);
    return false;
  }
  return true;
};

const beforeUploadProductImage = (file) => {
  if (!file?.type?.startsWith("image/")) {
    ElMessage.error("请上传图片文件");
    return false;
  }
  const maxSizeMb = 5;
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`);
    return false;
  }
  return true;
};

const handleTraceQrSuccess = async (response) => {
  if (response?.code === 200) {
    form.traceQrUrl = response.data || "";
    if (isEdit.value && form.id) {
      try {
        await updateProduct(form.id, { traceQrUrl: form.traceQrUrl });
        ElMessage.success("上传并保存成功");
      } catch (e) {
        ElMessage.success("上传成功");
        ElMessage.error("保存溯源二维码失败");
      }
      return;
    }
    ElMessage.success("上传成功");
    return;
  }
  ElMessage.error(response?.message || "上传失败");
};

const handleModelSuccess = async (response, uploadFile) => {
  if (response?.code === 200) {
    form.model3dUrl = response.data || "";
    uploadedModelName.value = uploadFile?.name || "";
    modelPreviewVisible.value = true;
    if (isEdit.value && form.id) {
      try {
        await updateProduct(form.id, { model3dUrl: form.model3dUrl });
        ElMessage.success("上传并保存成功");
      } catch (e) {
        ElMessage.success("上传成功");
        ElMessage.error("保存 3D 模型失败");
      }
      return;
    }
    ElMessage.success("上传成功");
    return;
  }
  ElMessage.error(response?.message || "上传失败");
};

const clearTraceQr = async () => {
  form.traceQrUrl = "";
  if (isEdit.value && form.id) {
    try {
      await updateProduct(form.id, { traceQrUrl: "" });
      ElMessage.success("已删除");
    } catch (e) {
      ElMessage.error("删除失败");
    }
  }
};

const clearModel = async () => {
  form.model3dUrl = "";
  uploadedModelName.value = "";
  modelPreviewVisible.value = false;
  modelPreviewLoading.value = false;
  modelPreviewError.value = "";
  if (isEdit.value && form.id) {
    try {
      await updateProduct(form.id, { model3dUrl: "" });
      ElMessage.success("已删除");
    } catch (e) {
      ElMessage.error("删除失败");
    }
  }
};

const productImages = ref([]);
const imagesLoading = ref(false);
const getImageRowClass = ({ row }) =>
  Number(row?.isCover) === 1 ? "row-is-cover" : "";

const uploadedModelName = ref("");
const modelPreviewVisible = ref(false);
const modelPreviewLoading = ref(false);
const modelPreviewError = ref("");
const modelPosterUrl = computed(() => {
  const cover = productImages.value?.find?.((img) => img?.isCover === 1);
  const url = normalizeUrl(cover?.imageUrl);
  return url || PLACEHOLDER_IMAGE;
});

const handleModelPreviewLoad = () => {
  modelPreviewLoading.value = false;
  modelPreviewError.value = "";
};

const handleModelPreviewError = (e) => {
  modelPreviewLoading.value = false;
  modelPreviewError.value = e?.detail?.message || e?.message || "模型加载失败";
};

watch(
  () => dialogVisible.value,
  (open) => {
    if (!open) {
      modelPreviewVisible.value = false;
      modelPreviewLoading.value = false;
      modelPreviewError.value = "";
    }
  },
);

watch(
  [
    () => dialogVisible.value,
    () => form.model3dUrl,
    () => modelPreviewVisible.value,
  ],
  async ([open, url, visible]) => {
    if (!open || !visible || !url) {
      modelPreviewLoading.value = false;
      modelPreviewError.value = "";
      return;
    }
    modelPreviewLoading.value = true;
    modelPreviewError.value = "";
    await nextTick();
  },
);

const fetchProductImages = async () => {
  if (!form.id) return;
  imagesLoading.value = true;
  try {
    const res = await listProductImages(form.id);
    const list = (res.data || []).map((img) => ({
      ...img,
      isCover: Number(img?.isCover ?? 0),
      sortOrder: Number(img?.sortOrder ?? 0),
    }));

    let hasCover = false;
    productImages.value = list.map((img) => {
      if (img.isCover === 1) {
        if (hasCover) return { ...img, isCover: 0 };
        hasCover = true;
        return img;
      }
      return img;
    });
  } catch (e) {
    productImages.value = [];
  } finally {
    imagesLoading.value = false;
  }
};

const handleProductImageUploadSuccess = async (response) => {
  if (response?.code !== 200) {
    ElMessage.error(response?.message || "上传失败");
    return;
  }
  const url = response.data;
  if (!url) {
    ElMessage.error("上传失败");
    return;
  }
  try {
    await addProductImages(form.id, {
      imageUrls: [url],
      setFirstAsCover: true,
    });
    ElMessage.success("上传成功");
    await fetchProductImages();
  } catch (e) {
    ElMessage.error("保存图片失败");
  }
};

const setCoverImage = async (row) => {
  if (!row?.id) return;
  try {
    const others = productImages.value.filter(
      (img) => img?.id && img.id !== row.id && Number(img.isCover) === 1,
    );
    if (others.length) {
      await Promise.all(
        others.map((img) => updateProductImage(img.id, { isCover: 0 })),
      );
    }
    await updateProductImage(row.id, { isCover: 1 });
    ElMessage.success("已设置封面");
    await fetchProductImages();
  } catch (e) {
    ElMessage.error("设置封面失败");
  }
};

const saveImageRow = async (row) => {
  if (!row?.id) return;
  await updateProductImage(row.id, { sortOrder: row.sortOrder ?? 0 });
  ElMessage.success("已保存");
  await fetchProductImages();
};

const deleteImageRow = async (row) => {
  if (!row?.id) return;
  await ElMessageBox.confirm("确认删除该图片吗？", "提示", {
    type: "warning",
    confirmButtonText: "删除",
    cancelButtonText: "取消",
  });
  await deleteProductImage(row.id);
  ElMessage.success("删除成功");
  await fetchProductImages();
};

const resetForm = () => {
  form.id = null;
  form.name = "";
  form.description = "";
  form.price = null;
  form.status = 1;
  form.traceCode = "";
  form.traceQrUrl = "";
  form.model3dUrl = "";
  productImages.value = [];
};

const fetchProducts = async () => {
  loading.value = true;
  try {
    const res = await pageProducts({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
    });
    products.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  currentPage.value = 1;
  await fetchProducts();
};

const handleCurrentChange = async (page) => {
  currentPage.value = page;
  await fetchProducts();
};

const handleSizeChange = async (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  await fetchProducts();
};

const handleAdd = async () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
};

const handleEdit = async (row) => {
  isEdit.value = true;
  resetForm();
  form.id = row.id;
  form.name = row.name || "";
  form.description = row.description || "";
  form.price = row.price ?? null;
  form.status = row.status ?? 1;
  form.traceCode = row.traceCode || "";
  form.traceQrUrl = row.traceQrUrl || "";
  form.model3dUrl = row.model3dUrl || "";
  uploadedModelName.value = "";
  modelPreviewVisible.value = false;
  dialogVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
  await fetchProductImages();
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate();
  saving.value = true;
  try {
    if (!isEdit.value) {
      const payload = {
        name: form.name,
        description: form.description,
      };
      if (form.price !== null && form.price !== undefined) {
        payload.price = form.price;
      }
      await createProduct(payload);
      ElMessage.success("添加成功");
    } else {
      const payload = {
        name: form.name,
        description: form.description,
        status: form.status,
      };
      if (form.price !== null && form.price !== undefined) {
        payload.price = form.price;
      }
      if (form.traceCode !== undefined) payload.traceCode = form.traceCode;
      if (form.traceQrUrl !== undefined) payload.traceQrUrl = form.traceQrUrl;
      if (form.model3dUrl !== undefined) payload.model3dUrl = form.model3dUrl;
      await updateProduct(form.id, payload);
      ElMessage.success("保存成功");
    }
    dialogVisible.value = false;
    await fetchProducts();
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, "提示", {
    type: "warning",
    confirmButtonText: "删除",
    cancelButtonText: "取消",
  });
  await deleteProduct(row.id);
  ElMessage.success("删除成功");
  await fetchProducts();
};

const handleStatusChange = async (row, val) => {
  const old = row.status === 1 ? 0 : 1;
  try {
    await updateProductStatus(row.id, { status: val });
    ElMessage.success(val === 1 ? "已上架" : "已下架");
  } catch (e) {
    row.status = old;
  }
};

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
.products {
  padding: 20px;
}

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  justify-content: center;
}

.product-cover {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  overflow: hidden;
  flex: 0 0 50px;
}

.product-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-primary);
}

.product-meta {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.upload-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.asset-box {
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}

.asset-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}

.asset-link {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qr-preview {
  width: 240px;
  max-width: 100%;
  aspect-ratio: 1 / 1;
  margin-top: 8px;
  border: 1px solid var(--border-color-base);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-base);
}

.qr-preview-image {
  width: 100%;
  height: 100%;
  display: block;
  background: var(--bg-base);
}

.model-preview {
  width: 240px;
  max-width: 100%;
  aspect-ratio: 1 / 1;
  position: relative;
  margin-top: 8px;
  border: 1px solid var(--border-color-base);
  border-radius: 8px;
  overflow: hidden;
  background: radial-gradient(
    120% 120% at 50% 30%,
    #1f2937 0%,
    #0b1220 55%,
    #05070d 100%
  );
}

.model-preview-viewer {
  width: 100%;
  height: 100%;
  display: block;
  background: transparent;
}

.model-preview-loading {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 6px;
  font-size: 12px;
}

.model-preview-error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.images-panel {
  width: 100%;
  max-width: 100%;
  margin: 0;
}

.images-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.image-thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--border-color-base);
  background: var(--bg-base);
}

.image-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: nowrap;
  width: 100%;
}

.image-actions :deep(.el-button.is-link) {
  padding: 4px 8px;
  border-radius: 8px;
}

.image-actions :deep(.el-button.is-link:hover) {
  background: var(--bg-elevated);
}

.muted {
  color: var(--text-color-secondary);
}

:deep(.images-table) {
  border-radius: 10px;
  overflow: hidden;
  width: 100% !important;
}

:deep(.images-table .el-table__cell) {
  padding: 12px 10px;
  vertical-align: middle;
}

:deep(.images-table .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.images-table .el-table__body tr > td.el-table__cell) {
  background: var(--bg-base);
}

:deep(.images-table .el-table__body tr:hover > td.el-table__cell) {
  background: var(--bg-base);
}

:deep(.images-table .el-table__body tr.row-is-cover > td.el-table__cell) {
  background: var(--bg-elevated);
}

:deep(.images-table .el-table__body tr.row-is-cover:hover > td.el-table__cell) {
  background: var(--bg-elevated);
}

:deep(.images-table .sort-input) {
  width: 110px;
}

:deep(.images-table .el-table__header th) {
  background-color: var(--table-header-bg);
  color: var(--table-header-text);
  font-weight: 600;
}

:deep(.el-table__fixed-right-patch) {
  background-color: var(--table-header-bg) !important;
}

:deep(.el-table__fixed-right .el-table__header-wrapper) {
  background-color: var(--table-header-bg) !important;
}

:deep(.el-table__fixed-right .el-table__header th) {
  background-color: var(--table-header-bg) !important;
}

:deep(.el-table__fixed-right .el-table__header-wrapper th),
:deep(.el-table__fixed-right .el-table__header-wrapper th.el-table__cell),
:deep(.el-table__fixed-right .el-table__header th.el-table__cell),
:deep(.el-table__fixed-right .el-table__fixed-header-wrapper),
:deep(.el-table__fixed-right .el-table__fixed-header-wrapper th),
:deep(
  .el-table__fixed-right .el-table__fixed-header-wrapper th.el-table__cell
) {
  background-color: var(--table-header-bg) !important;
}

:deep(.el-table__row:hover) {
  background-color: var(--table-row-hover-bg);
}

:deep(.el-table__fixed-right .el-table__body tr.hover-row > td.el-table__cell) {
  background-color: var(--table-row-hover-bg);
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.product-edit-dialog .el-dialog__body) {
  max-height: calc(100vh - 160px);
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 14px 12px;
}

.product-edit-form .form-grid {
  margin-bottom: 4px;
}

:deep(.product-edit-dialog .el-form-item__label) {
  white-space: nowrap;
}

:deep(.product-edit-dialog .el-form-item) {
  margin-bottom: 12px;
}

:deep(.product-edit-dialog .el-dialog__header) {
  padding: 12px 14px 0;
  margin-right: 0;
}

:deep(.product-edit-dialog .el-dialog__footer) {
  padding: 10px 14px 12px;
}

:deep(.product-edit-dialog .name-input) {
  width: 350px;
  max-width: 100%;
}

:deep(.product-edit-dialog .price-input) {
  width: 230px;
}

:deep(.product-edit-dialog .tracecode-input) {
  width: 230px;
  max-width: 100%;
}

:deep(.product-edit-dialog .images-item .el-form-item__content) {
  width: 100%;
  display: block;
}

@media (max-width: 768px) {
  .asset-box,
  .asset-preview,
  .image-actions {
    flex-wrap: wrap;
  }
}

.full-width {
  width: 100%;
}
</style>
