<template>
  <div class="home">
    <!-- 快速搜索 - 固定在顶部 -->
    <div class="search-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="🔍 搜索物品名称、房间、位置..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="clearSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 快捷搜索标签 -->
      <div v-if="!searchKeyword" class="quick-tags">
        <span
          v-for="tag in quickTags"
          :key="tag"
          class="tag-item"
          @click="searchKeyword = tag; handleSearch()"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchKeyword && searchResults.length > 0" class="section">
      <div class="section-header">
        <span class="section-title">搜索结果 ({{ searchResults.length }})</span>
        <el-button type="primary" link size="small" @click="clearSearch">
          清除
        </el-button>
      </div>
      <div class="items-list">
        <div
          v-for="item in searchResults"
          :key="item.id"
          class="list-item"
          @click="goToDetail(item.id)"
        >
          <div class="item-thumb">
            <img v-if="getFirstImage(item)" :src="getFirstImage(item)" />
            <span v-else>📦</span>
          </div>
          <div class="item-content">
            <div class="item-title">
              {{ item.name }}
              <el-tag v-if="item.favorite" size="small" type="warning" class="fav-tag">⭐</el-tag>
            </div>
            <div class="item-meta">
              <span class="meta-tag">{{ item.room }}</span>
              <span v-if="item.furniture" class="meta-tag">{{ item.furniture }}</span>
              <span v-if="item.category" class="meta-tag cat">{{ item.category }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 常用物品 -->
    <div v-if="favoriteItems.length > 0" class="section">
      <div class="section-header">
        <span class="section-title">⭐ 常用物品</span>
      </div>
      <div class="favorites-grid">
        <div
          v-for="item in favoriteItems"
          :key="item.id"
          class="fav-item"
          @click="goToDetail(item.id)"
        >
          <div class="fav-thumb">
            <img v-if="getFirstImage(item)" :src="getFirstImage(item)" />
            <span v-else>📦</span>
          </div>
          <div class="fav-name">{{ item.name }}</div>
        </div>
      </div>
    </div>

    <!-- 最近查看 -->
    <div v-if="recentlyViewed.length > 0" class="section">
      <div class="section-header">
        <span class="section-title">👀 最近查看</span>
        <el-button type="info" link size="small" @click="clearRecent">
          清除
        </el-button>
      </div>
      <div class="items-list">
        <div
          v-for="item in recentlyViewed.slice(0, 5)"
          :key="item.id"
          class="list-item"
          @click="goToDetail(item.id)"
        >
          <div class="item-thumb">
            <img v-if="getFirstImage(item)" :src="getFirstImage(item)" />
            <span v-else>📦</span>
          </div>
          <div class="item-content">
            <div class="item-title">
              {{ item.name }}
              <el-tag v-if="item.favorite" size="small" type="warning" class="fav-tag">⭐</el-tag>
            </div>
            <div class="item-meta">
              <span class="meta-tag">{{ item.room }}</span>
              <span v-if="item.furniture" class="meta-tag">{{ item.furniture }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 按房间浏览 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">🏠 按房间浏览</span>
      </div>
      <div class="rooms-list">
        <div
          v-for="room in stats.rooms"
          :key="room"
          class="room-item"
          @click="goToRoom(room)"
        >
          <span class="room-icon">🏠</span>
          <span class="room-name">{{ room }}</span>
          <el-icon class="room-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">📊 统计</span>
      </div>
      <div class="stats-row">
        <div class="stat-box">
          <div class="stat-num">{{ stats.totalItems || 0 }}</div>
          <div class="stat-label">物品总数</div>
        </div>
        <div class="stat-box">
          <div class="stat-num">{{ stats.rooms?.length || 0 }}</div>
          <div class="stat-label">房间数</div>
        </div>
        <div class="stat-box">
          <div class="stat-num">{{ favoriteItems.length }}</div>
          <div class="stat-label">常用物品</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi } from '../utils/api'

const router = useRouter()
const stats = ref({ totalItems: 0, rooms: [] })
const favoriteItems = ref([])
const recentlyViewed = ref([])
const searchKeyword = ref('')
const searchResults = ref([])

// 快捷搜索标签
const quickTags = ['身份证', '充电器', '钥匙', '遥控', '药']

// 获取后端 API 基础 URL
const getBaseUrl = () => {
  const host = window.location.hostname
  return `http://${host}:8080`
}

// 获取第一张图片
const getFirstImage = (item) => {
  if (!item.images) return null
  const image = item.images.split(',')[0]
  if (image.startsWith('http')) return image
  return getBaseUrl() + image
}

// 加载数据
const loadData = async () => {
  try {
    const [statsRes, favoritesRes] = await Promise.all([
      itemApi.getStats(),
      itemApi.getFavorites().catch(() => ({ data: [] }))
    ])
    stats.value = statsRes.data
    favoriteItems.value = favoritesRes.data
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 加载最近查看
const loadRecentViewed = () => {
  try {
    const stored = localStorage.getItem('recentlyViewed')
    if (stored) {
      recentlyViewed.value = JSON.parse(stored)
    }
  } catch (error) {
    console.error('加载最近查看失败:', error)
  }
}

// 智能搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await itemApi.getAll()
    const keyword = searchKeyword.value.toLowerCase()
    
    searchResults.value = res.data.filter(item => {
      const searchFields = [
        item.name,
        item.description,
        item.room,
        item.furniture,
        item.location,
        item.category,
        item.tags
      ].filter(Boolean).join(' ').toLowerCase()
      
      return searchFields.includes(keyword)
    })
    
    // 收藏优先
    searchResults.value.sort((a, b) => {
      if (a.favorite && !b.favorite) return -1
      if (!a.favorite && b.favorite) return 1
      return new Date(b.updatedAt) - new Date(a.updatedAt)
    })
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
}

// 跳转到详情
const goToDetail = async (id) => {
  try {
    const res = await itemApi.getById(id)
    const item = res.data
    
    // 记录到最近查看
    const index = recentlyViewed.value.findIndex(i => i.id === id)
    if (index > -1) {
      recentlyViewed.value.splice(index, 1)
    }
    recentlyViewed.value.unshift(item)
    if (recentlyViewed.value.length > 8) {
      recentlyViewed.value = recentlyViewed.value.slice(0, 8)
    }
    localStorage.setItem('recentlyViewed', JSON.stringify(recentlyViewed.value))
    
    // 增加查看次数
    itemApi.incrementView(id).catch(() => {})
  } catch (error) {
    console.error('记录查看失败:', error)
  }
  
  router.push(`/items/${id}`)
}

// 清除最近查看
const clearRecent = () => {
  recentlyViewed.value = []
  localStorage.removeItem('recentlyViewed')
}

// 跳转到房间
const goToRoom = (room) => {
  router.push({
    path: '/items',
    query: { room }
  })
}

onMounted(() => {
  loadData()
  loadRecentViewed()
})
</script>

<style scoped>
.home {
  padding: 12px;
  padding-bottom: 80px;
}

/* 搜索区域 */
.search-section {
  background: #fff;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box :deep(.el-input__inner) {
  height: 44px;
  font-size: 1rem;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag-item {
  padding: 6px 14px;
  background: #f0f2f5;
  border-radius: 16px;
  font-size: 0.875rem;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-item:hover {
  background: #409eff;
  color: white;
}

/* 区块 */
.section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #303133;
}

/* 常用物品网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.fav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: background 0.2s;
}

.fav-item:hover {
  background: #f5f7fa;
}

.fav-thumb {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.fav-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fav-name {
  font-size: 0.75rem;
  color: #606266;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* 列表样式 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.list-item:hover {
  background: #e8f4ff;
}

.item-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 0.9375rem;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.fav-tag {
  padding: 0 4px;
  height: 18px;
  line-height: 16px;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meta-tag {
  font-size: 0.6875rem;
  padding: 2px 8px;
  background: #e4e7ed;
  border-radius: 4px;
  color: #606266;
}

.meta-tag.cat {
  background: #409eff;
  color: white;
}

/* 房间列表 */
.rooms-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.room-item:hover {
  background: #409eff;
  color: white;
}

.room-icon {
  font-size: 1.25rem;
}

.room-name {
  flex: 1;
  font-size: 1rem;
  font-weight: 500;
}

.room-arrow {
  font-size: 1.125rem;
  opacity: 0.6;
}

/* 统计 */
.stats-row {
  display: flex;
  justify-content: space-around;
  gap: 12px;
}

.stat-box {
  text-align: center;
  flex: 1;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 10px;
}

.stat-num {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: #909399;
  margin-top: 4px;
}

/* 桌面端 */
@media (min-width: 769px) {
  .home {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(6, 1fr);
  }
  
  .fav-thumb {
    width: 64px;
    height: 64px;
  }
}
</style>
