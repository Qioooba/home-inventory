<template>
  <div class="item-detail" v-if="item">
    <el-page-header title="物品详情" @back="$router.back()" />
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：图片 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <div v-if="imageList.length > 0" class="image-gallery">
            <el-carousel height="350px" trigger="click">
              <el-carousel-item v-for="(img, index) in imageList" :key="index">
                <img :src="img" class="carousel-image" @click="previewImage(img)" />
              </el-carousel-item>
            </el-carousel>
            <div class="image-count">{{ imageList.length }} 张照片</div>
          </div>
          
          <el-empty v-else description="暂无照片" />
        </el-card>
      </el-col>
      
      <!-- 右侧：信息 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="item-title">{{ item.name }}</span>
              <el-button-group>
                <el-button type="primary" @click="showEdit = true">编辑</el-button>
                <el-button type="danger" @click="deleteItem">删除</el-button>
              </el-button-group>
            </div>
          </template>
          
          <div class="info-section">
            <h4>📍 存放位置</h4>
            <div class="info-content">
              <el-tag size="large" effect="dark" type="primary">
                {{ item.room }}
              </el-tag>
              
              <el-tag v-if="item.furniture" size="large" effect="dark" type="warning">
                {{ item.furniture }}
              </el-tag>
              
              <div v-if="item.location" class="location-detail">
                📌 {{ item.location }}
              </div>
            </div>
          </div>
          
          <el-divider />
          
          <div class="info-section">
            <h4>🏷️ 分类信息</h4>
            <div class="info-content">
              <el-tag v-if="item.category" type="info">{{ item.category }}</el-tag>
              <span v-else class="no-data">未设置分类</span>
            </div>
          </div>
          
          <el-divider v-if="item.tags" />
          
          <div v-if="item.tags" class="info-section">
            <h4>🔖 标签</h4>
            <div class="info-content">
              <el-tag
                v-for="tag in tagList"
                :key="tag"
                size="small"
                effect="plain"
                style="margin-right: 8px; margin-bottom: 5px;"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          
          <el-divider v-if="item.description" />
          
          <div v-if="item.description" class="info-section">
            <h4>📝 描述</h4>
            <p class="description">{{ item.description }}</p>
          </div>
          
          <el-divider />
          
          <div class="info-section">
            <div class="time-info">
              <div>创建时间：{{ formatTime(item.createdAt) }}</div>
              <div>更新时间：{{ formatTime(item.updatedAt) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="showEdit" title="编辑物品" width="600px">
      <item-edit-form :item="item" @saved="onEditSaved" @cancel="showEdit = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi } from '../utils/api'
import ItemEditForm from '../components/ItemEditForm.vue'

const router = useRouter()
const route = useRoute()

const item = ref(null)
const showEdit = ref(false)

// 获取后端 API 基础 URL
const getBaseUrl = () => {
  const host = window.location.hostname
  return `http://${host}:8080`
}

// 图片列表
const imageList = computed(() => {
  if (!item.value?.images) return []
  return item.value.images.split(',').map(img => {
    if (img.startsWith('http')) return img
    return getBaseUrl() + img
  })
})

// 标签列表
const tagList = computed(() => {
  if (!item.value?.tags) return []
  return item.value.tags.split(',').filter(t => t.trim())
})

// 加载物品详情
const loadItem = async () => {
  try {
    const res = await itemApi.getById(route.params.id)
    item.value = res.data
  } catch (error) {
    ElMessage.error('获取物品详情失败')
    router.back()
  }
}

// 删除物品
const deleteItem = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${item.value.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await itemApi.delete(item.value.id)
    ElMessage.success('删除成功')
    router.push('/items')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 预览图片
const previewImage = (url) => {
  // 简单实现：在新窗口打开
  window.open(url, '_blank')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 编辑保存成功
const onEditSaved = () => {
  showEdit.value = false
  loadItem()
}

onMounted(loadItem)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.item-title {
  font-size: 20px;
  font-weight: bold;
}

.image-gallery {
  position: relative;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  cursor: pointer;
}

.image-count {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.info-section h4 {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
}

.info-content {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.location-detail {
  width: 100%;
  margin-top: 10px;
  color: #409eff;
  font-size: 14px;
}

.no-data {
  color: #909399;
}

.description {
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.time-info {
  font-size: 12px;
  color: #909399;
}

.time-info div {
  margin-bottom: 5px;
}
</style>
