package com.office.purchase.common;

/**
 * 系统角色编码，与用户表 role 字段一致。
 */
public final class RoleConst {

    /** 系统管理员 */
    public static final String ADMIN = "admin";

    /** 采购审核人员 */
    public static final String AUDIT = "audit";

    /** 普通员工 */
    public static final String STAFF = "staff";

    private RoleConst() {
    }

    /**
     * 判断角色编码是否属于系统定义的三种角色。
     */
    public static boolean valid(String role) {
        return ADMIN.equals(role) || AUDIT.equals(role) || STAFF.equals(role);
    }
}
