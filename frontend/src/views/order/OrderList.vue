<template>
  <div class="panel">
    <p v-if="user.role === 'staff'" style="margin-top: 0">只显示由你的申请生成的订单。</p>
    <div class="filters">
      <el-input v-model.trim="query.keyword" placeholder="申请人或申请理由" clearable @keyup.enter.native="search" />
      <el-select v-model="query.orderStatus" placeholder="订单状态" clearable>
        <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="orderId" label="订单号" width="90" />
      <el-table-column prop="applyUserName" label="申请人" width="110" />
      <el-table-column prop="goodsSummary" label="明细" min-width="200" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="金额" width="100" />
      <el-table-column label="状态" width="110">
        <template slot-scope="scope"><Stamp :text="scope.row.orderStatus" /></template>
      </el-table-column>
      <el-table-column prop="createTime" label="生成时间" width="170" />
      <el-table-column v-if="canUpdate" label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="openStatus(scope.row)">更新状态</el-button>
        </template>
      </el-table-column>
      <template slot="empty"><div class="empty-hint">还没有采购订单。申请通过后才会生成。</div></template>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :current-page="query.current" :page-size="query.size" :total="total" @current-change="changePage" />

    <el-dialog title="更新订单状态" :visible.sync="visible" width="420px">
      <el-select v-model="statusForm.orderStatus" placeholder="选择状态" style="width: 100%">
        <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
      </el-select>
      <span slot="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStatus">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Stamp from '../../components/Stamp.vue'
import { pageOrders, updateOrderStatus } from '../../api'

/** 订单列表。管理员可以更新履约状态，员工只能看自己的订单。 */
export default {
  name: 'OrderList',
  components: { Stamp },
  data() {
    return {
      loading: false,
      saving: false,
      visible: false,
      total: 0,
      records: [],
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      statuses: ['待采购', '采购中', '已到货', '已完成'],
      query: { current: 1, size: 10, keyword: '', orderStatus: '' },
      statusForm: { orderId: null, orderStatus: '' }
    }
  },
  computed: {
    canUpdate() {
      return this.user.role === 'admin'
    }
  },
  created() {
    this.load()
  },
  methods: {
    search() {
      this.query.current = 1
      this.load()
    },
    changePage(page) {
      this.query.current = page
      this.load()
    },
    /** 读取订单分页。 */
    async load() {
      this.loading = true
      try {
        const res = await pageOrders(this.query)
        this.records = res.data.records
        this.total = res.data.total
      } catch (error) {
        this.records = []
      } finally {
        this.loading = false
      }
    },
    /** 打开状态修改窗口。 */
    openStatus(row) {
      this.statusForm = { orderId: row.orderId, orderStatus: row.orderStatus }
      this.visible = true
    },
    /** 保存订单状态。 */
    async saveStatus() {
      this.saving = true
      try {
        await updateOrderStatus(this.statusForm)
        this.$message.success('订单状态已更新')
        this.visible = false
        this.load()
      } catch (error) {
        /* 拦截器已提示 */
      } finally {
        this.saving = false
      }
    }
  }
}
</script>
