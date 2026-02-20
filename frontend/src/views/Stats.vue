<template>
  <div class="stats-page">
    <el-page-header title="数据统计" @back="$router.back()" />

    <!-- 总览卡片 -->
    <div class="stats-overview">
      <div class="overview-card primary">
        <div class="overview-icon"><el-icon size="32"><Box /></el-icon></div>
        <div class="overview-info">
          <div class="overview-value">{{ stats.totalItems || 0 }}</div>
          <div class="overview-label">物品总数</div>
        </div>
      </div>

      <div class="overview-card success">
        <div class="overview-icon"><el-icon size="32"><Office-Building /></el-icon></div>
        <div class="overview-info">
          <div class="overview-value">{{ stats.rooms?.length || 0 }}</div>
          <div class="overview-label">房间数</div>
        </div>
      </div>

      <div class="overview-card warning">
        <div class="overview-icon"><el-icon size="32"><Collection /></el-icon></div>
        <div class="overview-info">
          <div class="overview-value">{{ categoryStats.length }}</div>
          <div class="overview-label">分类数</div>
        </div>
      </div>
    </div>

    <!-- 分类统计 -->
    <el-card class="stats-section" shadow="hover">
      <template #header>
        <div class="section-header">
          <span>📊 分类统计</span>
        </div>
      </template>
      
      <div v-if="categoryStats.length === 0" class="empty-text">
        暂无分类数据
      </div>
      <div v-else class="category-list">
        <div
          v-for="cat in categoryStats"
          :key="cat.name"
          class="category-item"
        >
          <div class="category-info">
            <span class="category-name">{{ cat.name || '未分类' }}</span>
            <span class="category-count">{{ cat.count }} 件</span>
          </div>
          <div class="category-bar">
            <div
              class="category-progress"
              :style="{ width: cat.percentage + '%', backgroundColor: cat.color }"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 房间统计 -->
    <el-card class="stats-section" shadow="hover">
      <template #header>
        <div class="section-header">
          <span>🏠 房间分布</span>
        </div>
      </template>
      
      <div v-if="roomStats.length === 0" class="empty-text">
        暂无房间数据
      </div>
      
      <div v-else class="room-grid">
        <div
          v-for="room in roomStats"
          :key="room.name"
          class="room-stat-card"
          @click="$router.push({ path: '/items', query: { room: room.name } })"
        >
          <div class="room-name">{{ room.name }}</div>
          <div class="room-count">{{ room.count }}</div>
          <div class="room-label">件物品</div>
        </div>
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <el-card class="stats-section" shadow="hover">
      <template #header>
        <div class="section-header">
          <span>🛠️ 快捷操作</span>
        </div>
      </template>
      
      <div class="action-grid">
        <el-button type="primary" size="large" @click="exportData">
          <el-icon><Download /></el-icon>
          导出数据 (Excel)
        </el-button>

        <el-button type="success" size="large" @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>

        <el-button type="warning" size="large" @click="generateQRCode">
          <el-icon><FullScreen /></el-icon>
          生成位置二维码
        </el-button>
      </div>
    </el-card>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="showImportDialog"
      title="批量导入物品"
      width="90%"
      :max-width="500"
    >
      <div class="import-guide">
        <p>请上传 Excel 文件，格式如下：</p>
        <pre>名称 | 描述 | 房间 | 家具 | 位置 | 分类 | 标签</pre>
        <el-button type="primary" link @click="downloadTemplate">
          下载模板文件
        </el-button>
      </div>
      
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleImportFile"
        accept=".xlsx,.xls"
        class="import-upload"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
      </el-upload>
      
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importing">
          确认导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 二维码对话框 -->
    <el-dialog
      v-model="showQRDialog"
      title="位置二维码"
      width="90%"
      :max-width="400"
    >
      <div class="qr-content">
        <p>选择要生成二维码的房间/位置：</p>
        <el-select v-model="selectedRoom" placeholder="选择房间" style="width: 100%; margin-bottom: 10px;">
          <el-option
            v-for="room in stats.rooms"
            :key="room"
            :label="room"
            :value="room"
          />
        </el-select>
        
        <div v-if="qrCodeUrl" class="qr-display">
          <img :src="qrCodeUrl" alt="二维码" />
          <p class="qr-tip">打印后贴在柜子上，扫码即可查看该位置物品</p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showQRDialog = false">关闭</el-button>
        <el-button type="primary" @click="generateRoomQR" :loading="generatingQR">
          生成二维码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi } from '../utils/api'

const router = useRouter()
const stats = ref({ totalItems: 0, rooms: [] })
const items = ref([])
const showImportDialog = ref(false)
const showQRDialog = ref(false)
const selectedRoom = ref('')
const qrCodeUrl = ref('')
const importing = ref(false)
const generatingQR = ref(false)
const importFile = ref(null)

// 分类统计
const categoryStats = computed(() => {
  const categories = {}
  items.value.forEach(item => {
    const cat = item.category || '未分类'
    categories[cat] = (categories[cat] || 0) + 1
  })
  
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#13c2c2']
  const total = items.value.length
  
  return Object.entries(categories)
    .map(([name, count], index) => ({
      name,
      count,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0,
      color: colors[index % colors.length]
    }))
    .sort((a, b) => b.count - a.count)
})

// 房间统计
const roomStats = computed(() => {
  const rooms = {}
  items.value.forEach(item => {
    if (item.room) {
      rooms[item.room] = (rooms[item.room] || 0) + 1
    }
  })
  
  return Object.entries(rooms)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
})

// 加载数据
const loadData = async () => {
  try {
    const [statsRes, itemsRes] = await Promise.all([
      itemApi.getStats(),
      itemApi.getAll()
    ])
    stats.value = statsRes.data
    items.value = itemsRes.data
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 导出数据
const exportData = () => {
  const headers = ['名称', '描述', '房间', '家具', '位置', '分类', '标签', '创建时间']
  const rows = items.value.map(item => [
    item.name,
    item.description || '',
    item.room || '',
    item.furniture || '',
    item.location || '',
    item.category || '',
    item.tags || '',
    item.createdAt || ''
  ])
  
  const csvContent = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `物品清单_${new Date().toLocaleDateString()}.csv`
  link.click()
  
  ElMessage.success('导出成功！')
}

// 下载模板
const downloadTemplate = () => {
  const headers = ['名称', '描述', '房间', '家具', '位置', '分类', '标签']
  const example = ['示例物品', '物品描述', '客厅', '电视柜', '上层抽屉', '电子产品', '常用']
  
  const csvContent = [headers, example]
    .map(row => row.map(cell => `"${cell}"`).join(','))
    .join('\n')
  
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '物品导入模板.csv'
  link.click()
}

// 处理导入文件
const handleImportFile = (file) => {
  importFile.value = file.raw
}

// 确认导入
const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  importing.value = true
  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const text = e.target.result
      const lines = text.split('\n').filter(line => line.trim())
      
      // 跳过标题行
      const dataRows = lines.slice(1)
      let successCount = 0
      
      for (const line of dataRows) {
        const cols = line.split(',').map(col => col.replace(/^"|"$/g, '').trim())
        if (cols[0]) { // 有名称
          try {
            await itemApi.create({
              name: cols[0],
              description: cols[1] || '',
              room: cols[2] || '',
              furniture: cols[3] || '',
              location: cols[4] || '',
              category: cols[5] || '',
              tags: cols[6] || ''
            })
            successCount++
          } catch (err) {
            console.error('导入失败:', err)
          }
        }
      }
      
      ElMessage.success(`成功导入 ${successCount} 个物品`)
      showImportDialog.value = false
      loadData()
    }
    reader.readAsText(importFile.value)
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

// 生成二维码
const generateQRCode = () => {
  showQRDialog.value = true
  qrCodeUrl.value = ''
  selectedRoom.value = ''
}

// 生成房间二维码
const generateRoomQR = async () => {
  if (!selectedRoom.value) {
    ElMessage.warning('请选择房间')
    return
  }
  
  generatingQR.value = true
  try {
    // 使用 QRCode.js API 生成二维码
    const url = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
      `${window.location.origin}/#/items?room=${encodeURIComponent(selectedRoom.value)}`
    )}`
    qrCodeUrl.value = url
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generatingQR.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stats-page {
  padding-bottom: 20px;
}

/* 总览卡片 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 16px 0;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 16px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.overview-card.primary {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: white;
}

.overview-card.success {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
}

.overview-card.warning {
  background: linear-gradient(135deg, #e6a23c, #eebe77);
  color: white;
}

.overview-icon {
  opacity: 0.9;
}

.overview-value {
  font-size: 1.75rem;
  font-weight: bold;
  line-height: 1;
}

.overview-label {
  font-size: 0.75rem;
  opacity: 0.9;
}

/* 统计区块 */
.stats-section {
  margin-bottom: 16px;
}

.section-header {
  font-weight: 500;
  font-size: 0.9375rem;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 30px 0;
  font-size: 0.875rem;
}

/* 分类列表 */
.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8125rem;
}

.category-name {
  color: #303133;
  font-weight: 500;
}

.category-count {
  color: #909399;
}

.category-bar {
  height: 8px;
  background: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.category-progress {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

/* 房间网格 */
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.room-stat-card {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.room-stat-card:hover {
  background: #409eff;
  color: white;
  transform: translateY(-2px);
}

.room-stat-card:hover .room-count,
.room-stat-card:hover .room-label {
  color: white;
}

.room-name {
  font-size: 0.8125rem;
  color: #606266;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-count {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.room-label {
  font-size: 0.6875rem;
  color: #909399;
  margin-top: 2px;
}

/* 快捷操作 */
.action-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.action-grid .el-button {
  justify-content: center;
  padding: 16px;
  font-size: 0.9375rem;
}

/* 导入对话框 */
.import-guide {
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 0.8125rem;
}

.import-guide p {
  margin-bottom: 8px;
}

.import-guide pre {
  background: #e4e7ed;
  padding: 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  overflow-x: auto;
}

.import-upload {
  text-align: center;
}

/* 二维码 */
.qr-content {
  text-align: center;
}

.qr-content > p:first-child {
  margin-bottom: 16px;
  color: #606266;
}

.qr-display {
  margin-top: 20px;
}

.qr-display img {
  max-width: 200px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.qr-tip {
  margin-top: 12px;
  color: #909399;
  font-size: 0.75rem;
}

/* 桌面端 */
@media (min-width: 769px) {
  .stats-overview {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin: 20px 0;
  }
  
  .overview-card {
    padding: 24px;
    flex-direction: row;
    justify-content: flex-start;
  }
  
  .overview-value {
    font-size: 2rem;
  }
  
  .action-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .room-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}

@media (min-width: 1024px) {
  .overview-value {
    font-size: 2.5rem;
  }
}
</style>
