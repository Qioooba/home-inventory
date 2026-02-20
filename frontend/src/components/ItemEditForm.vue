<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
  >
    <el-form-item label="物品名称" prop="name">
      <el-input v-model="form.name" />
    </el-form-item>
    
    <el-form-item label="物品描述">
      <el-input v-model="form.description" type="textarea" :rows="3" />
    </el-form-item>
    
    <el-row :gutter="15">
      <el-col :span="8">
        <el-form-item label="房间" prop="room">
          <el-select v-model="form.room" filterable allow-create @change="onRoomChange">
            <el-option v-for="room in rooms" :key="room" :label="room" :value="room" />
          </el-select>
        </el-form-item>
      </el-col>
      
      <el-col :span="8">
        <el-form-item label="家具">
          <el-select v-model="form.furniture" filterable allow-create :disabled="!form.room">
            <el-option v-for="f in furnitureList" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
      </el-col>
      
      <el-col :span="8">
        <el-form-item label="具体位置">
          <el-input v-model="form.location" />
        </el-form-item>
      </el-col>
    </el-row>
    
    <el-row :gutter="15">
      <el-col :span="12">
        <el-form-item label="分类">
          <el-select v-model="form.category" filterable allow-create>
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
      </el-col>
      
      <el-col :span="12">
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="逗号分隔" />
        </el-form-item>
      </el-col>
    </el-row>
    
    <el-form-item label="重新上传照片（留空则保留原图）">
      <el-upload
        ref="uploadRef"
        action="#"
        list-type="picture-card"
        :auto-upload="false"
        :on-change="handleImageChange"
        :on-remove="handleImageRemove"
        :limit="5"
        multiple
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="submit" :loading="loading">保存</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { itemApi } from '../utils/api'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['saved', 'cancel'])

const formRef = ref()
const loading = ref(false)
const rooms = ref([])
const furnitureList = ref([])
const fileList = ref([])

const form = ref({
  name: props.item.name,
  description: props.item.description || '',
  room: props.item.room,
  furniture: props.item.furniture || '',
  location: props.item.location || '',
  category: props.item.category || '',
  tags: props.item.tags || ''
})

const rules = {
  name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  room: [{ required: true, message: '请选择房间', trigger: 'change' }]
}

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

const handleImageChange = (file, files) => {
  fileList.value = files
}

const handleImageRemove = (file, files) => {
  fileList.value = files
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('name', form.value.name)
    formData.append('description', form.value.description)
    formData.append('room', form.value.room)
    formData.append('furniture', form.value.furniture)
    formData.append('location', form.value.location)
    formData.append('category', form.value.category)
    formData.append('tags', form.value.tags)
    
    fileList.value.forEach(file => {
      formData.append('images', file.raw)
    })
    
    await itemApi.update(props.item.id, formData)
    ElMessage.success('保存成功')
    emit('saved')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const res = await itemApi.getRooms()
    rooms.value = res.data
    if (form.value.room) {
      const fRes = await itemApi.getFurniture(form.value.room)
      furnitureList.value = fRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})
</script>
