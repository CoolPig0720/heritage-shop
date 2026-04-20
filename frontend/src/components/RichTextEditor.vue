<template>
  <div class="rich-editor">
    <Toolbar
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
    />
    <Editor
      :defaultConfig="editorConfig"
      :modelValue="modelValue"
      mode="default"
      style="min-height: 300px"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref, shallowRef } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import "@wangeditor/editor/dist/css/style.css";
import { API_BASE_URL } from "@/config/api.js";

const getToken = () => localStorage.getItem("token");

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["update:modelValue"]);

const editorRef = shallowRef(null);

const toolbarConfig = {};

const editorConfig = {
  placeholder: "请输入内容，支持图文混排...",
  MENU_CONF: {
    uploadImage: {
      server: `${API_BASE_URL}/api/file/upload`,
      fieldName: "file",
      maxFileSize: 10 * 1024 * 1024,
      allowedFileTypes: ["image/*"],
      headers: {
        Authorization: getToken() ? `Bearer ${getToken()}` : "",
      },
      customInsert(res, insertFn) {
        if (res.code === 200 && res.data) {
          const url = API_BASE_URL + res.data;
          insertFn(url, "", "");
        }
      },
      customUpload(file, insertFn) {
        const formData = new FormData();
        formData.append("file", file);

        fetch(`${API_BASE_URL}/api/file/upload`, {
          method: "POST",
          headers: {
            Authorization: getToken() ? `Bearer ${getToken()}` : "",
          },
          body: formData,
        })
          .then((res) => res.json())
          .then((res) => {
            if (res.code === 200 && res.data) {
              const url = API_BASE_URL + res.data;
              insertFn(url, "", "");
            }
          })
          .catch(() => {
            console.error("图片上传失败");
          });
      },
    },
  },
};

const handleCreated = (editor) => {
  editorRef.value = editor;
};

const handleChange = (editor) => {
  emit("update:modelValue", editor.getHtml());
};

onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});
</script>

<style scoped>
.rich-editor {
  border: 1px solid var(--border-color-lighter, #dcdfe6);
  border-radius: 4px;
  overflow: hidden;
}

.rich-editor :deep(.w-e-toolbar) {
  border-bottom: 1px solid var(--border-color-lighter, #dcdfe6) !important;
}

.rich-editor :deep(.w-e-text-container) {
  min-height: 300px;
}
</style>
