<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { i18n, getElementLocale } from "@/i18n";
import {
  translatePageToEnglish,
  clearOriginalTextCache,
  startTranslationObserver,
  stopTranslationObserver,
} from "@/utils/autoTranslate";
import { initTheme } from "@/utils/theme";

const route = useRoute();
const elementLocale = computed(() =>
  getElementLocale(i18n.global.locale.value),
);

// 页面加载时，如果语言设置为英文，启动翻译观察器
onMounted(async () => {
  initTheme();
  if (i18n.global.locale.value === "en") {
    // 先翻译当前已有内容
    try {
      await translatePageToEnglish();
    } catch (error) {
      console.error("页面加载自动翻译失败:", error);
    }
    // 启动 Observer 监听后续 DOM 变化（如异步数据加载后渲染的内容）
    startTranslationObserver();
  }
});

// 监听路由变化，清除原始文本缓存
// Observer 会自动检测新页面渲染的中文内容并翻译
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath !== oldPath) {
      // 路由变化时清除原始文本缓存（保留翻译结果缓存）
      clearOriginalTextCache();
    }
  },
);

// 监听语言变化，启动/停止翻译观察器
watch(
  () => i18n.global.locale.value,
  async (newLang, oldLang) => {
    if (newLang === oldLang) return;

    if (newLang === "en") {
      // 切换到英文，先翻译当前内容，再启动观察器
      try {
        await translatePageToEnglish();
      } catch (error) {
        console.error("语言切换自动翻译失败:", error);
      }
      startTranslationObserver();
    } else {
      // 切换到中文，停止观察器并恢复原始文本
      stopTranslationObserver();
      clearOriginalTextCache();
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
