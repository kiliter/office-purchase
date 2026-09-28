<template>
  <div class="panel" style="max-width: 460px">
    <el-form ref="form" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存新密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { changePassword } from '../../api'

/** 修改当前账号密码。成功后要求重新登录。 */
export default {
  name: 'Password',
  data() {
    const confirm = (rule, value, callback) => {
      if (value !== this.form.newPassword) callback(new Error('两次输入的新密码不一致'))
      else callback()
    }
    return {
      saving: false,
      form: { oldPassword: '', newPassword: '', confirmPassword: '' },
      rules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ required: true, min: 6, max: 20, message: '新密码长度需为 6 到 20 位', trigger: 'blur' }],
        confirmPassword: [{ required: true, validator: confirm, trigger: 'blur' }]
      }
    }
  },
  methods: {
    /** 提交新密码，成功后清除登录状态。 */
    save() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.saving = true
        try {
          await changePassword({ oldPassword: this.form.oldPassword, newPassword: this.form.newPassword })
          this.$message.success('密码已修改，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          this.$router.push('/login')
        } catch (error) {
          /* 拦截器已提示 */
        } finally {
          this.saving = false
        }
      })
    }
  }
}
</script>
