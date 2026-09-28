package com.office.purchase.common;

import com.office.purchase.entity.User;

/**
 * 保存当前请求已登录的用户。拦截器放入，请求结束后必须清理。
 */
public final class UserContext {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<User>();

    private UserContext() {
    }

    /**
     * 绑定当前登录用户。
     */
    public static void set(User user) {
        HOLDER.set(user);
    }

    /**
     * 读取当前登录用户。未登录时返回 null。
     */
    public static User get() {
        return HOLDER.get();
    }

    /**
     * 防止线程复用时串入上一个用户。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
