<template>
  <div class="widget-home" ref="homeRef">
    <div
      v-for="(widget, i) in widgets"
      :key="widget.id"
      class="widget-block"
      :class="{ expanded: widget.expanded }"
      :style="getWidgetStyle(widget)"
      @mousedown.prevent="onDragStart($event, i)"
      @dblclick="toggleExpand(i)"
    >
      <!-- 时钟 -->
      <template v-if="widget.type === 'clock'">
        <div class="widget-clock">
          <div class="clock-narrow" v-show="!widget.expanded">
            <div class="clock-time">{{ clockData.h }}<span class="clock-blur">:{{ clockData.m }}</span></div>
            <div class="clock-ampm">{{ clockData.ampm }}</div>
            <div class="clock-line" :style="{ bottom: clockData.linePct + '%' }"></div>
          </div>
          <div class="clock-wide" v-show="widget.expanded">
            <div class="clock-time-wide">{{ clockData.h }}:{{ clockData.m }}<span class="clock-sec">:{{ clockData.s }}</span></div>
            <div class="clock-date-wide">{{ clockData.dateStr }}</div>
            <div class="clock-greeting">{{ clockData.greeting }}</div>
          </div>
        </div>
      </template>

      <!-- 统计 -->
      <template v-if="widget.type === 'stats'">
        <div class="widget-stats">
          <div class="stats-narrow" v-show="!widget.expanded">
            <div class="stats-main-num">{{ statsData.progress }}<span class="stats-unit">%</span></div>
            <div class="stats-main-label">目标进度</div>
          </div>
          <div class="stats-wide" v-show="widget.expanded">
            <div class="stats-item">
              <div class="stats-icon">🎯</div>
              <div class="stats-num">{{ statsData.progress }}%</div>
              <div class="stats-label">目标进度</div>
              <div class="stats-bar"><div class="stats-bar-fill" :style="{ width: statsData.progress + '%' }"></div></div>
            </div>
            <div class="stats-item">
              <div class="stats-icon">🍅</div>
              <div class="stats-num">{{ statsData.pomodoro }}</div>
              <div class="stats-label">今日番茄</div>
            </div>
            <div class="stats-item">
              <div class="stats-icon">😊</div>
              <div class="stats-num">{{ statsData.mood }}/10</div>
              <div class="stats-label">今日心情</div>
            </div>
          </div>
        </div>
      </template>

      <!-- 文章 -->
      <template v-if="widget.type === 'articles'">
        <div class="widget-articles">
          <div class="articles-narrow" v-show="!widget.expanded">
            <div class="articles-title-narrow">文章</div>
            <div class="articles-preview" v-if="articlesData.length">{{ articlesData[0].title }}</div>
            <div class="articles-empty" v-else>暂无</div>
          </div>
          <div class="articles-wide" v-show="widget.expanded">
            <div class="articles-header">最近文章</div>
            <div class="articles-list">
              <div v-for="a in articlesData.slice(0, 5)" :key="a.id" class="articles-item" @click.stop="goArticle(a.id)">
                <span class="articles-item-title">{{ a.title }}</span>
                <span :class="['tag-sm', a.articleType === 'diary' ? 'tag-diary' : 'tag-blog']">{{ a.articleType === 'diary' ? '日记' : '博客' }}</span>
              </div>
              <div v-if="!articlesData.length" class="articles-empty">暂无文章</div>
            </div>
          </div>
        </div>
      </template>

      <!-- 健身 -->
      <template v-if="widget.type === 'workouts'">
        <div class="widget-workouts">
          <div class="workouts-narrow" v-show="!widget.expanded">
            <div class="workouts-title-narrow">健身</div>
            <div class="workouts-preview" v-if="workoutsData.length">{{ workoutsData[0].exercise }}</div>
            <div class="workouts-empty" v-else>暂无</div>
          </div>
          <div class="workouts-wide" v-show="widget.expanded">
            <div class="workouts-header">最近健身</div>
            <div class="workouts-list">
              <div v-for="w in workoutsData.slice(0, 5)" :key="w.id" class="workouts-item">
                <span class="workouts-item-name">{{ w.exercise }}</span>
                <span class="workouts-item-meta">{{ w.sets }}组 × {{ w.reps }}次 · {{ w.weight }}kg</span>
              </div>
              <div v-if="!workoutsData.length" class="workouts-empty">暂无记录</div>
            </div>
          </div>
        </div>
      </template>

      <!-- 进度 -->
      <template v-if="widget.type === 'progress'">
        <div class="widget-progress" @click.stop="cycleProgress">
          <div class="progress-content">
            <div class="progress-grid" :style="progressGridStyle">
              <div
                v-for="(dot, di) in progressDots"
                :key="di"
                class="progress-dot"
                :style="{ opacity: dot }"
              ></div>
            </div>
            <div class="progress-label">{{ progressLabel }}</div>
            <div class="progress-pct">{{ progressPct }}%</div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, dailyLogApi, workoutApi, goalApi } from '../api'

const router = useRouter()
const homeRef = ref(null)

const GRID_COLS = 12
let gridSize = computed(() => Math.floor(window.innerWidth / GRID_COLS))

const savedLayout = JSON.parse(localStorage.getItem('vs_widget_layout') || 'null')

const widgets = reactive((savedLayout && savedLayout.length) ? savedLayout : [
  { id: 'clock', type: 'clock', x: 0, y: 0, expanded: false },
  { id: 'stats', type: 'stats', x: 0, y: 1, expanded: false },
  { id: 'articles', type: 'articles', x: 2, y: 0, expanded: false },
  { id: 'workouts', type: 'workouts', x: 2, y: 1, expanded: false },
  { id: 'progress', type: 'progress', x: 1, y: 2, expanded: false }
])

const clockData = reactive({ h: '00', m: '00', s: '00', ampm: 'AM', linePct: 0, dateStr: '', greeting: '' })
const statsData = reactive({ progress: 0, pomodoro: 0, mood: 0 })
const articlesData = ref([])
const workoutsData = ref([])

const PROGRESS_TYPES = ['day', 'week', 'month', 'year']
const progressIndex = ref(0)
const progressDots = ref([])
const progressLabel = ref('')
const progressPct = ref(0)
const progressCols = ref(12)

const progressGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${progressCols.value}, 1fr)`
}))

let dragIndex = -1
let dragOffX = 0, dragOffY = 0
let clockTimer = null
let refreshTimer = null

function getWidgetStyle(w) {
  const gs = gridSize.value
  const wdt = w.expanded ? gs * 2 : gs
  return {
    width: wdt + 'px',
    height: gs + 'px',
    left: (w.x * gs) + 'px',
    top: (w.y * gs) + 'px'
  }
}

function saveLayout() {
  const data = widgets.map(w => ({ id: w.id, type: w.type, x: w.x, y: w.y, expanded: w.expanded }))
  localStorage.setItem('vs_widget_layout', JSON.stringify(data))
}

function onDragStart(e, i) {
  if (e.button !== 0) return
  dragIndex = i
  const rect = homeRef.value.getBoundingClientRect()
  dragOffX = e.clientX - rect.left - widgets[i].x * gridSize.value
  dragOffY = e.clientY - rect.top - widgets[i].y * gridSize.value
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
}

function onDragMove(e) {
  if (dragIndex < 0) return
  const rect = homeRef.value.getBoundingClientRect()
  const gs = gridSize.value
  const w = widgets[dragIndex]
  const maxX = w.expanded ? GRID_COLS - 2 : GRID_COLS - 1
  const maxY = Math.floor((window.innerHeight - 80) / gs) - 1

  let newX = Math.round((e.clientX - rect.left - dragOffX) / gs)
  let newY = Math.round((e.clientY - rect.top - dragOffY) / gs)

  newX = Math.max(0, Math.min(newX, maxX))
  newY = Math.max(0, Math.min(newY, maxY))

  for (const [idx, other] of widgets.entries()) {
    if (idx === dragIndex) continue
    const ow = other.expanded ? 2 : 1
    if (newX < other.x + ow && newX + (w.expanded ? 2 : 1) > other.x && newY === other.y) {
      return
    }
  }

  w.x = newX
  w.y = newY
}

function onDragEnd() {
  if (dragIndex >= 0) saveLayout()
  dragIndex = -1
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
}

function toggleExpand(i) {
  const w = widgets[i]
  if (w.expanded || w.x < GRID_COLS - 1) {
    w.expanded = !w.expanded
    saveLayout()
  }
}

function updateClock() {
  const now = new Date()
  let h = now.getHours()
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  const ampm = h >= 12 ? 'PM' : 'AM'
  const h12 = h % 12 || 12
  clockData.h = String(h12).padStart(2, '0')
  clockData.m = m
  clockData.s = s
  clockData.ampm = ampm
  clockData.linePct = (now.getSeconds() / 59) * 96 + 2

  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  clockData.dateStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`

  const hour = now.getHours()
  if (hour < 6) clockData.greeting = '夜深了 🌙'
  else if (hour < 9) clockData.greeting = '早上好 ☀️'
  else if (hour < 12) clockData.greeting = '上午好 ✨'
  else if (hour < 14) clockData.greeting = '中午好 🍃'
  else if (hour < 18) clockData.greeting = '下午好 💪'
  else if (hour < 21) clockData.greeting = '傍晚好 🌆'
  else clockData.greeting = '晚安 🌙'
}

function updateProgress() {
  const now = new Date()
  const type = PROGRESS_TYPES[progressIndex.value % PROGRESS_TYPES.length]
  const hour = now.getHours()
  const minute = now.getMinutes()
  const second = now.getSeconds()
  const totalSec = hour * 3600 + minute * 60 + second

  let pct, total, label, rows, cols

  switch (type) {
    case 'day':
      pct = (totalSec / 86400) * 100
      label = '今天'
      rows = 2; cols = 12
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const startOfWeek = new Date(now)
      startOfWeek.setDate(now.getDate() - dayOfWeek)
      startOfWeek.setHours(0, 0, 0, 0)
      const secThisWeek = (now - startOfWeek) / 1000
      pct = (secThisWeek / (7 * 86400)) * 100
      label = '本周'
      rows = 1; cols = 7
      break
    case 'month':
      const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
      const dayOfMonth = now.getDate()
      pct = ((dayOfMonth - 1 + totalSec / 86400) / daysInMonth) * 100
      label = '本月'
      rows = 6; cols = 7
      break
    case 'year':
      const startOfYear = new Date(now.getFullYear(), 0, 1)
      const dayOfYear = Math.floor((now - startOfYear) / 86400000) + 1
      const totalDays = (now.getFullYear() % 4 === 0 && now.getFullYear() % 100 !== 0) || now.getFullYear() % 400 === 0 ? 366 : 365
      pct = (dayOfYear / totalDays) * 100
      label = '今年'
      rows = 2; cols = 6
      break
  }

  progressPct.value = pct.toFixed(1)
  progressLabel.value = label
  progressCols.value = cols

  const totalDots = rows * cols
  const fullDots = Math.floor((pct / 100) * totalDots)
  const partialAlpha = ((pct / 100) * totalDots) - fullDots

  const dots = []
  for (let i = 0; i < totalDots; i++) {
    if (i < fullDots) dots.push(1)
    else if (i === fullDots) dots.push(0.1 + partialAlpha * 0.9)
    else dots.push(0.1)
  }
  progressDots.value = dots
}

async function fetchData() {
  try {
    const [goalsRes, dailyRes, articlesRes, workoutRes] = await Promise.all([
      goalApi.getAll(), dailyLogApi.getToday(), articleApi.getAll(), workoutApi.getAll()
    ])

    const goals = goalsRes.data || []
    const completed = goals.filter(g => g.status === '已完成').length
    statsData.progress = goals.length ? Math.round((completed / goals.length) * 100) : 0

    const today = dailyRes.data
    if (today && today.id) {
      statsData.pomodoro = today.pomodoroCount ?? 0
      statsData.mood = today.moodRating ?? 0
    }

    articlesData.value = (articlesRes.data || []).slice(0, 5)
    workoutsData.value = (workoutRes.data || []).slice(0, 5)
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

function goArticle(id) {
  router.push({ name: 'ArticleDetail', params: { id } })
}

function onResize() {
  gridSize.value = Math.floor(window.innerWidth / GRID_COLS)
}

onMounted(() => {
  updateClock()
  updateProgress()
  fetchData()
  clockTimer = setInterval(() => {
    updateClock()
    updateProgress()
  }, 1000)
  refreshTimer = setInterval(fetchData, 30000)
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(refreshTimer)
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.widget-home {
  position: relative;
  width: 100%;
  min-height: calc(100vh - 100px);
}

.widget-block {
  position: absolute;
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10%;
  cursor: grab;
  overflow: hidden;
  transition: left 0.15s ease, top 0.15s ease, width 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.widget-block:hover {
  border-color: rgba(255, 255, 255, 0.35);
}

.widget-block.expanded {
  cursor: default;
}

/* === Clock Narrow === */
.clock-narrow {
  position: relative;
  display: flex;
  padding: 10%;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  align-items: flex-start;
  font-family: "Nothing", sans-serif;
}

.clock-time {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 0.98;
  font-size: calc(min(9vw, 52px));
  font-weight: normal;
  color: white;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.clock-blur {
  opacity: 0.35;
  font-size: 0.6em;
  line-height: 1;
}

.clock-ampm {
  font-size: calc(min(2vw, 13px));
  color: rgba(255, 255, 255, 0.4);
  font-weight: normal;
  text-align: right;
}

.clock-line {
  position: absolute;
  left: 0;
  width: 100%;
  height: 1.5px;
  background: rgba(255, 255, 255, 0.3);
  transition: bottom 0.3s ease;
}

/* === Clock Wide === */
.clock-wide {
  padding: 10%;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: left;
}

.clock-time-wide {
  font-family: "Simplon", sans-serif;
  font-weight: 500;
  font-size: calc(min(6vw, 38px));
  color: white;
  letter-spacing: -0.03em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.clock-sec {
  opacity: 0.35;
  font-size: 0.7em;
}

.clock-date-wide {
  font-size: calc(min(2vw, 13px));
  color: rgba(255, 255, 255, 0.4);
  margin-top: 6px;
  font-weight: 300;
}

.clock-greeting {
  font-size: calc(min(2.2vw, 14px));
  color: rgba(255, 255, 255, 0.25);
  margin-top: 4px;
  font-weight: 300;
}

/* === Stats === */
.stats-narrow {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 12%;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  justify-content: center;
}

.stats-main-num {
  font-size: calc(min(6vw, 38px));
  font-weight: 300;
  color: white;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.stats-unit {
  font-size: 0.5em;
  color: rgba(255, 255, 255, 0.3);
}

.stats-main-label {
  font-size: calc(min(1.8vw, 12px));
  color: rgba(255, 255, 255, 0.35);
  margin-top: 2px;
  font-weight: 300;
}

.stats-wide {
  display: flex;
  gap: 8px;
  padding: 10%;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
}

.stats-item {
  flex: 1;
  text-align: center;
}

.stats-icon {
  font-size: calc(min(3vw, 20px));
  margin-bottom: 2px;
}

.stats-num {
  font-size: calc(min(3vw, 22px));
  font-weight: 300;
  color: white;
  font-variant-numeric: tabular-nums;
}

.stats-label {
  font-size: calc(min(1.6vw, 10px));
  color: rgba(255, 255, 255, 0.3);
  margin-top: 2px;
  font-weight: 300;
}

.stats-bar {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin-top: 6px;
  overflow: hidden;
}

.stats-bar-fill {
  height: 100%;
  background: rgba(255, 255, 255, 0.6);
  transition: width 0.5s;
}

/* === Articles / Workouts === */
.articles-narrow, .workouts-narrow {
  padding: 12%;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.articles-title-narrow, .workouts-title-narrow {
  font-size: calc(min(2vw, 13px));
  font-weight: 300;
  color: rgba(255, 255, 255, 0.25);
  margin-bottom: 4px;
  letter-spacing: 0.02em;
}

.articles-preview, .workouts-preview {
  font-size: calc(min(1.8vw, 12px));
  color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  font-weight: 300;
}

.articles-empty, .workouts-empty {
  font-size: calc(min(1.8vw, 12px));
  color: rgba(255, 255, 255, 0.12);
}

.articles-wide, .workouts-wide {
  width: 100%;
  height: 100%;
  padding: 8% 10%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.articles-header, .workouts-header {
  font-size: calc(min(2vw, 12px));
  font-weight: 400;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 6px;
  letter-spacing: 0.05em;
}

.articles-list, .workouts-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.articles-item, .workouts-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 3px 0;
}

.articles-item {
  cursor: pointer;
}

.articles-item-title {
  font-size: calc(min(1.7vw, 11px));
  color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 6px;
  font-weight: 300;
}

.workouts-item-name {
  font-size: calc(min(1.7vw, 11px));
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
}

.workouts-item-meta {
  font-size: calc(min(1.5vw, 10px));
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
}

.tag-sm {
  font-size: calc(min(1.4vw, 9px));
  padding: 1px 5px;
  border-radius: 10px;
  flex-shrink: 0;
  border: 1px solid;
  font-weight: 300;
}

.tag-diary {
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  border-color: rgba(255, 255, 255, 0.15);
}

.tag-blog {
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  border-color: rgba(255, 255, 255, 0.15);
}

/* === Progress === */
.widget-progress {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-content {
  text-align: center;
  width: 100%;
  padding: 8%;
  box-sizing: border-box;
}

.progress-grid {
  display: grid;
  gap: 3px;
  justify-items: center;
  margin-bottom: 8px;
}

.progress-dot {
  aspect-ratio: 1;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  width: 100%;
  transition: opacity 0.3s;
}

.progress-label {
  font-size: calc(min(1.6vw, 10px));
  color: rgba(255, 255, 255, 0.25);
  margin-bottom: 2px;
  font-weight: 300;
  letter-spacing: 0.05em;
}

.progress-pct {
  font-size: calc(min(3vw, 20px));
  font-weight: 300;
  color: rgba(255, 255, 255, 0.5);
  font-variant-numeric: tabular-nums;
}
</style>
