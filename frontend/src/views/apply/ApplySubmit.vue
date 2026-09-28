<template>
  <div class="panel">
    <el-form label-width="90px">
      <el-form-item label="申请理由">
        <el-input v-model.trim="applyReason" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="写清用途，例如部门季度补货" />
      </el-form-item>
      <el-form-item label="筛选">
        <el-input v-model.trim="keyword" placeholder="按名称或类别筛选" clearable style="width: 240px" />
      </el-form-item>
    </el-form>
    <el-table :data="filtered" v-loading="loading" border>
      <el-table-column prop="goodsName" label="商品名称" min-width="140" />
      <el-table-column prop="goodsType" label="类别" width="120" />
      <el-table-column prop="spec" label="规格" min-width="120" />
      <el-table-column prop="price" label="单价" width="90" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="采购数量" width="160">
        <template slot-scope="scope">
          <el-input-number v-model="scope.row.buyNum" :min="0" :max="100000" :step="1" size="small" />
        </template>
      </el-table-column>
      <template slot="empty"><div class="empty-hint">没有可申请的商品。</div></template>
    </el-table>
    <div style="margin-top: 16px; display: flex; justify-content: space-between; align-items: center">
      <span>已选 {{ selected.length }} 种，金额 {{ totalAmount }} 元。库存仅供参考，提交后不会自动扣减。</span>
      <el-button type="primary" :loading="saving" @click="submit">提交采购申请</el-button>
    </div>
  </div>
</template>

<script>
import { pageGoods, submitApply } from '../../api'

/** 员工选择商品并提交采购申请。数量为 0 的行不会提交。 */
export default {
  name: 'ApplySubmit',
  data() {
    return {
      loading: false,
      saving: false,
      keyword: '',
      applyReason: '',
      goodsList: []
    }
  },
  computed: {
    /** 按名称或类别在已加载的商品里筛选。 */
    filtered() {
      const text = this.keyword
      if (!text) return this.goodsList
      return this.goodsList.filter((item) => item.goodsName.includes(text) || item.goodsType.includes(text))
    },
    /** 数量大于 0 的商品才算选中。 */
    selected() {
      return this.goodsList.filter((item) => item.buyNum > 0)
    },
    totalAmount() {
      return this.selected.reduce((sum, item) => sum + Number(item.price) * item.buyNum, 0).toFixed(2)
    }
  },
  created() {
    this.load()
  },
  methods: {
    /** 加载可选商品。演示数据规模较小，一次取 500 条。 */
    async load() {
      this.loading = true
      try {
        const res = await pageGoods({ current: 1, size: 500 })
        this.goodsList = res.data.records.map((item) => ({ ...item, buyNum: 0 }))
      } catch (error) {
        this.goodsList = []
      } finally {
        this.loading = false
      }
    },
    /** 校验理由和选品后提交。 */
    async submit() {
      if (!this.applyReason) {
        this.$message.warning('请填写申请理由')
        return
      }
      if (this.selected.length === 0) {
        this.$message.warning('请至少选择一种商品并填写数量')
        return
      }
      this.saving = true
      try {
        await submitApply({
          applyReason: this.applyReason,
          items: this.selected.map((item) => ({ goodsId: item.goodsId, buyNum: item.buyNum }))
        })
        this.$message.success('采购申请已提交')
        this.$router.push('/apply/mine')
      } catch (error) {
        /* 拦截器已提示 */
      } finally {
        this.saving = false
      }
    }
  }
}
</script>
