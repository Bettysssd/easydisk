import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  
  server: {
    port: 8080, //前端服务器启动的端口号
    hmr: true, // 开启热部署模式，自动更新
    proxy: {  // 代理，转发请求
      "/api": {
        target: "http://localhost:7090",  // 当设置为true时，代理服务器会修改请求头中的Host字段，将其值改为目标服务器的地址（即target字段所指定的地址）
        changeOrigin: true,  // 通过设置changeOrigin: true，前端代码可以绕过跨域限制，成功调用远程服务器上的API。
                              // 代理反向地址，
                             
        pathRewrite: { 
          "^api": "/api"  //  如果所有API请求都以/api为前缀，通过设置pathRewrite: {'^/api': ''}，
                          //可以将这些请求转发到目标服务器时去掉/api前缀。
        }
      }
    }
  }
})
