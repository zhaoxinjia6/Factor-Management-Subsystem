import { mount, flushPromises } from '@vue/test-utils'
import * as api from '../../src/api'
import DerivativeFactorPage from '@pages/DerivativeFactorPage.vue'

describe('DerivativeFactorPage', () => {
  let wrapper

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(api, 'getDerivativeFactors').mockResolvedValue([
      { id: 1, name: '因子1', code: 'f1', formula: 'a+b' }
    ])
    vi.spyOn(api, 'getBaseFactors').mockResolvedValue({ items: [] })
    vi.spyOn(api, 'createDerivativeFactor').mockResolvedValue({ success: true })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('页面加载时应该获取因子列表', async () => {
    wrapper = mount(DerivativeFactorPage)
    await flushPromises()
    expect(api.getDerivativeFactors).toHaveBeenCalled()
  })

  it('应该正确显示因子数据', async () => {
    wrapper = mount(DerivativeFactorPage)
    await flushPromises()
    expect(wrapper.vm.factors).toHaveLength(1)
  })

  it('点击创建按钮应该打开弹窗', async () => {
    wrapper = mount(DerivativeFactorPage)
    await flushPromises()
    wrapper.vm.openCreate()
    expect(wrapper.vm.dialog.visible).toBe(true)
  })

  it('提交创建应该调用 API', async () => {
    wrapper = mount(DerivativeFactorPage)
    await flushPromises()
    wrapper.vm.openCreate()
    wrapper.vm.dialog.form = { name: '新因子', code: 'new' }
    wrapper.vm.dialog.selectedBaseIds = [1]
    wrapper.vm.dialog.weightRows = [{ id: 1, name: '因子1', weight: 100 }]
    await wrapper.vm.submitDialog()
    expect(api.createDerivativeFactor).toHaveBeenCalled()
  })
})