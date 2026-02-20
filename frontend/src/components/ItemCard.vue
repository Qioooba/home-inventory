<template>
  <div class="item-card" @click="$emit('click')">
    <div class="item-image">
      <img v-if="firstImage" :src="firstImage" :alt="item.name" />
      <div v-else class="no-image">
        <el-icon size="32"><Picture /></el-icon>
      </div>
      
      <!-- 收藏按钮 -->
      <div 
        class="favorite-btn"
        :class="{ active: isFavorite }"
        @click.stop="toggleFavorite"
      >
        <el-icon size="16" :color="isFavorite ? '#e6a23c' : '#c0c4cc'">
          <Star-filled v-if="isFavorite" />
          <Star v-else />
        </el-icon>
      </div>
      
      <!-- 分类标签 -->
      <div v-if="item.category" class="category-badge">
        {{ item.category }}
      </div>
    </div>
    
    <div class="item-info">
      <h4 class="item-name">{{ item.name }}</h4>
      <div class="item-location">
        <span class="location-tag">
          <el-icon size="12"><House /></el-icon>
          {{ item.room }}
        </span>
        <span v-if="item.furniture" class="furniture-tag">
          {{ item.furniture }}
        </span>
      </div>
      <div v-if="item.location" class="item-detail">
        📍 {{ item.location }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'toggle-favorite'])

// 本地响应式收藏状态
const isFavorite = ref(props.item.favorite || false)

// 监听外部变化
watch(() => props.item.favorite, (newVal) => {
  isFavorite.value = newVal || false
})

// 获取后端 API 基础 URL
const getBaseUrl = () => {
  const host = window.location.hostname
  return `http://${host}:8080`
}

// 获取第一张图片
const firstImage = computed(() => {
  if (!props.item.images) return null
  const image = props.item.images.split(',')[0]
  if (image.startsWith('http')) return image
  return getBaseUrl() + image
})

// 切换收藏
const toggleFavorite = () => {
  // 立即切换本地状态（视觉反馈）
  isFavorite.value = !isFavorite.value
  
  // 更新 item 对象
  props.item.favorite = isFavorite.value
  
  // 通知父组件
  emit('toggle-favorite', props.item)
}
</script>

<style scoped>
.item-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.item-image {
  height: 120px;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #c0c4cc;
}

.favorite-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 2;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.favorite-btn:hover {
  transform: scale(1.1);
}

.favorite-btn.active {
  background: #fff;
}

.category-badge {
  position: absolute;
  bottom: 6px;
  left: 6px;
  background: rgba(64, 158, 255, 0.9);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.625rem;
  max-width: 80%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.item-name {
  margin: 0 0 6px 0;
  font-size: 0.875rem;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
  line-height: 1.3;
}

.item-location {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.location-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 0.6875rem;
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.furniture-tag {
  font-size: 0.6875rem;
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.item-detail {
  font-size: 0.6875rem;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: auto;
}

/* 桌面端 */
@media (min-width: 769px) {
  .item-image {
    height: 160px;
  }
  
  .favorite-btn {
    width: 32px;
    height: 32px;
  }
  
  .item-info {
    padding: 14px;
  }
  
  .item-name {
    font-size: 1rem;
  }
}

@media (max-width: 375px) {
  .item-image {
    height: 100px;
  }
}
</style>
