<template>
  <div class="main-layout">
    <Header />
    <div class="main-content">
      <router-view />
    </div>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
  transition: background-color 0.3s ease;
}

.main-content {
  flex: 1;
  padding-top: 60px;
  background-color: var(--bg-page);
}
</style>
