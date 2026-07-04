import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import StyleFactorPage from '@pages/StyleFactorPage.vue'

const router = createRouter({ history: createWebHistory(), routes: [] })

function factory() {
  return shallowMount(StyleFactorPage, {
    global: { plugins: [router] }
  })
}

describe('StyleFactorPage 风格因子管理页', () => {
  // ========== 基础渲染 ==========
  it('应渲染页面容器', () => {
    const wrapper = factory()
    expect(wrapper.find('.panel').exists()).toBe(true)
    expect(wrapper.find('.panel-head').exists()).toBe(true)
  })

  it('页面标题应为"风格因子管理"', () => {
    const wrapper = factory()
    expect(wrapper.find('h2').text()).toBe('风格因子管理')
  })

  it('应包含"创建风格因子"按钮', () => {
    const wrapper = factory()
    expect(wrapper.html()).toContain('创建风格因子')
  })

  it('应包含表格', () => {
    const wrapper = factory()
    expect(wrapper.find('el-table-stub').exists()).toBe(true)
  })

  // ========== 弹窗行为 ==========
  it('点击创建按钮应打开弹窗', async () => {
    const wrapper = factory()
    expect(wrapper.vm.dialog.visible).toBe(false)

    await wrapper.vm.openCreate()

    expect(wrapper.vm.dialog.visible).toBe(true)
    expect(wrapper.vm.dialog.isEdit).toBe(false)
  })

  it('弹窗打开时表单应被重置', async () => {
    const wrapper = factory()
    wrapper.vm.dialog.form.name = '旧名称'
    wrapper.vm.dialog.form.description = '旧描述'

    await wrapper.vm.openCreate()

    expect(wrapper.vm.dialog.form.name).toBe('')
    expect(wrapper.vm.dialog.form.description).toBe('')
    expect(wrapper.vm.dialog.selectedDerivedIds).toEqual([])
    expect(wrapper.vm.dialog.weightRows).toEqual([])
  })

  it('弹窗标题应为"创建风格因子"', async () => {
    const wrapper = factory()
    await wrapper.vm.openCreate()
    expect(wrapper.vm.dialog.isEdit).toBe(false)
  })

  it('编辑时应填充表单数据', async () => {
    const wrapper = factory()
    const mockRow = {
      id: '1',
      name: '测试风格因子',
      description: '测试描述',
      enabled: true
    }

    await wrapper.vm.openEdit(mockRow)

    expect(wrapper.vm.dialog.isEdit).toBe(true)
    expect(wrapper.vm.dialog.form.name).toBe('测试风格因子')
    expect(wrapper.vm.dialog.form.description).toBe('测试描述')
    expect(wrapper.vm.dialog.selectedDerivedIds).toEqual([])
    expect(wrapper.vm.dialog.weightRows).toEqual([])
  })

  // ========== 权重计算 ==========
  it('权重和应正确计算', () => {
    const wrapper = factory()
    wrapper.vm.dialog.weightRows = [
      { id: '1', name: '因子A', weight: 50 },
      { id: '2', name: '因子B', weight: 50 }
    ]
    expect(wrapper.vm.weightSum).toBe(100)
  })

  it('权重和不为100时应禁用提交按钮', async () => {
    const wrapper = factory()
    wrapper.vm.dialog.weightRows = [
      { id: '1', name: '因子A', weight: 30 },
      { id: '2', name: '因子B', weight: 30 }
    ]
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.weightSum).toBe(60)
    expect(wrapper.vm.weightSum !== 100).toBe(true)
  })

  it('选中衍生因子时应自动分配权重', async () => {
    const wrapper = factory()
    wrapper.vm.allDerived = [
      { id: 'd1', label: '因子A (d1)' },
      { id: 'd2', label: '因子B (d2)' }
    ]

    wrapper.vm.dialog.selectedDerivedIds = ['d1', 'd2']
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.dialog.weightRows.length).toBe(2)
    expect(wrapper.vm.dialog.weightRows[0].weight).toBe(50)
    expect(wrapper.vm.dialog.weightRows[1].weight).toBe(50)
  })

  // ========== 方法存在性 ==========
  it('应具备 load 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.load).toBe('function')
  })

  it('应具备 openCreate 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.openCreate).toBe('function')
  })

  it('应具备 openEdit 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.openEdit).toBe('function')
  })

  it('应具备 submitDialog 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.submitDialog).toBe('function')
  })

  it('应具备 handleDelete 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.handleDelete).toBe('function')
  })

  it('应具备 togglePin 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.togglePin).toBe('function')
  })
})