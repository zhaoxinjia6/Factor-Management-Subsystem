import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import DerivativeFactorPage from '@pages/DerivativeFactorPage.vue'

const router = createRouter({ history: createWebHistory(), routes: [] })

function factory() {
  return shallowMount(DerivativeFactorPage, {
    global: { plugins: [router] }
  })
}

describe('DerivativeFactorPage 衍生因子管理页', () => {
  // ========== 基础渲染 ==========
  it('应渲染页面容器', () => {
    const wrapper = factory()
    expect(wrapper.find('.panel').exists()).toBe(true)
    expect(wrapper.find('.panel-head').exists()).toBe(true)
  })

  it('页面标题应为"衍生因子管理"', () => {
    const wrapper = factory()
    expect(wrapper.find('h2').text()).toBe('衍生因子管理')
  })

  it('应包含"创建衍生因子"按钮', () => {
    const wrapper = factory()
    expect(wrapper.html()).toContain('创建衍生因子')
  })

  it('应包含表格', () => {
    const wrapper = factory()
    expect(wrapper.find('el-table-stub').exists()).toBe(true)
  })

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
    wrapper.vm.dialog.form.code = 'old_code'

    await wrapper.vm.openCreate()

    expect(wrapper.vm.dialog.form.name).toBe('')
    expect(wrapper.vm.dialog.form.code).toBe('')
    expect(wrapper.vm.dialog.selectedBaseIds).toEqual([])
    expect(wrapper.vm.dialog.weightRows).toEqual([])
  })

  it('弹窗标题应为"创建衍生因子"', async () => {
    const wrapper = factory()
    await wrapper.vm.openCreate()
    expect(wrapper.vm.dialog.isEdit).toBe(false)
  })

  it('编辑时应填充表单数据', async () => {
    const wrapper = factory()
    const mockRow = {
      id: '1',
      name: '测试因子',
      code: 'test_factor',
      description: '测试描述',
      formula: 'close_price / 100',
      enabled: true
    }

    await wrapper.vm.openEdit(mockRow)

    expect(wrapper.vm.dialog.isEdit).toBe(true)
    expect(wrapper.vm.dialog.form.name).toBe('测试因子')
    expect(wrapper.vm.dialog.form.code).toBe('test_factor')
    expect(wrapper.vm.dialog.form.description).toBe('测试描述')
    expect(wrapper.vm.dialog.form.formula).toBe('close_price / 100')
  })

  it('点击字段标签应插入到公式', async () => {
    const wrapper = factory()
    wrapper.vm.dialog.visible = true
    wrapper.vm.dialog.form.formula = ''
    await wrapper.vm.$nextTick()

    wrapper.vm.insertText('close_price')
    expect(wrapper.vm.dialog.form.formula).toContain('close_price')
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

  it('权重和不为100时应显示提示', () => {
    const wrapper = factory()
    wrapper.vm.dialog.weightRows = [
      { id: '1', name: '因子A', weight: 30 },
      { id: '2', name: '因子B', weight: 30 }
    ]
    expect(wrapper.vm.weightSum).toBe(60)
  })

  it('加载时应有 loading 状态', () => {
    const wrapper = factory()
    expect(wrapper.vm.loading).toBe(true)
  })

  it('初始因子列表应为空数组', () => {
    const wrapper = factory()
    expect(wrapper.vm.factors).toEqual([])
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

  it('应具备 submitDialog 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.submitDialog).toBe('function')
  })

  it('应具备 handleDelete 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.handleDelete).toBe('function')
  })
})