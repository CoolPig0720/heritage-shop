<template>
  <div class="customize">
    <div class="hero">
      <div class="hero-inner">
        <div class="hero-title">非遗智能定制</div>
        <div class="hero-subtitle">上传图片，AI智能生成个性化定制方案</div>
        <div class="hero-actions">
          <el-button @click="openHistoryDialog">历史记录</el-button>
          <el-button
            v-if="!isMerchant"
            type="primary"
            @click="openCustomizeDialog"
            >发起定制</el-button
          >
        </div>
      </div>
    </div>

    <div class="container">
      <el-tabs v-model="activeTab" class="customize-tabs" stretch>
        <el-tab-pane label="图生图" name="img2img">
          <div class="customize-content">
            <div class="card upload-section">
              <div class="card-header">
                <div class="card-title">上传图片</div>
                <div class="card-desc">
                  支持 jpg、png、bmp、webp 格式，最大 10MB
                </div>
              </div>
              <el-upload
                class="upload"
                drag
                :auto-upload="false"
                :limit="1"
                accept=".jpg,.jpeg,.png,.bmp,.webp"
                :file-list="fileList"
                :before-upload="beforeSelectImage"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :on-exceed="handleExceed"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg、png、bmp、webp 格式，最大 10MB
                  </div>
                </template>
              </el-upload>

              <div v-if="previewUrl" class="preview">
                <el-image
                  :src="previewUrl"
                  fit="contain"
                  class="preview-image"
                />
              </div>
            </div>

            <div class="card options-section">
              <div class="card-header">
                <div class="card-title">生成参数</div>
                <div class="card-desc">调整参数以获得更好的生成效果</div>
              </div>
              <el-form :model="img2imgForm" label-width="110px" class="form">
                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      高清增强
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          增强图像清晰度，提升细节表现
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <el-switch v-model="img2imgForm.enhanceImage" />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      面部优化
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          优化面部细节，使人物更加自然
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <el-switch v-model="img2imgForm.restoreFace" />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      生成自由度
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          值越小越接近原图，值越大改动越明显。范围 0~1（建议
                          0.6~0.8）。
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <div class="slider-row">
                    <el-slider
                      v-model="img2imgForm.strength"
                      :min="0.01"
                      :max="1"
                      :step="0.01"
                      style="flex: 1"
                    />
                    <div class="slider-value">
                      {{ img2imgForm.strength.toFixed(2) }}
                    </div>
                  </div>
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      Prompt
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          用文字描述你希望生成的画面内容、风格与细节。描述越具体，结果越稳定。
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <el-input
                    v-model="img2imgForm.prompt"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入prompt（最多256字）"
                    maxlength="256"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      输出尺寸
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          选择生成图分辨率。“与输入图一致”会尽量跟随原图比例。
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <el-select
                    v-model="img2imgForm.resolution"
                    placeholder="请选择输出尺寸"
                    style="width: 100%"
                  >
                    <el-option label="与输入图一致" value="" />
                    <el-option label="768x768" value="768:768" />
                    <el-option label="1024x1024" value="1024:1024" />
                    <el-option label="768x1024" value="768:1024" />
                    <el-option label="1024x768" value="1024:768" />
                  </el-select>
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label" @click.prevent>
                      生成数量
                      <el-tooltip placement="top" effect="dark">
                        <template #content>
                          一次生成的图片张数，范围 1~4。数量越多耗时越长。
                        </template>
                        <el-icon class="help-icon" @click.prevent.stop
                          ><QuestionFilled
                        /></el-icon>
                      </el-tooltip>
                    </span>
                  </template>
                  <el-select v-model="img2imgForm.count" style="width: 100%">
                    <el-option label="1" :value="1" />
                    <el-option label="2" :value="2" />
                    <el-option label="3" :value="3" />
                    <el-option label="4" :value="4" />
                  </el-select>
                </el-form-item>

                <el-form-item class="actions">
                  <div class="actions-row">
                    <el-button :disabled="generating" @click="resetImg2Img"
                      >重置</el-button
                    >
                    <el-button
                      type="primary"
                      :loading="generating"
                      @click="handleGenerateImg2Img"
                      >生成</el-button
                    >
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文生图" name="txt2img">
          <div class="txt2img-layout">
            <div class="card txt2img-form">
              <div class="card-header">
                <div class="card-title">文生图</div>
                <div class="card-desc">输入Prompt，选择输出尺寸</div>
              </div>
              <el-form :model="txt2imgForm" label-width="110px" class="form">
                <el-form-item label="Prompt">
                  <el-input
                    v-model="txt2imgForm.prompt"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入prompt（最多256字）"
                    maxlength="256"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="输出尺寸">
                  <el-select
                    v-model="txt2imgForm.resolution"
                    placeholder="请选择输出尺寸"
                    style="width: 100%"
                  >
                    <el-option label="768x768" value="768:768" />
                    <el-option label="1024x1024" value="1024:1024" />
                    <el-option label="768x1024" value="768:1024" />
                    <el-option label="1024x768" value="1024:768" />
                  </el-select>
                </el-form-item>
                <el-form-item class="actions">
                  <div class="actions-row">
                    <el-button
                      type="primary"
                      :loading="generating"
                      @click="handleGenerateTxt2Img"
                      >生成</el-button
                    >
                  </div>
                </el-form-item>
              </el-form>
            </div>

            <div class="card txt2img-result">
              <div class="card-header">
                <div class="card-title">生图结果</div>
                <div class="card-desc">支持完整预览与放大查看</div>
              </div>
              <div class="txt2img-result-body">
                <el-empty
                  v-if="!resultImages.length"
                  description="暂无生成结果"
                  :image-size="90"
                />
                <div v-else class="txt2img-result-grid">
                  <el-image
                    v-for="(img, idx) in resultImages"
                    :key="`txt2img-${idx}-${img}`"
                    :src="img"
                    fit="contain"
                    :preview-src-list="resultImages"
                    preview-teleported
                    class="txt2img-result-image"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div
        v-if="resultImages.length && activeTab === 'img2img'"
        class="result-section card"
      >
        <div class="card-header">
          <div class="card-title">生成结果</div>
          <div class="card-desc">点击图片可预览</div>
        </div>
        <div class="result-grid">
          <el-image
            v-for="(img, idx) in resultImages"
            :key="`${idx}-${img}`"
            :src="img"
            fit="cover"
            :preview-src-list="resultImages"
            preview-teleported
            class="result-image"
          />
        </div>
      </div>

      <!-- 发起定制对话框 -->
      <el-dialog
        v-model="customizeDialogVisible"
        title="发起定制"
        width="560px"
      >
        <el-form label-position="top">
          <el-form-item label="定制标题" required>
            <el-input
              v-model="customizeForm.title"
              placeholder="如：蜡染定制围巾"
              maxlength="200"
            />
          </el-form-item>
          <el-form-item label="需求描述" required>
            <el-input
              v-model="customizeForm.description"
              type="textarea"
              :rows="3"
              placeholder="描述你的定制需求"
              maxlength="2000"
            />
          </el-form-item>
          <el-form-item label="参考图片">
            <div class="customize-upload-area">
              <div class="customize-upload-list">
                <div
                  v-for="(url, idx) in customizeForm.imageUrls"
                  :key="idx"
                  class="customize-upload-item"
                >
                  <img
                    :src="getImageUrl(url)"
                    class="customize-upload-thumb"
                    @click="previewCustomizeImage(url)"
                  />
                  <div
                    class="customize-upload-item-delete"
                    @click="removeCustomizeImage(idx)"
                  >
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
                <el-upload
                  v-if="customizeForm.imageUrls.length < 3"
                  :auto-upload="true"
                  :show-file-list="false"
                  :before-upload="beforeCustomizeImageUpload"
                  :on-success="handleCustomizeImageSuccess"
                  :on-error="handleCustomizeImageError"
                  accept=".jpg,.jpeg,.png,.bmp,.webp"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  name="file"
                >
                  <div class="customize-upload-trigger">
                    <el-icon class="customize-upload-icon"><Plus /></el-icon>
                    <div class="customize-upload-text">上传图片</div>
                    <div class="customize-upload-hint">
                      {{ customizeForm.imageUrls.length }}/3
                    </div>
                  </div>
                </el-upload>
              </div>
              <div class="customize-upload-tip">
                支持 JPG/PNG，不超过 5MB，最多 3 张
              </div>
            </div>
          </el-form-item>
          <el-form-item label="选择商家" required>
            <div class="merchant-select-row">
              <span v-if="customizeForm.merchantId" class="selected-merchant"
                >已选：{{ customizeForm.merchantName }}</span
              >
              <span v-else class="no-merchant">未选择商家</span>
              <el-button type="primary" link @click="openMerchantDialog"
                >选择商家</el-button
              >
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="customizeDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="customizeSubmitting"
            @click="handleSubmitCustomize"
            >提交</el-button
          >
        </template>
      </el-dialog>

      <!-- 商家选择弹窗 -->
      <el-dialog
        v-model="merchantDialogVisible"
        title="选择商家"
        width="960px"
        top="5vh"
      >
        <div v-loading="merchantLoading">
          <el-empty
            v-if="!merchantLoading && merchantList.length === 0"
            description="暂无商家"
          />
          <el-table
            v-else
            :data="merchantList"
            style="width: 100%"
            highlight-current-row
            @current-change="handleMerchantCurrentChange"
            :row-class-name="getMerchantRowClass"
          >
            <el-table-column label="选择" width="60" align="center">
              <template #default="{ row }">
                <el-radio v-model="selectedMerchantId" :value="row.id"
                  >&nbsp;</el-radio
                >
              </template>
            </el-table-column>
            <el-table-column label="商家" min-width="160" align="center">
              <template #default="{ row }">
                <div class="merchant-table-info">
                  <el-avatar :size="36" :src="getAvatarUrl(row.avatar)">{{
                    row.name?.charAt(0)
                  }}</el-avatar>
                  <div class="merchant-table-detail">
                    <div class="merchant-table-name">{{ row.name }}</div>
                    <div class="merchant-table-count">
                      在售 {{ row.productCount }} 件
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="高星商品" min-width="420">
              <template #default="{ row }">
                <div
                  class="merchant-table-products"
                  v-if="row.topProducts?.length"
                >
                  <div
                    v-for="p in row.topProducts"
                    :key="p.id"
                    class="merchant-table-product"
                  >
                    <img
                      :src="getImageUrl(p.coverImageUrl)"
                      class="merchant-table-product-thumb"
                    />
                    <div class="merchant-table-product-info">
                      <div class="merchant-table-product-name">
                        {{ p.name }}
                      </div>
                      <div class="merchant-table-product-meta">
                        <span class="merchant-table-product-price"
                          >¥{{ p.price }}</span
                        >
                        <span
                          v-if="p.avgRating"
                          class="merchant-table-product-rating"
                          >{{ p.avgRating }}分</span
                        >
                      </div>
                    </div>
                  </div>
                </div>
                <span v-else class="merchant-table-empty">暂无商品</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="openMerchantDetail(row)"
                  >详情</el-button
                >
              </template>
            </el-table-column>
          </el-table>

          <div
            class="merchant-table-pagination"
            v-if="merchantTotal > merchantSize"
          >
            <AppPagination
              v-model:current-page="merchantPage"
              v-model:page-size="merchantSize"
              :total="merchantTotal"
              @current-change="fetchMerchantList"
              @size-change="handleMerchantSizeChange"
            />
          </div>
        </div>
        <template #footer>
          <el-button @click="merchantDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!selectedMerchantId"
            @click="confirmMerchantSelection"
            >确认选择</el-button
          >
        </template>
      </el-dialog>

      <!-- 商家商品详情弹窗 -->
      <el-dialog
        v-model="merchantDetailVisible"
        :title="merchantDetailName + ' 的商品'"
        width="800px"
        top="6vh"
      >
        <div v-loading="merchantDetailLoading">
          <el-empty
            v-if="!merchantDetailLoading && merchantDetailProducts.length === 0"
            description="暂无商品"
          />
          <div class="merchant-detail-grid" v-else>
            <div
              v-for="p in merchantDetailProducts"
              :key="p.id"
              class="merchant-detail-card"
              @click="goProductDetail(p.id)"
            >
              <img
                :src="getImageUrl(p.coverImageUrl)"
                class="merchant-detail-cover"
              />
              <div class="merchant-detail-body">
                <div class="merchant-detail-name">{{ p.name }}</div>
                <div class="merchant-detail-meta">
                  <span class="merchant-detail-price">¥{{ p.price }}</span>
                  <span v-if="p.avgRating" class="merchant-detail-rating"
                    >{{ p.avgRating }}分</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          class="merchant-detail-pagination"
          v-if="merchantDetailTotal > merchantDetailSize"
        >
          <AppPagination
            v-model:current-page="merchantDetailPage"
            v-model:page-size="merchantDetailSize"
            :total="merchantDetailTotal"
            @current-change="fetchMerchantDetailProducts"
            @size-change="handleMerchantDetailSizeChange"
          />
        </div>
        <template #footer>
          <el-button @click="merchantDetailVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="historyDialogVisible" width="1100px">
        <template #header>
          <div class="history-dialog-header">
            <div class="history-dialog-title">历史生图记录</div>
            <div class="history-dialog-desc">
              按是否存在参考图分为图生图/文生图，仅展示参考图、提示词与生成图
            </div>
          </div>
        </template>

        <div v-if="historyLoading" class="history-loading">
          <el-skeleton :rows="3" animated />
        </div>

        <el-empty
          v-else-if="!historyRecords.length"
          description="暂无历史记录"
          :image-size="80"
        />

        <div v-else class="history-dialog-body">
          <el-tabs v-model="historyTypeTab" class="history-type-tabs" stretch>
            <el-tab-pane
              :label="`图生图（${img2imgHistoryRecords.length}）`"
              name="img2img"
            >
              <el-empty
                v-if="!img2imgHistoryRecords.length"
                description="暂无图生图记录"
                :image-size="80"
              />
              <div v-else class="history-list">
                <div
                  v-for="item in img2imgHistoryRecords"
                  :key="item.id"
                  class="history-row"
                >
                  <div class="history-row-prompt" :title="item.prompt || ''">
                    {{ item.prompt ? item.prompt : "（无提示词）" }}
                  </div>
                  <div class="history-row-images">
                    <div class="history-img-cell">
                      <div class="history-img-label">参考图</div>
                      <el-image
                        v-if="item.originalImageUrl"
                        :src="normalizeUrl(item.originalImageUrl)"
                        fit="contain"
                        :preview-src-list="[
                          normalizeUrl(item.originalImageUrl),
                        ]"
                        preview-teleported
                        class="history-row-image"
                      />
                      <div v-else class="history-image-placeholder"></div>
                    </div>
                    <div class="history-img-cell">
                      <div class="history-img-label">生成图</div>
                      <el-image
                        :src="normalizeUrl(item.resultImageUrl)"
                        fit="contain"
                        :preview-src-list="img2imgHistoryPreviewList"
                        :initial-index="
                          img2imgHistoryIndexMap.get(item.id) ?? 0
                        "
                        preview-teleported
                        class="history-row-image"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane
              :label="`文生图（${txt2imgHistoryRecords.length}）`"
              name="txt2img"
            >
              <el-empty
                v-if="!txt2imgHistoryRecords.length"
                description="暂无文生图记录"
                :image-size="80"
              />
              <div v-else class="history-list">
                <div
                  v-for="item in txt2imgHistoryRecords"
                  :key="item.id"
                  class="history-row"
                >
                  <div class="history-row-prompt" :title="item.prompt || ''">
                    {{ item.prompt ? item.prompt : "（无提示词）" }}
                  </div>
                  <div class="history-row-images history-row-images--single">
                    <div class="history-img-cell">
                      <div class="history-img-label">生成图</div>
                      <el-image
                        :src="normalizeUrl(item.resultImageUrl)"
                        fit="contain"
                        :preview-src-list="txt2imgHistoryPreviewList"
                        :initial-index="
                          txt2imgHistoryIndexMap.get(item.id) ?? 0
                        "
                        preview-teleported
                        class="history-row-image"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>

          <AppPagination
            v-if="historyTotal > historySize"
            variant="history"
            v-model:current-page="historyPage"
            v-model:page-size="historySize"
            :total="historyTotal"
            @size-change="handleHistorySizeChange"
            @current-change="fetchHistory"
          />
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  QuestionFilled,
  UploadFilled,
  Plus,
  Close,
} from "@element-plus/icons-vue";
import AppPagination from "@/components/AppPagination.vue";
import { imageToImage, pageAiImageRecords, textToImage } from "@/api/aiImage";
import {
  createCustomizeRequest,
  getMerchantList,
  pageMerchantProducts,
} from "@/api/customize";
import { getImageUrl, UPLOAD_URL, getAvatarUrl } from "@/config/api.js";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const isMerchant = computed(() => userStore.userInfo?.role === "MERCHANT");

const uploadUrl = UPLOAD_URL;
const uploadHeaders = computed(() => {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
});

const activeTab = ref("img2img");

const router = useRouter();
const route = useRoute();
const generating = ref(false);

const fileList = ref([]);
const selectedFile = ref(null);
const previewObjectUrl = ref("");

const previewUrl = computed(() => previewObjectUrl.value);

const img2imgForm = ref({
  enhanceImage: true,
  restoreFace: false,
  strength: 0.5,
  prompt: "",
  resolution: "",
  count: 1,
});

const txt2imgForm = ref({
  prompt: "",
  resolution: "1024:1024",
});

const resultImages = ref([]);

// 定制渠道
const customizeDialogVisible = ref(false);
const customizeSubmitting = ref(false);
const customizeForm = ref({
  title: "",
  description: "",
  imageUrls: [],
  merchantId: null,
  merchantName: "",
});

const historyLoading = ref(false);
const historyRecords = ref([]);
const historyTotal = ref(0);
const historyPage = ref(1);
const historySize = ref(12);
const historyDialogVisible = ref(false);
const historyTypeTab = ref("img2img");

const openCustomizeDialog = () => {
  customizeForm.value = {
    title: "",
    description: "",
    imageUrls: [],
    merchantId: route.query.merchantId ? Number(route.query.merchantId) : null,
    merchantName: route.query.merchantName || "",
  };
  customizeDialogVisible.value = true;
};

// 商家选择弹窗
const merchantDialogVisible = ref(false);
const merchantLoading = ref(false);
const merchantList = ref([]);
const selectedMerchantId = ref(null);
const merchantPage = ref(1);
const merchantSize = ref(10);
const merchantTotal = ref(0);

// 商家商品详情弹窗
const merchantDetailVisible = ref(false);
const merchantDetailLoading = ref(false);
const merchantDetailProducts = ref([]);
const merchantDetailName = ref("");
const merchantDetailId = ref(null);
const merchantDetailPage = ref(1);
const merchantDetailSize = ref(8);
const merchantDetailTotal = ref(0);

const openMerchantDialog = async () => {
  selectedMerchantId.value = customizeForm.value.merchantId;
  merchantPage.value = 1;
  merchantDialogVisible.value = true;
  await fetchMerchantList();
};

const fetchMerchantList = async () => {
  merchantLoading.value = true;
  try {
    const res = await getMerchantList({
      page: merchantPage.value,
      size: merchantSize.value,
    });
    // 兼容分页和非分页返回
    if (res?.data?.records) {
      merchantList.value = res.data.records;
      merchantTotal.value = res.data.total || 0;
    } else {
      merchantList.value = res?.data || [];
      merchantTotal.value = merchantList.value.length;
    }
  } catch (e) {
    ElMessage.error("获取商家列表失败");
  } finally {
    merchantLoading.value = false;
  }
};

const handleMerchantSizeChange = () => {
  merchantPage.value = 1;
  fetchMerchantList();
};

const handleMerchantCurrentChange = (row) => {
  if (row) {
    selectedMerchantId.value = row.id;
  }
};

const getMerchantRowClass = ({ row }) => {
  return row.id === selectedMerchantId.value ? "merchant-selected-row" : "";
};

const confirmMerchantSelection = () => {
  if (!selectedMerchantId.value) return;
  const m = merchantList.value.find((x) => x.id === selectedMerchantId.value);
  customizeForm.value.merchantId = selectedMerchantId.value;
  customizeForm.value.merchantName = m?.name || "";
  merchantDialogVisible.value = false;
};

const openMerchantDetail = (merchant) => {
  merchantDetailId.value = merchant.id;
  merchantDetailName.value = merchant.name;
  merchantDetailPage.value = 1;
  merchantDetailVisible.value = true;
  fetchMerchantDetailProducts();
};

const fetchMerchantDetailProducts = async () => {
  merchantDetailLoading.value = true;
  try {
    const res = await pageMerchantProducts(merchantDetailId.value, {
      page: merchantDetailPage.value,
      size: merchantDetailSize.value,
    });
    merchantDetailProducts.value = res?.data?.records || [];
    merchantDetailTotal.value = res?.data?.total || 0;
  } catch (e) {
    ElMessage.error("获取商品列表失败");
  } finally {
    merchantDetailLoading.value = false;
  }
};

const handleMerchantDetailSizeChange = () => {
  merchantDetailPage.value = 1;
  fetchMerchantDetailProducts();
};

const goProductDetail = (productId) => {
  const url = router.resolve({ path: `/product/${productId}` }).href;
  window.open(url, "_blank");
};

const handleSubmitCustomize = async () => {
  const form = customizeForm.value;
  if (!form.title.trim()) {
    ElMessage.warning("请输入定制标题");
    return;
  }
  if (!form.description.trim()) {
    ElMessage.warning("请输入需求描述");
    return;
  }
  if (!form.merchantId) {
    ElMessage.warning("请选择商家");
    return;
  }
  customizeSubmitting.value = true;
  try {
    const res = await createCustomizeRequest({
      merchantId: form.merchantId,
      title: form.title.trim(),
      description: form.description.trim(),
      imageUrls: form.imageUrls.length > 0 ? form.imageUrls : undefined,
    });
    ElMessage.success("定制请求已提交");
    customizeDialogVisible.value = false;
    router.push(`/customize/requests/${res.data}`);
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "提交失败");
  } finally {
    customizeSubmitting.value = false;
  }
};

const beforeCustomizeImageUpload = (file) => {
  if (customizeForm.value.imageUrls.length >= 3) {
    ElMessage.warning("最多上传3张参考图片");
    return false;
  }
  const isImage = [
    "image/jpeg",
    "image/png",
    "image/bmp",
    "image/webp",
  ].includes(file.type);
  if (!isImage) {
    ElMessage.error("仅支持 JPG/PNG/BMP/WEBP 格式");
    return false;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error("图片大小不能超过 5MB");
    return false;
  }
  return true;
};

const handleCustomizeImageSuccess = (response) => {
  if (response.code === 200 && response.data) {
    customizeForm.value.imageUrls.push(response.data);
  } else {
    ElMessage.error(response.message || "上传失败");
  }
};

const handleCustomizeImageError = () => {
  ElMessage.error("图片上传失败");
};

const removeCustomizeImage = (idx) => {
  customizeForm.value.imageUrls.splice(idx, 1);
};

const previewCustomizeImage = (url) => {
  ElMessageBox({
    message: `<img src="${getImageUrl(url)}" style="max-width:100%;max-height:70vh" />`,
    dangerouslyUseHTMLString: true,
    showConfirmButton: false,
    customClass: "image-preview-dialog",
  });
};

const normalizeUrl = (url) => {
  return getImageUrl(url);
};

const isBlank = (value) => value == null || String(value).trim() === "";

const img2imgHistoryRecords = computed(() =>
  historyRecords.value.filter((x) => !isBlank(x?.originalImageUrl)),
);
const txt2imgHistoryRecords = computed(() =>
  historyRecords.value.filter((x) => isBlank(x?.originalImageUrl)),
);

const img2imgHistoryPreviewList = computed(() =>
  img2imgHistoryRecords.value
    .map((x) => normalizeUrl(x?.resultImageUrl))
    .filter(Boolean),
);
const txt2imgHistoryPreviewList = computed(() =>
  txt2imgHistoryRecords.value
    .map((x) => normalizeUrl(x?.resultImageUrl))
    .filter(Boolean),
);

const img2imgHistoryIndexMap = computed(() => {
  const map = new Map();
  img2imgHistoryRecords.value.forEach((x, idx) => {
    if (x?.id != null) map.set(x.id, idx);
  });
  return map;
});

const txt2imgHistoryIndexMap = computed(() => {
  const map = new Map();
  txt2imgHistoryRecords.value.forEach((x, idx) => {
    if (x?.id != null) map.set(x.id, idx);
  });
  return map;
});

const revokePreviewUrl = () => {
  if (previewObjectUrl.value) {
    URL.revokeObjectURL(previewObjectUrl.value);
    previewObjectUrl.value = "";
  }
};

onBeforeUnmount(() => {
  revokePreviewUrl();
});

const fetchHistory = async () => {
  historyLoading.value = true;
  try {
    const res = await pageAiImageRecords({
      page: historyPage.value,
      size: historySize.value,
    });
    const data = res?.data || {};
    historyRecords.value = Array.isArray(data.records) ? data.records : [];
    historyTotal.value = Number(data.total ?? 0);
  } catch (e) {
    historyRecords.value = [];
    historyTotal.value = 0;
  } finally {
    historyLoading.value = false;
  }
};

const handleHistorySizeChange = async () => {
  historyPage.value = 1;
  await fetchHistory();
};

const openHistoryDialog = async () => {
  historyDialogVisible.value = true;
  if (
    historyTypeTab.value !== "img2img" &&
    historyTypeTab.value !== "txt2img"
  ) {
    historyTypeTab.value = "img2img";
  }
  await fetchHistory();
};

const handleExceed = () => {
  ElMessage.warning("一次只能上传一张图片");
};

const isAllowedImageExt = (name) => {
  const lowered = (name || "").toLowerCase();
  return (
    lowered.endsWith(".jpg") ||
    lowered.endsWith(".jpeg") ||
    lowered.endsWith(".png") ||
    lowered.endsWith(".bmp") ||
    lowered.endsWith(".webp")
  );
};

const validateImageDimension = (file) =>
  new Promise((resolve) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      const ok = img.width <= 5000 && img.height <= 5000;
      URL.revokeObjectURL(url);
      resolve(ok);
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      resolve(false);
    };
    img.src = url;
  });

const beforeSelectImage = async (file) => {
  if (!isAllowedImageExt(file?.name) || !file?.type?.startsWith("image/")) {
    ElMessage.error("请上传jpg、jpeg、png、bmp、webp格式图片");
    return false;
  }

  const maxBase64Bytes = 8 * 1024 * 1024;
  const estimatedBase64Bytes = Math.ceil((file.size * 4) / 3);
  if (estimatedBase64Bytes > maxBase64Bytes) {
    ElMessage.error("图片过大：base64编码后需≤8MB");
    return false;
  }

  const ok = await validateImageDimension(file);
  if (!ok) {
    ElMessage.error("图片长宽需≤5000");
    return false;
  }
  return true;
};

const handleFileChange = (uploadFile, uploadFiles) => {
  const raw = uploadFile?.raw;
  if (!raw) return;

  selectedFile.value = raw;
  fileList.value = uploadFiles.slice(-1);

  revokePreviewUrl();
  previewObjectUrl.value = URL.createObjectURL(raw);
};

const handleFileRemove = () => {
  selectedFile.value = null;
  fileList.value = [];
  revokePreviewUrl();
};

const resetImg2Img = () => {
  handleFileRemove();
  img2imgForm.value = {
    enhanceImage: true,
    restoreFace: false,
    strength: 0.5,
    prompt: "",
    resolution: "",
    count: 1,
  };
  resultImages.value = [];
};

const handleGenerateImg2Img = async () => {
  if (!selectedFile.value) {
    ElMessage.error("请先上传一张原图");
    return;
  }
  if (!img2imgForm.value.prompt?.trim()) {
    ElMessage.error("请输入Prompt");
    return;
  }

  generating.value = true;
  try {
    const fd = new FormData();
    fd.append("file", selectedFile.value);
    fd.append("prompt", img2imgForm.value.prompt.trim());
    fd.append("strength", String(img2imgForm.value.strength));
    fd.append("enhanceImage", String(img2imgForm.value.enhanceImage));
    fd.append("restoreFace", String(img2imgForm.value.restoreFace));
    fd.append("count", String(img2imgForm.value.count));
    fd.append("rspImgType", "url");
    if (img2imgForm.value.resolution) {
      fd.append("resolution", img2imgForm.value.resolution);
    }

    const res = await imageToImage(fd);
    const images = res?.data?.resultImages || [];
    resultImages.value = images.filter(Boolean);
    if (!resultImages.value.length && res?.data?.resultImage) {
      resultImages.value = [res.data.resultImage];
    }
    if (!resultImages.value.length) {
      ElMessage.error("生成失败：未返回图片");
      return;
    }
    ElMessage.success("生成成功");
    await fetchHistory();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "生成失败";
    ElMessage.error(msg);
  } finally {
    generating.value = false;
  }
};

const handleGenerateTxt2Img = async () => {
  if (!txt2imgForm.value.prompt?.trim()) {
    ElMessage.error("请输入Prompt");
    return;
  }

  generating.value = true;
  try {
    const res = await textToImage({
      prompt: txt2imgForm.value.prompt.trim(),
      resolution: txt2imgForm.value.resolution,
      rspImgType: "url",
    });
    const images = res?.data?.resultImages || [];
    resultImages.value = images.filter(Boolean);
    if (!resultImages.value.length && res?.data?.resultImage) {
      resultImages.value = [res.data.resultImage];
    }
    if (!resultImages.value.length) {
      ElMessage.error("生成失败：未返回图片");
      return;
    }
    ElMessage.success("生成成功");
    await fetchHistory();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "生成失败";
    ElMessage.error(msg);
  } finally {
    generating.value = false;
  }
};

onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
.customize {
  min-height: calc(100vh - 60px);
  background: var(--bg-page);
  padding-bottom: 32px;
  transition: background-color 0.3s ease;
}

.hero {
  background:
    radial-gradient(
      1200px 600px at 50% -20%,
      rgba(64, 158, 255, 0.35),
      rgba(64, 158, 255, 0) 60%
    ),
    linear-gradient(180deg, var(--bg-elevated), transparent);
  border-bottom: 1px solid var(--border-color-base);
  padding: 28px 20px 18px;
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-color-primary);
  letter-spacing: 0.2px;
}

.hero-subtitle {
  font-size: 14px;
  color: var(--text-color-secondary);
}

.container {
  max-width: 1200px;
  margin: 16px auto 0;
  padding: 0 20px;
}

.customize-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.customize-tabs {
  background: transparent;
}

.card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 14px;
  box-shadow: var(--card-shadow);
  backdrop-filter: blur(6px);
}

.upload-section,
.options-section,
.single-pane,
.result-section {
  padding: 18px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color-primary);
}

.card-desc {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.preview {
  margin-top: 16px;
  border: 1px solid var(--border-color-base);
  border-radius: 12px;
  padding: 10px;
  background: var(--bg-elevated);
}

.preview-image {
  width: 100%;
  height: 340px;
}

.slider-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.slider-value {
  width: 56px;
  text-align: right;
  color: var(--text-color-secondary);
  font-variant-numeric: tabular-nums;
}

.form-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.help-icon {
  font-size: 14px;
  color: var(--text-color-secondary);
  cursor: pointer;
}

.help-icon:hover {
  color: var(--color-primary);
}

.single-pane {
  max-width: 860px;
  margin: 0 auto;
}

.txt2img-layout {
  display: grid;
  grid-template-columns: minmax(420px, 1fr) minmax(420px, 1fr);
  gap: 16px;
  align-items: stretch;
}

.txt2img-form,
.txt2img-result {
  padding: 18px;
  min-height: 520px;
  display: flex;
  flex-direction: column;
}

.txt2img-result-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.txt2img-result-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  flex: 1;
}

.txt2img-result-image {
  width: 100%;
  height: 100%;
  min-height: 360px;
  border-radius: 12px;
  background: var(--bg-elevated);
}

.result-section {
  margin-top: 16px;
}

.hero-actions {
  position: absolute;
  right: 0;
  top: 4px;
}

.hero-actions :deep(.el-button) {
  border-radius: 10px;
}

.history-dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 42px;
}

.history-dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color-primary);
  line-height: 1.2;
}

.history-dialog-desc {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: var(--text-color-secondary);
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-dialog-body {
  max-height: 70vh;
  overflow: auto;
  padding-right: 6px;
}

.history-loading {
  padding: 10px 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-row {
  border: 1px solid var(--border-color-base);
  border-radius: 14px;
  background: var(--bg-elevated);
  padding: 16px;
}

.history-row-prompt {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-primary);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.history-row-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.history-row-images--single {
  grid-template-columns: 1fr;
}

.history-img-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.history-img-label {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.history-row-image {
  width: 100%;
  height: 360px;
  border-radius: 12px;
  background: var(--bg-base);
}

.history-image-placeholder {
  width: 100%;
  height: 360px;
  border-radius: 12px;
  border: 1px dashed var(--border-color-base);
  background: var(--bg-elevated);
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.history-item {
  border: 1px solid var(--border-color-base);
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-elevated);
}

.history-image {
  width: 100%;
  height: 180px;
}

.history-meta {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-prompt {
  font-size: 13px;
  color: var(--text-color-primary);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-ref {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.history-ref.empty {
  color: var(--text-color-placeholder);
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.result-image {
  width: 100%;
  height: 200px;
  border-radius: 10px;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  width: 100%;
}

:deep(.el-tabs__header) {
  margin: 0 0 12px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0;
}

:deep(.el-tabs__item) {
  font-weight: 600;
}

:deep(.el-upload-dragger) {
  border-radius: 12px;
  border: 1px dashed rgba(64, 158, 255, 0.4);
  background: rgba(64, 158, 255, 0.04);
}

:deep(.el-upload-dragger:hover) {
  border-color: rgba(64, 158, 255, 0.8);
  background: rgba(64, 158, 255, 0.06);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 10px;
}

@media (max-width: 960px) {
  .customize-content {
    grid-template-columns: 1fr;
  }

  .txt2img-layout {
    grid-template-columns: 1fr;
  }

  .txt2img-result-image {
    height: 360px;
  }

  .preview-image {
    height: 260px;
  }
}

.customize-ai-preview {
  max-width: 100%;
  max-height: 150px;
  border-radius: 6px;
}

.merchant-select-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-merchant {
  color: var(--el-color-primary);
  font-weight: 500;
}

.no-merchant {
  color: var(--el-text-color-placeholder);
}

.customize-upload-area {
  width: 100%;
}

.customize-upload-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.customize-upload-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.customize-upload-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.customize-upload-item-delete {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.customize-upload-item:hover .customize-upload-item-delete {
  opacity: 1;
}

.customize-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--el-fill-color-lighter);
}

.customize-upload-trigger:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.customize-upload-icon {
  font-size: 24px;
  color: var(--el-text-color-placeholder);
  margin-bottom: 6px;
}

.customize-upload-text {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.customize-upload-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 2px;
}

.customize-upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 6px;
}

/* 商家选择弹窗 */
/* 商家选择表格 */
.merchant-table-info {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.merchant-table-detail {
  text-align: left;
}

.merchant-table-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.merchant-table-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.merchant-table-products {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.merchant-table-product {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 8px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  min-width: 170px;
  max-width: 200px;
}

.merchant-table-product-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.merchant-table-product-info {
  min-width: 0;
  flex: 1;
}

.merchant-table-product-name {
  font-size: 13px;
  color: var(--el-text-color-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 110px;
}

.merchant-table-product-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  margin-top: 2px;
}

.merchant-table-product-price {
  color: var(--el-color-danger);
  font-weight: 600;
}

.merchant-table-product-rating {
  color: #ff9900;
}

.merchant-table-empty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.merchant-table-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

:deep(.merchant-selected-row) {
  background-color: var(--el-color-primary-light-9) !important;
}

:deep(.merchant-selected-row:hover > td) {
  background-color: var(--el-color-primary-light-8) !important;
}

/* 商家商品详情弹窗 */
.merchant-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}

.merchant-detail-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.merchant-detail-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.merchant-detail-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.merchant-detail-body {
  padding: 10px;
}

.merchant-detail-name {
  font-size: 14px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-detail-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.merchant-detail-price {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-color-danger);
}

.merchant-detail-rating {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.merchant-detail-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
