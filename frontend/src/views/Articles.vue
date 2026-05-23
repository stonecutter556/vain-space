<template>
  <n-space vertical :size="16">
    <n-space align="center" justify="space-between">
      <n-h2 prefix="bar" style="margin: 0;">文章与日记</n-h2>
      <n-button type="primary" @click="openAdd">新增文章</n-button>
    </n-space>

    <n-tabs :value="activeTab" @update:value="handleTabChange">
      <n-tab name="all" tab="全部" />
      <n-tab name="diary" tab="日记" />
      <n-tab name="blog" tab="博客" />
    </n-tabs>

    <n-space vertical :size="12">
      <n-card v-for="article in articles" :key="article.id" size="small" hoverable @click="goDetail(article.id)">
        <n-space align="center" justify="space-between">
          <n-text strong>{{ article.title }}</n-text>
          <n-tag :type="article.articleType === 'diary' ? 'info' : 'success'" size="small">
            {{ article.articleType === 'diary' ? '日记' : '博客' }}
          </n-tag>
        </n-space>
        <n-text depth="3" style="font-size: 13px;">{{ formatTime(article.createdAt) }}</n-text>
      </n-card>
    </n-space>
    <n-empty v-if="!articles.length" description="暂无文章" />

    <n-modal v-model:show="showModal" title="新增文章" preset="card" style="width: 600px;">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="请输入文章标题" />
        </n-form-item>
        <n-form-item label="类型" path="articleType">
          <n-select v-model:value="form.articleType" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="内容" path="content">
          <n-input v-model:value="form.content" type="textarea" rows="8" placeholder="请输入文章内容" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { articleApi } from '../api'

const router = useRouter()
const message = useMessage()

const articles = ref([])
const activeTab = ref('all')
const showModal = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const typeOptions = [
  { label: '日记', value: 'diary' },
  { label: '博客', value: 'blog' }
]

const form = ref({
  title: '',
  articleType: 'diary',
  content: '',
  isPublic: false
})

const rules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  articleType: { required: true, message: '请选择类型', trigger: 'change' },
  content: { required: true, message: '请输入内容', trigger: 'blur' }
}

async function fetchArticles() {
  try {
    let res
    if (activeTab.value === 'all') {
      res = await articleApi.getAll()
    } else {
      res = await articleApi.getByType(activeTab.value)
    }
    articles.value = (res.data || []).slice().reverse()
  } catch (e) {
    message.error('加载文章失败')
  }
}

function handleTabChange(val) {
  activeTab.value = val
  fetchArticles()
}

function openAdd() {
  form.value = { title: '', articleType: 'diary', content: '', isPublic: false }
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
    await articleApi.create(form.value)
    message.success('文章已创建')
    showModal.value = false
    await fetchArticles()
  } catch (e) {
    message.error('创建失败')
  } finally {
    submitting.value = false
  }
}

function goDetail(id) {
  router.push({ name: 'ArticleDetail', params: { id } })
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchArticles)
</script>
