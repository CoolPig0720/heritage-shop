<template>
  <div class="footer">
    <div class="footer-container">
      <div class="footer-section">
        <h3>关于我们</h3>
        <p>
          遗韵——非遗智能定制商城，融合AI技术与非遗文化，为您提供智能定制、商品交易、非遗资讯一站式服务体验。
        </p>
      </div>
      <div class="footer-section">
        <h3>快速链接</h3>
        <ul>
          <li><router-link to="/heritage">非遗文化</router-link></li>
          <li><router-link to="/products">商品列表</router-link></li>
          <li><router-link to="/customize">智能定制</router-link></li>
          <li><router-link to="/info">资讯中心</router-link></li>
        </ul>
      </div>
      <div class="footer-section">
        <h3>联系我们</h3>
        <ul>
          <li>电话：400-888-6688</li>
          <li>邮箱：service@yiyun-heritage.com</li>
          <li>地址：浙江省杭州市余杭区非遗数字产业园</li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2025 遗韵——非遗智能定制商城 版权所有</p>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.footer {
  background-color: var(--bg-elevated);
  color: var(--text-color-regular);
  padding: 40px 0 20px;
  margin-top: 40px;
  border-top: 1px solid var(--border-color-base);
  transition:
    background-color 0.3s ease,
    color 0.3s ease,
    border-color 0.3s ease;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.footer-section {
  flex: 1;
  margin: 0 20px;
}

.footer-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: var(--primary-color);
}

.footer-section p {
  line-height: 1.8;
  color: var(--text-color-secondary);
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
  color: var(--text-color-secondary);
}

.footer-section ul li a {
  color: var(--text-color-secondary);
  transition: color 0.3s;
}

.footer-section ul li a:hover {
  color: var(--primary-color);
}

.footer-bottom {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color-base);
}

.footer-bottom p {
  color: var(--text-color-secondary);
  font-size: 14px;
}
</style>
