<template>
  <div id="h5-frs">
    <div class="row">
      <video id="camera" :style="cameraStyle()"></video>
    </div>
    <div>
      <div id="action" class="row">动作：{{ actions[actionIdx].name }}</div>
      <div id="faceNum" class="row">人脸数：{{ actions[actionIdx].faceNum }}</div>
      <div id="faceProbability" class="row">置信度：{{ actions[actionIdx].faceProbability }}</div>
      <div id="score" class="row">匹配得分：{{ actions[actionIdx].score }}</div>
      <div id="retry" class="row">重试：{{ retry }}</div>
      <div id="msg" class="row">消息：{{ msg }}</div>
    </div>
    <div class="row">
      <canvas id="picture"></canvas>
    </div>
  </div>
</template>

<script>
import http from '@/util/http'

// 人脸动作模板
const cactions = [
  {
    // 动作KEY
    key: 'front',
    // 动作名称
    name: '正视',
    // 图片中的人脸数量
    faceNum: 0,
    // 人脸置信度
    faceProbability: 0,
    // 匹配得分
    score: 0,
    // 人脸标识
    faceToken: null,
    // 人脸识别授权码。人脸识别成功后，后台返回一个临时授权码。
    // 授权码可以作为跳转URL的参数，业务系统根据此授权码登录。
    frsCode: null,
    // 动作启用/禁用
    status: 1,
  },
  {
    key: 'side',
    name: '摇头',
    faceNum: 0,
    faceProbability: 0,
    score: 0,
    faceToken: null,
    frsCode: null,
    status: 0,
  }
]
export default {
  name: "H5Frs",
  data() {
    return {
      // 业务系统参数
      // 页面标识。用于区分当前是登录还是注册。
      bpage: null,
      // 跳转URL。人脸识别完成后根据此URL跳转到业务系统。
      burl: null,
      // 鉴权TOKEN。人脸注册时，用户应当处于已登入业务系统的状态，注册相关的接口应当附带此鉴权TOKEN。
      btoken: null,
      // 用户标识。人脸注册时用户标识作为人脸绑定的主体。
      buser: null,

      // 人脸接口
      // 接口授权码。人脸接口可能会产生付费流量，可以使用自定义的接口授权码避免受到恶意攻击。
      authCode: null,
      // 检测人脸
      detectFaceApi: '/frs/bd/detectFace',
      // 注册人脸
      addFaceApi: '/frs/bd/addFace',
      // 识别人脸
      searchFaceApi: '/frs/bd/searchFace',

      // 识别配置
      // 当前重试次数
      retry: 0,
      // 最大重试次数
      retryMax: 30,
      // 当前动作
      actionIdx: 0,
      // 所有动作
      actions: [],

      // 其它
      captureInterval: 1000,
      pictureWidth: null,
      pictureHeight: null,
      msg: null,
    }
  },
  async created() {
    // 初始化参数
    // 业务系统参数
    let searchObj = this.searchObj()
    this.bpage = searchObj.bpage
    this.burl = searchObj.burl
    this.btoken = searchObj.btoken
    this.buser = searchObj.buser
    // 人脸接口
    this.authCode = searchObj.authCode
    // 识别配置
    this.retry = 0
    this.actionIdx = 0
    this.actions = JSON.parse(JSON.stringify(cactions))
    // 打开相机
    await this.camera()
    // 截图
    this.capture(1)
  },
  methods: {
    /**
     * 解析URL参数
     */
    searchObj() {
      let search = window.location.search.substring(1) || 'testk=testv'
      return JSON.parse(`{"${decodeURI(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')}"}`)
    },
    /**
     * 打开相机
     */
    async camera() {
      let res = await navigator.mediaDevices.getUserMedia({video: true})
      let videoElement = document.getElementById('camera')
      videoElement.srcObject = res
      await videoElement.play()
    },
    /**
     * 调整相机样式
     */
    cameraStyle() {
      let style = {}
      // 镜像
      style.transform = `rotateY(180deg) scale(0.9)`
      // 边框
      let faceToken = this.actions[this.actionIdx].faceToken
      style.border = `5px solid`
      style.borderColor = `hsl(${faceToken ? 120 : 0}, 100%, 50%)`
      // 透明度
      let pictureWidth = this.pictureWidth
      let pictureHeight = this.pictureHeight
      style.opacity = pictureWidth ? 1 : 0
      // 大小
      if (pictureWidth && pictureHeight) {
        let pictureRatio = pictureWidth / pictureHeight
        let screenWidth = screen.width
        let screenHeight = screen.height
        let cameraWidth = screenWidth
        let cameraHeight = cameraWidth / pictureRatio
        if (cameraHeight > screenHeight) {
          cameraHeight = screenHeight
          cameraWidth = cameraHeight * pictureRatio
        }
        style.width = `${cameraWidth}px`
        style.height = `${cameraHeight}px`
      }
      return style
    },
    /**
     * 截图
     */
    capture(interval) {
      setTimeout(() => {
        let picture = document.getElementById('picture')
        let camera = document.getElementById('camera')
        picture.width = camera.videoWidth
        picture.height = camera.videoHeight
        this.pictureWidth = picture.width
        this.pictureHeight = picture.height
        picture.getContext('2d').drawImage(camera, 0, 0, picture.width, picture.height)
        // 图片转base64
        let base64 = picture.toDataURL().replace('data:image/png;base64,', '')
        // 调用人脸服务
        this.server(base64)
      }, interval || this.captureInterval)
    },
    /**
     * 人脸服务
     */
    server(base64) {
      // 人脸注册
      if (this.bpage === 'register') {
        this.register(base64)
      }
      // 人脸登录
      else if (this.bpage === 'login') {
        this.login(base64)
      }
    },

    /**
     * 人脸注册
     */
    async register(base64) {
      // 重试次数限制
      if (++this.retry > this.retryMax) {
        this.msg = '检测失败请返回重试'
        return
      }
      // 检测人脸
      this.msg = '检测人脸'
      let res = await http({
        url: this.detectFaceApi,
        method: 'POST',
        data: {
          action: this.actions[this.actionIdx].key,
          image: base64,
          width: this.pictureWidth,
          height: this.pictureHeight
        },
        headers: {
          'AuthCode': this.authCode
        }
      })
      res = res.data
      console.log(`注册>检测人脸`, res)
      if (res.code !== 200) {
        this.msg = res.msg
        this.capture()
        return
      }
      let data = res.data
      this.actions[this.actionIdx].faceNum = data.face_num
      let faceList = data.face_list
      if (faceList && faceList.length > 0) {
        this.actions[this.actionIdx].faceProbability = faceList[0].face_probability
        this.actions[this.actionIdx].faceToken = faceList[0].face_token
      }
      // 没有faceToken则不合格
      if (!this.actions[this.actionIdx].faceToken) {
        this.msg = '检测未通过'
        this.capture()
        return
      }

      // 动作组未完成，继续下一个动作。
      let factions = this.actions.filter(ele => ele.faceToken !== null)
      let tactions = this.actions.filter(ele => ele.status === 1)
      if (factions.length < tactions.length) {
        this.actionIdx = (this.actionIdx + 1) % this.actions.length
        this.retry = 0
        this.capture()
        return
      }

      // 注册人脸
      this.msg = '注册人脸'
      res = await http({
        url: this.addFaceApi,
        method: 'POST',
        data: {
          userId: this.buser,
          faceTokens: factions.map(ele => ele.faceToken)
        },
        headers: {
          'AuthCode': this.authCode,
          'Authorization': `Bearer ${this.btoken}`
        }
      })
      res = res.data
      console.log(`注册>注册人脸`, res)
      if (res.code !== 200) {
        this.msg = res.msg
        return
      }
      this.msg = '注册成功'
    },

    /**
     * 人脸登录
     */
    async login(base64) {
      // 重试次数限制
      if (++this.retry > this.retryMax) {
        this.msg = '识别失败请返回重试'
        return
      }
      // 检测人脸
      this.msg = '检测人脸'
      let res = await http({
        url: this.detectFaceApi,
        method: 'POST',
        data: {
          action: this.actions[this.actionIdx].key,
          image: base64,
          width: this.pictureWidth,
          height: this.pictureHeight
        },
        headers: {
          'AuthCode': this.authCode
        }
      })
      res = res.data
      console.log(`登录>检测人脸`, res)
      if (res.code !== 200) {
        this.msg = res.msg
        this.capture()
        return
      }
      let data = res.data
      this.actions[this.actionIdx].faceNum = data.face_num
      let faceList = data.face_list
      if (faceList && faceList.length > 0) {
        this.actions[this.actionIdx].faceProbability = faceList[0].face_probability
        this.actions[this.actionIdx].faceToken = faceList[0].face_token
      }
      // 没有faceToken则不合格
      if (!this.actions[this.actionIdx].faceToken) {
        this.msg = '检测未通过'
        this.capture()
        return
      }
      // 搜索人脸
      this.msg = '搜索人脸'
      res = await http({
        url: this.searchFaceApi,
        method: 'POST',
        data: {
          faceToken: this.actions[this.actionIdx].faceToken
        },
        headers: {
          'AuthCode': this.authCode
        }
      })
      res = res.data
      console.log(`登录>搜索人脸`, res)
      if (res.code === 417) {
        this.retry += 5
        this.msg = res.msg
        this.capture()
        return
      }
      if (res.code !== 200) {
        this.msg = res.msg
        this.capture()
        return
      }
      // 搜索成功则保存临时授权码
      this.actions[this.actionIdx].frsCode = res.data.frsCode
      this.actions[this.actionIdx].score = res.data.score
      this.msg = '识别成功'
      this.back()
    },
    // 跳转业务系统
    back() {
      if (this.burl) {
        let url = new URL(decodeURIComponent(this.burl))
        url.searchParams.set('frs', this.bpage)
        if (this.bpage === 'login') {
          url.searchParams.set('frsCode', this.actions[this.actionIdx].frsCode)
        }
        window.location.href = url.toString()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.row {
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-content: center;
}

#camera {
  width: 360px;
  height: 640px;
}

#picture {
  display: none;
  transform: rotateY(180deg);
}
</style>