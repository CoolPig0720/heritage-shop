/**
 * 主题管理工具
 * 支持三种模式：light（亮色）、dark（暗色）、system（跟随系统）
 */

// 主题类型枚举
export const ThemeMode = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system'
}

// 获取当前主题模式
export const getThemeMode = () => {
  return localStorage.getItem('themeMode') || ThemeMode.SYSTEM
}

// 设置主题模式
export const setThemeMode = (mode) => {
  localStorage.setItem('themeMode', mode)
  applyTheme(mode)
}

// 检测系统主题偏好
export const getSystemPrefersDark = () => {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

// 监听系统主题变化
export const watchSystemTheme = (callback) => {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const handler = (e) => {
    if (getThemeMode() === ThemeMode.SYSTEM) {
      callback(e.matches)
    }
  }
  mediaQuery.addEventListener('change', handler)
  return () => mediaQuery.removeEventListener('change', handler)
}

// 应用主题
export const applyTheme = (mode) => {
  const html = document.documentElement
  let isDark = false

  if (mode === ThemeMode.DARK) {
    isDark = true
  } else if (mode === ThemeMode.SYSTEM) {
    isDark = getSystemPrefersDark()
  } else {
    isDark = false
  }

  if (isDark) {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
}

// 初始化主题
export const initTheme = () => {
  const mode = getThemeMode()
  applyTheme(mode)

  // 监听系统主题变化
  watchSystemTheme((isDark) => {
    const html = document.documentElement
    if (isDark) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  })
}

// ========== 旧API兼容（保持向后兼容） ==========

// 判断是否暗色模式
export const isDark = () => document.documentElement.classList.contains('dark')

// 初始化黑夜模式（兼容旧版）
export const initDarkMode = () => {
  initTheme()
}

// 切换暗色模式（兼容旧版）
export const toggleDark = () => {
  const currentMode = getThemeMode()
  const newMode = currentMode === ThemeMode.DARK ? ThemeMode.LIGHT : ThemeMode.DARK
  setThemeMode(newMode)
}

// 设置主题（兼容旧版）
export const setTheme = (theme) => {
  const mode = theme === 'dark' ? ThemeMode.DARK : ThemeMode.LIGHT
  setThemeMode(mode)
}
