<template>
  <div v-loading="loading">
    <p style="margin-top: 0">{{ guide }}</p>
    <div class="ledger-row" v-if="summary">
      <div v-for="item in figures" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
    <div class="manila-tab">最新公告</div>
    <div class="panel">
      <div v-if="notices.length === 0" class="empty-hint">还没有公告。</div>
      <div v-else class="note-list">
        <article v-for="item in notices" :key="item.noticeId" class="note">
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
          <time>{{ item.publishTime }}</time>
        </article>
      </div>
    </div>
  </div>
</template>

<script>
import { homeSummary } from '../api'

/** 工作台只展示当前角色真正要处理的数量和公告。 */
export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      summary: null,
      user: JSON.parse(localStorage.getItem('user') || '{}')
    }
  },
  computed: {
    notices() {
      return (this.summary && this.summary.notices) || []
    },
    guide() {
      const name = this.user.realName || ''
      if (this.user.role === 'staff') {
        return name + '，需要用品时提交申请。被驳回的申请可以修改后重新提交。'
      }
      if (this.user.role === 'audit') {
        return name + '，待审批的申请通过后会自动生成订单。驳回时请写明原因。'
      }
      return name + '，可以维护商品、用户和公告，并更新订单到货进度。库存不会在审批时自动扣减。'
    },
    figures() {
      if (!this.summary) return []
      if (this.user.role === 'staff') {
        return [
          { label: '待审批', value: this.summary.myPendingCount },
          { label: '已驳回', value: this.summary.myRejectedCount },
          { label: '我的订单', value: this.summary.orderCount }
        ]
      }
      if (this.user.role === 'audit') {
        return [
          { label: '待审批', value: this.summary.pendingAuditCount },
          { label: '全部订单', value: this.summary.orderCount },
          { label: '在册商品', value: this.summary.goodsCount }
        ]
      }
      return [
        { label: '待审批', value: this.summary.pendingAuditCount },
        { label: '全部订单', value: this.summary.orderCount },
        { label: '在册商品', value: this.summary.goodsCount }
      ]
    }
  },
  created() {
    this.load()
  },
  methods: {
    /** 读取工作台摘要。 */
    async load() {
      this.loading = true
      try {
        const res = await homeSummary()
        this.summary = res.data
      } catch (error) {
        this.summary = null
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
