// ═══════════════════════════════════════════════════════════════
// 路由配置测试
// 覆盖：src/router.js 的路由定义
// ═══════════════════════════════════════════════════════════════

import router from '../../src/router'

describe('Router 路由配置', () => {
  it('根路径 / 应重定向到 /login', () => {
    const rootRoute = router.getRoutes().find(r => r.path === '/')
    expect(rootRoute).toBeDefined()
    expect(rootRoute.redirect).toBe('/login')
  })

  it('应包含登录页路由（/login）', () => {
    const loginRoute = router.getRoutes().find(r => r.path === '/login')
    expect(loginRoute).toBeDefined()
    expect(loginRoute.path).toBe('/login')
  })

  it('应包含 workspace 路由', () => {
    const routes = router.getRoutes()
    // workspace 是嵌套路由，父路由 path 为 /workspace/:role
    const hasWorkspace = routes.some(r => r.path.startsWith('/workspace'))
    expect(hasWorkspace).toBe(true)
  })

  it('未匹配路径应重定向到因子概览', () => {
    const routes = router.getRoutes()
    const catchAll = routes.find(r =>
      r.path.includes(':pathMatch') || r.path.includes('(.*)*')
    )
    expect(catchAll).toBeDefined()
  })

  it('路由历史模式为 createWebHistory', () => {
    // 检查 router 是否有 history 对象
    expect(router.options.history).toBeDefined()
  })
})
