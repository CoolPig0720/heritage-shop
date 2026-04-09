<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted, watch } from "vue";
import { i18n, getElementLocale } from "@/i18n";
import { translatePageToEnglish } from "@/utils/autoTranslate";
import { initTheme } from "@/utils/theme";

const elementLocale = computed(() =>
  getElementLocale(i18n.global.locale.value),
);

// 页面加载时，如果语言设置为英文，自动翻译
onMounted(async () => {
  initTheme();
  if (i18n.global.locale.value === "en") {
    // 等待 DOM 渲染完成后翻译
    setTimeout(async () => {
      try {
        await translatePageToEnglish();
      } catch (error) {
        console.error("页面加载自动翻译失败:", error);
      }
    }, 500);
  }
});

// 监听语言变化，切换为英文时自动翻译
watch(
  () => i18n.global.locale.value,
  async (newLang) => {
    if (newLang === "en") {
      setTimeout(async () => {
        try {
          await translatePageToEnglish();
        } catch (error) {
          console.error("语言切换自动翻译失败:", error);
        }
      }, 300);
    }
  },
);
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --app-max-width: 1200px;
  --app-bg: var(--bg-page);
  --app-border: var(--border-color-base);
  --app-radius: 12px;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
}

body {
  background: var(--bg-page);
  color: var(--text-color-primary);
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, "Noto Sans", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei",
    sans-serif;
  transition:
    background-color 0.3s ease,
    color 0.3s ease;
}
</style>
