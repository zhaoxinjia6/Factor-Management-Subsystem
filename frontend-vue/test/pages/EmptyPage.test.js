import { shallowMount } from '@vue/test-utils'
import EmptyPage from '@pages/EmptyPage.vue'

describe('EmptyPage 空页面', () => {
  it('应渲染页面容器', () => {
    const wrapper = shallowMount(EmptyPage)
    expect(wrapper.find('.panel').exists()).toBe(true)
    expect(wrapper.find('.panel-head').exists()).toBe(true)
  })

  it('应显示标题', () => {
    const wrapper = shallowMount(EmptyPage, {
      props: { title: '测试模块' }
    })
    expect(wrapper.find('h2').text()).toBe('测试模块')
  })

  it('应显示"正在建设中"', () => {
    const wrapper = shallowMount(EmptyPage)
    expect(wrapper.html()).toContain('正在建设中')
  })

  it('应显示提示信息', () => {
    const wrapper = shallowMount(EmptyPage)
    expect(wrapper.find('.shortcut').exists()).toBe(true)
    expect(wrapper.html()).toContain('该模块已接入路由')
  })

  it('title prop 默认为 undefined', () => {
    const wrapper = shallowMount(EmptyPage)
    expect(wrapper.props('title')).toBeUndefined()
  })

  it('传入 title 时应正确渲染', () => {
    const wrapper = shallowMount(EmptyPage, {
      props: { title: '我的模块' }
    })
    expect(wrapper.find('h2').text()).toBe('我的模块')
  })
})