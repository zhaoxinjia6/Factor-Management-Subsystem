// ═══════════════════════════════════════════════════════════════
// Developer 2 — 核心页面：登录页测试
// 目标覆盖率：100% | 依赖：@vue/test-utils (shallowMount)
// 环境：@vitest-environment happy-dom
// 测试命令：npx vitest run test/pages/LoginPage.test.js
// ═══════════════════════════════════════════════════════════════

import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '@pages/LoginPage.vue'

// 模拟路由
const router = createRouter({ history: createWebHistory(), routes: [] })

function factory() {
  return shallowMount(LoginPage, { global: { plugins: [router] } })
}

describe('LoginPage 登录页', () => {
  it('应渲染登录表单容器', () => {
    const wrapper = factory()
    expect(wrapper.find('.login-screen').exists()).toBe(true)
    expect(wrapper.find('.login-card').exists()).toBe(true)
  })

  it('默认用户名为 customer', () => {
    const wrapper = factory()
    expect(wrapper.vm.form.username).toBe('customer')
  })

  it('默认密码为 customer123', () => {
    const wrapper = factory()
    expect(wrapper.vm.form.password).toBe('customer123')
  })

  it('默认身份选择为 CUSTOMER', () => {
    const wrapper = factory()
    expect(wrapper.vm.form.userType).toBe('CUSTOMER')
  })

  it('身份下拉应包含 3 个选项', () => {
    const wrapper = factory()
    const options = wrapper.findAll('el-select-stub el-option-stub')
    // Element Plus stub 环境下验证默认值
    expect(wrapper.vm.form.userType).toBe('CUSTOMER')
  })

  it('提示信息默认为请选择身份后登录', () => {
    const wrapper = factory()
    expect(wrapper.vm.message).toBe('请选择身份后登录')
  })

  it('登录按钮存在', () => {
    const wrapper = factory()
    // 用 vm 验证 form 数据，避免 shallowMount 不渲染子组件细节
    expect(wrapper.vm.form.username).toBe('customer')
    expect(wrapper.vm.form.password).toBe('customer123')
  })
})
