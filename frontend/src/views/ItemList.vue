<template>
  <div class="item-list">
    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-content">
        <div class="search-row">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索物品名称..."
            clearable
            @keyup.enter="handleSearch"
            size="large"
            class="search-input"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="filter-row">
          <el-select 
            v-model="selectedRoom" 
            placeholder="选择房间筛选" 
            clearable 
            @change="filterByRoom"
            size="large"
            class="room-select"
          >
            <el-option
              v-for="room in rooms"
              :key="room"
              :label="room"
              :value="room"
            />
          </el-select>
          
          <el-button @click="loadItems" size="large">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 物品列表 -->
    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>物品列表（共 {{ filteredItems.length }} 件）</span>
        </div>
      </template>
      
      <el-empty v-if="filteredItems.length === 0" description="暂无物品，快去添加吧！" />
      
      <div v-else class="items-grid">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="item-wrapper"
        >
          <item-card :item="item" @click="goToDetail(item.id)" @toggle-favorite="toggleFavorite" />
          <div class="item-actions">
            <el-button type="primary" link size="small" @click.stop="goToDetail(item.id)">
              查看
            </el-button>
            <el-button type="danger" link size="small" @click.stop="deleteItem(item)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi } from '../utils/api'
import ItemCard from '../components/ItemCard.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const items = ref([])
const rooms = ref([])
const searchKeyword = ref('')
const selectedRoom = ref('')

// 筛选后的物品
const filteredItems = computed(() => {
  let result = items.value
  
  if (selectedRoom.value) {
    result = result.filter(item => item.room === selectedRoom.value)
  }
  
  return result
})

// 获取物品列表
const loadItems = async () => {
  loading.value = true
  try {
    const res = await itemApi.getAll()
    items.value = res.data
  } catch (error) {
    ElMessage.error('获取物品列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取房间列表
const loadRooms = async () => {
  try {
    const res = await itemApi.getRooms()
    rooms.value = res.data
  } catch (error) {
    console.error('获取房间列表失败:', error)
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadItems()
    return
  }
  loading.value = true
  try {
    const res = await itemApi.search(searchKeyword.value)
    items.value = res.data
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 按房间筛选
const filterByRoom = () => {
  if (selectedRoom.value) {
    loadItemsByRoom(selectedRoom.value)
  } else {
    loadItems()
  }
}

// 获取房间物品
const loadItemsByRoom = async (room) => {
  loading.value = true
  try {
    const res = await itemApi.getByRoom(room)
    items.value = res.data
  } catch (error) {
    ElMessage.error('获取房间物品失败')
  } finally {
    loading.value = false
  }
}

// 删除物品
const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${item.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await itemApi.delete(item.id)
    ElMessage.success('删除成功')
    loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 跳转到详情
const goToDetail = (id) => {
  router.push(`/items/${id}`)
}

// 切换收藏
const toggleFavorite = async (item) => {
  try {
    // 状态已在 ItemCard 中切换
    const newStatus = item.favorite
    await itemApi.toggleFavorite(item.id, newStatus)
    ElMessage.success(newStatus ? '已收藏' : '已取消收藏')
  } catch (error) {
    // 失败时恢复状态
    item.favorite = !item.favorite
    ElMessage.error('操作失败')
  }
}

// 监听路由参数
watch(() => route.query.room, (newRoom) => {
  if (newRoom) {
    selectedRoom.value = newRoom
    loadItemsByRoom(newRoom)
  }
}, { immediate: true })

onMounted(() => {
  loadRooms()
  if (!route.query.room) {
    loadItems()
  }
})
</script>

<style scoped>
.item-list {
  padding-bottom: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-row {
  display: flex;
}

.search-input {
  flex: 1;
}

.filter-row {
  display: flex;
  gap: 10px;
}

.room-select {
  flex: 1;
}

.card-header {
  font-weight: 500;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.item-wrapper {
  display: flex;
  flex-direction: column;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 6px;
  padding: 0 4px;
}

@media (min-width: 769px) {
  .filter-content {
    flex-direction: row;
    align-items: center;
  }
  
  .search-row {
    flex: 1;
    max-width: 400px;
  }
  
  .filter-row {
    flex: 0 0 auto;
  }
  
  .room-select {
    width: 180px;
  }
  
  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 375px) {
  .items-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>
