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
} from "@/utils/autoTranslate";
import { initTheme } from "@/utils/theme";

const route = useRoute();
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

// 监听路由变化，清除原始文本缓存
// 这解决了页面切换时 DOM 节点引用失效的问题
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath !== oldPath) {
      // 路由变化时清除原始文本缓存（保留翻译结果缓存）
      clearOriginalTextCache();

      // 如果当前是英文模式，等待新页面渲染后翻译
      if (i18n.global.locale.value === "en") {
        setTimeout(async () => {
          try {
            await translatePageToEnglish();
          } catch (error) {
            console.error("路由切换后自动翻译失败:", error);
          }
        }, 300);
      }
    }
  },
);

// 监听语言变化，自动翻译或恢复
watch(
  () => i18n.global.locale.value,
  async (newLang, oldLang) => {
    if (newLang === oldLang) return;

    if (newLang === "en") {
      // 切换到英文，翻译页面
      setTimeout(async () => {
        try {
          await translatePageToEnglish();
        } catch (error) {
          console.error("语言切换自动翻译失败:", error);
        }
      }, 300);
    } else {
      // 切换到中文，恢复原始文本
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
