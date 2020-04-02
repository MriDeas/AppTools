package com.mimi.xichelapp.utils.interceptor;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * author: pro
 * time：2019/7/12
 * desc：
 * fixed time：
 */
public interface IInterceptor {
    boolean intercept(@NotNull Class target);
}
