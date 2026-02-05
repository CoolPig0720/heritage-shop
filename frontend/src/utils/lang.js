import { i18n } from '@/i18n'

export const setLang = (lang) => {
  i18n.global.locale.value = lang
  localStorage.setItem('lang', lang)
}

export const getLang = () => localStorage.getItem('lang') || 'zh'
