<template>
  <div class="goals-page">
    <div class="goals-card">
      <div class="goals-card-header">
        <span class="goals-card-title">目标管理</span>
      </div>

      <n-tabs type="line" animated class="goals-tabs">
        <n-tab-pane name="today" tab="今日计划">
          <div class="task-list" v-if="todayTasks.length > 0">
            <div class="task-item" v-for="task in todayTasks" :key="task.id">
              <n-checkbox :checked="task.done" @update:checked="val => toggleDone(task, val)" />
              <span class="task-title" :class="{ 'task-done': task.done }">{{ task.title }}</span>
              <span class="task-time">{{ formatTime(task.createdAt) }}</span>
              <button class="task-delete-btn" @click="handleDelete(task.id)">✕</button>
            </div>
          </div>
          <div v-else class="goals-empty">今天还没有任务</div>
          <div class="add-task-row">
            <n-input v-model:value="newTaskTitle" placeholder="输入新任务，按 Enter 添加" class="add-task-input" @keyup.enter="addTask" />
            <n-button @click="addTask" :disabled="!newTaskTitle.trim()" class="add-task-btn">添加</n-button>
          </div>
        </n-tab-pane>

        <n-tab-pane name="longterm" tab="长期目标">
          <div class="goals-list" v-if="longTermGoals.length > 0">
            <div class="goal-card-item" v-for="goal in longTermGoals" :key="goal.id">
              <div class="goal-item-header">
                <span class="goal-item-title">{{ goal.title }}</span>
                <div class="goal-item-actions">
                  <button class="goal-action-btn" @click="openEdit(goal)">编辑</button>
                  <button class="goal-action-btn goal-action-danger" @click="handleDelete(goal.id)">删除</button>
                </div>
              </div>
              <div class="goal-item-desc" v-if="goal.description">{{ goal.description }}</div>
              <div class="goal-item-meta">
                <span>截止 {{ goal.targetDate ? new Date(goal.targetDate).toLocaleDateString('zh-CN') : '-' }}</span>
                <span class="goal-status-tag" :class="'goal-status-' + (goal.status === '已完成' ? 'done' : goal.status === '进行中' ? 'active' : 'abandoned')">{{ goal.status }}</span>
              </div>
              <div class="goal-item-hours">
                <span>预计 {{ goal.estimatedHours ?? '-' }}h</span>
                <span class="hours-sep">|</span>
                <span>已完成 {{ (goal.completedHours ?? 0).toFixed(1) }}h</span>
                <button class="goal-hour-plus" @click="addHalfHour(goal)">+0.5h</button>
              </div>
            </div>
          </div>
          <div v-else class="goals-empty">暂无长期目标</div>
          <button class="add-goal-btn" @click="openAdd">+ 新增目标</button>
        </n-tab-pane>
      </n-tabs>

      <n-modal v-model:show="showModal" preset="card" style="width: 540px;" :mask-closable="false">
        <template #header>{{ isEdit ? '编辑目标' : '新增目标' }}</template>
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <n-form-item label="标题" path="title">
            <n-input v-model:value="form.title" placeholder="请输入目标标题" />
          </n-form-item>
          <n-form-item label="描述" path="description">
            <n-input v-model:value="form.description" type="textarea" rows="3" placeholder="请输入目标描述" />
          </n-form-item>
          <n-form-item label="截止日期" path="targetDate">
            <n-date-picker v-model:value="form.targetDate" type="date" placeholder="选择截止日期" />
          </n-form-item>
          <n-form-item label="预计时长（小时）" path="estimatedHours">
            <n-input-number v-model:value="form.estimatedHours" placeholder="0" :min="0" :step="0.5" clearable style="width:100%" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="form.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="公开">
            <n-switch v-model:value="form.isPublic" />
          </n-form-item>
        </n-form>
        <template #footer>
          <div class="modal-footer">
            <button class="goal-action-btn" @click="showModal = false">取消</button>
            <button class="goal-action-btn goal-action-primary" @click="handleSubmit" :disabled="submitting">{{ submitting ? '提交中...' : '确定' }}</button>
          </div>
        </template>
      </n-modal>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { goalApi } from '../api'

const message = useMessage()

const allTasks = ref([])
const longTermGoals = ref([])
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const newTaskTitle = ref('')

const statusOptions = [
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' },
  { label: '已放弃', value: '已放弃' }
]

const statusMap = {
  '进行中': { type: 'warning' },
  '已完成': { type: 'success' },
  '已放弃': { type: 'default' }
}

const form = ref({
  title: '',
  description: '',
  targetDate: null,
  estimatedHours: null,
  status: '进行中',
  isPublic: false
})

const rules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' }
}

function isToday(dateStr) {
  if (!dateStr) return false
  const d = new Date(dateStr)
  const now = new Date()
  return d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

const todayTasks = computed(() =>
  allTasks.value.filter(t => t.type === 'task' && isToday(t.createdAt))
)

async function fetchData() {
  loading.value = true
  try {
    const [taskRes, goalRes] = await Promise.all([
      goalApi.getAll({ type: 'task' }),
      goalApi.getAll({ type: 'goal' })
    ])
    allTasks.value = taskRes.data || []
    longTermGoals.value = goalRes.data || []
  } catch (e) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function toggleDone(task, val) {
  try {
    await goalApi.update(task.id, { ...task, done: val })
    task.done = val
  } catch (e) {
    message.error('操作失败')
  }
}

async function addTask() {
  const title = newTaskTitle.value.trim()
  if (!title) return
  try {
    await goalApi.create({ title, type: 'task' })
    message.success('任务已添加')
    newTaskTitle.value = ''
    await fetchData()
  } catch (e) {
    message.error('添加失败')
  }
}

function openAdd() {
  isEdit.value = false
  editingId.value = null
  form.value = {
    title: '',
    description: '',
    targetDate: null,
    estimatedHours: null,
    status: '进行中',
    isPublic: false
  }
  showModal.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.value = {
    title: row.title,
    description: row.description || '',
    targetDate: row.targetDate ? new Date(row.targetDate).getTime() : null,
    estimatedHours: row.estimatedHours ?? null,
    status: row.status || '进行中',
    isPublic: row.isPublic ?? false
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      targetDate: form.value.targetDate ? new Date(form.value.targetDate).toISOString().split('T')[0] : null,
      estimatedHours: form.value.estimatedHours,
      status: form.value.status,
      isPublic: form.value.isPublic,
      type: 'goal'
    }
    if (isEdit.value && editingId.value) {
      await goalApi.update(editingId.value, payload)
      message.success('目标已更新')
    } else {
      await goalApi.create(payload)
      message.success('目标已创建')
    }
    showModal.value = false
    await fetchData()
  } catch (e) {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await goalApi.delete(id)
    message.success('已删除')
    await fetchData()
  } catch (e) {
    message.error('删除失败')
  }
}

async function addHalfHour(goal) {
  const newHours = (goal.completedHours || 0) + 0.5
  try {
    await goalApi.update(goal.id, { ...goal, completedHours: newHours })
    goal.completedHours = newHours
  } catch (e) {
    message.error('操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.goals-page {
  max-width: 640px;
  margin: 0 auto;
  padding-top: 20px;
}

.goals-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 32px;
  position: relative;
}

.goals-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.goals-card-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  letter-spacing: 0.05em;
}

.goals-tabs {
  color: rgba(255, 255, 255, 0.6);
}

.goals-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
  font-weight: 300;
  font-size: 14px;
  padding: 32px 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  transition: all 0.2s;
}

.task-item:hover {
  border-color: rgba(255, 255, 255, 0.15);
}

.task-title {
  flex: 1;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.task-done {
  text-decoration: line-through;
  color: rgba(255, 255, 255, 0.3);
}

.task-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
}

.task-delete-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  font-size: 14px;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
}

.task-delete-btn:hover {
  color: rgba(255, 80, 80, 0.7);
  background: rgba(255, 80, 80, 0.1);
}

.add-task-row {
  display: flex;
  gap: 8px;
}

.add-task-input {
  flex: 1;
}

.add-task-btn {
  flex-shrink: 0;
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goal-card-item {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
}

.goal-card-item:hover {
  border-color: rgba(255, 255, 255, 0.15);
}

.goal-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.goal-item-title {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 400;
}

.goal-item-actions {
  display: flex;
  gap: 6px;
}

.goal-action-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.55);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 12px;
  padding: 4px 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.goal-action-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.goal-action-danger:hover {
  border-color: rgba(255, 80, 80, 0.4);
  color: rgba(255, 80, 80, 0.7);
}

.goal-action-primary {
  border-color: rgba(255, 255, 255, 0.25);
  color: rgba(255, 255, 255, 0.7);
}

.goal-action-primary:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.12);
}

.goal-action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.goal-item-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  margin-bottom: 8px;
  line-height: 1.5;
}

.goal-item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  font-weight: 300;
  margin-bottom: 8px;
}

.goal-status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 300;
}

.goal-status-active {
  background: rgba(255, 184, 0, 0.12);
  color: rgba(255, 184, 0, 0.7);
  border: 1px solid rgba(255, 184, 0, 0.2);
}

.goal-status-done {
  background: rgba(0, 200, 83, 0.12);
  color: rgba(0, 200, 83, 0.7);
  border: 1px solid rgba(0, 200, 83, 0.2);
}

.goal-status-abandoned {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.goal-item-hours {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  font-weight: 300;
}

.hours-sep {
  color: rgba(255, 255, 255, 0.15);
}

.goal-hour-plus {
  background: rgba(0, 200, 83, 0.08);
  border: 1px solid rgba(0, 200, 83, 0.2);
  border-radius: 6px;
  color: rgba(0, 200, 83, 0.6);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 11px;
  padding: 2px 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: 4px;
}

.goal-hour-plus:hover {
  background: rgba(0, 200, 83, 0.15);
  border-color: rgba(0, 200, 83, 0.35);
  color: rgba(0, 200, 83, 0.8);
}

.add-goal-btn {
  width: 100%;
  padding: 12px;
  margin-top: 12px;
  border-radius: 10px;
  border: 1px dashed rgba(255, 255, 255, 0.15);
  background: transparent;
  color: rgba(255, 255, 255, 0.35);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.add-goal-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.03);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 768px) {
  .goals-page { padding: 0 10px; }
  .n-data-table { font-size: 12px; }
}
</style>
