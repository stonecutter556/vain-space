<template>
  <n-space vertical :size="16">
    <n-space align="center" justify="space-between">
      <n-h2 prefix="bar" style="margin: 0;">目标管理</n-h2>
      <n-button type="primary" @click="openAdd">新增目标</n-button>
    </n-space>

    <n-data-table :columns="columns" :data="goals" :bordered="true" :loading="loading" />

    <n-modal v-model:show="showModal" :title="isEdit ? '编辑目标' : '新增目标'" preset="card" style="width: 540px;">
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
        <n-form-item label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="公开">
          <n-switch v-model:value="form.isPublic" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { goalApi } from '../api'

const message = useMessage()
const goals = ref([])
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const statusOptions = [
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' },
  { label: '已放弃', value: '已放弃' }
]

const statusMap = {
  '进行中': { label: '进行中', type: 'warning' },
  '已完成': { label: '已完成', type: 'success' },
  '已放弃': { label: '已放弃', type: 'default' }
}

const form = ref({
  title: '',
  description: '',
  targetDate: null,
  status: '进行中',
  isPublic: false
})

const rules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  targetDate: { type: 'number', required: true, message: '请选择截止日期', trigger: 'change' }
}

function createColumns() {
  return [
    { title: '标题', key: 'title', width: 180 },
    { title: '描述', key: 'description', ellipsis: { tooltip: true } },
    {
      title: '截止日期',
      key: 'targetDate',
      width: 120,
      render(row) {
        return row.targetDate ? new Date(row.targetDate).toLocaleDateString('zh-CN') : '-'
      }
    },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render(row) {
        const s = statusMap[row.status] || { label: row.status, type: 'default' }
        return h('n-tag', { type: s.type, size: 'small' }, { default: () => s.label })
      }
    },
    {
      title: '操作',
      key: 'actions',
      width: 140,
      render(row) {
        return h('n-space', null, {
          default: () => [
            h('n-button', { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
            h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => '删除' })
          ]
        })
      }
    }
  ]
}

const columns = createColumns()

async function fetchGoals() {
  loading.value = true
  try {
    const res = await goalApi.getAll()
    goals.value = res.data || []
  } catch (e) {
    message.error('加载目标失败')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  editingId.value = null
  form.value = { title: '', description: '', targetDate: null, status: '进行中', isPublic: false }
  showModal.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.value = {
    title: row.title,
    description: row.description || '',
    targetDate: row.targetDate ? new Date(row.targetDate).getTime() : null,
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
      status: form.value.status,
      isPublic: form.value.isPublic
    }
    if (isEdit.value && editingId.value) {
      await goalApi.update(editingId.value, payload)
      message.success('目标已更新')
    } else {
      await goalApi.create(payload)
      message.success('目标已创建')
    }
    showModal.value = false
    await fetchGoals()
  } catch (e) {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await goalApi.delete(id)
    message.success('目标已删除')
    await fetchGoals()
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(fetchGoals)
</script>
