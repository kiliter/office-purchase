import http from '../utils/request'

/** 账号密码登录。 */
export function login(data) {
  return http.post('/user/login', data)
}

/** 当前登录用户。 */
export function currentUser() {
  return http.get('/user/me')
}

/** 修改自己的密码。 */
export function changePassword(data) {
  return http.put('/user/password', data)
}

/** 分页查询用户。 */
export function pageUsers(params) {
  return http.get('/user/page', { params })
}

/** 新增用户。 */
export function createUser(data) {
  return http.post('/user', data)
}

/** 修改用户。 */
export function updateUser(data) {
  return http.put('/user', data)
}

/** 删除用户。 */
export function deleteUser(userId) {
  return http.delete('/user/' + userId)
}

/** 重置用户密码。 */
export function resetPassword(userId) {
  return http.put('/user/reset/' + userId)
}

/** 分页查询商品。 */
export function pageGoods(params) {
  return http.get('/goods/page', { params })
}

/** 新增商品。 */
export function createGoods(data) {
  return http.post('/goods', data)
}

/** 修改商品。 */
export function updateGoods(data) {
  return http.put('/goods', data)
}

/** 删除商品。 */
export function deleteGoods(goodsId) {
  return http.delete('/goods/' + goodsId)
}

/** 提交采购申请。 */
export function submitApply(data) {
  return http.post('/apply/submit', data)
}

/** 驳回后重新提交。 */
export function resubmitApply(data) {
  return http.put('/apply/resubmit', data)
}

/** 分页查询申请。 */
export function pageApplies(params) {
  return http.get('/apply/page', { params })
}

/** 申请详情。 */
export function applyDetail(applyId) {
  return http.get('/apply/' + applyId)
}

/** 审批申请。 */
export function auditApply(data) {
  return http.post('/apply/audit', data)
}

/** 分页查询订单。 */
export function pageOrders(params) {
  return http.get('/order/page', { params })
}

/** 更新订单状态。 */
export function updateOrderStatus(data) {
  return http.put('/order/status', data)
}

/** 分页查询公告。 */
export function pageNotices(params) {
  return http.get('/notice/page', { params })
}

/** 发布公告。 */
export function createNotice(data) {
  return http.post('/notice', data)
}

/** 修改公告。 */
export function updateNotice(data) {
  return http.put('/notice', data)
}

/** 删除公告。 */
export function deleteNotice(noticeId) {
  return http.delete('/notice/' + noticeId)
}

/** 工作台摘要。 */
export function homeSummary() {
  return http.get('/home/summary')
}
