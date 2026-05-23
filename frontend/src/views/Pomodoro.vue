<template>
  <div class="pomodoro-page">
    <div class="pomo-card">
      <div class="pomo-timer-wrap">
        <svg class="pomo-ring" viewBox="0 0 260 260">
          <circle class="pomo-ring-bg" cx="130" cy="130" r="115" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="4" />
          <circle class="pomo-ring-fill" cx="130" cy="130" r="115" fill="none" stroke="rgba(255,255,255,0.4)" stroke-width="4"
            :stroke-dasharray="circumference" :stroke-dashoffset="dashOffset" stroke-linecap="round"
            transform="rotate(-90, 130, 130)" />
        </svg>
        <div class="pomo-time">{{ formatTime(remainingSeconds) }}</div>
        <div class="pomo-status">{{ isRunning ? '专注中' : isPaused ? '已暂停' : '准备就绪' }}</div>
      </div>

      <div class="pomo-controls">
        <button v-if="!isRunning && !isPaused" class="pomo-btn pomo-btn-primary" @click="start">开始</button>
        <button v-if="isRunning" class="pomo-btn pomo-btn-warn" @click="pause">暂停</button>
        <button v-if="isPaused" class="pomo-btn pomo-btn-primary" @click="resume">继续</button>
        <button v-if="isRunning || isPaused" class="pomo-btn pomo-btn-muted" @click="reset">重置</button>
      </div>

      <div class="pomo-custom">
        <span class="pomo-custom-label">分钟</span>
        <input type="number" class="pomo-custom-input" v-model.number="customMinutes" :min="1" :max="120"
          :disabled="isRunning || isPaused" />
        <button class="pomo-btn pomo-btn-sm" :disabled="isRunning || isPaused" @click="applyCustom">应用</button>
      </div>
    </div>

    <div class="pomo-section">
      <div class="pomo-section-title">今日记录</div>
      <div class="pomo-records" v-if="todayRecords.length">
        <div class="pomo-record" v-for="r in todayRecords" :key="r.id">
          <span class="pomo-record-dur">{{ r.focusMinutes }} 分钟</span>
          <span class="pomo-record-time">{{ formatRecordTime(r.endTime) }}</span>
        </div>
      </div>
      <div class="pomo-empty" v-else>今天还没有完成过番茄钟</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useMessage } from 'naive-ui'
import { pomodoroApi } from '../api'

const message = useMessage()

const DEFAULT_MINUTES = 25
const remainingSeconds = ref(DEFAULT_MINUTES * 60)
const isRunning = ref(false)
const isPaused = ref(false)
const customMinutes = ref(DEFAULT_MINUTES)
const todayRecords = ref([])
let timer = null

const totalSeconds = computed(() => customMinutes.value * 60)
const progressPct = computed(() => ((totalSeconds.value - remainingSeconds.value) / totalSeconds.value) * 100)
const circumference = 2 * Math.PI * 115
const dashOffset = computed(() => circumference - (progressPct.value / 100) * circumference)

function formatTime(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function applyCustom() {
  remainingSeconds.value = customMinutes.value * 60
}

function start() {
  if (remainingSeconds.value <= 0) return
  isRunning.value = true
  isPaused.value = false
  timer = setInterval(() => {
    remainingSeconds.value--
    if (remainingSeconds.value <= 0) finish()
  }, 1000)
}

function pause() {
  isRunning.value = false
  isPaused.value = true
  clearInterval(timer)
}

function resume() {
  isRunning.value = true
  isPaused.value = false
  timer = setInterval(() => {
    remainingSeconds.value--
    if (remainingSeconds.value <= 0) finish()
  }, 1000)
}

function reset() {
  isRunning.value = false
  isPaused.value = false
  clearInterval(timer)
  remainingSeconds.value = customMinutes.value * 60
}

async function finish() {
  isRunning.value = false
  isPaused.value = false
  clearInterval(timer)
  remainingSeconds.value = 0
  try {
    await pomodoroApi.create({
      focusMinutes: customMinutes.value,
      startTime: new Date(Date.now() - customMinutes.value * 60000).toISOString(),
      endTime: new Date().toISOString(),
      status: '已完成'
    })
    message.success('番茄钟完成！')
    await fetchTodayRecords()
  } catch (e) {
    message.error('记录保存失败')
  }
  remainingSeconds.value = customMinutes.value * 60
}

async function fetchTodayRecords() {
  try {
    const res = await pomodoroApi.getAll()
    const all = res.data || []
    const today = new Date().toDateString()
    todayRecords.value = all.filter(r => {
      const d = new Date(r.startTime || r.createdAt)
      return d.toDateString() === today
    })
  } catch (e) {
    console.error('加载番茄钟记录失败', e)
  }
}

function formatRecordTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(fetchTodayRecords)
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.pomodoro-page {
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pomo-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.pomo-timer-wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 260px;
  height: 260px;
}

.pomo-ring {
  position: absolute;
  width: 100%;
  height: 100%;
}

.pomo-ring-fill {
  transition: stroke-dashoffset 0.5s ease;
}

.pomo-time {
  font-family: "Simplon", sans-serif;
  font-weight: 500;
  font-size: 56px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  z-index: 1;
}

.pomo-status {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
  margin-top: 8px;
  font-weight: 300;
  z-index: 1;
}

.pomo-controls {
  display: flex;
  gap: 10px;
}

.pomo-btn {
  padding: 10px 28px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(255, 255, 255, 0.6);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.02em;
}

.pomo-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.05);
}

.pomo-btn-primary {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
}

.pomo-btn-warn {
  border-color: rgba(255, 200, 100, 0.3);
  color: rgba(255, 200, 100, 0.6);
}

.pomo-btn-warn:hover {
  border-color: rgba(255, 200, 100, 0.5);
  color: rgba(255, 200, 100, 0.8);
}

.pomo-btn-muted {
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
}

.pomo-btn-sm {
  padding: 6px 16px;
  font-size: 12px;
}

.pomo-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.pomo-custom {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pomo-custom-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
}

.pomo-custom-input {
  width: 70px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  text-align: center;
  outline: none;
}

.pomo-custom-input:focus {
  border-color: rgba(255, 255, 255, 0.25);
}

.pomo-custom-input:disabled {
  opacity: 0.3;
}

.pomo-section-title {
  font-size: 13px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 10px;
  letter-spacing: 0.05em;
}

.pomo-records {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pomo-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.pomo-record-dur {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
}

.pomo-record-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
}

.pomo-empty {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.12);
  font-weight: 300;
}
</style>
