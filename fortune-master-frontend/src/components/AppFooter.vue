<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="brand">玄机命阁 · AI 算命大师</div>
      <div class="copy">© {{ currentYear }} 命理解读仅供参考，重要决策请结合现实情况判断。</div>
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear()
</script>

<style scoped>
.app-footer {
  border-top: 1px solid rgba(112, 72, 34, 0.15);
  background: rgba(255, 248, 238, 0.72);
  backdrop-filter: blur(10px);
}

.footer-content {
  max-width: 1180px;
  margin: 0 auto;
  padding: 18px 20px 26px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: #654632;
  font-size: 14px;
}

.brand {
  font-weight: 700;
}

@media (max-width: 720px) {
  .footer-content {
    flex-direction: column;
  }
}
</style>
