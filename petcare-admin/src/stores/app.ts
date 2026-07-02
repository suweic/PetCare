import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const cachedViews = ref<string[]>([])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function addCachedView(name: string) {
    if (!cachedViews.value.includes(name)) {
      cachedViews.value.push(name)
    }
  }

  return { sidebarCollapsed, cachedViews, toggleSidebar, addCachedView }
})
