const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    https: true,
    open: false,
    host: '0.0.0.0',
    port: 20086,
    proxy: `http://10.21.2.75:10086`
  }
})
