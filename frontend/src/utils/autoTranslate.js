/**
 * DOM自动翻译工具
 * 自动扫描页面中的中文文本并翻译为英文
 */

import axios from "axios";

// localStorage 缓存键名
const CACHE_KEY = "translation_cache";
const CACHE_EXPIRE_DAYS = 7; // 缓存有效期 7 天

// 存储已翻译的文本缓存（翻译结果缓存，跨页面保留以提升性能）
const translationCache = new Map();
// 存储原始中文文本缓存（用于恢复，页面切换时需清除）
const originalTextCache = new Map();

/**
 * 从 localStorage 加载翻译缓存
 */
function loadCacheFromStorage() {
  try {
    const stored = localStorage.getItem(CACHE_KEY);
    if (stored) {
      const { data, expireTime } = JSON.parse(stored);
      if (Date.now() < expireTime) {
        const map = new Map(Object.entries(data));
        map.forEach((value, key) => translationCache.set(key, value));
        console.log(`从缓存加载 ${map.size} 条翻译记录`);
      } else {
        // 缓存过期，清除
        localStorage.removeItem(CACHE_KEY);
      }
    }
  } catch (e) {
    console.warn("加载翻译缓存失败:", e);
  }
}

/**
 * 保存翻译缓存到 localStorage
 */
function saveCacheToStorage() {
  try {
    const data = Object.fromEntries(translationCache);
    const expireTime = Date.now() + CACHE_EXPIRE_DAYS * 24 * 60 * 60 * 1000;
    localStorage.setItem(CACHE_KEY, JSON.stringify({ data, expireTime }));
    console.log(`保存 ${translationCache.size} 条翻译记录到缓存`);
  } catch (e) {
    console.warn("保存翻译缓存失败:", e);
  }
}

// 模块加载时从 localStorage 恢复缓存
loadCacheFromStorage();

/**
 * 清除原始文本缓存（路由切换时调用）
 * 注意：不清除 translationCache，保留翻译结果以提升性能
 */
export function clearOriginalTextCache() {
  // 先尝试恢复所有节点的文本（如果节点还在DOM中）
  originalTextCache.forEach((originalText, key) => {
    if (key instanceof Node && key.parentNode) {
      key.nodeValue = originalText;
    }
  });
  // 清除缓存
  originalTextCache.clear();
  console.log("已清除原始文本缓存（路由切换）");
}

/**
 * 清除所有缓存（包括 localStorage）
 */
export function clearAllCache() {
  originalTextCache.clear();
  translationCache.clear();
  localStorage.removeItem(CACHE_KEY);
  console.log("已清除所有翻译缓存");
}

/**
 * 检测文本是否包含中文
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含中文
 */
function containsChinese(text) {
  return /[\u4e00-\u9fa5]/.test(text);
}

/**
 * 清理文本，移除多余空格和换行
 * @param {string} text - 原始文本
 * @returns {string} 清理后的文本
 */
function cleanText(text) {
  return text.replace(/\s+/g, " ").trim();
}

/**
 * 批量翻译文本数组
 * @param {string[]} texts - 要翻译的文本数组
 * @returns {Promise<Map<string, string>>} 翻译结果映射
 */
async function batchTranslate(texts) {
  const resultMap = new Map();

  // 过滤空文本和已缓存的文本
  const uniqueTexts = [...new Set(texts.filter((text) => text && text.trim()))];
  const textsToTranslate = uniqueTexts.filter(
    (text) => !translationCache.has(text),
  );

  // 批量翻译未缓存的文本
  if (textsToTranslate.length > 0) {
    try {
      const response = await axios.post("/api/translate/batch", {
        texts: textsToTranslate,
        from: "zh",
        to: "en",
      });

      if (response.data.code === 200) {
        const translations = response.data.data;
        // 更新缓存
        textsToTranslate.forEach((original, index) => {
          const translated = translations[index] || original;
          translationCache.set(original, translated);
          resultMap.set(original, translated);
        });
        // 保存到 localStorage
        saveCacheToStorage();
      }
    } catch (error) {
      console.error("批量翻译失败:", error);
      // 失败时使用原始文本
      textsToTranslate.forEach((text) => {
        resultMap.set(text, text);
      });
    }
  }

  // 添加已缓存的文本
  uniqueTexts.forEach((text) => {
    if (translationCache.has(text)) {
      resultMap.set(text, translationCache.get(text));
    }
  });

  return resultMap;
}

/**
 * 扫描DOM节点中的文本节点和属性
 * @param {Node} rootNode - 起始节点
 * @returns {Object} 包含文本节点和属性的对象
 */
function scanAllChineseContent(rootNode) {
  const textNodes = [];
  const attributeNodes = [];

  // 确保rootNode有效
  const root = rootNode || document.body;
  if (!root) {
    console.warn("scanAllChineseContent: 没有有效的根节点");
    return { textNodes, attributeNodes };
  }

  const walker = document.createTreeWalker(
    rootNode || document.body,
    NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT,
    {
      acceptNode: function (node) {
        // 文本节点处理
        if (node.nodeType === Node.TEXT_NODE) {
          const text = node.nodeValue.trim();
          const parent = node.parentElement;
          if (!parent) return NodeFilter.FILTER_REJECT;

          const tagName = parent.tagName.toLowerCase();
          const excludeTags = ["script", "style"];
          const excludeClasses = ["no-translate", "code", "pre"];

          if (excludeTags.includes(tagName)) {
            return NodeFilter.FILTER_REJECT;
          }

          if (excludeClasses.some((cls) => parent.classList.contains(cls))) {
            return NodeFilter.FILTER_REJECT;
          }

          if (containsChinese(text) && !/^\d+$/.test(text)) {
            return NodeFilter.FILTER_ACCEPT;
          }
          return NodeFilter.FILTER_REJECT;
        }

        // 元素节点处理
        if (node.nodeType === Node.ELEMENT_NODE) {
          const tagName = node.tagName.toLowerCase();
          const excludeTags = ["script", "style"];

          if (excludeTags.includes(tagName)) {
            return NodeFilter.FILTER_REJECT;
          }

          return NodeFilter.FILTER_SKIP;
        }

        return NodeFilter.FILTER_REJECT;
      },
    },
    false,
  );

  let node;
  while ((node = walker.nextNode())) {
    if (node.nodeType === Node.TEXT_NODE) {
      textNodes.push(node);
    }
  }

  // 额外扫描Element Plus渲染后的label文本
  // 表单项标签
  const formLabels = root.querySelectorAll(".el-form-item__label");
  console.log("找到表单标签:", formLabels.length);
  formLabels.forEach((label) => {
    const text = label.textContent.trim();
    if (text && containsChinese(text)) {
      // 找到label内的文本节点
      const textNode = Array.from(label.childNodes).find(
        (n) => n.nodeType === Node.TEXT_NODE,
      );
      if (textNode && !textNodes.includes(textNode)) {
        textNodes.push(textNode);
        console.log("添加表单标签文本:", text);
      }
    }
  });

  // Tab标签
  const tabLabels = root.querySelectorAll(".el-tabs__item");
  console.log("找到Tab标签:", tabLabels.length);
  tabLabels.forEach((tab) => {
    // 递归查找所有文本节点
    const findAllTextNodes = (element) => {
      const nodes = [];
      for (const child of element.childNodes) {
        if (child.nodeType === Node.TEXT_NODE) {
          const text = child.nodeValue.trim();
          if (text && containsChinese(text)) {
            nodes.push(child);
          }
        } else if (child.nodeType === Node.ELEMENT_NODE) {
          nodes.push(...findAllTextNodes(child));
        }
      }
      return nodes;
    };

    const textNodesInTab = findAllTextNodes(tab);
    textNodesInTab.forEach((textNode) => {
      if (!textNodes.includes(textNode)) {
        textNodes.push(textNode);
        console.log("添加Tab标签文本:", textNode.nodeValue.trim());
      }
    });
  });

  // 扫描placeholder等属性
  const inputElements = root.querySelectorAll(
    "input[placeholder], textarea[placeholder]",
  );
  inputElements.forEach((el) => {
    const placeholder = el.getAttribute("placeholder");
    if (placeholder && containsChinese(placeholder)) {
      attributeNodes.push({
        element: el,
        attributeName: "placeholder",
        originalValue: placeholder,
      });
    }
  });

  return { textNodes, attributeNodes };
}

/**
 * 翻译页面到英文
 * @returns {Promise<void>}
 */
export async function translatePageToEnglish() {
  console.log("开始自动翻译页面...");

  // 扫描页面中的中文文本节点和属性
  const { textNodes, attributeNodes } = scanAllChineseContent();
  console.log(
    `找到 ${textNodes.length} 个文本节点，${attributeNodes.length} 个属性需要翻译`,
  );

  if (textNodes.length === 0 && attributeNodes.length === 0) {
    console.log("未找到需要翻译的中文内容");
    return;
  }

  // 提取所有需要翻译的文本
  const allTexts = [
    ...textNodes.map((node) => cleanText(node.nodeValue)),
    ...attributeNodes.map((attr) => cleanText(attr.originalValue)),
  ];

  // 批量翻译
  const translationMap = await batchTranslate(allTexts);

  // 替换文本节点
  let translatedCount = 0;
  textNodes.forEach((node, index) => {
    const originalText = cleanText(node.nodeValue);
    const translatedText = translationMap.get(originalText);

    if (translatedText && translatedText !== originalText) {
      // 缓存原始文本用于恢复
      originalTextCache.set(node, originalText);
      node.nodeValue = translatedText;
      translatedCount++;
    }
  });

  // 替换属性值
  attributeNodes.forEach((attrInfo, index) => {
    const originalValue = cleanText(attrInfo.originalValue);
    const translatedValue = translationMap.get(originalValue);

    if (translatedValue && translatedValue !== originalValue) {
      // 缓存原始属性值用于恢复
      originalTextCache.set(
        `${attrInfo.element.tagName}-${attrInfo.attributeName}`,
        attrInfo.originalValue,
      );
      attrInfo.element.setAttribute(attrInfo.attributeName, translatedValue);
      translatedCount++;
    }
  });

  console.log(`翻译完成，共翻译 ${translatedCount} 个内容`);
}

/**
 * 恢复页面到中文
 * @returns {Promise<void>}
 */
export async function restorePageToChinese() {
  console.log("恢复页面到中文...");

  let restoredCount = 0;

  // 恢复文本节点
  originalTextCache.forEach((originalText, key) => {
    if (key instanceof Node) {
      // 文本节点恢复
      if (key.parentNode) {
        // 确保节点仍然在DOM中
        key.nodeValue = originalText;
        restoredCount++;
      }
    } else if (typeof key === "string" && key.includes("-")) {
      // 属性恢复
      const [tagName, attrName] = key.split("-");
      const elements = document.getElementsByTagName(tagName);
      for (let element of elements) {
        if (element.hasAttribute && element.hasAttribute(attrName)) {
          element.setAttribute(attrName, originalText);
          restoredCount++;
          break;
        }
      }
    }
  });

  // 清除缓存
  originalTextCache.clear();

  console.log(`中文恢复完成，共恢复 ${restoredCount} 个内容`);
}

/**
 * 清除翻译缓存
 */
export function clearTranslationCache() {
  translationCache.clear();
  console.log("翻译缓存已清除");
}

// 导出工具函数
export default {
  translatePageToEnglish,
  restorePageToChinese,
  clearTranslationCache,
  clearOriginalTextCache,
  clearAllCache,
  containsChinese,
  cleanText,
};
