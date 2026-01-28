<template>
  <div class="footer">
    <div class="footer-container">
      <div class="footer-section">
        <h3>关于我们</h3>
        <p>非遗产品智能定制商城致力于传承和弘扬中华优秀传统文化，为用户提供优质的非遗产品和个性化定制服务。</p>
      </div>
      <div class="footer-section">
        <h3>快速链接</h3>
        <ul>
          <li><router-link to="/heritage">非遗文化</router-link></li>
          <li><router-link to="/products">商品列表</router-link></li>
          <li><router-link to="/customize">智能定制</router-link></li>
        </ul>
      </div>
      <div class="footer-section">
        <h3>联系我们</h3>
        <ul>
          <li>电话：400-123-4567</li>
          <li>邮箱：support@heritage.com</li>
          <li>地址：北京市朝阳区非遗产业园</li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2024 非遗产品智能定制商城 版权所有</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 40px;
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
  color: #3498db;
}

.footer-section p {
  line-height: 1.8;
  color: #bdc3c7;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
}

.footer-section ul li a {
  color: #bdc3c7;
  transition: color 0.3s;
}

.footer-section ul li a:hover {
  color: #3498db;
}

.footer-bottom {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #34495e;
}

.footer-bottom p {
  color: #7f8c8d;
  font-size: 14px;
}
</style>
