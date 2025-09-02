<template>
  <div class="my-meals-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">我的饮食记录</h2>
        <p class="page-subtitle">记录您的每一餐，分享健康生活</p>
      </div>
      <el-button type="primary" @click="showAddMealDialog = true" class="add-meal-btn">
        <el-icon><Plus /></el-icon>
        添加记录
      </el-button>
    </div>

    <!-- 餐次筛选标签 -->
    <div class="meal-filter">
      <el-radio-group v-model="selectedMealType" @change="filterMeals">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="breakfast">早餐</el-radio-button>
        <el-radio-button label="lunch">午餐</el-radio-button>
        <el-radio-button label="dinner">晚餐</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 饮食记录列表 -->
    <div class="meals-list">
      <div v-if="filteredMeals.length === 0" class="empty-state">
        <el-empty description="暂无饮食记录" :image-size="120">
          <el-button type="primary" @click="showAddMealDialog = true">开始记录</el-button>
        </el-empty>
      </div>
      
      <div v-else class="meals-grid">
        <div 
          v-for="meal in filteredMeals" 
          :key="meal.id"
          class="meal-card"
        >
          <div class="meal-header">
            <div class="meal-info">
              <div class="meal-type">
                <el-tag :type="getMealTypeColor(meal.type)" size="small">
                  {{ getMealTypeName(meal.type) }}
                </el-tag>
              </div>
              <div class="meal-time">{{ formatTime(meal.createdAt) }}</div>
            </div>
            <div class="meal-actions">
              <el-button type="text" size="small" @click="editMeal(meal)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="text" size="small" @click="deleteMeal(meal.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="meal-content">
            <div class="meal-description">{{ meal.description }}</div>
            <div v-if="meal.images && meal.images.length > 0" class="meal-images">
              <div class="image-grid">
                <div 
                  v-for="(image, index) in meal.images.slice(0, 3)" 
                  :key="index"
                  class="meal-image"
                  @click="previewImage(meal.images, index)"
                >
                  <img :src="image" :alt="`餐食图片${index + 1}`" />
                </div>
                <div v-if="meal.images.length > 3" class="more-images">
                  +{{ meal.images.length - 3 }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="meal-footer">
            <div class="meal-stats">
              <span class="calories">{{ meal.calories }} 卡路里</span>
              <span class="rating">
                <el-rate 
                  v-model="meal.rating" 
                  disabled 
                  show-score 
                  text-color="#ff9900"
                  score-template="{value} 分"
                />
              </span>
            </div>
            <div class="meal-share">
              <el-switch
                v-model="meal.isPublic"
                active-text="已分享"
                inactive-text="未分享"
                @change="toggleShare(meal)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑饮食记录对话框 -->
    <el-dialog
      v-model="showAddMealDialog"
      :title="editingMeal ? '编辑饮食记录' : '添加饮食记录'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="mealForm" :rules="mealRules" ref="mealFormRef" label-width="80px">
        <el-form-item label="餐次类型" prop="type">
          <el-select v-model="mealForm.type" placeholder="请选择餐次">
            <el-option label="早餐" value="breakfast" />
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="mealForm.description"
            type="textarea"
            :rows="3"
            placeholder="请描述您的餐食内容..."
          />
        </el-form-item>
        
        <el-form-item label="卡路里">
          <el-input-number
            v-model="mealForm.calories"
            :min="0"
            :max="5000"
            placeholder="估算卡路里"
          />
        </el-form-item>
        
        <el-form-item label="评分">
          <el-rate v-model="mealForm.rating" show-text />
        </el-form-item>
        
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="mealForm.images"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="6"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-change="handleChange"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="分享到社区">
          <el-switch v-model="mealForm.isPublic" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddMealDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMeal">保存</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="showImagePreview" title="图片预览" width="80%">
      <div class="image-preview-container">
        <el-carousel :initial-index="previewIndex" height="400px">
          <el-carousel-item v-for="(image, index) in previewImages" :key="index">
            <img :src="image" class="preview-image" />
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

// 响应式数据
const selectedMealType = ref('all')
const showAddMealDialog = ref(false)
const showImagePreview = ref(false)
const editingMeal = ref(null)
const previewImages = ref([])
const previewIndex = ref(0)
const mealFormRef = ref()

// 表单数据
const mealForm = ref({
  type: '',
  description: '',
  calories: 0,
  rating: 5,
  images: [],
  isPublic: false
})

// 表单验证规则
const mealRules = {
  type: [{ required: true, message: '请选择餐次类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入餐食描述', trigger: 'blur' }]
}

// 模拟数据
const meals = ref([
  {
    id: 1,
    type: 'breakfast',
    description: '营养丰富的早餐：全麦面包配鸡蛋，一杯牛奶，还有新鲜的水果沙拉。',
    calories: 450,
    rating: 4,
    images: [
      'https://images.unsplash.com/photo-1551782450-17144efb9c50?w=300&h=200&fit=crop',
      'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=300&h=200&fit=crop'
    ],
    isPublic: true,
    createdAt: new Date('2024-01-15 08:30:00')
  },
  {
    id: 2,
    type: 'lunch',
    description: '健康午餐：蒸蛋羹、清炒时蔬、紫菜蛋花汤，营养均衡。',
    calories: 380,
    rating: 5,
    images: [
      'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300&h=200&fit=crop'
    ],
    isPublic: false,
    createdAt: new Date('2024-01-15 12:15:00')
  },
  {
    id: 3,
    type: 'dinner',
    description: '轻食晚餐：蔬菜沙拉配鸡胸肉，少油少盐，健康美味。',
    calories: 320,
    rating: 4,
    images: [
      'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300&h=200&fit=crop',
      'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300&h=200&fit=crop',
      'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=300&h=200&fit=crop'
    ],
    isPublic: true,
    createdAt: new Date('2024-01-15 18:45:00')
  },
  {
    id: 4,
    type: 'breakfast',
    description: '简单早餐：燕麦粥配坚果，一杯豆浆。',
    calories: 280,
    rating: 3,
    images: [],
    isPublic: false,
    createdAt: new Date('2024-01-14 07:20:00')
  }
])

// 计算属性
const filteredMeals = computed(() => {
  if (selectedMealType.value === 'all') {
    return meals.value
  }
  return meals.value.filter(meal => meal.type === selectedMealType.value)
})

// 方法
const getMealTypeName = (type) => {
  const typeMap = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐'
  }
  return typeMap[type] || type
}

const getMealTypeColor = (type) => {
  const colorMap = {
    breakfast: 'warning',
    lunch: 'success',
    dinner: 'info'
  }
  return colorMap[type] || 'default'
}

const formatTime = (date) => {
  return new Date(date).toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const filterMeals = () => {
  // 筛选逻辑已在计算属性中处理
}

const editMeal = (meal) => {
  editingMeal.value = meal
  mealForm.value = {
    type: meal.type,
    description: meal.description,
    calories: meal.calories,
    rating: meal.rating,
    images: meal.images.map((url, index) => ({
      name: `image-${index}.jpg`,
      url: url
    })),
    isPublic: meal.isPublic
  }
  showAddMealDialog.value = true
}

const deleteMeal = async (mealId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条饮食记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = meals.value.findIndex(meal => meal.id === mealId)
    if (index > -1) {
      meals.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消操作
  }
}

const toggleShare = (meal) => {
  ElMessage.success(meal.isPublic ? '已分享到社区' : '已取消分享')
}

const saveMeal = async () => {
  try {
    await mealFormRef.value.validate()
    
    const mealData = {
      ...mealForm.value,
      images: mealForm.value.images.map(img => img.url || img.raw),
      id: editingMeal.value ? editingMeal.value.id : Date.now(),
      createdAt: editingMeal.value ? editingMeal.value.createdAt : new Date()
    }
    
    if (editingMeal.value) {
      const index = meals.value.findIndex(meal => meal.id === editingMeal.value.id)
      if (index > -1) {
        meals.value[index] = mealData
      }
      ElMessage.success('更新成功')
    } else {
      meals.value.unshift(mealData)
      ElMessage.success('添加成功')
    }
    
    showAddMealDialog.value = false
    resetForm()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetForm = () => {
  mealForm.value = {
    type: '',
    description: '',
    calories: 0,
    rating: 5,
    images: [],
    isPublic: false
  }
  editingMeal.value = null
  mealFormRef.value?.resetFields()
}

const previewImage = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  showImagePreview.value = true
}

const handlePreview = (file) => {
  console.log('预览文件:', file)
}

const handleRemove = (file, fileList) => {
  console.log('移除文件:', file, fileList)
}

const handleChange = (file, fileList) => {
  console.log('文件变化:', file, fileList)
}
</script>

<style scoped>
.my-meals-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.add-meal-btn {
  height: 40px;
  padding: 0 20px;
}

.meal-filter {
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.meals-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.meals-grid {
  display: grid;
  gap: 0;
}

.meal-card {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  transition: background-color 0.2s;
}

.meal-card:hover {
  background-color: #f8fafc;
}

.meal-card:last-child {
  border-bottom: none;
}

.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.meal-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meal-time {
  color: #94a3b8;
  font-size: 12px;
}

.meal-actions {
  display: flex;
  gap: 4px;
}

.meal-content {
  margin-bottom: 16px;
}

.meal-description {
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.meal-images {
  margin-top: 12px;
}

.image-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.meal-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.meal-image:hover {
  transform: scale(1.05);
}

.meal-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.more-images {
  width: 80px;
  height: 80px;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}

.meal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.calories {
  color: #f59e0b;
  font-size: 12px;
  font-weight: 500;
}

.rating {
  display: flex;
  align-items: center;
}

.meal-share {
  display: flex;
  align-items: center;
}

.image-preview-container {
  text-align: center;
}

.preview-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
}

@media (max-width: 768px) {
  .my-meals-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .meal-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .meal-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .meal-stats {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>
