<template>
  <n-space vertical :size="16">
    <n-space align="center" justify="space-between">
      <n-h2 prefix="bar" style="margin: 0;">常用链接</n-h2>
      <n-button type="primary" @click="openAdd">新增书签</n-button>
    </n-space>

    <template v-if="groupedBookmarks.length">
      <template v-for="group in groupedBookmarks" :key="group.category">
        <n-h3 style="margin-bottom: 8px;">{{ group.category }}</n-h3>
        <n-grid :cols="4" :x-gap="16" :y-gap="16">
          <n-gi v-for="item in group.items" :key="item.id">
            <n-card size="small" hoverable @click="openLink(item.url)">
              <n-text strong>{{ item.title }}</n-text>
              <n-text depth="3" style="font-size: 12px; display: block; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{ item.url }}
              </n-text>
            </n-card>
          </n-gi>
        </n-grid>
      </template>
    </template>
    <n-empty v-else description="暂无书签" />

    <n-modal v-model:show="showModal" title="新增书签" preset="card" style="width: 480px;">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="请输入链接标题" />
        </n-form-item>
        <n-form-item label="URL" path="url">
          <n-input v-model:value="form.url" placeholder="https://example.com" />
        </n-form-item>
        <n-form-item label="分类" path="category">
          <n-input v-model:value="form.category" placeholder="如：开发工具、学习资源" />
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
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { bookmarkApi } from '../api'

const message = useMessage()

const bookmarks = ref([])
const showModal = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  title: '',
  url: '',
  category: ''
})

const rules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  url: { required: true, message: '请输入URL', trigger: 'blur' },
  category: { required: true, message: '请输入分类', trigger: 'blur' }
}

const groupedBookmarks = computed(() => {
  const groups = {}
  bookmarks.value.forEach(item => {
    const cat = item.category || '未分类'
    if (!groups[cat]) groups[cat] = { category: cat, items: [] }
    groups[cat].items.push(item)
  })
  return Object.values(groups)
})

async function fetchBookmarks() {
  try {
    const res = await bookmarkApi.getAll()
    bookmarks.value = res.data || []
  } catch (e) {
    message.error('加载书签失败')
  }
}

function openAdd() {
  form.value = { title: '', url: '', category: '' }
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
    await bookmarkApi.create(form.value)
    message.success('书签已添加')
    showModal.value = false
    await fetchBookmarks()
  } catch (e) {
    message.error('添加失败')
  } finally {
    submitting.value = false
  }
}

function openLink(url) {
  if (url) window.open(url, '_blank')
}

onMounted(fetchBookmarks)
</script>
