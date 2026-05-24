import axios from 'axios'

const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: BACKEND_URL + '/api',
  timeout: 30000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('vs_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('vs_token')
      localStorage.removeItem('vs_username')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => axios.post(BACKEND_URL + '/api/auth/login', data),
  register: (data) => axios.post(BACKEND_URL + '/api/auth/register', data)
}

export const goalApi = {
  getAll: (params) => api.get('/goals', { params }),
  getPublic: () => api.get('/goals/public'),
  create: (data) => api.post('/goals', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  delete: (id) => api.delete(`/goals/${id}`)
}

export const articleApi = {
  getAll: () => api.get('/articles'),
  getPublic: () => api.get('/articles/public'),
  getByType: (type) => api.get(`/articles/type/${type}`),
  getById: (id) => api.get(`/articles/${id}`),
  create: (data) => api.post('/articles', data),
  update: (id, data) => api.put(`/articles/${id}`, data),
  delete: (id) => api.delete(`/articles/${id}`)
}

export const dailyLogApi = {
  getAll: () => api.get('/daily-logs'),
  getPublic: () => api.get('/daily-logs/public'),
  getToday: () => api.get('/daily-logs/today'),
  create: (data) => api.post('/daily-logs', data),
  update: (id, data) => api.put(`/daily-logs/${id}`, data)
}

export const workoutApi = {
  getAll: () => api.get('/workout-logs'),
  getPublic: () => api.get('/workout-logs/public'),
  create: (data) => api.post('/workout-logs', data),
  update: (id, data) => api.put(`/workout-logs/${id}`, data),
  delete: (id) => api.delete(`/workout-logs/${id}`)
}

export const pomodoroApi = {
  getAll: () => api.get('/pomodoro'),
  getPublic: () => api.get('/pomodoro/public'),
  getStats: (date) => api.get('/pomodoro/stats', { params: { date } }),
  create: (data) => api.post('/pomodoro', data),
  update: (id, data) => api.put(`/pomodoro/${id}`, data)
}

export const bookmarkApi = {
  getAll: () => api.get('/bookmarks'),
  getPublic: () => api.get('/bookmarks/public'),
  create: (data) => api.post('/bookmarks', data),
  update: (id, data) => api.put(`/bookmarks/${id}`, data),
  delete: (id) => api.delete(`/bookmarks/${id}`)
}
