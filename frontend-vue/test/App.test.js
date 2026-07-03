// ═══════════════════════════════════════════════════════════════
// App 根组件测试
// 覆盖：src/App.vue
// ═══════════════════════════════════════════════════════════════

import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import App from '../src/App.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/:pathMatch(.*)*', component: { template: '<div>default</div>' } }]
})

describe('App.vue 根组件', () => {
  it('应渲染根组件', () => {
    const wrapper = shallowMount(App, {
      global: { plugins: [router] },
      stubs: ['router-view']
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应包含 router-view', () => {
    const wrapper = shallowMount(App, {
      global: { plugins: [router] },
      stubs: ['router-view']
    })
    expect(wrapper.findComponent({ name: 'RouterView' }).exists()).toBe(true)
  })
})
