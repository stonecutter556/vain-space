import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('vs_token') || '')
  const username = ref(localStorage.getItem('vs_username') || '')
  const role = ref(localStorage.getItem('vs_role') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'admin')

  function setAuth(newToken, newUsername, newRole) {
    token.value = newToken
    username.value = newUsername
    role.value = newRole || ''
    localStorage.setItem('vs_token', newToken)
    localStorage.setItem('vs_username', newUsername)
    if (newRole) localStorage.setItem('vs_role', newRole)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('vs_token')
    localStorage.removeItem('vs_username')
    localStorage.removeItem('vs_role')
  }

  return { token, username, role, isLoggedIn, isAdmin, setAuth, logout }
})
