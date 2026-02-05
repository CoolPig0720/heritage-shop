export const isDark = () => document.documentElement.classList.contains('dark')

// 初始化黑夜模式
export const initDarkMode = () => {
  const theme = localStorage.getItem('theme')
  if (theme === 'dark') {
    document.documentElement.classList.add('dark')
  }
}

export const toggleDark = () => {
  const html = document.documentElement
  html.classList.toggle('dark')
  localStorage.setItem('theme', isDark() ? 'dark' : 'light')
}

export const setTheme = (theme) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
  localStorage.setItem('theme', theme === 'dark' ? 'dark' : 'light')
}
