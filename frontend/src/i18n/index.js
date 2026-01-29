import { createI18n } from 'vue-i18n'
import zh from '@/locales/zh'
import en from '@/locales/en'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import enUs from 'element-plus/es/locale/lang/en'

const lang = localStorage.getItem('lang') || 'zh'

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: lang,
  fallbackLocale: 'zh',
  messages: {
    zh: { ...zh, el: zhCn },
    en: { ...en, el: enUs }
  }
})

export const getElementLocale = (langKey) => {
  const msg = i18n.global.getLocaleMessage(langKey)
  return msg?.el || zhCn
}
