<template>
  <n-space vertical :size="16">
    <n-space align="center" justify="space-between">
      <n-h2 prefix="bar" style="margin: 0;">健身记录</n-h2>
      <n-button type="primary" @click="showForm = true">新增记录</n-button>
    </n-space>

    <n-card v-if="showForm">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-grid :cols="4" :x-gap="16">
          <n-gi>
            <n-form-item label="日期" path="workoutDate">
              <n-date-picker v-model:value="form.workoutDate" type="date" placeholder="选择日期" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="动作" path="exercise">
              <n-input v-model:value="form.exercise" placeholder="如：卧推" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="组数" path="sets">
              <n-input-number v-model:value="form.sets" :min="1" placeholder="组数" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="次数" path="reps">
              <n-input-number v-model:value="form.reps" :min="1" placeholder="次数" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="重量(kg)" path="weight">
              <n-input-number v-model:value="form.weight" :min="0" placeholder="重量" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="备注" path="notes">
              <n-input v-model:value="form.notes" placeholder="备注（可选）" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-space justify="end">
          <n-button @click="showForm = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">保存</n-button>
        </n-space>
      </n-form>
    </n-card>

    <n-data-table :columns="columns" :data="workouts" :bordered="true" :loading="loading" />
  </n-space>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { workoutApi } from '../api'

const message = useMessage()

const workouts = ref([])
const loading = ref(false)
const showForm = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  workoutDate: Date.now(),
  exercise: '',
  sets: 4,
  reps: 12,
  weight: 0,
  notes: ''
})

const rules = {
  workoutDate: { type: 'number', required: true, message: '请选择日期', trigger: ['change', 'blur'] },
  exercise: { required: true, message: '请输入动作名称', trigger: 'blur' }
}

const columns = [
  {
    title: '日期',
    key: 'workoutDate',
    width: 110,
    render(row) {
      return row.workoutDate ? new Date(row.workoutDate).toLocaleDateString('zh-CN') : '-'
    }
  },
  { title: '动作', key: 'exercise', width: 120 },
  { title: '组数', key: 'sets', width: 70 },
  { title: '次数', key: 'reps', width: 70 },
  { title: '重量(kg)', key: 'weight', width: 90 },
  { title: '备注', key: 'notes', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render(row) {
      return h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
    }
  }
]

async function fetchWorkouts() {
  loading.value = true
  try {
    const res = await workoutApi.getAll()
    workouts.value = (res.data || []).slice().reverse()
  } catch (e) {
    message.error('加载健身记录失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.exercise || !form.value.sets || !form.value.reps) {
    message.warning('请填写完整信息（动作、组数、次数）')
    return
  }
  submitting.value = true
  try {
    const payload = {
      workoutDate: new Date(form.value.workoutDate).toISOString().split('T')[0],
      exercise: form.value.exercise,
      sets: form.value.sets,
      reps: form.value.reps,
      weight: form.value.weight,
      notes: form.value.notes || ''
    }
    await workoutApi.create(payload)
    message.success('记录已保存')
    showForm.value = false
    form.value = { workoutDate: Date.now(), exercise: '', sets: 4, reps: 12, weight: 0, notes: '' }
    await fetchWorkouts()
  } catch (e) {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await workoutApi.delete(id)
    message.success('记录已删除')
    await fetchWorkouts()
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(fetchWorkouts)
</script>
