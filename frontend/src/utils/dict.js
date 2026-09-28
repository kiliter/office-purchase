/** 角色编码对应的中文名称。 */
export const ROLE_LABEL = {
  admin: '管理员',
  audit: '审核人员',
  staff: '普通员工'
}

/**
 * 按业务状态选择印章颜色。颜色只是辅助，文字本身仍然可读。
 */
export function stampKind(status) {
  if (status === '已通过' || status === '已完成' || status === '已到货') {
    return 'ok'
  }
  if (status === '已驳回') {
    return 'no'
  }
  return 'wait'
}
