<template>
  <div class="panel">
    <div class="filters">
      <el-input v-model.trim="query.username" placeholder="账号" clearable @keyup.enter.native="search" />
      <el-input v-model.trim="query.realName" placeholder="姓名" clearable @keyup.enter.native="search" />
      <el-select v-model="query.role" placeholder="角色" clearable>
        <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="openCreate">新增用户</el-button>
    </div>
    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column label="角色" width="120">
        <template slot-scope="scope">{{ roleLabel(scope.row.role) }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="180">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">修改</el-button>
          <el-button type="text" @click="reset(scope.row)">重置密码</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template slot="empty"><div class="empty-hint">没有符合条件的用户。</div></template>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :current-page="query.current" :page-size="query.size" :total="total" @current-change="changePage" />

    <el-dialog :title="form.userId ? '修改用户' : '新增用户'" :visible.sync="visible" width="480px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="密码" prop="password">
          <el-input v-model="form.password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model.trim="form.realName" /></el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model.trim="form.phone" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { ROLE_LABEL } from '../../utils/dict'
import { createUser, deleteUser, pageUsers, resetPassword, updateUser } from '../../api'

/** 管理员维护账号、角色和初始密码。 */
export default {
  name: 'UserList',
  data() {
    return {
      loading: false,
      saving: false,
      visible: false,
      total: 0,
      records: [],
      roles: [
        { label: '管理员', value: 'admin' },
        { label: '审核人员', value: 'audit' },
        { label: '普通员工', value: 'staff' }
      ],
      query: { current: 1, size: 10, username: '', realName: '', role: '' },
      form: this.emptyForm(),
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    roleLabel(role) {
      return ROLE_LABEL[role] || role
    },
    emptyForm() {
      return { userId: null, username: '', password: '123456', realName: '', role: 'staff', phone: '' }
    },
    search() {
      this.query.current = 1
      this.load()
    },
    changePage(page) {
      this.query.current = page
      this.load()
    },
    /** 读取用户分页。 */
    async load() {
      this.loading = true
      try {
        const res = await pageUsers(this.query)
        this.records = res.data.records
        this.total = res.data.total
      } catch (error) {
        this.records = []
      } finally {
        this.loading = false
      }
    },
    openCreate() {
      this.form = this.emptyForm()
      this.visible = true
    },
    openEdit(row) {
      this.form = { userId: row.userId, username: row.username, realName: row.realName, role: row.role, phone: row.phone || '' }
      this.visible = true
    },
    /** 新增或修改用户。 */
    save() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.saving = true
        try {
          if (this.form.userId) {
            await updateUser(this.form)
          } else {
            await createUser(this.form)
          }
          this.$message.success('用户已保存')
          this.visible = false
          this.load()
        } catch (error) {
          /* 拦截器已提示 */
        } finally {
          this.saving = false
        }
      })
    },
    /** 把密码重置为 123456。 */
    reset(row) {
      this.$confirm('将把 ' + row.realName + ' 的密码重置为 123456。', '重置密码', { type: 'warning' })
        .then(async () => {
          await resetPassword(row.userId)
          this.$message.success('密码已重置为 123456')
        })
        .catch(() => {})
    },
    /** 删除没有单据的用户。 */
    remove(row) {
      this.$confirm('删除后不能恢复。已有申请或审批记录的用户不能删除。', '删除用户', { type: 'warning' })
        .then(async () => {
          await deleteUser(row.userId)
          this.$message.success('用户已删除')
          this.load()
        })
        .catch(() => {})
    }
  }
}
</script>
