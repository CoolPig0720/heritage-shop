/**
 * 翻译工具函数
 * 使用百度翻译API实现中英文翻译
 */

import request from './request'

// 翻译缓存存储
const CACHE_KEY = 'translation_cache'
const CACHE_EXPIRE = 7 * 24 * 60 * 60 * 1000 // 7天过期

// 内存缓存
let memoryCache = null

/**
 * 加载持久化缓存
 */
function loadCache() {
  if (memoryCache) return memoryCache
  
  try {
    const data = localStorage.getItem(CACHE_KEY)
    if (data) {
      const parsed = JSON.parse(data)
      // 检查过期
      if (parsed._expire && parsed._expire > Date.now()) {
        memoryCache = parsed
        return memoryCache
      }
    }
  } catch (e) {
    console.warn('Failed to load translation cache:', e)
  }
  
  memoryCache = { _expire: Date.now() + CACHE_EXPIRE }
  return memoryCache
}

/**
 * 保存缓存到持久化存储
 */
function saveCache() {
  try {
    if (memoryCache) {
      memoryCache._expire = Date.now() + CACHE_EXPIRE
      localStorage.setItem(CACHE_KEY, JSON.stringify(memoryCache))
    }
  } catch (e) {
    console.warn('Failed to save translation cache:', e)
  }
}

/**
 * 获取缓存key
 */
function getCacheKey(from, to, text) {
  return `${from}_${to}_${text}`
}

/**
 * 从缓存获取翻译结果
 */
function getFromCache(from, to, text) {
  const cache = loadCache()
  const key = getCacheKey(from, to, text)
  return cache[key] || null
}

/**
 * 存入缓存
 */
function setToCache(from, to, text, result) {
  const cache = loadCache()
  const key = getCacheKey(from, to, text)
  cache[key] = result
  saveCache()
}

// 请求队列，避免重复请求
const pendingRequests = new Map()

/**
 * 翻译单条文本
 * @param {string} text - 要翻译的文本
 * @param {string} from - 源语言（默认zh）
 * @param {string} to - 目标语言（默认en）
 * @returns {Promise<string>} 翻译结果
 */
export async function translateText(text, from = 'zh', to = 'en') {
  // 空文本直接返回
  if (!text || typeof text !== 'string' || !text.trim()) {
    return text
  }
  
  // 去除首尾空格
  const trimmedText = text.trim()
  
  // 检查缓存
  const cached = getFromCache(from, to, trimmedText)
  if (cached) {
    return cached
  }
  
  // 检查是否有相同请求正在进行
  const pendingKey = getCacheKey(from, to, trimmedText)
  if (pendingRequests.has(pendingKey)) {
    return pendingRequests.get(pendingKey)
  }
  
  // 发起翻译请求
  const requestPromise = (async () => {
    try {
      const res = await request.get('/api/translate', {
        params: { q: trimmedText, from, to }
      })
      
      // 解析百度翻译结果
      // 返回格式: { code: 200, data: { trans_result: [{ src, dst }] } }
      const transResult = res?.data?.trans_result
      if (transResult && transResult.length > 0) {
        const result = transResult[0].dst
        
        // 存入缓存
        setToCache(from, to, trimmedText, result)
        
        return result
      }
      
      return trimmedText // 翻译失败返回原文
    } catch (error) {
      console.error('Translation failed:', error)
      return trimmedText // 失败时返回原文
    } finally {
      // 从待处理队列中移除
      pendingRequests.delete(pendingKey)
    }
  })()
  
  pendingRequests.set(pendingKey, requestPromise)
  return requestPromise
}

/**
 * 批量翻译
 * @param {string[]} texts - 要翻译的文本数组
 * @param {string} from - 源语言
 * @param {string} to - 目标语言
 * @returns {Promise<string[]>} 翻译结果数组
 */
export async function translateBatch(texts, from = 'zh', to = 'en') {
  if (!texts || texts.length === 0) return texts
  
  // 分离已缓存和未缓存的文本
  const results = new Array(texts.length)
  const uncached = []
  const uncachedIndices = []
  
  texts.forEach((text, index) => {
    if (!text || typeof text !== 'string') {
      results[index] = text
      return
    }
    
    const trimmedText = text.trim()
    const cached = getFromCache(from, to, trimmedText)
    
    if (cached) {
      results[index] = cached
    } else {
      results[index] = trimmedText // 默认原文
      uncached.push(trimmedText)
      uncachedIndices.push(index)
    }
  })
  
  // 如果全部已缓存，直接返回
  if (uncached.length === 0) {
    return results
  }
  
  try {
    // 批量翻译（用换行符分隔）
    const combinedText = uncached.join('\n')
    const res = await request.post('/api/translate/batch', {
      q: combinedText,
      from,
      to
    })
    
    const transResult = res?.data?.trans_result
    if (transResult && transResult.length > 0) {
      // 解析批量翻译结果
      transResult.forEach((item, i) => {
        const originalIndex = uncachedIndices[i]
        results[originalIndex] = item.dst
        
        // 存入缓存
        setToCache(from, to, uncached[i], item.dst)
      })
    }
  } catch (error) {
    console.error('Batch translation failed:', error)
  }
  
  return results
}

/**
 * 清除翻译缓存
 */
export function clearTranslationCache() {
  memoryCache = { _expire: Date.now() + CACHE_EXPIRE }
  localStorage.removeItem(CACHE_KEY)
}

/**
 * 获取当前语言
 */
export function getCurrentLang() {
  return localStorage.getItem('lang') || 'zh'
}
