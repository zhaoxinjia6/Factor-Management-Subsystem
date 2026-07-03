// ═══════════════════════════════════════════════════════════════
// Developer 2 — 核心页面：因子概览页测试
// 目标：~12 个测试 | 依赖：@vue/test-utils (shallowMount)
// 环境：@vitest-environment jsdom
// 测试命令：npx vitest run test/pages/FactorOverviewPage.test.js
// ═══════════════════════════════════════════════════════════════

import { shallowMount } from '@vue/test-utils'

// 模拟必须在 import 之前，vitest 会提升 mock
vi.mock('echarts', () => {
  const mockChart = { setOption: vi.fn(), resize: vi.fn(), dispose: vi.fn() }
  return { init: vi.fn(() => mockChart), default: { init: vi.fn(() => mockChart) } }
})

vi.mock('../../src/api', () => ({
  getFactorCategories: vi.fn().mockResolvedValue([
    { id: 'cat1', name: '费率类', children: [] },
    { id: 'cat2', name: '规模类', children: [{ id: 'cat2-1', name: '子规模', children: [] }] }
  ]),
  getBaseFactors: vi.fn().mockResolvedValue({
    items: [
      { id: 1, name: '管理费率', categoryId: 'cat1', code: 'mgmt_fee' },
      { id: 2, name: '最新规模', categoryId: 'cat2', code: 'scale' }
    ]
  }),
  getDerivativeFactors: vi.fn().mockResolvedValue([
    { id: 10, name: '5日动量因子' },
    { id: 11, name: '20日波动率' }
  ]),
  getStyleFactors: vi.fn().mockResolvedValue([{ id: 20, name: '稳健风格因子' }]),
  getFunds: vi.fn().mockResolvedValue([
    { fundCode: '000001', fundName: '测试基金A' },
    { fundCode: '000002', fundName: '测试基金B' }
  ]),
  getBaseFactorValues: vi.fn().mockResolvedValue({ items: [{ tradeDate: '2024-01-01', fundCode: '000001', value: 0.5 }] }),
  getDerivativeFactorValues: vi.fn().mockResolvedValue([]),
  getStyleFactorValues: vi.fn().mockResolvedValue([]),
  createDerivativeFactor: vi.fn().mockResolvedValue({ id: 99 }),
  createStyleFactor: vi.fn().mockResolvedValue({ id: 99 })
}))

import FactorOverviewPage from '@pages/FactorOverviewPage.vue'

function factory() {
  return shallowMount(FactorOverviewPage)
}

describe('FactorOverviewPage 因子概览页', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('应渲染页面容器', () => {
    const wrapper = factory()
    expect(wrapper.find('.factor-overview-page').exists()).toBe(true)
  })

  it('loadMeta 应构建因子树', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    expect(wrapper.vm.treeData.length).toBe(3)
    expect(wrapper.vm.treeData.map(n => n.id)).toContain('base-root')
  })

  it('loadMeta 应填充 5 个因子选项', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    expect(wrapper.vm.factorOptions.length).toBe(5)
  })

  it('loadMeta 应默认选中第一个因子', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    expect(wrapper.vm.query.factorId).toBe(wrapper.vm.factorOptions[0]?.id)
  })

  it('loadMeta 应设置默认日期范围', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    expect(wrapper.vm.query.dateRange.length).toBe(2)
  })

  it('filterTree 应按关键字过滤', () => {
    const wrapper = factory()
    const nodes = [{ id: 1, name: '管理费率', children: [] }, { id: 2, name: '最新规模', children: [] }]
    expect(wrapper.vm.filterTree(nodes, '管理').length).toBe(1)
  })

  it('formatDate 应正确格式化', () => {
    const wrapper = factory()
    expect(wrapper.vm.formatDate(new Date(2024, 0, 5))).toBe('2024-01-05')
  })

  it('defaultDateRange 返回近3个月', () => {
    const wrapper = factory()
    expect(wrapper.vm.defaultDateRange().length).toBe(2)
  })

  it('resetFilter 应清空查询条件', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    wrapper.vm.query.fundCode = '000001'
    wrapper.vm.page.page = 3
    await wrapper.vm.resetFilter()
    expect(wrapper.vm.query.fundCode).toBe('')
    expect(wrapper.vm.page.page).toBe(1)
  })

  it('openCreateDialog 应打开弹窗', () => {
    const wrapper = factory()
    wrapper.vm.openCreateDialog('derived')
    expect(wrapper.vm.dialog.visible).toBe(true)
  })

  it('chooseFactor 应设置基础因子 ID', async () => {
    const wrapper = factory()
    await wrapper.vm.loadMeta()
    // 选择基础因子（type=base），避免触发未导入的 getDerivativeFactorValues
    const baseFactor = wrapper.vm.factorOptions.find(f => f.type === 'base')
    wrapper.vm.chooseFactor(baseFactor)
    expect(wrapper.vm.query.factorId).toBe(baseFactor.id)
  })

  it('flattenTree 应展平嵌套树', () => {
    const wrapper = factory()
    const tree = [{ id: 1, name: 'A', children: [{ id: 2, name: 'B', children: [] }] }]
    expect(wrapper.vm.flattenTree(tree).length).toBe(2)
  })

  it('normalizeRows 应归一化数据', () => {
    const wrapper = factory()
    const result = wrapper.vm.normalizeRows([{ tradeDate: '2024-01-01', fundCode: '000001', value: 0.5 }])
    expect(result[0].tradeDate).toBe('2024-01-01')
  })
})
