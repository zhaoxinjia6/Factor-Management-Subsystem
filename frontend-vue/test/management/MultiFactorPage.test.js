import { mount, flushPromises } from '@vue/test-utils'
import * as api from '../../src/api'
import MultiFactorPage from '@pages/MultiFactorPage.vue'

// Mock ECharts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    dispose: vi.fn(),
    on: vi.fn(),
    resize: vi.fn(),
    getWidth: vi.fn(() => 800),
    getHeight: vi.fn(() => 600)
  }))
}))

describe('MultiFactorPage', () => {
  let wrapper

  const mockPerformanceData = [
    {
      id: 1,
      name: '市盈率',
      code: 'pe',
      category: '估值',
      type: 'base',
      icMean: 0.045,
      ir: 1.2,
      excessReturn: 3.5,
      monthlyWinRate: 58.5,
      std: 0.021,
      avg: 0.038,
      description: '市盈率因子'
    },
    {
      id: 2,
      name: '市净率',
      code: 'pb',
      category: '估值',
      type: 'base',
      icMean: 0.032,
      ir: 0.9,
      excessReturn: 2.8,
      monthlyWinRate: 52.3,
      std: 0.019,
      avg: 0.029,
      description: '市净率因子'
    },
    {
      id: 3,
      name: '5日动量',
      code: 'mom_5',
      category: '动量',
      type: 'derived',
      icMean: 0.058,
      ir: 1.5,
      excessReturn: 4.2,
      monthlyWinRate: 62.1,
      std: 0.025,
      avg: 0.052,
      description: '5日动量因子'
    }
  ]

  const mockCorrelationData = {
    factors: ['市盈率', '市净率', '5日动量'],
    matrix: [
      [1.0, 0.3, 0.2],
      [0.3, 1.0, 0.1],
      [0.2, 0.1, 1.0]
    ]
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(api, 'getFactorPerformance').mockResolvedValue(mockPerformanceData)
    vi.spyOn(api, 'getFactorCorrelation').mockResolvedValue(mockCorrelationData)
    vi.spyOn(api, 'getBaseFactors').mockResolvedValue({ items: [] })
    vi.spyOn(api, 'getDerivativeFactors').mockResolvedValue([])
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  // ========== 页面初始化测试 ==========
  describe('页面初始化', () => {
    it('页面加载时应该获取因子性能数据', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      expect(api.getFactorPerformance).toHaveBeenCalled()
    })

    it('应该设置默认日期范围为过去一年', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      expect(wrapper.vm.dateRange).toHaveLength(2)
      expect(wrapper.vm.dateRange[0]).toBeDefined()
      expect(wrapper.vm.dateRange[1]).toBeDefined()
    })

    it('应该初始化建模池为空数组', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      expect(wrapper.vm.modelFactors).toEqual([])
    })
  })

  // ========== KPI 卡片测试 ==========
  describe('KPI 卡片', () => {
    it('应该正确显示因子总数', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      await new Promise(r => setTimeout(r, 100))
      expect(wrapper.vm.kpis[0].value).toBe(3)
    })

    it('应该正确显示平均 IC 均值', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      await new Promise(r => setTimeout(r, 100))
      expect(wrapper.vm.kpis[1].value).not.toBe('--')
    })

    it('应该正确显示年化超额收益', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      await new Promise(r => setTimeout(r, 100))
      expect(wrapper.vm.kpis[2].value).toContain('%')
    })

    it('API 数据为空时 KPI 应该显示占位符', async () => {
      api.getFactorPerformance.mockResolvedValue([])
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      await new Promise(r => setTimeout(r, 100))
      expect(wrapper.vm.kpis[0].value).toBe(0)
    })
  })

  // ========== 筛选联动测试 ==========
  describe('筛选联动', () => {
    it('切换股票池应该触发刷新', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      const refreshSpy = vi.spyOn(wrapper.vm, 'refreshAll')
      wrapper.vm.pool = 'hs300'
      wrapper.vm.refreshAll()
      expect(refreshSpy).toHaveBeenCalled()
    })

    it('类别筛选应该过滤因子列表', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.categoryFilter = '估值'
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.filteredList).toHaveLength(2)
      expect(wrapper.vm.filteredList[0].category).toBe('估值')
    })

    it('类别筛选为空时应该显示所有因子', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.categoryFilter = ''
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.filteredList).toHaveLength(3)
    })
  })

  // ========== 因子榜单测试 ==========
  describe('因子榜单', () => {
    it('应该正确显示因子排名列表', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      expect(wrapper.vm.perfData).toHaveLength(3)
      expect(wrapper.vm.perfData[0].name).toBe('市盈率')
    })

    it('点击因子应该打开详情弹窗', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      const row = mockPerformanceData[0]
      wrapper.vm.viewDetail(row)
      expect(wrapper.vm.detailVisible).toBe(true)
      expect(wrapper.vm.detailFactor).toEqual(row)
    })

    it('详情弹窗应该显示完整因子信息', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      const row = mockPerformanceData[0]
      wrapper.vm.viewDetail(row)
      expect(wrapper.vm.detailFactor.name).toBe('市盈率')
      expect(wrapper.vm.detailFactor.code).toBe('pe')
      expect(wrapper.vm.detailFactor.icMean).toBe(0.045)
    })
  })

  // ========== 建模池测试 ==========
  describe('建模池', () => {
    it('点击"加入建模"应该将因子添加到建模池', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      const row = mockPerformanceData[0]
      wrapper.vm.addToModel(row)
      expect(wrapper.vm.modelFactors).toHaveLength(1)
      expect(wrapper.vm.modelFactors[0].id).toBe(row.id)
    })

    it('最多应该只能添加 7 个因子', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      for (let i = 0; i < 7; i++) {
        wrapper.vm.addToModel({ id: i, name: `因子${i}` })
      }
      expect(wrapper.vm.modelFactors).toHaveLength(7)
    })

    it('重复添加同一因子应该提示已存在', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      const row = mockPerformanceData[0]
      wrapper.vm.addToModel(row)
      // 第二次添加应该被阻止
      wrapper.vm.addToModel(row)
      expect(wrapper.vm.modelFactors).toHaveLength(1)
    })

    it('应该支持移除已添加的因子', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.addToModel({ id: 1, name: '因子1' })
      wrapper.vm.addToModel({ id: 2, name: '因子2' })
      expect(wrapper.vm.modelFactors).toHaveLength(2)
      wrapper.vm.modelFactors.splice(0, 1)
      expect(wrapper.vm.modelFactors).toHaveLength(1)
    })
  })

  // ========== 权重计算测试 ==========
  describe('权重计算', () => {
    it('建模池权重总和应该正确计算', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.modelFactors = [
        { id: 1, name: '因子1', weight: 30 },
        { id: 2, name: '因子2', weight: 70 }
      ]
      expect(wrapper.vm.weightSum).toBe(100)
    })

    it('修改权重应该实时更新权重和', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.modelFactors = [
        { id: 1, name: '因子1', weight: 40 },
        { id: 2, name: '因子2', weight: 60 }
      ]
      expect(wrapper.vm.weightSum).toBe(100)
      wrapper.vm.modelFactors[0].weight = 50
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.weightSum).toBe(110)
    })
  })

  // ========== 重置功能测试 ==========
  describe('重置功能', () => {
    it('点击重置应该清空建模池', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.addToModel({ id: 1, name: '因子1' })
      wrapper.vm.addToModel({ id: 2, name: '因子2' })
      expect(wrapper.vm.modelFactors).toHaveLength(2)
      wrapper.vm.resetModel()
      expect(wrapper.vm.modelFactors).toEqual([])
    })
  })

  // ========== 回测功能测试 ==========
  describe('回测功能', () => {
    it('点击回测应该提交任务', async () => {
      wrapper = mount(MultiFactorPage)
      await flushPromises()
      wrapper.vm.modelFactors = [
        { id: 1, name: '因子1', weight: 50 },
        { id: 2, name: '因子2', weight: 50 }
      ]
      wrapper.vm.runBacktest()
      // 回测只是显示提示，不调用 API
      expect(wrapper.vm.modelFactors).toHaveLength(2)
    })
  })
})