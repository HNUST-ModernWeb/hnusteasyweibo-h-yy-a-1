<template>
  <!-- 路由出口：用来显示 Login.vue、Home.vue 等页面 -->
  <router-view />
</template>

<script setup>
// 空的就行，不用写别的
</script>

<style>
/* 全局样式重置，去掉默认边距 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>