// ═══════════════════════════════════════════════════════════════
// Developer 2 — 核心页面：工作台导航页测试
// 目标：~6 个测试 | 依赖：@vue/test-utils (shallowMount)
// 环境：@vitest-environment jsdom
// 测试命令：npx vitest run test/pages/WorkspacePage.test.js
// ═══════════════════════════════════════════════════════════════

import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import WorkspacePage from '@pages/WorkspacePage.vue'

const routes = [
  { path: '/workspace/:role', component: { template: '<div>workspace</div>' } },
  { path: '/login', component: { template: '<div>login</div>' } }
]
const router = createRouter({ history: createWebHistory(), routes })

const mockSession = {
  account: { displayName: '测试客户', userType: 'CUSTOMER', username: 'customer' }
}

function factory(session = mockSession) {
  if (session) localStorage.setItem('session', JSON.stringify(session))
  else localStorage.removeItem('session')
  return shallowMount(WorkspacePage, { global: { plugins: [router] }, props: { role: 'customer' } })
}

describe('WorkspacePage 工作台导航页', () => {
  beforeEach(() => { localStorage.clear() })

  it('有 session 时应渲染工作台', () => {
    const wrapper = factory()
    expect(wrapper.find('.workspace-shell').exists()).toBe(true)
  })

  it('无 session 时应重定向到 /login', () => {
    const replaceSpy = vi.spyOn(router, 'replace')
    factory(null)
    expect(replaceSpy).toHaveBeenCalledWith('/login')
  })

  it('导航栏包含 4 个导航项', () => {
    const wrapper = factory()
    expect(wrapper.vm.navItems.length).toBe(4)
    expect(wrapper.vm.navItems.map(n => n.label)).toContain('因子查询')
  })

  it('应显示当前用户名', () => {
    const wrapper = factory()
    expect(wrapper.vm.session.account.displayName).toBe('测试客户')
  })

  it('logout 应清除 session 并跳转', () => {
    const replaceSpy = vi.spyOn(router, 'replace')
    const wrapper = factory()
    wrapper.vm.logout()
    expect(localStorage.getItem('session')).toBeNull()
    expect(replaceSpy).toHaveBeenCalledWith('/login')
  })

  it('toggleTheme 应切换主题', () => {
    const wrapper = factory()
    const initial = wrapper.vm.isDark
    wrapper.vm.toggleTheme()
    expect(wrapper.vm.isDark).toBe(!initial)
    wrapper.vm.toggleTheme()
    expect(wrapper.vm.isDark).toBe(initial)
  })

  it('chatVisible 可控制聊天侧栏', () => {
    const wrapper = factory()
    expect(wrapper.vm.chatVisible).toBe(false)
    wrapper.vm.chatVisible = true
    expect(wrapper.vm.chatVisible).toBe(true)
  })
})
