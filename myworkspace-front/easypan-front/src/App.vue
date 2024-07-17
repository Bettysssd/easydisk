<template>
<!--  <router-view></router-view> 其实他的作用就相当于导入了/router/index.js-->
  <el-config-provider :locale="locale" :message="config">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { reactive } from "vue";
import zhCn from "element-plus/lib/locale/lang/zh-cn";  // 导入了一个中文的包
const locale = zhCn;
const config = reactive({
  max: 1,
});  // reactive 被用来创建一个名为 config 的响应式对象。
// 这个对象有一个属性 max，其初始值为 1。由于 config 是一个响应式对象，所以当 max 的值被修改时，任何依赖于 max 的视图或计算属性都会自动更新
</script>

<style lang="scss" scoped>
</style>
