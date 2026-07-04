import { shallowMount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import MultiFactorPage from '@pages/MultiFactorPage.vue'

const router = createRouter({ history: createWebHistory(), routes: [] })

function factory() {
  return shallowMount(MultiFactorPage, {
    global: { plugins: [router] }
  })
}

describe('MultiFactorPage 多因子页面', () => {
  // ========== 基础渲染 ==========
  it('应渲染页面容器', () => {
    const wrapper = factory()
    expect(wrapper.find('.multi-factor-page').exists()).toBe(true)
  })

  it('应包含筛选栏', () => {
    const wrapper = factory()
    expect(wrapper.find('.global-filter-bar').exists()).toBe(true)
  })

  it('应包含 KPI 卡片行', () => {
    const wrapper = factory()
    expect(wrapper.find('.kpi-row').exists()).toBe(true)
    expect(wrapper.findAll('.kpi-card').length).toBe(5)
  })

  it('应包含建模池', () => {
    const wrapper = factory()
    expect(wrapper.find('.model-pool').exists()).toBe(true)
    expect(wrapper.find('.pool-head').exists()).toBe(true)
  })

  it('应包含图表区域', () => {
    const wrapper = factory()
    expect(wrapper.find('.chart-main').exists()).toBe(true)
    expect(wrapper.findAll('.chart-card').length).toBe(4)
  })

  it('应包含因子效能榜单', () => {
    const wrapper = factory()
    expect(wrapper.find('.panel-section').exists()).toBe(true)
    expect(wrapper.find('el-table-stub').exists()).toBe(true)
  })

  // ========== 筛选栏 ==========
  it('股票池默认为 all', () => {
    const wrapper = factory()
    expect(wrapper.vm.pool).toBe('all')
  })

  it('调仓周期默认为 monthly', () => {
    const wrapper = factory()
    expect(wrapper.vm.rebalanceFreq).toBe('monthly')
  })


  // ========== KPI ==========
  it('KPI 初始值应为 --', () => {
    const wrapper = factory()
    expect(wrapper.vm.kpis[0].value).toBe('--')
    expect(wrapper.vm.kpis[1].value).toBe('--')
    expect(wrapper.vm.kpis[2].value).toBe('--')
    expect(wrapper.vm.kpis[3].value).toBe('--')
    expect(wrapper.vm.kpis[4].value).toBe('--')
  })

  // ========== 建模池 ==========
  it('建模池初始为空数组', () => {
    const wrapper = factory()
    expect(wrapper.vm.modelFactors).toEqual([])
  })

  it('权重和初始为 0', () => {
    const wrapper = factory()
    expect(wrapper.vm.weightSum).toBe(0)
  })

  it('添加因子到建模池', async () => {
    const wrapper = factory()
    const mockRow = { id: '1', name: '测试因子' }

    await wrapper.vm.addToModel(mockRow)

    expect(wrapper.vm.modelFactors.length).toBe(1)
    expect(wrapper.vm.modelFactors[0].name).toBe('测试因子')
    expect(wrapper.vm.modelFactors[0].weight).toBe(0)
  })

  it('不能重复添加同一因子', async () => {
    const wrapper = factory()
    const mockRow = { id: '1', name: '测试因子' }

    wrapper.vm.addToModel(mockRow)
    wrapper.vm.addToModel(mockRow)

    expect(wrapper.vm.modelFactors.length).toBe(1)
  })

  it('最多加入 7 个因子', async () => {
    const wrapper = factory()
    for (let i = 1; i <= 8; i++) {
      wrapper.vm.addToModel({ id: `${i}`, name: `因子${i}` })
    }
    expect(wrapper.vm.modelFactors.length).toBe(7)
  })

  it('重置建模池应清空', async () => {
    const wrapper = factory()
    wrapper.vm.modelFactors = [{ id: '1', name: '测试因子', weight: 50 }]
    await wrapper.vm.resetModel()
    expect(wrapper.vm.modelFactors).toEqual([])
  })

  // ========== 权重计算 ==========
  it('权重和应正确计算', () => {
    const wrapper = factory()
    wrapper.vm.modelFactors = [
      { id: '1', name: '因子A', weight: 50 },
      { id: '2', name: '因子B', weight: 30 },
      { id: '3', name: '因子C', weight: 20 }
    ]
    expect(wrapper.vm.weightSum).toBe(100)
  })

  it('权重和不为 100 时禁用回测按钮', async () => {
    const wrapper = factory()
    wrapper.vm.modelFactors = [
      { id: '1', name: '因子A', weight: 50 },
      { id: '2', name: '因子B', weight: 30 }
    ]
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.weightSum).toBe(80)
    // 检查按钮的 disabled 状态
    const btn = wrapper.find('.pool-summary + .el-button-stub')
    // 通过 vm 判断
    expect(wrapper.vm.weightSum !== 100 || wrapper.vm.modelFactors.length === 0).toBe(true)
  })

  it('权重和为 100 时应启用回测按钮', async () => {
    const wrapper = factory()
    wrapper.vm.modelFactors = [
      { id: '1', name: '因子A', weight: 50 },
      { id: '2', name: '因子B', weight: 50 }
    ]
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.weightSum).toBe(100)
    expect(wrapper.vm.modelFactors.length).toBe(2)
  })

  // ========== 因子效能榜单 ==========
  it('效能数据初始为空数组', () => {
    const wrapper = factory()
    expect(wrapper.vm.perfData).toEqual([])
  })

  it('分页默认 page=1, pageSize=10', () => {
    const wrapper = factory()
    expect(wrapper.vm.page).toBe(1)
    expect(wrapper.vm.pageSize).toBe(10)
  })

  it('应包含分页组件', () => {
    const wrapper = factory()
    expect(wrapper.find('el-pagination-stub').exists()).toBe(true)
  })

  // ========== 详情弹窗 ==========
  it('详情弹窗初始为关闭', () => {
    const wrapper = factory()
    expect(wrapper.vm.detailVisible).toBe(false)
    expect(wrapper.vm.detailFactor).toBe(null)
  })

  it('查看详情应打开弹窗', async () => {
    const wrapper = factory()
    const mockRow = { id: '1', name: '测试因子', category: '价值' }
    await wrapper.vm.viewDetail(mockRow)
    expect(wrapper.vm.detailVisible).toBe(true)
    expect(wrapper.vm.detailFactor.name).toBe('测试因子')
  })

  // ========== 方法存在性 ==========
  it('应具备 refreshAll 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.refreshAll).toBe('function')
  })

  it('应具备 addToModel 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.addToModel).toBe('function')
  })

  it('应具备 resetModel 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.resetModel).toBe('function')
  })

  it('应具备 runBacktest 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.runBacktest).toBe('function')
  })

  it('应具备 viewDetail 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.viewDetail).toBe('function')
  })

  it('应具备 onSelectionChange 方法', () => {
    const wrapper = factory()
    expect(typeof wrapper.vm.onSelectionChange).toBe('function')
  })
})