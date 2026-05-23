<template>
  <n-space vertical :size="16">
    <n-button quaternary @click="goBack" style="align-self: flex-start;">
      <template #icon>
        <n-icon><arrow-back-outline /></n-icon>
      </template>
      返回
    </n-button>

    <n-card v-if="article" :title="article.title">
      <template #header-extra>
        <n-tag :type="article.articleType === 'diary' ? 'info' : 'success'" size="small">
          {{ article.articleType === 'diary' ? '日记' : '博客' }}
        </n-tag>
      </template>
      <n-text depth="3" style="font-size: 13px; display: block; margin-bottom: 16px;">
        发布于 {{ formatTime(article.createdAt) }}
      </n-text>
      <n-divider />
      <div style="white-space: pre-wrap; line-height: 1.8;">{{ article.content }}</div>
    </n-card>
    <n-spin v-else size="large" />
  </n-space>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { articleApi } from '../api'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const article = ref(null)

async function fetchArticle() {
  try {
    const id = route.params.id
    const res = await articleApi.getById(id)
    article.value = res.data
  } catch (e) {
    message.error('加载文章失败')
  }
}

function goBack() {
  router.push({ name: 'Articles' })
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchArticle)
</script>
