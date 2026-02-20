import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 物品相关 API
export const itemApi = {
  // 获取所有物品
  getAll: () => api.get('/items'),
  
  // 获取单个物品
  getById: (id) => api.get(`/items/${id}`),
  
  // 创建物品
  create: (formData) => api.post('/items', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  
  // 更新物品
  update: (id, formData) => api.put(`/items/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  
  // 删除物品
  delete: (id) => api.delete(`/items/${id}`),
  
  // 搜索物品
  search: (keyword) => api.get('/items/search', { params: { keyword } }),
  
  // 按房间查找
  getByRoom: (room) => api.get(`/items/room/${encodeURIComponent(room)}`),
  
  // 获取所有房间
  getRooms: () => api.get('/items/rooms'),
  
  // 获取房间的家具
  getFurniture: (room) => api.get(`/items/rooms/${encodeURIComponent(room)}/furniture`),
  
  // 获取收藏物品
  getFavorites: () => api.get('/items/favorites'),
  
  // 获取热门物品
  getPopular: () => api.get('/items/popular'),
  
  // 切换收藏状态
  toggleFavorite: (id, favorite) => api.post(`/items/${id}/favorite`, null, { params: { favorite } }),
  
  // 增加查看次数
  incrementView: (id) => api.post(`/items/${id}/view`),
  
  // 获取统计
  getStats: () => api.get('/items/stats')
}

export default api
