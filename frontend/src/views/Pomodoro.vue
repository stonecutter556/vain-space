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

      <div class="pomo-goal-selector">
        <span class="pomo-goal-label">关联目标</span>
        <select class="pomo-goal-select" v-model="selectedGoalId" :disabled="isRunning || isPaused">
          <option :value="null">无关联</option>
          <option v-for="g in goalOptions" :key="g.id" :value="g.id">{{ g.title }}</option>
        </select>
      </div>
    </div>

    <div class="pomo-section">
      <div class="pomo-section-title">今日统计</div>
      <div v-if="statsTotal > 0" class="pomo-stats">
        <div class="pomo-stats-row">
          <div class="pomo-stats-chart-wrap">
            <canvas ref="pieCanvas" class="pomo-canvas"></canvas>
          </div>
          <div class="pomo-stats-chart-wrap">
            <canvas ref="lineCanvas" class="pomo-canvas"></canvas>
          </div>
        </div>
      </div>
      <div v-else class="pomo-empty">今日暂无数据</div>
    </div>

    <div class="pomo-section">
      <div class="pomo-section-title">今日记录</div>
      <div class="pomo-records" v-if="todayRecords.length">
        <div class="pomo-record" v-for="r in todayRecords" :key="r.id">
          <span class="pomo-record-dur">{{ r.focusMinutes }} 分钟</span>
          <span v-if="r.goalId && goalMap[r.goalId]" class="pomo-record-goal">{{ goalMap[r.goalId] }}</span>
          <span class="pomo-record-time">{{ formatRecordTime(r.endTime) }}</span>
        </div>
      </div>
      <div class="pomo-empty" v-else>今天还没有完成过番茄钟</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'

const message = useMessage()
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

function getToken() {
  return localStorage.getItem('vs_token')
}

function apiGet(url) {
  return axios.get(BACKEND_URL + url, { headers: { Authorization: 'Bearer ' + getToken() } })
}

function apiPost(url, data) {
  return axios.post(BACKEND_URL + url, data, { headers: { Authorization: 'Bearer ' + getToken() } })
}

function apiPut(url, data) {
  return axios.put(BACKEND_URL + url, data, { headers: { Authorization: 'Bearer ' + getToken() } })
}

const DEFAULT_MINUTES = 25
const remainingSeconds = ref(DEFAULT_MINUTES * 60)
const isRunning = ref(false)
const isPaused = ref(false)
const customMinutes = ref(DEFAULT_MINUTES)
const todayRecords = ref([])
const goalOptions = ref([])
const goalMap = ref({})
const selectedGoalId = ref(null)
const pieCanvas = ref(null)
const lineCanvas = ref(null)
const statsTotal = ref(0)
const statsGoalMinutes = ref({})
const statsTimeline = ref([])
let timer = null
let startTimestamp = 0
let pausedRemaining = 0

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
  startTimestamp = Date.now()
  timer = setInterval(() => {
    const elapsed = Math.floor((Date.now() - startTimestamp) / 1000)
    remainingSeconds.value = Math.max(0, customMinutes.value * 60 - elapsed)
    if (remainingSeconds.value <= 0) finish()
  }, 200)
}

function pause() {
  isRunning.value = false
  isPaused.value = true
  clearInterval(timer)
  pausedRemaining = remainingSeconds.value
}

function resume() {
  isRunning.value = true
  isPaused.value = false
  const resumeTarget = pausedRemaining
  startTimestamp = Date.now()
  timer = setInterval(() => {
    const elapsed = Math.floor((Date.now() - startTimestamp) / 1000)
    remainingSeconds.value = Math.max(0, resumeTarget - elapsed)
    if (remainingSeconds.value <= 0) finish()
  }, 200)
}

function reset() {
  isRunning.value = false
  isPaused.value = false
  clearInterval(timer)
  remainingSeconds.value = customMinutes.value * 60
  pausedRemaining = 0
}

async function finish() {
  isRunning.value = false
  isPaused.value = false
  clearInterval(timer)
  remainingSeconds.value = 0
  const focusMins = customMinutes.value
  try {
    const payload = {
      focusMinutes: focusMins,
      startTime: new Date(Date.now() - focusMins * 60000).toISOString(),
      endTime: new Date().toISOString(),
      status: '已完成'
    }
    if (selectedGoalId.value) {
      payload.goalId = selectedGoalId.value
    }
    await apiPost('/api/pomodoro', payload)
    if (selectedGoalId.value) {
      const goalRes = await apiGet('/api/goals')
      const goals = goalRes.data || []
      const goal = goals.find(g => g.id === selectedGoalId.value)
      if (goal) {
        const currentHours = goal.completedHours || 0
        await apiPut(`/api/goals/${selectedGoalId.value}`, {
          ...goal,
          completedHours: currentHours + focusMins / 60
        })
      }
    }
    message.success('番茄钟完成！')
    await Promise.all([fetchTodayRecords(), fetchStats(), fetchGoals()])
  } catch (e) {
    message.error('记录保存失败')
  }
  remainingSeconds.value = customMinutes.value * 60
}

async function fetchTodayRecords() {
  try {
    const res = await apiGet('/api/pomodoro')
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

async function fetchGoals() {
  try {
    const res = await apiGet('/api/goals')
    const all = res.data || []
    const filtered = all.filter(g => g.type === 'goal')
    goalOptions.value = filtered
    const map = {}
    filtered.forEach(g => { map[g.id] = g.title })
    goalMap.value = map
  } catch (e) {
    console.error('加载目标失败', e)
  }
}

async function fetchStats() {
  try {
    const today = new Date()
    const dateStr = today.getFullYear() + '-' +
      String(today.getMonth() + 1).padStart(2, '0') + '-' +
      String(today.getDate()).padStart(2, '0')
    const res = await apiGet('/api/pomodoro/stats?date=' + dateStr)
    const data = res.data || {}
    statsTotal.value = data.totalMinutes || 0
    statsGoalMinutes.value = data.goalMinutes || {}
    statsTimeline.value = data.timeline || []
    await nextTick()
    drawCharts()
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

function drawCharts() {
  drawPieChart()
  drawLineChart()
}

const PIE_COLORS = [
  'rgba(255,200,100,0.85)',
  'rgba(100,200,255,0.85)',
  'rgba(200,150,255,0.85)',
  'rgba(100,255,180,0.85)',
  'rgba(255,140,140,0.85)',
  'rgba(255,220,80,0.85)',
  'rgba(140,220,255,0.85)'
]

function drawPieChart() {
  const canvas = pieCanvas.value
  if (!canvas) return
  const rect = canvas.parentElement.getBoundingClientRect()
  const size = rect.width || 180
  const dpr = window.devicePixelRatio || 1
  canvas.width = size * dpr
  canvas.height = size * dpr
  canvas.style.width = size + 'px'
  canvas.style.height = size + 'px'
  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  const cx = size / 2
  const cy = size / 2
  const radius = size / 2 - 12
  const goalEntries = Object.entries(statsGoalMinutes.value)
  const total = goalEntries.reduce((s, [, m]) => s + m, 0)

  if (total === 0) {
    ctx.beginPath()
    ctx.arc(cx, cy, radius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,255,255,0.06)'
    ctx.fill()
    ctx.fillStyle = 'rgba(255,255,255,0.2)'
    ctx.font = '12px "Urbanist","Mgen",sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', cx, cy)
    return
  }

  let startAngle = -Math.PI / 2
  goalEntries.forEach(([gid, mins], i) => {
    const sliceAngle = (mins / total) * Math.PI * 2
    ctx.beginPath()
    ctx.moveTo(cx, cy)
    ctx.arc(cx, cy, radius, startAngle, startAngle + sliceAngle)
    ctx.closePath()
    ctx.fillStyle = PIE_COLORS[i % PIE_COLORS.length]
    ctx.fill()
    startAngle += sliceAngle
  })

  ctx.beginPath()
  ctx.arc(cx, cy, radius * 0.55, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(0,0,0,0.45)'
  ctx.fill()

  ctx.fillStyle = 'rgba(255,255,255,0.7)'
  ctx.font = '600 16px "Urbanist","Mgen",sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(total + '分钟', cx, cy - 6)
  ctx.fillStyle = 'rgba(255,255,255,0.3)'
  ctx.font = '11px "Urbanist","Mgen",sans-serif'
  ctx.fillText('共' + Object.keys(statsGoalMinutes.value).length + '项目标', cx, cy + 16)

  const legendY = size - 6
  let lx = 10
  goalEntries.forEach(([gid, mins], i) => {
    const label = goalMap.value[gid] || '目标#' + gid
    ctx.fillStyle = PIE_COLORS[i % PIE_COLORS.length]
    ctx.fillRect(lx, legendY - 6, 8, 8)
    ctx.fillStyle = 'rgba(255,255,255,0.4)'
    ctx.font = '10px "Urbanist","Mgen",sans-serif'
    ctx.textAlign = 'left'
    ctx.textBaseline = 'middle'
    const displayLabel = label.length > 6 ? label.slice(0, 6) + '..' : label
    ctx.fillText(displayLabel, lx + 12, legendY)
    lx += ctx.measureText(displayLabel).width + 26
    if (lx > size - 10) return
  })
}

function drawLineChart() {
  const canvas = lineCanvas.value
  if (!canvas) return
  const rect = canvas.parentElement.getBoundingClientRect()
  const size = rect.width || 180
  const dpr = window.devicePixelRatio || 1
  canvas.width = size * dpr
  canvas.height = size * dpr
  canvas.style.width = size + 'px'
  canvas.style.height = size + 'px'
  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  const pad = { top: 16, right: 10, bottom: 20, left: 8 }
  const w = size - pad.left - pad.right
  const h = size - pad.top - pad.bottom

  ctx.strokeStyle = 'rgba(255,255,255,0.06)'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (h / 4) * i
    ctx.beginPath()
    ctx.moveTo(pad.left, y)
    ctx.lineTo(pad.left + w, y)
    ctx.stroke()
  }

  const data = statsTimeline.value
  if (!data.length) {
    ctx.fillStyle = 'rgba(255,255,255,0.2)'
    ctx.font = '12px "Urbanist","Mgen",sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', size / 2, size / 2)
    return
  }

  const maxMins = Math.max(...data.map(d => d.minutes), 1)
  const points = data.map((d, i) => {
    const x = pad.left + (i / (data.length - 1 || 1)) * w
    const y = pad.top + h - (d.minutes / maxMins) * h
    return { x, y, minutes: d.minutes, time: d.time }
  })

  ctx.beginPath()
  ctx.strokeStyle = 'rgba(255,200,100,0.7)'
  ctx.lineWidth = 2
  ctx.lineJoin = 'round'
  points.forEach((p, i) => {
    i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y)
  })
  ctx.stroke()

  points.forEach(p => {
    ctx.beginPath()
    ctx.arc(p.x, p.y, 3, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,200,100,0.9)'
    ctx.fill()
  })

  if (data.length > 1) {
    const labelStep = Math.max(1, Math.floor(data.length / 6))
    data.forEach((d, i) => {
      if (i % labelStep !== 0 && i !== data.length - 1) return
      const x = pad.left + (i / (data.length - 1)) * w
      ctx.fillStyle = 'rgba(255,255,255,0.2)'
      ctx.font = '9px "Urbanist","Mgen",sans-serif'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'top'
      ctx.fillText(d.time, x, size - pad.bottom + 4)
    })
  }
}

onMounted(async () => {
  await Promise.all([fetchTodayRecords(), fetchStats(), fetchGoals()])
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.pomodoro-page {
  max-width: 480px;
  margin: 0 auto;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.pomo-goal-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  justify-content: center;
}

.pomo-goal-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
  white-space: nowrap;
}

.pomo-goal-select {
  flex: 1;
  max-width: 240px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  outline: none;
  cursor: pointer;
  appearance: auto;
}

.pomo-goal-select:focus {
  border-color: rgba(255, 255, 255, 0.25);
}

.pomo-goal-select:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.pomo-section-title {
  font-size: 13px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 10px;
  letter-spacing: 0.05em;
}

.pomo-stats {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 16px;
}

.pomo-stats-row {
  display: flex;
  gap: 12px;
}

.pomo-stats-chart-wrap {
  flex: 1;
  min-width: 0;
}

.pomo-canvas {
  display: block;
  width: 100%;
  height: auto;
  aspect-ratio: 1 / 1;
}

.pomo-records {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pomo-record {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  gap: 10px;
}

.pomo-record-dur {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
  min-width: 60px;
}

.pomo-record-goal {
  font-size: 12px;
  color: rgba(255, 200, 100, 0.5);
  font-weight: 300;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pomo-record-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
  margin-left: auto;
}

.pomo-empty {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.12);
  font-weight: 300;
}

@media (max-width: 768px) {
  .pomodoro-page { padding: 0 10px; }
  .pomo-card { padding: 20px 16px; }
  .pomo-timer-wrap { width: 200px; height: 200px; }
  .pomo-time { font-size: 42px; }
  .pomo-stats-row { flex-direction: column; }
  .pomo-canvas { max-height: 180px; }
  .pomo-records { gap: 6px; }
  .pomo-record { padding: 8px 12px; }
}
</style>
