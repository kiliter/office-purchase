<template>
  <div class="shell">
    <aside class="cabinet">
      <div class="brand">
        <span class="brand-kicker">内部申领</span>
        <strong>办公用品采购</strong>
      </div>
      <div class="drawer-tag">{{ roleLabel }}</div>
      <nav>
        <router-link v-for="item in menus" :key="item.path" :to="item.path" class="drawer">
          {{ item.title }}
        </router-link>
        <!-- 说明书是独立页面，不进入业务路由，所以放在修改密码下面单独打开。 -->
        <button type="button" class="drawer" @click="openGuide">软件说明书</button>
      </nav>
    </aside>
    <section class="main">
      <header class="top">
        <div>
          <p class="eyebrow">{{ $route.meta.eyebrow || "办公用品" }}</p>
          <h1>{{ $route.meta.title || "工作台" }}</h1>
        </div>
        <div class="who">
          <span>{{ user.realName }}</span>
          <button type="button" @click="logout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script>
import { ROLE_LABEL } from '../utils/dict'

/** 按角色放不同的抽屉入口。 */
const MENUS = {
  admin: [
    { path: '/home', title: '工作台' },
    { path: '/goods', title: '商品管理' },
    { path: '/apply', title: '采购申请' },
    { path: '/order', title: '订单管理' },
    { path: '/user', title: '用户管理' },
    { path: '/notice', title: '公告管理' },
    { path: '/password', title: '修改密码' }
  ],
  audit: [
    { path: '/home', title: '工作台' },
    { path: '/goods', title: '商品浏览' },
    { path: '/audit', title: '待我审批' },
    { path: '/apply', title: '申请记录' },
    { path: '/order', title: '订单查询' },
    { path: '/notice', title: '系统公告' },
    { path: '/password', title: '修改密码' }
  ],
  staff: [
    { path: '/home', title: '工作台' },
    { path: '/goods', title: '商品浏览' },
    { path: '/apply/submit', title: '提交申请' },
    { path: '/apply/mine', title: '我的申请' },
    { path: '/order', title: '我的订单' },
    { path: '/notice', title: '系统公告' },
    { path: '/password', title: '修改密码' }
  ]
}

export default {
  name: 'Layout',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}')
    }
  },
  computed: {
    /** 当前角色能看到的菜单。 */
    menus() {
      return MENUS[this.user.role] || []
    },
    roleLabel() {
      return ROLE_LABEL[this.user.role] || '未识别角色'
    }
  },
  methods: {
    /** 清除本地登录状态并回到登录页。 */
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
    /** 在新标签页打开代码说明书，避免盖住当前办理中的页面。 */
    openGuide() {
      window.open('/guide.html', '_blank', 'noopener')
    }
  }
}
</script>
