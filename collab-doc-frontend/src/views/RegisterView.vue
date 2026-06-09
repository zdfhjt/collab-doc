<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="brand-area">
        <div class="brand-logo">C</div>
        <h1 class="brand-name">CollabDoc</h1>
        <p class="brand-tagline">Write together, beautifully.</p>
      </div>
      <div class="brand-decoration">
        <div class="deco-line"></div>
        <div class="deco-dot"></div>
        <div class="deco-line"></div>
      </div>
    </div>
    <div class="auth-right">
      <div class="auth-card">
        <h2 class="auth-title">Create account</h2>
        <p class="auth-subtitle">Start collaborating today</p>
        <RegisterForm />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import RegisterForm from '@/components/auth/RegisterForm.vue'
</script>

<style scoped>
.auth-page {
  min-height: 100dvh;
  display: flex;
}
.auth-left {
  flex: 1;
  background: #0c0a09;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}
.auth-left::before {
  content: '';
  position: absolute;
  top: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(196, 98, 45, 0.25) 0%, rgba(196, 98, 45, 0.05) 40%, transparent 70%);
  animation: blobPulse 8s ease-in-out infinite;
}
.auth-left::after {
  content: '';
  position: absolute;
  bottom: -15%;
  left: -10%;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(196, 98, 45, 0.12) 0%, transparent 60%);
  animation: blobPulse 10s ease-in-out infinite reverse;
}
@keyframes blobPulse {
  0%, 100% { transform: scale(1) translate(0, 0); opacity: 1; }
  50% { transform: scale(1.08) translate(10px, -10px); opacity: 0.8; }
}
.auth-left .brand-area::before {
  content: '';
  position: fixed;
  inset: 0;
  right: 50%;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
  z-index: 0;
}
.brand-area {
  position: relative;
  z-index: 1;
  text-align: center;
}
.brand-logo {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--color-primary), #e07a3a);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin: 0 auto 32px;
  animation: logoFloat 6s ease-in-out infinite;
  box-shadow: 0 0 60px rgba(196, 98, 45, 0.3), 0 0 120px rgba(196, 98, 45, 0.1);
}
@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.brand-name {
  font-family: var(--font-display);
  font-size: 52px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.03em;
  line-height: 1;
}
.brand-tagline {
  font-size: 18px;
  color: rgba(255,255,255,0.4);
  margin-top: 12px;
  font-weight: 300;
  letter-spacing: 0.02em;
}
.brand-decoration {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 56px;
  position: relative;
  z-index: 1;
}
.deco-line {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
}
.deco-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 12px rgba(196, 98, 45, 0.6);
}
.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: var(--color-bg);
}
.auth-card {
  width: 100%;
  max-width: 400px;
  animation: cardSlideIn 0.6s cubic-bezier(0.32, 0.72, 0, 1) both;
  animation-delay: 0.15s;
}
@keyframes cardSlideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.auth-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.03em;
  line-height: 1.1;
}
.auth-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-top: 8px;
  margin-bottom: 36px;
}
</style>
