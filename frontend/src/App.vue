<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { i18n, getElementLocale } from '@/i18n'

const elementLocale = computed(() => getElementLocale(i18n.global.locale.value))
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

html, body, #app {
  width: 100%;
  height: 100%;
}

body {
  background: var(--bg-page);
  color: var(--text-color-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans',
    'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
}
</style>
