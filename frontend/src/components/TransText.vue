<template>
  <span :class="{ 'trans-loading': loading }">{{ displayText }}</span>
</template>

<script setup>
/**
 * 统一翻译组件
 * 所有文本都通过百度翻译API翻译
 * 中文模式直接显示原文，其他语言调用API翻译
 */
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { translateText } from '@/utils/translate'

const props = defineProps({
  // 要翻译的文本
  text: {
    type: String,
    default: ''
  },
  // 是否启用翻译（用于某些需要条件禁用的场景）
  enabled: {
    type: Boolean,
    default: true
  }
})

const { locale } = useI18n()
const translated = ref('')
const loading = ref(false)

// 显示文本
const displayText = computed(() => {
  // 未启用或中文直接返回原文
  if (!props.enabled || locale.value === 'zh') {
    return props.text
  }
  // 其他语言返回翻译结果（翻译中显示原文）
  return translated.value || props.text
})

// 监听文本和语言变化
watch(
  [() => props.text, locale],
  async ([text, lang]) => {
    // 中文模式或未启用时不需要翻译
    if (lang === 'zh' || !text || !props.enabled) {
      translated.value = ''
      return
    }
    
    // 调用API翻译
    loading.value = true
    try {
      translated.value = await translateText(text, 'zh', 'en')
    } catch (e) {
      console.error('Translation error:', e)
      translated.value = '' // 失败时显示原文
    } finally {
      loading.value = false
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.trans-loading {
  opacity: 0.6;
}
</style>
