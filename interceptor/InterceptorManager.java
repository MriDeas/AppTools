package com.mimi.xichelapp.utils.interceptor;

import android.app.Application;

import com.mimi.xichelapp.utils.interceptor.interceptors.MiServicesPermissionInterceptor;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * author: pro
 * time：2019/7/12
 * desc：
 * fixed time：
 */
public class InterceptorManager {
    private static final LinkedHashSet<IInterceptor> interceptorList = new LinkedHashSet<>();

    private InterceptorManager() {
    }

    private static class ManagerHolder {
        private static final InterceptorManager manager = new InterceptorManager();
    }

    public static InterceptorManager getManager() {
        return ManagerHolder.manager;
    }

    public void init(Application app) {
        interceptorList.clear();
        interceptorList.add(MiServicesPermissionInterceptor.init(app));
    }

    public boolean check(@NotNull Class target) {
        if (interceptorList.isEmpty()) return true;
        for (IInterceptor interceptor : interceptorList) {
            boolean allow = interceptor.intercept(target);
            if (!allow) {
                return false;
            }
        }
        return true;
    }
}
