<template>
  <div class="model-viewer-container" :style="{ width, height }">
    <!-- 加载前封面图 -->
    <div v-if="showPoster" class="model-viewer-poster" :style="posterStyle">
      <span class="poster-text">3D 模型</span>
    </div>
    <!-- 加载中 -->
    <div v-if="loading" class="model-viewer-loading">加载中...</div>
    <!-- 加载失败 -->
    <div v-if="errorMsg" class="model-viewer-error">
      <p>{{ errorMsg }}</p>
      <p v-if="errorMsg.includes('WebGL')">
        请尝试：更新显卡驱动 / 启用硬件加速 / 更换浏览器
      </p>
    </div>
    <!-- 3D 画布 -->
    <canvas ref="canvasRef" class="model-viewer-canvas" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import * as THREE from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader.js";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { isWebGLSupported } from "@/utils/webgl.js";

const props = defineProps({
  src: { type: String, default: "" },
  poster: { type: String, default: "" },
  alt: { type: String, default: "3D 模型" },
  autoRotate: { type: Boolean, default: true },
  width: { type: String, default: "100%" },
  height: { type: String, default: "500px" },
});

const emit = defineEmits(["load", "error"]);

const canvasRef = ref(null);
const loading = ref(false);
const errorMsg = ref("");
const showPoster = ref(true);

const posterStyle = computed(() => {
  if (props.poster) {
    return { backgroundImage: `url(${props.poster})` };
  }
  return {};
});

// Three.js 核心对象
let renderer = null;
let scene = null;
let camera = null;
let controls = null;
let model = null;
let animationId = null;
let groundMesh = null;
let initialized = false;
let observer = null;

/** 创建 WebGLRenderer，支持 WebGL2→WebGL1 回退 */
function createRenderer(canvas) {
  // 策略1：默认 WebGL2 + 抗锯齿
  try {
    const r = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
    if (r.getContext()) return r;
    r.dispose();
  } catch (e) {
    console.warn("[ModelViewer] WebGL2 创建失败，尝试回退:", e.message);
  }

  // 策略2：手动获取 WebGL1 上下文 + 抗锯齿
  try {
    const gl = canvas.getContext("webgl", { antialias: true });
    if (gl) {
      return new THREE.WebGLRenderer({ canvas, context: gl });
    }
  } catch (e) {
    console.warn("[ModelViewer] WebGL1 + 抗锯齿失败，尝试无抗锯齿:", e.message);
  }

  // 策略3：WebGL1 + 无抗锯齿
  try {
    const gl = canvas.getContext("webgl", { antialias: false });
    if (gl) {
      return new THREE.WebGLRenderer({ canvas, context: gl });
    }
  } catch (e) {
    console.warn("[ModelViewer] WebGL1 无抗锯齿也失败:", e.message);
  }

  // 策略4：experimental-webgl
  try {
    const gl = canvas.getContext("experimental-webgl", { antialias: false });
    if (gl) {
      return new THREE.WebGLRenderer({ canvas, context: gl });
    }
  } catch (e) {
    // ignore
  }

  return null;
}

/** 初始化场景 */
function initScene() {
  const canvas = canvasRef.value;
  if (!canvas) return false;

  // 检查 WebGL 支持
  if (!isWebGLSupported()) {
    errorMsg.value = "您的浏览器不支持 WebGL，无法显示3D模型。";
    emit("error", { message: errorMsg.value });
    return false;
  }

  // 创建渲染器
  renderer = createRenderer(canvas);
  if (!renderer) {
    errorMsg.value = "WebGL 上下文创建失败，无法显示3D模型。";
    emit("error", { message: errorMsg.value });
    return false;
  }

  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.outputEncoding = THREE.sRGBEncoding;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.0;
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;

  // 场景
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x2d3748);

  // 相机
  const rect = canvas.parentElement.getBoundingClientRect();
  camera = new THREE.PerspectiveCamera(
    45,
    rect.width / rect.height,
    0.01,
    1000,
  );
  camera.position.set(0, 1, 3);

  // 光照
  const ambientLight = new THREE.AmbientLight(0xffffff, 1.0);
  scene.add(ambientLight);

  const hemiLight = new THREE.HemisphereLight(0xffffff, 0x888888, 0.8);
  hemiLight.position.set(0, 20, 0);
  scene.add(hemiLight);

  // 主光源（右上前方）
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.2);
  dirLight.position.set(5, 10, 7);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 1024;
  dirLight.shadow.mapSize.height = 1024;
  dirLight.shadow.camera.near = 0.1;
  dirLight.shadow.camera.far = 50;
  dirLight.shadow.camera.left = -5;
  dirLight.shadow.camera.right = 5;
  dirLight.shadow.camera.top = 5;
  dirLight.shadow.camera.bottom = -5;
  dirLight.shadow.bias = -0.001;
  scene.add(dirLight);

  // 补光（左下方，填充主光照射不到的暗面）
  const fillLight = new THREE.DirectionalLight(0xffffff, 0.6);
  fillLight.position.set(-5, 3, -5);
  scene.add(fillLight);

  // 顶光（从正上方打下来，增强顶部亮度）
  const topLight = new THREE.DirectionalLight(0xffffff, 0.4);
  topLight.position.set(0, 15, 0);
  scene.add(topLight);

  // 控制器
  controls = new OrbitControls(camera, canvas);
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;
  controls.enablePan = true;
  controls.minDistance = 0.5;
  controls.maxDistance = 50;
  controls.target.set(0, 0, 0);

  // 地面阴影接收面
  const groundGeo = new THREE.CircleGeometry(5, 64);
  const groundMat = new THREE.ShadowMaterial({ opacity: 0.3 });
  groundMesh = new THREE.Mesh(groundGeo, groundMat);
  groundMesh.rotation.x = -Math.PI / 2;
  groundMesh.position.y = 0;
  groundMesh.receiveShadow = true;
  scene.add(groundMesh);

  return true;
}

/** 加载模型 */
function loadModel(url) {
  if (!url || !scene) return;

  loading.value = true;
  errorMsg.value = "";

  // 移除旧模型
  if (model) {
    scene.remove(model);
    disposeObject(model);
    model = null;
  }

  const loader = new GLTFLoader();
  loader.load(
    url,
    (gltf) => {
      model = gltf.scene;

      // 开启阴影
      model.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true;
          child.receiveShadow = true;
        }
      });

      scene.add(model);

      // 计算包围盒，自适应相机
      const box = new THREE.Box3().setFromObject(model);
      const size = box.getSize(new THREE.Vector3());
      const center = box.getCenter(new THREE.Vector3());
      const maxDim = Math.max(size.x, size.y, size.z);
      const fov = camera.fov * (Math.PI / 180);
      const cameraDistance = (maxDim / (2 * Math.tan(fov / 2))) * 1.8;

      // 将模型居中
      model.position.sub(center);
      model.position.y -= box.min.y - center.y; // 让模型底部落在 y=0

      // 设置地面位置
      groundMesh.position.y = 0;

      // 设置相机和控制目标
      camera.position.set(
        cameraDistance * 0.6,
        cameraDistance * 0.5,
        cameraDistance * 0.8,
      );
      controls.target.set(0, size.y * 0.3, 0);
      controls.update();

      loading.value = false;
      showPoster.value = false;
      emit("load");
    },
    (progress) => {
      // 加载进度，暂不处理
    },
    (error) => {
      loading.value = false;
      errorMsg.value = "模型加载失败：" + (error.message || "未知错误");
      showPoster.value = false;
      emit("error", { message: errorMsg.value });
    },
  );
}

/** 动画循环 */
function animate() {
  animationId = requestAnimationFrame(animate);

  if (controls) controls.update();

  if (model && props.autoRotate) {
    model.rotation.y += 0.005;
  }

  if (renderer && scene && camera) {
    renderer.render(scene, camera);
  }
}

/** 响应尺寸变化 */
function onResize() {
  if (!renderer || !camera || !canvasRef.value) return;
  const rect = canvasRef.value.parentElement.getBoundingClientRect();
  camera.aspect = rect.width / rect.height;
  camera.updateProjectionMatrix();
  renderer.setSize(rect.width, rect.height);
}

/** 递归释放 GPU 资源 */
function disposeObject(obj) {
  obj.traverse((child) => {
    if (child.geometry) child.geometry.dispose();
    if (child.material) {
      if (Array.isArray(child.material)) {
        child.material.forEach((m) => m.dispose());
      } else {
        child.material.dispose();
      }
    }
  });
}

/** 完整清理 */
function cleanup() {
  if (animationId) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }
  if (controls) {
    controls.dispose();
    controls = null;
  }
  if (model) {
    disposeObject(model);
    model = null;
  }
  if (groundMesh) {
    groundMesh.geometry.dispose();
    groundMesh.material.dispose();
    groundMesh = null;
  }
  if (renderer) {
    renderer.dispose();
    renderer = null;
  }
  scene = null;
  camera = null;
}

// 监听 src 变化
watch(
  () => props.src,
  (newSrc) => {
    if (newSrc) {
      loadModel(newSrc);
    }
  },
);

onMounted(() => {
  // 使用 IntersectionObserver 等待画布可见后再初始化
  // 解决 el-dialog 关闭时画布 display:none 导致 WebGL 上下文创建失败的问题
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !initialized) {
        initialized = true;
        observer.disconnect();
        observer = null;
        // 延迟一帧确保 el-dialog 的 CSS 过渡完成，画布有实际尺寸
        requestAnimationFrame(() => {
          nextTick(() => {
            if (initScene()) {
              const rect =
                canvasRef.value.parentElement.getBoundingClientRect();
              // 确保尺寸有效（宽高>0）
              if (rect.width > 0 && rect.height > 0) {
                renderer.setSize(rect.width, rect.height);
              }
              animate();
              if (props.src) {
                loadModel(props.src);
              }
            }
          });
        });
      }
    },
    { threshold: 0.01 },
  );
  observer.observe(canvasRef.value);
  window.addEventListener("resize", onResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", onResize);
  if (observer) {
    observer.disconnect();
    observer = null;
  }
  cleanup();
});
</script>

<style scoped>
.model-viewer-container {
  position: relative;
  overflow: hidden;
  background: radial-gradient(circle at center, #2d3748 0%, #1a202c 100%);
  border-radius: 8px;
}

.model-viewer-canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.model-viewer-poster {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  background-color: #1a202c;
  z-index: 1;
}

.poster-text {
  color: rgba(255, 255, 255, 0.4);
  font-size: 16px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.5);
}

.model-viewer-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  padding: 8px 20px;
  border-radius: 4px;
  z-index: 2;
  font-size: 14px;
}

.model-viewer-error {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  padding: 24px;
  text-align: center;
  font-size: 14px;
  line-height: 1.8;
  z-index: 2;
}

.model-viewer-error p {
  margin: 0;
}
</style>
