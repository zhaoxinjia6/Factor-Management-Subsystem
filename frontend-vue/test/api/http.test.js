// ═══════════════════════════════════════════════════════════════
// API HTTP 实例测试
// 覆盖：src/api.js 中的 axios 实例配置
// ═══════════════════════════════════════════════════════════════

import axios from 'axios'

// 重新导入 api 模块获取 http 实例
// 由于 api.js 内部创建了 axios 实例，我们通过测试导出的函数来间接覆盖
import * as api from '../../src/api'

describe('API HTTP 实例', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login 应 POST /api/auth/login', async () => {
    const mockData = { data: { data: { token: 'test' } } }
    vi.spyOn(axios, 'post').mockResolvedValue(mockData)
    const result = await api.login({ username: 'admin', password: 'admin123' })
    expect(axios.post).toHaveBeenCalledWith('/api/auth/login', { username: 'admin', password: 'admin123' })
    expect(result).toEqual({ token: 'test' })
  })

  it('getRoles 应 GET /api/admin/roles', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { data: ['admin', 'user'] } })
    const result = await api.getRoles()
    expect(axios.get).toHaveBeenCalledWith('/api/admin/roles')
    expect(result).toEqual(['admin', 'user'])
  })

  it('getDashboard 应带 role 参数', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { data: {} } })
    await api.getDashboard('CUSTOMER')
    expect(axios.get).toHaveBeenCalledWith('/api/dashboard', { params: { role: 'CUSTOMER' } })
  })

  it('getBaseFactorValues 应带查询参数', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { data: { items: [] } } })
    const params = { fundCode: '000001', factorId: 1, startDate: '2024-01-01', endDate: '2024-02-01' }
    await api.getBaseFactorValues(params)
    expect(axios.get).toHaveBeenCalledWith('/api/factors/base/value', { params })
  })

  it('createDerivativeFactor 应 POST 并返回 data.data', async () => {
    vi.spyOn(axios, 'post').mockResolvedValue({ data: { data: { id: 99 } } })
    const result = await api.createDerivativeFactor({ name: 'test' })
    expect(result).toEqual({ id: 99 })
  })

  it('createStyleFactor 应 POST', async () => {
    vi.spyOn(axios, 'post').mockResolvedValue({ data: { data: {} } })
    await api.createStyleFactor({ name: 'style' })
    expect(axios.post).toHaveBeenCalledWith('/api/factors/style', { name: 'style' })
  })

  it('updateDerivativeFactor 应 PUT', async () => {
    vi.spyOn(axios, 'put').mockResolvedValue({ data: { data: {} } })
    await api.updateDerivativeFactor(1, { name: 'updated' })
    expect(axios.put).toHaveBeenCalledWith('/api/factors/derived/1', { name: 'updated' })
  })

  it('deleteDerivativeFactor 应 DELETE', async () => {
    vi.spyOn(axios, 'delete').mockResolvedValue({ data: { data: {} } })
    await api.deleteDerivativeFactor(5)
    expect(axios.delete).toHaveBeenCalledWith('/api/factors/derived/5')
  })

  it('deleteStyleFactor 应 DELETE', async () => {
    vi.spyOn(axios, 'delete').mockResolvedValue({ data: { data: {} } })
    await api.deleteStyleFactor(3)
    expect(axios.delete).toHaveBeenCalledWith('/api/factors/style/3')
  })

  it('updateStyleFactor 应 PUT', async () => {
    vi.spyOn(axios, 'put').mockResolvedValue({ data: { data: {} } })
    await api.updateStyleFactor(2, { name: 'updated' })
    expect(axios.put).toHaveBeenCalledWith('/api/factors/style/2', { name: 'updated' })
  })

  it('getFactorPerformance 应带 pool 参数', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { data: [] } })
    await api.getFactorPerformance({ pool: 'all' })
    expect(axios.get).toHaveBeenCalledWith('/api/factors/analysis/performance', { params: { pool: 'all' } })
  })

  it('getFactorCorrelation 应带 topN 参数', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { data: {} } })
    await api.getFactorCorrelation({ topN: 5 })
    expect(axios.get).toHaveBeenCalledWith('/api/factors/analysis/correlation', { params: { topN: 5 } })
  })

  it('aiChat 应 POST 并设置长超时', async () => {
    vi.spyOn(axios, 'post').mockResolvedValue({ data: { reply: 'hello' } })
    const result = await api.aiChat({ query: 'test' })
    expect(axios.post).toHaveBeenCalledWith('/api/ai/chat', { query: 'test' }, { timeout: 120000 })
    expect(result).toEqual({ reply: 'hello' })
  })

  it('aiConversations 应 GET 带 user 和 limit 参数', async () => {
    vi.spyOn(axios, 'get').mockResolvedValue({ data: { conversations: [] } })
    const result = await api.aiConversations('test_user', 10)
    expect(axios.get).toHaveBeenCalledWith('/api/ai/conversations', { params: { user: 'test_user', limit: 10 } })
  })

  it('aiDeleteConversation 应 DELETE', async () => {
    vi.spyOn(axios, 'delete').mockResolvedValue({ data: {} })
    await api.aiDeleteConversation('conv_123', 'test_user')
    expect(axios.delete).toHaveBeenCalledWith('/api/ai/conversations/conv_123', { params: { user: 'test_user' } })
  })
})
