<template>
  <div class="app">
    <el-container>
      <!-- 顶部导航 - 桌面端 -->
      <el-header class="header desktop-only">
        <div class="header-content">
          <div class="logo">
            <el-icon size="24"><Home-Filled /></el-icon>
            <span>物品位置管理</span>
          </div>
          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            router
            class="nav-menu"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="items">
              <el-icon><Box /></el-icon>
              <span>物品列表</span>
            </el-menu-item>
            <el-menu-item index="/rooms">
              <el-icon><Office-Building /></el-icon>
              <span>房间管理</span>
            </el-menu-item>
            <el-menu-item index="/items/add">
              <el-icon><Plus /></el-icon>
              <span>添加物品</span>
            </el-menu-item>
            <el-menu-item index="/stats">
              <el-icon><TrendCharts /></el-icon>
              <span>统计</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>

      <!-- 底部导航 - 移动端 -->
      <div class="mobile-nav mobile-only">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          <el-icon size="24"><House /></el-icon>
          <span>首页</span>
        </router-link>
        <router-link to="/items" class="nav-item" :class="{ active: $route.path === '/items' || $route.path.startsWith('/items/') && $route.path !== '/items/add' }">
          <el-icon size="24"><Box /></el-icon>
          <span>物品</span>
        </router-link>
        <router-link to="/items/add" class="nav-item nav-item-center">
          <div class="add-btn">
            <el-icon size="28"><Plus /></el-icon>
          </div>
          <span>添加</span>
        </router-link>
        <router-link to="/rooms" class="nav-item" :class="{ active: $route.path === '/rooms' }">
          <el-icon size="24"><Office-Building /></el-icon>
          <span>房间</span>
        </router-link>
        <router-link to="/stats" class="nav-item" :class="{ active: $route.path === '/stats' }">
          <el-icon size="24"><TrendCharts /></el-icon>
          <span>统计</span>
        </router-link>
      </div>
    </el-container>
  </div>
</template>

<script setup>
// App 根组件
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
}

@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

.app {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 桌面端导航 */
.header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  font-weight: bold;
  color: #409eff;
  margin-right: 40px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

/* 主内容区 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 80px 20px 20px;
  min-height: 100vh;
}

/* 移动端底部导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  text-decoration: none;
  font-size: 0.75rem;
  gap: 2px;
  flex: 1;
  height: 100%;
  transition: color 0.3s;
}

.nav-item.active {
  color: #409eff;
}

.nav-item-center {
  position: relative;
  top: -15px;
}

.add-btn {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 显示/隐藏控制 */
.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

@media (max-width: 768px) {
  .desktop-only {
    display: none !important;
  }
  
  .mobile-only {
    display: flex !important;
  }
  
  .main-content {
    padding: 10px 10px 80px;
    margin-top: 0;
  }
  
  .el-header {
    display: none;
  }
}

/* 全局移动端优化 */
@media (max-width: 768px) {
  .el-card {
    margin-bottom: 10px;
  }
  
  .el-card__header {
    padding: 12px 15px;
    font-size: 0.9rem;
  }
  
  .el-card__body {
    padding: 15px;
  }
  
  .el-button {
    font-size: 0.875rem;
  }
  
  .el-button--large {
    padding: 12px 20px;
    font-size: 1rem;
  }
  
  .el-input__inner {
    height: 40px;
    font-size: 1rem;
  }
  
  .el-form-item__label {
    font-size: 0.875rem;
    padding-bottom: 4px;
  }
}
</style>
