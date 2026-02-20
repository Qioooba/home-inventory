<template>
  <div class="rooms">
    <el-page-header title="房间管理" @back="$router.back()" />
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col
        v-for="room in rooms"
        :key="room"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card class="room-card" shadow="hover">
          <div class="room-header" @click="viewRoom(room)">
            <el-icon size="40" class="room-icon"><House /></el-icon>
            <h3>{{ room }}</h3>
          </div>
          
          <div class="room-stats">
            <div class="stat">
              <div class="stat-value">{{ roomStats[room]?.count || 0 }}</div>
              <div class="stat-label">物品</div>
            </div>
            
            <div class="stat">
              <div class="stat-value">{{ roomStats[room]?.furniture?.length || 0 }}</div>
              <div class="stat-label">家具/区域</div>
            </div>
          </div>
          
          <div class="room-furniture">
            <el-tag
              v-for="f in roomStats[room]?.furniture?.slice(0, 5)"
              :key="f"
              size="small"
              effect="plain"
              style="margin: 3px;"
            >
              {{ f }}
            </el-tag>
            <el-tag
              v-if="roomStats[room]?.furniture?.length > 5"
              size="small"
              type="info"
            >
              +{{ roomStats[room].furniture.length - 5 }}
            </el-tag>
          </div>
          
          <el-button
            type="primary"
            text
            @click="viewRoom(room)"
            class="view-btn"
          >
            查看物品 →
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    
    <el-empty v-if="rooms.length === 0" description="还没有房间，快去添加物品吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../utils/api'

const router = useRouter()
const rooms = ref([])
const roomStats = ref({})

// 加载房间数据
const loadData = async () => {
  try {
    // 获取所有物品
    const itemsRes = await itemApi.getAll()
    const items = itemsRes.data
    
    // 计算每个房间的统计
    const stats = {}
    const roomSet = new Set()
    
    items.forEach(item => {
      roomSet.add(item.room)
      
      if (!stats[item.room]) {
        stats[item.room] = {
          count: 0,
          furniture: new Set()
        }
      }
      
      stats[item.room].count++
      if (item.furniture) {
        stats[item.room].furniture.add(item.furniture)
      }
    })
    
    // 转换 Set 为 Array
    for (const room in stats) {
      stats[room].furniture = Array.from(stats[room].furniture)
    }
    
    rooms.value = Array.from(roomSet).sort()
    roomStats.value = stats
  } catch (error) {
    console.error('加载房间数据失败:', error)
  }
}

// 查看房间物品
const viewRoom = (room) => {
  router.push({
    path: '/items',
    query: { room }
  })
}

onMounted(loadData)
</script>

<style scoped>
.room-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
}

.room-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.room-icon {
  color: #409eff;
}

.room-header h3 {
  margin: 0;
  font-size: 18px;
}

.room-stats {
  display: flex;
  justify-content: space-around;
  padding: 15px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.stat {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.room-furniture {
  margin-bottom: 15px;
  min-height: 40px;
}

.view-btn {
  width: 100%;
}
</style>
