<template>
  <div class="characters-container" ref="containerRef">
    <!-- Logo -->
    <div class="logo">
      <span class="logo-icon">🏪</span>
      <span class="logo-text">非遗商城</span>
    </div>

    <!-- 紫色高矩形 - 最底层 -->
    <div
      class="character character-purple"
      :class="{ blinking: purpleBlinking }"
      :style="purpleStyle"
      ref="purpleRef"
    >
      <div class="eye eye-left">
        <div class="pupil" :style="purplePupilStyle"></div>
      </div>
      <div class="eye eye-right">
        <div class="pupil" :style="purplePupilStyle"></div>
      </div>
    </div>

    <!-- 深灰矩形 - 中间层 -->
    <div
      class="character character-dark"
      :class="{ blinking: darkBlinking }"
      :style="darkStyle"
      ref="darkRef"
    >
      <div class="eye eye-left">
        <div class="pupil white-pupil" :style="darkPupilStyle"></div>
      </div>
      <div class="eye eye-right">
        <div class="pupil white-pupil" :style="darkPupilStyle"></div>
      </div>
    </div>

    <!-- 橙色半圆 - 前左层 -->
    <div
      class="character character-orange"
      :class="{ blinking: orangeBlinking }"
      :style="orangeStyle"
      ref="orangeRef"
    >
      <div class="eye eye-left">
        <div class="pupil" :style="orangePupilStyle"></div>
      </div>
      <div class="eye eye-right">
        <div class="pupil" :style="orangePupilStyle"></div>
      </div>
    </div>

    <!-- 黄色圆顶 - 最前层 -->
    <div
      class="character character-yellow"
      :class="{ blinking: yellowBlinking }"
      :style="yellowStyle"
      ref="yellowRef"
    >
      <div class="eyes-row">
        <div class="eye eye-left">
          <div class="pupil" :style="yellowPupilStyle"></div>
        </div>
        <div class="eye eye-right">
          <div class="pupil" :style="yellowPupilStyle"></div>
        </div>
      </div>
      <div class="mouth"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";

// Props
const props = defineProps({
  isInputFocused: {
    type: Boolean,
    default: false,
  },
  isPasswordVisible: {
    type: Boolean,
    default: false,
  },
  passwordLength: {
    type: Number,
    default: 0,
  },
  isLogin: {
    type: Boolean,
    default: true,
  },
});

// Refs
const containerRef = ref(null);
const purpleRef = ref(null);
const darkRef = ref(null);
const orangeRef = ref(null);
const yellowRef = ref(null);

// Mouse position
const mouseX = ref(0);
const mouseY = ref(0);

// Blink states
const purpleBlinking = ref(false);
const darkBlinking = ref(false);
const orangeBlinking = ref(false);
const yellowBlinking = ref(false);

// Look at each other state
const isLookingAtEachOther = ref(false);

// Sneak peek state
const isSneaking = ref(false);

// Max distance for pupil movement
const MAX_PUPIL_DISTANCE = 5;

// Calculate pupil position
const calculatePupilPosition = (elementRef) => {
  if (!elementRef.value) return { x: 0, y: 0 };

  const rect = elementRef.value.getBoundingClientRect();
  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 2;

  const deltaX = mouseX.value - centerX;
  const deltaY = mouseY.value - centerY;
  const distance = Math.min(
    Math.sqrt(deltaX ** 2 + deltaY ** 2),
    MAX_PUPIL_DISTANCE,
  );
  const angle = Math.atan2(deltaY, deltaX);

  return {
    x: Math.cos(angle) * distance,
    y: Math.sin(angle) * distance,
  };
};

// Calculate body transform - 提高灵敏度
const calculateBodyTransform = (refEl) => {
  if (!refEl.value) return { skewX: 0, translateX: 0, translateY: 0 };

  const rect = refEl.value.getBoundingClientRect();
  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 3;

  const deltaX = mouseX.value - centerX;
  const deltaY = mouseY.value - centerY;

  const skewX = Math.max(-8, Math.min(8, -deltaX / 40));
  const translateX = Math.max(-20, Math.min(20, deltaX / 30));
  const translateY = Math.max(-15, Math.min(15, deltaY / 50));

  return { skewX, translateX, translateY };
};

// Pupil styles
const purplePupilStyle = computed(() => {
  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 眼睛向左看（不看密码）
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return { transform: `translate(-4px, 0)` };
    }

    // 密码隐藏且有内容 - 眼睛看向密码框方向（偷看）
    if (!props.isPasswordVisible && props.passwordLength > 0) {
      return { transform: `translate(4px, 0)` };
    }
  }

  // 打字时互相看
  if (isLookingAtEachOther.value) {
    return { transform: `translate(4px, 0)` };
  }

  const pos = calculatePupilPosition(purpleRef);
  return { transform: `translate(${pos.x}px, ${pos.y}px)` };
});

const darkPupilStyle = computed(() => {
  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 眼睛向左看
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return { transform: `translate(-4px, 0)` };
    }

    // 密码隐藏且有内容 - 眼睛看向输入框方向（右边）
    if (props.passwordLength > 0) {
      return { transform: `translate(4px, 0)` };
    }
  }

  if (isLookingAtEachOther.value) {
    return { transform: `translate(-4px, 0)` };
  }
  const pos = calculatePupilPosition(darkRef);
  return { transform: `translate(${pos.x}px, ${pos.y}px)` };
});

const orangePupilStyle = computed(() => {
  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 眼睛向左看
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return { transform: `translate(-4px, 0)` };
    }

    // 密码隐藏且有内容 - 眼睛看向输入框方向（右边）
    if (props.passwordLength > 0) {
      return { transform: `translate(4px, 0)` };
    }
  }

  if (isLookingAtEachOther.value) {
    return { transform: `translate(-4px, 0)` };
  }
  const pos = calculatePupilPosition(orangeRef);
  return { transform: `translate(${pos.x}px, ${pos.y}px)` };
});

const yellowPupilStyle = computed(() => {
  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 眼睛向左看
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return { transform: `translate(-4px, 0)` };
    }

    // 密码隐藏且有内容 - 眼睛看向输入框方向（右边）
    if (props.passwordLength > 0) {
      return { transform: `translate(4px, 0)` };
    }
  }

  if (isLookingAtEachOther.value) {
    return { transform: `translate(4px, 0)` };
  }
  const pos = calculatePupilPosition(yellowRef);
  return { transform: `translate(${pos.x}px, ${pos.y}px)` };
});

// Body styles
const purpleStyle = computed(() => {
  const { skewX, translateX, translateY } = calculateBodyTransform(purpleRef);

  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 身体正常摆正
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(0deg) translateX(0px) translateY(0px)`,
        height: "420px",
      };
    }

    // 密码隐藏且有内容 - 向右伸头偷看（大幅伸头）
    if (!props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(-10deg) translateX(60px) translateY(-30px)`,
        height: "500px",
      };
    }
  }

  // 正常状态
  return {
    transform: `skewX(${skewX}deg) translateX(${translateX}px) translateY(${translateY}px)`,
    height: "420px",
  };
});

const darkStyle = computed(() => {
  const { skewX, translateX, translateY } = calculateBodyTransform(darkRef);

  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 身体正常摆正
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(0deg) translateX(0px) translateY(0px)`,
      };
    }

    // 密码隐藏且有内容 - 向右倾斜偷看
    if (!props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(-5deg) translateX(15px)`,
      };
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      transform: `skewX(-4deg) translateX(-15px)`,
    };
  }
  return {
    transform: `skewX(${skewX}deg) translateX(${translateX}px) translateY(${translateY}px)`,
  };
});

const orangeStyle = computed(() => {
  const { skewX, translateX, translateY } = calculateBodyTransform(orangeRef);

  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 身体正常摆正
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(0deg) translateX(0px) translateY(0px)`,
      };
    }

    // 密码隐藏且有内容 - 向右倾斜偷看
    if (!props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(-5deg) translateX(12px)`,
      };
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      transform: `skewX(4deg) translateX(15px)`,
    };
  }
  return {
    transform: `skewX(${skewX}deg) translateX(${translateX}px) translateY(${translateY}px)`,
  };
});

const yellowStyle = computed(() => {
  const { skewX, translateX, translateY } = calculateBodyTransform(yellowRef);

  // 输入框聚焦时才有特殊效果
  if (props.isInputFocused) {
    // 密码可见时 - 身体正常摆正
    if (props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(0deg) translateX(0px) translateY(0px)`,
      };
    }

    // 密码隐藏且有内容 - 向右倾斜偷看
    if (!props.isPasswordVisible && props.passwordLength > 0) {
      return {
        transform: `skewX(-4deg) translateX(10px)`,
      };
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      transform: `skewX(-3deg) translateX(-8px)`,
    };
  }
  return {
    transform: `skewX(${skewX}deg) translateX(${translateX}px) translateY(${translateY}px)`,
  };
});

// Random blink interval
const getRandomBlinkInterval = () => Math.random() * 4000 + 3000;

// Schedule blink for a character
const scheduleBlink = (blinkingRef) => {
  const timeout = setTimeout(() => {
    blinkingRef.value = true;
    setTimeout(() => {
      blinkingRef.value = false;
      scheduleBlink(blinkingRef);
    }, 150);
  }, getRandomBlinkInterval());
  return timeout;
};

// Typing timeout
let typingTimeout = null;
let sneakTimeout = null;

// Watch typing state
watch(
  () => props.isTyping,
  (newVal) => {
    if (newVal) {
      isLookingAtEachOther.value = true;
      clearTimeout(typingTimeout);
      typingTimeout = setTimeout(() => {
        isLookingAtEachOther.value = false;
      }, 800);
    }
  },
);

// Watch password visibility
watch(
  () => props.isPasswordVisible,
  () => {
    // 密码可见性变化时的处理
  },
);

// Mouse move handler
const handleMouseMove = (e) => {
  mouseX.value = e.clientX;
  mouseY.value = e.clientY;
};

// Blink timeouts
let purpleBlinkTimeout = null;
let darkBlinkTimeout = null;
let orangeBlinkTimeout = null;
let yellowBlinkTimeout = null;

onMounted(() => {
  window.addEventListener("mousemove", handleMouseMove);

  if (containerRef.value) {
    const rect = containerRef.value.getBoundingClientRect();
    mouseX.value = rect.left + rect.width / 2;
    mouseY.value = rect.top + rect.height / 2;
  }

  setTimeout(() => {
    purpleBlinkTimeout = scheduleBlink(purpleBlinking);
  }, 0);
  setTimeout(() => {
    darkBlinkTimeout = scheduleBlink(darkBlinking);
  }, 1000);
  setTimeout(() => {
    orangeBlinkTimeout = scheduleBlink(orangeBlinking);
  }, 2000);
  setTimeout(() => {
    yellowBlinkTimeout = scheduleBlink(yellowBlinking);
  }, 3000);
});

onUnmounted(() => {
  window.removeEventListener("mousemove", handleMouseMove);
  clearTimeout(purpleBlinkTimeout);
  clearTimeout(darkBlinkTimeout);
  clearTimeout(orangeBlinkTimeout);
  clearTimeout(yellowBlinkTimeout);
  clearTimeout(typingTimeout);
  clearTimeout(sneakTimeout);
});
</script>

<style scoped>
.characters-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  position: absolute;
  top: 24px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  z-index: 10;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* Character base styles */
.character {
  position: absolute;
  transform-origin: bottom center;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 50px;
  gap: 16px;
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.character.blinking .eye {
  transform: scaleY(0.1);
  transition: transform 0.1s ease-in-out;
}

.character.blinking .eyes-row .eye {
  transform: scaleY(0.1);
  transition: transform 0.1s ease-in-out;
}

/* Purple character - 高矩形 */
.character-purple {
  width: 150px;
  height: 420px;
  background: linear-gradient(180deg, #a78bfa 0%, #8b5cf6 50%, #7c3aed 100%);
  border-radius: 75px 75px 30px 30px;
  left: calc(50% - 180px);
  bottom: 60px;
  z-index: 1;
  box-shadow: 0 15px 50px rgba(139, 92, 246, 0.4);
  transition:
    transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    height 0.4s ease;
}

.character-purple .eye {
  width: 28px;
  height: 28px;
}

.character-purple .pupil {
  width: 15px;
  height: 15px;
}

/* Dark character - 中等矩形 */
.character-dark {
  width: 110px;
  height: 350px;
  background: linear-gradient(180deg, #4b5563 0%, #374151 50%, #1f2937 100%);
  border-radius: 55px 55px 22px 22px;
  left: calc(50% - 25px);
  bottom: 60px;
  z-index: 2;
  box-shadow: 0 15px 50px rgba(55, 65, 81, 0.4);
}

.character-dark .eye {
  width: 25px;
  height: 25px;
}

/* Orange character - 矮胖半圆 */
.character-orange {
  width: 220px;
  height: 230px;
  background: linear-gradient(180deg, #fdba74 0%, #fb923c 50%, #f97316 100%);
  border-radius: 110px 110px 36px 36px;
  left: calc(50% - 160px);
  bottom: 60px;
  z-index: 3;
  padding-top: 55px;
  box-shadow: 0 15px 50px rgba(249, 115, 22, 0.4);
}

.character-orange .eye {
  width: 22px;
  height: 22px;
}

.character-orange .pupil {
  width: 12px;
  height: 12px;
}

/* Yellow character - 圆顶矩形 */
.character-yellow {
  width: 135px;
  height: 280px;
  background: linear-gradient(180deg, #fde047 0%, #facc15 50%, #eab308 100%);
  border-radius: 68px 68px 27px 27px;
  left: calc(50% + 40px);
  bottom: 60px;
  z-index: 4;
  padding-top: 50px;
  box-shadow: 0 15px 50px rgba(251, 191, 36, 0.4);
  flex-direction: column;
  align-items: center;
}

.eyes-row {
  display: flex;
  gap: 20px;
}

.character-yellow .eye {
  width: 22px;
  height: 22px;
}

.character-yellow .pupil {
  width: 12px;
  height: 12px;
}

/* Eye styles - 白色眼睛+黑瞳孔 */
.eye {
  width: 25px;
  height: 25px;
  background: white;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease-out;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pupil {
  width: 13px;
  height: 13px;
  background: #1a1a1a;
  border-radius: 50%;
  position: absolute;
  transition: transform 0.12s ease-out;
}

.pupil.white-pupil {
  background: white;
  border: 3px solid #1a1a1a;
  width: 13px;
  height: 13px;
  box-sizing: border-box;
}

/* Mouth for yellow character */
.mouth {
  width: 32px;
  height: 5px;
  background: #1a1a1a;
  border-radius: 3px;
  margin-top: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .characters-container {
    display: none;
  }
}
</style>
