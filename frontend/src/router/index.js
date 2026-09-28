import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../layout/Layout.vue'
import Home from '../views/Home.vue'
import GoodsList from '../views/goods/GoodsList.vue'
import ApplySubmit from '../views/apply/ApplySubmit.vue'
import ApplyTable from '../views/apply/ApplyTable.vue'
import OrderList from '../views/order/OrderList.vue'
import UserList from '../views/user/UserList.vue'
import NoticeList from '../views/notice/NoticeList.vue'
import Password from '../views/profile/Password.vue'

Vue.use(VueRouter)

const router = new VueRouter({
  mode: 'history',
  routes: [
    { path: '/login', component: Login, meta: { public: true, title: '登录' } },
    {
      path: '/',
      component: Layout,
      redirect: '/home',
      children: [
        { path: '/home', component: Home, meta: { title: '工作台', eyebrow: '今天要处理的事' } },
        { path: '/goods', component: GoodsList, meta: { title: '办公用品', eyebrow: '库存台账' } },
        { path: '/apply/submit', component: ApplySubmit, meta: { title: '提交申请', eyebrow: '选品并说明用途', roles: ['staff'] } },
        { path: '/apply/mine', component: ApplyTable, meta: { title: '我的申请', eyebrow: '申请进度', roles: ['staff'], mode: 'mine' } },
        { path: '/audit', component: ApplyTable, meta: { title: '待我审批', eyebrow: '还没处理的申请', roles: ['audit'], mode: 'audit' } },
        { path: '/apply', component: ApplyTable, meta: { title: '申请记录', eyebrow: '全部采购申请', roles: ['admin', 'audit'], mode: 'all' } },
        { path: '/order', component: OrderList, meta: { title: '采购订单', eyebrow: '到货进度' } },
        { path: '/user', component: UserList, meta: { title: '用户管理', eyebrow: '账号与角色', roles: ['admin'] } },
        { path: '/notice', component: NoticeList, meta: { title: '系统公告', eyebrow: '通知' } },
        { path: '/password', component: Password, meta: { title: '修改密码', eyebrow: '个人账号' } }
      ]
    }
  ]
})

/** 未登录去登录页；角色不匹配时回到工作台。 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) next('/home')
    else next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const roles = to.matched.map((record) => record.meta.roles).filter(Boolean).pop()
  if (roles && !roles.includes(user.role)) {
    next('/home')
    return
  }
  next()
})

export default router
