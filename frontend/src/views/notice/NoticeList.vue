<template>
  <div>
    <div v-if="canEdit" class="panel" style="margin-bottom: 16px">
      <el-form label-width="70px">
        <el-form-item label="标题"><el-input v-model.trim="form.title" maxlength="100" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">{{ form.noticeId ? '保存修改' : '发布公告' }}</el-button>
          <el-button v-if="form.noticeId" @click="reset">取消修改</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="filters" v-if="canEdit">
      <el-input v-model.trim="keyword" placeholder="按标题筛选" clearable style="width: 220px" @keyup.enter.native="search" />
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <div v-loading="loading" class="note-list">
      <div v-if="records.length === 0" class="empty-hint">还没有公告。</div>
      <article v-for="item in records" :key="item.noticeId" class="note">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
        <time>{{ item.publishTime }}</time>
        <div v-if="canEdit" class="note-actions">
          <el-button type="text" @click="edit(item)">修改</el-button>
          <el-button type="text" @click="remove(item)">删除</el-button>
        </div>
      </article>
    </div>
    <el-pagination class="pager" background layout="total, prev, pager, next" :current-page="current" :page-size="size" :total="total" @current-change="changePage" />
  </div>
</template>

<script>
import { createNotice, deleteNotice, pageNotices, updateNotice } from '../../api'

/** 公告栏。所有角色可看，只有管理员能发布和删除。 */
export default {
  name: 'NoticeList',
  data() {
    return {
      loading: false,
      saving: false,
      keyword: '',
      current: 1,
      size: 10,
      total: 0,
      records: [],
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      form: { noticeId: null, title: '', content: '' }
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
    search() {
      this.current = 1
      this.load()
    },
    changePage(page) {
      this.current = page
      this.load()
    },
    /** 读取公告分页。 */
    async load() {
      this.loading = true
      try {
        const res = await pageNotices({ current: this.current, size: this.size, title: this.keyword })
        this.records = res.data.records
        this.total = res.data.total
      } catch (error) {
        this.records = []
      } finally {
        this.loading = false
      }
    },
    reset() {
      this.form = { noticeId: null, title: '', content: '' }
    },
    edit(item) {
      this.form = { noticeId: item.noticeId, title: item.title, content: item.content }
      window.scrollTo(0, 0)
    },
    /** 发布或修改公告。 */
    async save() {
      if (!this.form.title || !this.form.content.trim()) {
        this.$message.warning('请填写标题和内容')
        return
      }
      this.saving = true
      try {
        if (this.form.noticeId) {
          await updateNotice(this.form)
        } else {
          await createNotice(this.form)
        }
        this.$message.success(this.form.noticeId ? '公告已保存' : '公告已发布')
        this.reset()
        this.load()
      } catch (error) {
        /* 拦截器已提示 */
      } finally {
        this.saving = false
      }
    },
    /** 删除公告。 */
    remove(item) {
      this.$confirm('删除后不能恢复。', '删除公告', { type: 'warning' })
        .then(async () => {
          await deleteNotice(item.noticeId)
          this.$message.success('公告已删除')
          this.load()
        })
        .catch(() => {})
    }
  }
}
</script>
