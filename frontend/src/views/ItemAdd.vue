<template>
  <div class="item-add">
    <el-page-header title="添加物品" @back="$router.back()" />
    
    <el-card class="form-card" shadow="hover">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="mobile-form"
      >
        <!-- 物品名称 -->
        <el-form-item label="物品名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="例如：身份证、冬季外套"
            size="large"
          />
        </el-form-item>
        
        <!-- 物品描述 -->
        <el-form-item label="物品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="可选：描述物品特征、颜色、数量等"
          />
        </el-form-item>
        
        <!-- 位置选择 -->
        <el-form-item label="房间" prop="room">
          <el-select
            v-model="form.room"
            placeholder="选择或输入房间"
            filterable
            allow-create
            @change="onRoomChange"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="room in rooms"
              :key="room"
              :label="room"
              :value="room"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="家具/区域">
          <el-select
            v-model="form.furniture"
            placeholder="选择或输入家具"
            filterable
            allow-create
            :disabled="!form.room"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="f in furnitureList"
              :key="f"
              :label="f"
              :value="f"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="具体位置">
          <el-input
            v-model="form.location"
            placeholder="例如：上层抽屉、左边柜门"
            size="large"
          />
        </el-form-item>
        
        <!-- 分类 -->
        <el-form-item label="物品分类">
          <el-select
            v-model="form.category"
            placeholder="选择或输入分类"
            filterable
            allow-create
            size="large"
            style="width: 100%"
          >
            <el-option label="电子产品" value="电子产品" />
            <el-option label="衣物鞋帽" value="衣物鞋帽" />
            <el-option label="文件证件" value="文件证件" />
            <el-option label="食品" value="食品" />
            <el-option label="工具" value="工具" />
            <el-option label="书籍" value="书籍" />
            <el-option label="药品" value="药品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="逗号分隔，例如：重要,常用,季节性"
            size="large"
          />
        </el-form-item>
        
        <!-- 图片上传 -->
        <el-form-item label="物品照片">
          <div class="upload-section">
            <div class="upload-actions">
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                multiple
                style="display: none"
                @change="handleFileSelect"
              />
              <el-button type="primary" @click="$refs.fileInput.click()">
                <el-icon><Camera /></el-icon>
                拍照/选图
              </el-button>
              
              <el-switch
                v-model="compressEnabled"
                active-text="压缩图片"
                inline-prompt
                style="margin-left: 10px;"
              />
            </div>
            
            <div v-if="imagePreviewList.length > 0" class="image-preview-list">
              <div
                v-for="(img, index) in imagePreviewList"
                :key="index"
                class="preview-item"
              >
                <img :src="img.preview" />
                <div class="preview-info">
                  <span v-if="img.compressed" class="compressed-badge">已压缩</span>
                </div>
                <div class="preview-remove" @click="removeImage(index)">
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>
            
            <div class="upload-tip">
              <p>已选择 {{ imagePreviewList.length }}/5 张照片</p>
              <p v-if="compressEnabled">图片将自动压缩到 800KB 以下，节省存储空间</p>
            </div>
          </div>
        </el-form-item>
        
        <!-- 提交按钮 -->
        <el-form-item class="submit-actions">
          <el-button 
            type="primary" 
            size="large" 
            @click="submitForm" 
            :loading="submitting"
            class="submit-btn"
          >
            <el-icon><Check /></el-icon>
            保存物品
          </el-button>
          
          <el-button 
            size="large" 
            @click="$router.back()"
            class="cancel-btn"
          >
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi } from '../utils/api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const rooms = ref([])
const furnitureList = ref([])
const imagePreviewList = ref([])
const compressEnabled = ref(true)

const form = ref({
  name: '',
  description: '',
  room: '',
  furniture: '',
  location: '',
  category: '',
  tags: ''
})

const rules = {
  name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  room: [{ required: true, message: '请选择房间', trigger: 'change' }]
}

// 压缩图片
const compressImage = (file, maxSize = 800 * 1024) => {
  return new Promise((resolve) => {
    if (file.size <= maxSize) {
      resolve(file)
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')

        // 计算缩放比例
        let width = img.width
        let height = img.height
        const maxDimension = 1920

        if (width > maxDimension || height > maxDimension) {
          if (width > height) {
            height = Math.round((height * maxDimension) / width)
            width = maxDimension
          } else {
            width = Math.round((width * maxDimension) / height)
            height = maxDimension
          }
        }

        canvas.width = width
        canvas.height = height
        ctx.drawImage(img, 0, 0, width, height)

        // 压缩质量
        let quality = 0.9
        let compressedDataUrl = canvas.toDataURL('image/jpeg', quality)

        while (compressedDataUrl.length > maxSize * 1.37 && quality > 0.1) {
          quality -= 0.1
          compressedDataUrl = canvas.toDataURL('image/jpeg', quality)
        }

        // 转换回文件
        const byteString = atob(compressedDataUrl.split(',')[1])
        const mimeString = compressedDataUrl.split(',')[0].split(':')[1].split(';')[0]
        const ab = new ArrayBuffer(byteString.length)
        const ia = new Uint8Array(ab)
        for (let i = 0; i < byteString.length; i++) {
          ia[i] = byteString.charCodeAt(i)
        }
        const blob = new Blob([ab], { type: mimeString })
        const compressedFile = new File([blob], file.name, { type: mimeString })

        resolve(compressedFile)
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

// 处理文件选择
const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files)
  
  if (imagePreviewList.value.length + files.length > 5) {
    ElMessage.warning('最多只能上传5张照片')
    return
  }

  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`${file.name} 不是图片文件`)
      continue
    }

    try {
      const previewUrl = URL.createObjectURL(file)
      let processedFile = file
      let compressed = false

      // 压缩图片
      if (compressEnabled.value && file.size > 800 * 1024) {
        ElMessage.info(`正在压缩 ${file.name}...`)
        processedFile = await compressImage(file)
        compressed = true
        ElMessage.success(`${file.name} 已压缩 ${Math.round((1 - processedFile.size / file.size) * 100)}%`)
      }

      imagePreviewList.value.push({
        file: processedFile,
        preview: previewUrl,
        compressed
      })
    } catch (error) {
      console.error('处理图片失败:', error)
      ElMessage.error(`处理 ${file.name} 失败`)
    }
  }

  // 清空 input 以便可以重复选择相同文件
  event.target.value = ''
}

// 移除图片
const removeImage = (index) => {
  URL.revokeObjectURL(imagePreviewList.value[index].preview)
  imagePreviewList.value.splice(index, 1)
}

// 房间变化时加载家具
const onRoomChange = async (room) => {
  form.value.furniture = ''
  if (!room) {
    furnitureList.value = []
    return
  }
  try {
    const res = await itemApi.getFurniture(room)
    furnitureList.value = res.data
  } catch (error) {
    console.error('获取家具失败:', error)
  }
}

// 提交表单
const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('name', form.value.name)
    formData.append('description', form.value.description || '')
    formData.append('room', form.value.room)
    formData.append('furniture', form.value.furniture || '')
    formData.append('location', form.value.location || '')
    formData.append('category', form.value.category || '')
    formData.append('tags', form.value.tags || '')
    
    // 添加图片
    imagePreviewList.value.forEach(img => {
      formData.append('images', img.file)
    })
    
    await itemApi.create(formData)
    ElMessage.success('添加成功！')
    router.push('/items')
  } catch (error) {
    ElMessage.error('添加失败，请重试')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 加载房间列表
onMounted(async () => {
  try {
    const res = await itemApi.getRooms()
    rooms.value = res.data
  } catch (error) {
    console.error('获取房间失败:', error)
  }
})
</script>

<style scoped>
.form-card {
  margin-top: 16px;
}

.mobile-form :deep(.el-form-item__label) {
  font-size: 0.875rem;
  font-weight: 500;
  padding-bottom: 4px;
}

/* 上传区域 */
.upload-section {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.upload-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.image-preview-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
  text-align: center;
}

.compressed-badge {
  font-size: 0.625rem;
  color: #67c23a;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 4px;
}

.preview-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(245, 108, 108, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  font-size: 12px;
}

.upload-tip {
  margin-top: 12px;
  text-align: center;
  color: #909399;
  font-size: 0.75rem;
  line-height: 1.5;
}

.upload-tip p {
  margin: 0;
}

/* 提交按钮 */
.submit-actions {
  margin-top: 24px;
  margin-bottom: 0;
}

.submit-actions :deep(.el-form-item__content) {
  display: flex;
  gap: 10px;
}

.submit-btn {
  flex: 1;
  height: 44px;
  font-size: 1rem;
}

.cancel-btn {
  flex: 1;
  height: 44px;
}

@media (min-width: 769px) {
  .form-card {
    margin-top: 20px;
    max-width: 800px;
  }
  
  .upload-section {
    padding: 20px;
  }
  
  .image-preview-list {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .submit-actions :deep(.el-form-item__content) {
    justify-content: flex-start;
  }
  
  .submit-btn {
    flex: 0 0 auto;
    min-width: 120px;
  }
  
  .cancel-btn {
    flex: 0 0 auto;
    min-width: 100px;
  }
}
</style>
