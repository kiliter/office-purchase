<template>
  <div class="panel">
    <div class="filters">
      <el-input v-model.trim="query.keyword" placeholder="理由或申请人" clearable @keyup.enter.native="search" />
      <el-select v-if="mode !== 'audit'" v-model="query.auditStatus" placeholder="审批状态" clearable>
        <el-option label="待审批" value="待审批" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已驳回" value="已驳回" />
      </el-select>
      <el-date-picker
        v-model="range"
        type="daterange"
        value-format="yyyy-MM-dd"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="applyTime" label="申请时间" width="170" />
      <el-table-column v-if="mode !== 'mine'" prop="applyUserName" label="申请人" width="110" />
      <el-table-column prop="applyReason" label="申请理由" min-width="160" show-overflow-tooltip />
      <el-table-column prop="goodsSummary" label="明细" min-width="180" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="金额" width="100" />
      <el-table-column label="状态" width="110">
        <template slot-scope="scope"><Stamp :text="scope.row.auditStatus" /></template>
      </el-table-column>
      <el-table-column prop="auditOpinion" label="审批意见" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button type="text" @click="openDetail(scope.row)">{{ mode === 'audit' ? '审批' : '查看' }}</el-button>
          <el-button v-if="mode === 'mine' && scope.row.auditStatus === '已驳回'" type="text" @click="openResubmit(scope.row)">重新提交</el-button>
        </template>
      </el-table-column>
      <template slot="empty"><div class="empty-hint">{{ emptyText }}</div></template>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :current-page="query.current" :page-size="query.size" :total="total" @current-change="changePage" />

    <el-dialog title="申请详情" :visible.sync="detailVisible" width="680px">
      <div v-if="detail">
        <p>申请人：{{ detail.applyUserName }}</p>
        <p>申请时间：{{ detail.applyTime }}</p>
        <p>申请理由：{{ detail.applyReason }}</p>
        <p>状态：<Stamp :text="detail.auditStatus" /></p>
        <p v-if="detail.auditOpinion">审批意见：{{ detail.auditOpinion }} <span v-if="detail.auditUserName">（{{ detail.auditUserName }} {{ detail.auditTime }}）</span></p>
        <el-table :data="detail.items" border>
          <el-table-column prop="goodsName" label="商品" />
          <el-table-column prop="spec" label="规格" />
          <el-table-column prop="price" label="单价" width="90" />
          <el-table-column prop="buyNum" label="数量" width="80" />
          <el-table-column prop="amount" label="金额" width="100" />
        </el-table>
        <p>合计 {{ detail.totalAmount }} 元</p>
        <el-input v-if="mode === 'audit'" v-model.trim="opinion" type="textarea" :rows="3" maxlength="500" placeholder="驳回时必须填写意见；通过时可不填，默认记为同意" />
      </div>
      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="mode === 'audit'">
          <el-button type="danger" :loading="acting" @click="audit('已驳回')">驳回</el-button>
          <el-button type="primary" :loading="acting" @click="audit('已通过')">通过</el-button>
        </template>
      </span>
    </el-dialog>

    <el-dialog title="重新提交" :visible.sync="editVisible" width="720px">
      <el-input v-model.trim="editReason" type="textarea" :rows="3" maxlength="500" placeholder="申请理由" />
      <el-table :data="editGoods" border style="margin-top: 12px">
        <el-table-column prop="goodsName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="90" />
        <el-table-column label="采购数量" width="160">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.buyNum" :min="0" :max="100000" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="resubmit">重新提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Stamp from '../../components/Stamp.vue'
import { applyDetail, auditApply, pageApplies, pageGoods, resubmitApply } from '../../api'

/** 申请列表。mine 只看自己，audit 只看待审批，all 查看全部。 */
export default {
  name: 'ApplyTable',
  components: { Stamp },
  data() {
    return {
      loading: false,
      acting: false,
      total: 0,
      records: [],
      range: [],
      query: { current: 1, size: 10, keyword: '', auditStatus: '' },
      detailVisible: false,
      detail: null,
      opinion: '',
      editVisible: false,
      editId: null,
      editReason: '',
      editGoods: []
    }
  },
  computed: {
    mode() {
      return this.$route.meta.mode || 'all'
    },
    emptyText() {
      if (this.mode === 'audit') return '没有待审批的申请。'
      if (this.mode === 'mine') return '还没有采购申请。需要用品时，去提交申请。'
      return '没有符合条件的申请。'
    }
  },
  watch: {
    '$route.fullPath'() {
      this.query = { current: 1, size: 10, keyword: '', auditStatus: '' }
      this.range = []
      this.load()
    }
  },
  created() {
    this.load()
  },
  methods: {
    /** 组装查询参数。待审批页固定状态。 */
    params() {
      const params = { ...this.query }
      if (this.mode === 'audit') params.auditStatus = '待审批'
      if (this.range && this.range.length === 2) {
        params.beginTime = this.range[0] + ' 00:00:00'
        params.endTime = this.range[1] + ' 23:59:59'
      }
      return params
    },
    search() {
      this.query.current = 1
      this.load()
    },
    changePage(page) {
      this.query.current = page
      this.load()
    },
    /** 读取申请分页。 */
    async load() {
      this.loading = true
      try {
        const res = await pageApplies(this.params())
        this.records = res.data.records
        this.total = res.data.total
      } catch (error) {
        this.records = []
      } finally {
        this.loading = false
      }
    },
    /** 打开详情。审批页同时准备意见框。 */
    async openDetail(row) {
      const res = await applyDetail(row.applyId)
      this.detail = res.data
      this.opinion = ''
      this.detailVisible = true
    },
    /** 通过或驳回当前申请。 */
    async audit(status) {
      if (status === '已驳回' && !this.opinion) {
        this.$message.warning('驳回时请填写意见')
        return
      }
      this.acting = true
      try {
        await auditApply({ applyId: this.detail.applyId, auditStatus: status, auditOpinion: this.opinion })
        this.$message.success(status === '已通过' ? '已通过，并生成订单' : '已驳回')
        this.detailVisible = false
        this.load()
      } catch (error) {
        /* 拦截器已提示 */
      } finally {
        this.acting = false
      }
    },
    /** 回填被驳回的申请，供修改数量和理由。 */
    async openResubmit(row) {
      const [detailRes, goodsRes] = await Promise.all([
        applyDetail(row.applyId),
        pageGoods({ current: 1, size: 500 })
      ])
      const countMap = {}
      detailRes.data.items.forEach((item) => { countMap[item.goodsId] = item.buyNum })
      this.editId = row.applyId
      this.editReason = detailRes.data.applyReason
      this.editGoods = goodsRes.data.records.map((item) => ({ ...item, buyNum: countMap[item.goodsId] || 0 }))
      this.editVisible = true
    },
    /** 提交修改后的申请。 */
    async resubmit() {
      const selected = this.editGoods.filter((item) => item.buyNum > 0)
      if (!this.editReason) {
        this.$message.warning('请填写申请理由')
        return
      }
      if (selected.length === 0) {
        this.$message.warning('请至少选择一种商品')
        return
      }
      this.acting = true
      try {
        await resubmitApply({
          applyId: this.editId,
          applyReason: this.editReason,
          items: selected.map((item) => ({ goodsId: item.goodsId, buyNum: item.buyNum }))
        })
        this.$message.success('申请已重新提交')
        this.editVisible = false
        this.load()
      } catch (error) {
        /* 拦截器已提示 */
      } finally {
        this.acting = false
      }
    }
  }
}
</script>
