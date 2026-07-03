/**
 * @vitest-environment jsdom
 */
import { mount, flushPromises } from '@vue/test-utils'
import * as api from '../../src/api'
import { ElMessageBox } from 'element-plus'
import StyleFactorPage from '@pages/StyleFactorPage.vue'

// Mock ElMessageBox
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessageBox: {
      confirm: vi.fn(() => Promise.resolve())
    },
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('StyleFactorPage', () => {
  let wrapper

  const mockStyleFactors = [
    { id: 1, name: '成长风格', description: '高增长股票', createdAt: '2026-06-01T10:00:00', enabled: true },
    { id: 2, name: '价值风格', description: '低估值股票', createdAt: '2026-06-02T14:30:00', enabled: false }
  ]

  const mockDerivativeFactors = [
    { id: 1, name: '5日动量', code: 'mom_5' },
    { id: 2, name: '10日波动率', code: 'vol_10' }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(api, 'getStyleFactors').mockResolvedValue(mockStyleFactors)
    vi.spyOn(api, 'getDerivativeFactors').mockResolvedValue(mockDerivativeFactors)
    vi.spyOn(api, 'createStyleFactor').mockResolvedValue({ success: true })
    vi.spyOn(api, 'updateStyleFactor').mockResolvedValue({ success: true })
    vi.spyOn(api, 'deleteStyleFactor').mockResolvedValue({ success: true })
    
    // 默认 ElMessageBox.confirm 返回 Promise.resolve（用户点击确认）
    ElMessageBox.confirm.mockResolvedValue('confirm')
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  // ========== 列表渲染测试 ==========
  describe('列表渲染', () => {
    it('页面加载时应该获取风格因子列表', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      expect(api.getStyleFactors).toHaveBeenCalled()
    })

    it('页面加载时应该获取衍生因子列表', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      expect(api.getDerivativeFactors).toHaveBeenCalled()
    })

    it('应该正确显示风格因子数据', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      expect(wrapper.vm.factors).toHaveLength(2)
      expect(wrapper.vm.factors[0].name).toBe('成长风格')
      expect(wrapper.vm.factors[1].name).toBe('价值风格')
    })

    it('应该正确显示衍生因子数据', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      expect(wrapper.vm.allDerived).toHaveLength(2)
      expect(wrapper.vm.allDerived[0].label).toContain('5日动量')
    })
  })

  // ========== 创建功能测试 ==========
  describe('创建功能', () => {
    it('点击创建按钮应该打开弹窗', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      expect(wrapper.vm.dialog.visible).toBe(true)
      expect(wrapper.vm.dialog.isEdit).toBe(false)
    })

    it('打开创建弹窗时应该重置表单', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      expect(wrapper.vm.dialog.form.name).toBe('')
      expect(wrapper.vm.dialog.form.description).toBe('')
      expect(wrapper.vm.dialog.selectedDerivedIds).toEqual([])
      expect(wrapper.vm.dialog.weightRows).toEqual([])
    })

    it('提交创建应该调用创建 API', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      wrapper.vm.dialog.form = {
        name: '动量风格',
        description: '基于动量的风格因子'
      }
      wrapper.vm.dialog.selectedDerivedIds = [1, 2]
      wrapper.vm.dialog.weightRows = [
        { id: 1, name: '5日动量', weight: 50 },
        { id: 2, name: '10日波动率', weight: 50 }
      ]
      await wrapper.vm.submitDialog()
      expect(api.createStyleFactor).toHaveBeenCalledWith({
        name: '动量风格',
        description: '基于动量的风格因子',
        items: [
          { derivativeFactorId: 1, weight: 50 },
          { derivativeFactorId: 2, weight: 50 }
        ]
      })
    })

    it('创建成功后应该刷新列表并关闭弹窗', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      wrapper.vm.dialog.form = { name: '动量风格' }
      wrapper.vm.dialog.selectedDerivedIds = [1]
      wrapper.vm.dialog.weightRows = [{ id: 1, name: '5日动量', weight: 100 }]
      await wrapper.vm.submitDialog()
      expect(api.getStyleFactors).toHaveBeenCalledTimes(2)
      expect(wrapper.vm.dialog.visible).toBe(false)
    })
  })

  // ========== 编辑功能测试 ==========
  describe('编辑功能', () => {
    it('点击编辑按钮应该打开编辑弹窗并回显数据', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      const row = wrapper.vm.factors[0]
      wrapper.vm.openEdit(row)
      expect(wrapper.vm.dialog.visible).toBe(true)
      expect(wrapper.vm.dialog.isEdit).toBe(true)
      expect(wrapper.vm.dialog.form.name).toBe(row.name)
      expect(wrapper.vm.dialog.form.description).toBe(row.description)
    })

    it('编辑成功后应该调用更新 API', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      const row = { id: 1, name: '成长风格' }
      wrapper.vm.openEdit(row)
      wrapper.vm.dialog.form.name = '更新后的风格'
      wrapper.vm.dialog.selectedDerivedIds = [1]
      wrapper.vm.dialog.weightRows = [{ id: 1, name: '5日动量', weight: 100 }]
      await wrapper.vm.submitDialog()
      expect(api.updateStyleFactor).toHaveBeenCalled()
    })
  })

  // ========== 权重校验测试 ==========
  describe('权重校验', () => {
    it('选中衍生因子应该自动分配权重', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      wrapper.vm.dialog.selectedDerivedIds = [1, 2]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.dialog.weightRows).toHaveLength(2)
      expect(wrapper.vm.dialog.weightRows[0].weight).toBe(50)
      expect(wrapper.vm.dialog.weightRows[1].weight).toBe(50)
    })

    it('权重和应该正确计算', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      wrapper.vm.openCreate()
      wrapper.vm.dialog.selectedDerivedIds = [1, 2]
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.weightSum).toBe(100)
    })
  })

  // ========== 删除功能测试 ==========
  describe('删除功能', () => {
    it('点击删除应该调用删除 API', async () => {
      // 确保 ElMessageBox.confirm 返回 resolved
      ElMessageBox.confirm.mockResolvedValue('confirm')
      
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      const row = { id: 1, name: '成长风格' }
      
      // 直接调用 handleDelete
      await wrapper.vm.handleDelete(row)
      
      expect(api.deleteStyleFactor).toHaveBeenCalledWith(row.id)
    })

    it('删除成功后应该刷新列表', async () => {
      ElMessageBox.confirm.mockResolvedValue('confirm')
      
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      const row = { id: 1, name: '成长风格' }
      
      const callCount = api.getStyleFactors.mock.calls.length
      await wrapper.vm.handleDelete(row)
      
      expect(api.getStyleFactors).toHaveBeenCalledTimes(callCount + 1)
    })

    it('取消删除时不应该调用删除 API', async () => {
      // 用户点击取消
      ElMessageBox.confirm.mockRejectedValue('cancel')
      
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      const row = { id: 1, name: '成长风格' }
      
      await wrapper.vm.handleDelete(row)
      
      expect(api.deleteStyleFactor).not.toHaveBeenCalled()
    })
  })

  // ========== 置顶功能测试 ==========
  describe('置顶功能', () => {
    it('点击置顶按钮应该显示提示', async () => {
      wrapper = mount(StyleFactorPage)
      await flushPromises()
      
      // 重置调用记录
      vi.clearAllMocks()
      
      // 重新设置 spy
      const getStyleFactorsSpy = vi.spyOn(api, 'getStyleFactors')
      getStyleFactorsSpy.mockClear()
      
      const row = { id: 1, name: '成长风格', enabled: true }
      
      // 调用 togglePin
      wrapper.vm.togglePin(row)
      
      // 等待可能的异步操作
      await flushPromises()
      
      // togglePin 不应该调用 getStyleFactors
      expect(getStyleFactorsSpy).not.toHaveBeenCalled()
    })
  })
})