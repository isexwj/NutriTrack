<template>
  <div class="community-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">社区记录</h2>
        <p class="page-subtitle">发现更多健康饮食灵感，与社区分享您的健康生活</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <div class="stat-number">{{ totalPosts }}</div>
          <div class="stat-label">总记录</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ todayPosts }}</div>
          <div class="stat-label">今日分享</div>
        </div>
      </div>
    </div>

    <!-- 筛选和排序 -->
    <div class="filter-section">
      <div class="filter-left">
        <el-radio-group v-model="selectedMealType" @change="filterPosts">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="breakfast">早餐</el-radio-button>
          <el-radio-button label="lunch">午餐</el-radio-button>
          <el-radio-button label="dinner">晚餐</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-right">
        <el-select v-model="sortBy" placeholder="排序方式" @change="sortPosts">
          <el-option label="最新发布" value="latest" />
          <el-option label="最多点赞" value="likes" />
          <el-option label="最多评论" value="comments" />
        </el-select>
      </div>
    </div>

    <!-- 社区记录列表 -->
    <div class="community-posts">
      <div v-if="filteredPosts.length === 0" class="empty-state">
        <el-empty description="暂无社区记录" :image-size="120" />
      </div>
      
      <div v-else class="posts-list">
        <div 
          v-for="post in filteredPosts" 
          :key="post.id"
          class="post-card"
        >
          <!-- 用户信息 -->
          <div class="post-header">
            <div class="user-info">
              <el-avatar :size="40" class="user-avatar">
                {{ post.user.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ post.user.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>
            <div class="meal-type">
              <el-tag :type="getMealTypeColor(post.type)" size="small">
                {{ getMealTypeName(post.type) }}
              </el-tag>
            </div>
          </div>
          
          <!-- 内容区域 -->
          <div class="post-content">
            <div class="post-description">{{ post.description }}</div>
            <!-- 替换原来的 post-images 部分 -->
            <div class="post-images">
              <div class="image-grid">
                <div
                    v-for="(image, index) in post.images.slice(0, 3)"
                    :key="index"
                    class="meal-image"
                    @click="previewImage(post.images, index)"
                >
                  <img :src="image" :alt="`餐食图片${index + 1}`" />
                </div>
                <div v-if="post.images.length > 3" class="more-images">
                  +{{ post.images.length - 3 }}
                </div>
              </div>
            </div>

          </div>
          
          <!-- 统计信息 -->
          <div class="post-stats">
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ post.calories }} 卡路里</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ post.rating }} 分</span>
            </div>
          </div>
          
          <!-- 互动区域 -->
          <div class="post-actions">
            <div class="action-buttons">
              <el-button 
                type="text" 
                :class="{ liked: post.isLiked }"
                @click="toggleLike(post)"
              >
                <el-icon><Star /></el-icon>
                {{ post.likes }}
              </el-button>
              <el-button type="text" @click="toggleComments(post)">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.comments.length }}
              </el-button>
              <el-button type="text" @click="sharePost(post)">
                <el-icon><Share /></el-icon>
                分享
              </el-button>
            </div>
          </div>
          
          <!-- 评论区域 -->
          <div v-if="post.showComments" class="comments-section">
            <div class="comments-list">
              <div 
                v-for="comment in post.comments" 
                :key="comment.id"
                class="comment-item"
              >
                <el-avatar :size="24" class="comment-avatar">
                  {{ comment.user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-user">{{ comment.user.username }}</span>
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                </div>
              </div>
            </div>
            <div class="comment-input">
              <el-input
                v-model="post.newComment"
                placeholder="写下您的评论..."
                @keyup.enter="addComment(post)"
              >
                <template #append>
                  <el-button @click="addComment(post)">发送</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </div>

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

    <!-- 分享对话框 -->
    <el-dialog v-model="showShareDialog" title="分享记录" width="400px">
      <div class="share-content">
        <div class="share-preview">
          <div class="preview-card">
            <div class="preview-header">
              <el-avatar :size="32">
                {{ sharingPost.user.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="preview-info">
                <div class="preview-username">{{ sharingPost.user.username }}</div>
                <div class="preview-time">{{ formatTime(sharingPost.createdAt) }}</div>
              </div>
            </div>
            <div class="preview-description">{{ sharingPost.description }}</div>
          </div>
        </div>
        <div class="share-options">
          <el-button type="primary" @click="copyLink">
            <el-icon><Link /></el-icon>
            复制链接
          </el-button>
          <el-button @click="shareToWechat">
            <el-icon><Share /></el-icon>
            分享到微信
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, ChatDotRound, Share, Link } from '@element-plus/icons-vue'
import * as communityApi from '@/api/community'

// 响应式数据
const selectedMealType = ref('all')
const sortBy = ref('latest')
const showImagePreview = ref(false)
const showShareDialog = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const sharingPost = ref({})
const posts = ref([])
const loading = ref(false)


// 计算属性
const totalPosts = computed(() => posts.value.length)
const todayPosts = computed(() => {
  const today = new Date().toDateString()
  return posts.value.filter(post => 
    new Date(post.createdAt).toDateString() === today
  ).length
})

const filteredPosts = computed(() => {
  let filtered = posts.value
  
  // 按餐次筛选
  if (selectedMealType.value !== 'all') {
    filtered = filtered.filter(post => post.type === selectedMealType.value)
  }
  
  // 排序
  switch (sortBy.value) {
    case 'likes':
      filtered = [...filtered].sort((a, b) => b.likes - a.likes)
      break
    case 'comments':
      filtered = [...filtered].sort((a, b) => b.comments.length - a.comments.length)
      break
    case 'latest':
    default:
      filtered = [...filtered].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      break
  }
  
  return filtered
})

// 方法
const fetchCommunityRecords = async () => {
  try {
    loading.value = true;
    const response = await communityApi.getCommunityRecords(localStorage.getItem('username'));
    posts.value = response.data.map(post => ({
      ...post,
      // 拼接完整图片URL
      images: post.images ? post.images.map(img => getFullImageUrl(img)) : [],
      showComments: false,
      newComment: ''
    }));
  } catch (error) {
    console.error('获取社区记录失败:', error);
    ElMessage.error('获取社区记录失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 点赞
const likeRecord = async (recordId, username) => {
  try {
    await communityApi.likeRecord(recordId, username);
    return true;
  } catch (error) {
    console.error('点赞失败:', error);
    return false;
  }
};

// 取消点赞
const unlikeRecord = async (recordId, username) => {
  try {
    await communityApi.unlikeRecord(recordId, username);
    return true;
  } catch (error) {
    console.error('点赞失败:', error);
    return false;
  }
};

// 确保函数被调用 - 在onMounted或需要的地方调用
onMounted(() => {
  fetchCommunityRecords();
});


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

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080/images/${url}`
}

const formatTime = (date) => {
  const now = new Date()
  const postDate = new Date(date)
  const diff = now - postDate
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return postDate.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const filterPosts = () => {
  // 筛选逻辑已在计算属性中处理
}

const sortPosts = () => {
  // 排序逻辑已在计算属性中处理
}

const toggleLike = (post) => {
  if (post.isLiked) {
    post.likes--
    post.isLiked = false
    unlikeRecord(post.id,localStorage.getItem('username'))
    ElMessage.success('取消点赞')
  } else {
    post.likes++
    post.isLiked = true
    likeRecord(post.id,localStorage.getItem('username'))
    ElMessage.success('点赞成功')
  }
}

const toggleComments = (post) => {
  post.showComments = !post.showComments
}

const addComment = async (post) => {
  if (!post.newComment.trim()) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  try {
    // 从本地存储获取用户名
    const username = localStorage.getItem('username') || '当前用户';

    // 调用后端API发表评论
    const response = await communityApi.createComment(
        post.id,           // recordId: 当前记录的ID
        username,          // username: 评论者用户名
        post.newComment,   // content: 评论内容
        null               // parentCommentId: 可选参数，这里设为null
    );

    if (response.code === 200) {
      // 使用后端返回的真实评论数据
      const newComment = {
        id: response.data.id,           // 使用后端生成的ID
        user: response.data.user,       // 使用后端返回的用户信息
        content: response.data.content, // 使用后端处理后的内容
        createdAt: new Date() // 使用后端的时间戳
      };

      // 添加到评论列表
      post.comments.push(newComment);
      post.newComment = '';
      ElMessage.success('评论成功');

    } else {
      ElMessage.error(response.message || '评论失败');
    }
  } catch (error) {
    console.error('发表评论失败:', error);
    ElMessage.error('发表评论失败，请稍后重试');
  }
};

const sharePost = (post) => {
  sharingPost.value = post
  showShareDialog.value = true
}

const copyLink = () => {
  navigator.clipboard.writeText(`${window.location.origin}/community/post/${sharingPost.value.id}`)
  ElMessage.success('链接已复制到剪贴板')
  showShareDialog.value = false
}

const shareToWechat = () => {
  ElMessage.info('分享到微信功能开发中...')
  showShareDialog.value = false
}

const previewImage = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  showImagePreview.value = true
}
</script>

<style scoped>
.community-container {
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

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  color: #3b82f6;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  color: #64748b;
  font-size: 12px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.community-posts {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.posts-list {
  display: flex;
  flex-direction: column;
}

.post-card {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  transition: background-color 0.2s;
}

.post-card:hover {
  background-color: #f8fafc;
}

.post-card:last-child {
  border-bottom: none;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: #3b82f6;
  color: white;
  font-weight: 600;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

.post-time {
  color: #94a3b8;
  font-size: 12px;
}

.post-content {
  margin-bottom: 16px;
}

.post-description {
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-images {
  margin-top: 12px;
}

/* 替换原来的 .post-images 和相关样式 */
.post-images {
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

/*.image-grid {
  display: grid;
  gap: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.image-grid.grid-1 {
  grid-template-columns: 1fr;
}

.image-grid.grid-2 {
  grid-template-columns: 1fr 1fr;
}

.image-grid.grid-3 {
  grid-template-columns: 1fr 1fr 1fr;
}

.image-grid.grid-4 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}

.post-image {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s;
  overflow: hidden;
  border-radius: 8px;
}

.post-image:hover {
  transform: scale(1.02);
}

.post-image img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.more-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 600;
}*/

.post-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.post-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 12px;
}

.post-actions {
  border-top: 1px solid #f1f5f9;
  padding-top: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.action-buttons .el-button {
  color: #64748b;
  font-size: 13px;
}

.action-buttons .el-button.liked {
  color: #f59e0b;
}

.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.comments-list {
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-avatar {
  background: #e2e8f0;
  color: #64748b;
  font-size: 10px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-user {
  color: #1e293b;
  font-size: 12px;
  font-weight: 600;
}

.comment-time {
  color: #94a3b8;
  font-size: 11px;
}

.comment-text {
  color: #374151;
  font-size: 13px;
  line-height: 1.4;
}

.comment-input {
  margin-top: 12px;
}

.image-preview-container {
  text-align: center;
}

.preview-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
}

.share-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.share-preview {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.preview-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.preview-info {
  display: flex;
  flex-direction: column;
}

.preview-username {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

.preview-time {
  color: #94a3b8;
  font-size: 12px;
}

.preview-description {
  color: #374151;
  font-size: 13px;
  line-height: 1.4;
}

.share-options {
  display: flex;
  gap: 12px;
  justify-content: center;
}

@media (max-width: 768px) {
  .community-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-stats {
    justify-content: space-around;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .filter-right {
    justify-content: center;
  }
  
  .post-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .action-buttons {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .share-options {
    flex-direction: column;
  }
}
</style>
