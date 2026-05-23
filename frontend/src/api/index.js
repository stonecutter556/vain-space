import axios from 'axios'

const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: BACKEND_URL + '/api',
  timeout: 10000
})

export const goalApi = {
  getAll: () => api.get('/goals'),
  create: (data) => api.post('/goals', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  delete: (id) => api.delete(`/goals/${id}`)
}

export const articleApi = {
  getAll: () => api.get('/articles'),
  getByType: (type) => api.get(`/articles/type/${type}`),
  getById: (id) => api.get(`/articles/${id}`),
  create: (data) => api.post('/articles', data),
  update: (id, data) => api.put(`/articles/${id}`, data),
  delete: (id) => api.delete(`/articles/${id}`)
}

export const dailyLogApi = {
  getAll: () => api.get('/daily-logs'),
  getToday: () => api.get('/daily-logs/today'),
  create: (data) => api.post('/daily-logs', data),
  update: (id, data) => api.put(`/daily-logs/${id}`, data)
}

export const workoutApi = {
  getAll: () => api.get('/workout-logs'),
  create: (data) => api.post('/workout-logs', data),
  update: (id, data) => api.put(`/workout-logs/${id}`, data),
  delete: (id) => api.delete(`/workout-logs/${id}`)
}

export const pomodoroApi = {
  getAll: () => api.get('/pomodoro'),
  create: (data) => api.post('/pomodoro', data),
  update: (id, data) => api.put(`/pomodoro/${id}`, data)
}

export const bookmarkApi = {
  getAll: () => api.get('/bookmarks'),
  create: (data) => api.post('/bookmarks', data),
  update: (id, data) => api.put(`/bookmarks/${id}`, data),
  delete: (id) => api.delete(`/bookmarks/${id}`)
}
