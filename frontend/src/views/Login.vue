<template>
  <div class="login-page">
    <section class="login-story">
      <span class="brand-kicker">OFFICE SUPPLY</span>
      <h1>办公用品采购</h1>
      <p class="lead">纸质申领单收进系统里。员工选品提交，审核人在线处理，通过后生成订单。</p>
      <div class="steps">
        <div class="step"><b>选品</b><span>从库存台账里勾选用品，写清申请理由。</span></div>
        <div class="step"><b>审批</b><span>审核人通过或驳回。驳回意见会回到申请人。</span></div>
        <div class="step"><b>跟单</b><span>通过后生成订单，管理员更新到货进度。</span></div>
      </div>
    </section>
    <section class="login-form-wrap">
      <div class="login-card">
        <div class="manila-tab">账号登录</div>
        <div class="panel">
          <el-form ref="form" :model="form" :rules="rules" label-position="top" @submit.native.prevent>
            <el-form-item label="账号" prop="username">
              <el-input v-model.trim="form.username" autocomplete="username" @keyup.enter.native="submit" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" autocomplete="current-password" show-password @keyup.enter.native="submit" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" style="width: 100%" @click="submit">登录</el-button>
            </el-form-item>
          </el-form>
        </div>
        <p class="demo-accounts">
          演示账号密码都是 123456。<br />
          管理员 admin，审核人员 audit01，员工 staff01。
        </p>
      </div>
    </section>
  </div>
</template>

<script>
import { login } from '../api'

/** 登录页。成功后把令牌和用户信息放进本地存储。 */
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    /** 校验表单后请求登录接口。 */
    submit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.form)
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('user', JSON.stringify(res.data))
          this.$router.push('/home')
        } catch (error) {
          /* 拦截器已提示 */
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>
