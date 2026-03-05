<template>
  <div class="admin-shell">
    <Header />

    <div class="admin-content">
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
.admin-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
}

.admin-content {
  flex: 1;
  padding-top: 60px;
  background: var(--bg-page);
  overflow-y: auto;
  transition: background-color 0.3s ease;
}
</style>
