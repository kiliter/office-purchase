<template>
  <div class="panel">
    <div class="filters">
      <el-input v-model.trim="query.goodsName" placeholder="商品名称" clearable @keyup.enter.native="search" />
      <el-input v-model.trim="query.goodsType" placeholder="商品类别" clearable @keyup.enter.native="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button v-if="canEdit" @click="openCreate">新增商品</el-button>
    </div>
    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="goodsName" label="商品名称" min-width="140" />
      <el-table-column prop="goodsType" label="类别" width="120" />
      <el-table-column prop="spec" label="规格" min-width="140" />
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column v-if="canEdit" label="操作" width="150">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">修改</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template slot="empty"><div class="empty-hint">没有符合条件的商品。</div></template>
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="total, prev, pager, next"
      :current-page="query.current"
      :page-size="query.size"
      :total="total"
      @current-change="changePage"
    />

    <el-dialog :title="form.goodsId ? '修改商品' : '新增商品'" :visible.sync="visible" width="480px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="goodsName"><el-input v-model.trim="form.goodsName" /></el-form-item>
        <el-form-item label="类别" prop="goodsType"><el-input v-model.trim="form.goodsType" /></el-form-item>
        <el-form-item label="规格"><el-input v-model.trim="form.spec" /></el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { createGoods, deleteGoods, pageGoods, updateGoods } from '../../api'

/** 商品台账。管理员可维护，其他角色只读。库存由管理员手工调整。 */
export default {
  name: 'GoodsList',
  data() {
    return {
      loading: false,
      saving: false,
      visible: false,
      total: 0,
      records: [],
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      query: { current: 1, size: 10, goodsName: '', goodsType: '' },
      form: this.emptyForm(),
      rules: {
        goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        goodsType: [{ required: true, message: '请输入商品类别', trigger: 'blur' }],
        price: [{ required: true, message: '请输入单价', trigger: 'change' }],
        stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
      }
    }
  },
  computed: {
    canEdit() {
      return this.user.role === 'admin'
    }
  },
  created() {
    this.load()
  },
  methods: {
    /** 空白商品表单。 */
    emptyForm() {
      return { goodsId: null, goodsName: '', goodsType: '', spec: '', price: 0, stock: 0 }
    },
    /** 按当前条件重新查询商品。 */
    search() {
      this.query.current = 1
      this.load()
    },
    /** 翻页。 */
    changePage(page) {
      this.query.current = page
      this.load()
    },
    /** 请求商品分页。 */
    async load() {
      this.loading = true
      try {
        const res = await pageGoods(this.query)
        this.records = res.data.records
        this.total = res.data.total
      } catch (error) {
        this.records = []
      } finally {
        this.loading = false
      }
    },
    /** 打开新增窗口。 */
    openCreate() {
      this.form = this.emptyForm()
      this.visible = true
    },
    /** 打开修改窗口并回填当前行。 */
    openEdit(row) {
      this.form = { ...row }
      this.visible = true
    },
    /** 新增或修改商品。 */
    save() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.saving = true
        try {
          if (this.form.goodsId) {
            await updateGoods(this.form)
          } else {
            await createGoods(this.form)
          }
          this.$message.success('商品已保存')
          this.visible = false
          this.load()
        } catch (error) {
          /* 拦截器已提示 */
        } finally {
          this.saving = false
        }
      })
    },
    /** 删除未被申请引用的商品。 */
    remove(row) {
      this.$confirm('删除后不能恢复。已被申请引用的商品会保存失败。', '删除商品', { type: 'warning' })
        .then(async () => {
          await deleteGoods(row.goodsId)
          this.$message.success('商品已删除')
          this.load()
        })
        .catch(() => {})
    }
  }
}
</script>
