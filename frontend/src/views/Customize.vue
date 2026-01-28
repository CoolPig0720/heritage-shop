<template>
  <div class="customize">
    <div class="page-header">
      <h1>智能定制</h1>
      <p>AI 助力，打造专属非遗产品</p>
    </div>
    <div class="customize-content">
      <div class="upload-section">
        <el-upload
          class="upload-demo"
          drag
          action="/api/upload"
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 jpg/png 文件，文件大小不超过 5MB
            </div>
          </template>
        </el-upload>
      </div>
      <div class="options-section">
        <h3>定制选项</h3>
        <el-form :model="customizeForm" label-width="100px">
          <el-form-item label="产品类型">
            <el-select v-model="customizeForm.productType" placeholder="请选择产品类型">
              <el-option label="剪纸艺术" value="1" />
              <el-option label="蜡染工艺" value="2" />
              <el-option label="景泰蓝" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="风格偏好">
            <el-checkbox-group v-model="customizeForm.styles">
              <el-checkbox label="传统" />
              <el-checkbox label="现代" />
              <el-checkbox label="简约" />
              <el-checkbox label="华丽" />
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="颜色偏好">
            <el-color-picker v-model="customizeForm.color" />
          </el-form-item>
          <el-form-item label="定制说明">
            <el-input
              v-model="customizeForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入您的定制需求"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generateDesign">生成设计方案</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const customizeForm = reactive({
  productType: '',
  styles: [],
  color: '',
  description: ''
})

const generateDesign = () => {
  ElMessage.success('正在生成设计方案，请稍候...')
}
</script>

<style scoped>
.customize {
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 36px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 18px;
  color: #909399;
}

.customize-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.upload-section {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.options-section {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.options-section h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}
</style>
