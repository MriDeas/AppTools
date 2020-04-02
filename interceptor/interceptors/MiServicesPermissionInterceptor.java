package com.mimi.xichelapp.utils.interceptor.interceptors;

import android.app.Application;

import com.mimi.xichelapp.R;
import com.mimi.xichelapp.activity.CreditCardsActivity;
import com.mimi.xichelapp.activity.InsuranceActivity;
import com.mimi.xichelapp.activity.MortgageActivity;
import com.mimi.xichelapp.utils.interceptor.IInterceptor;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * author: pro
 * time：2019/7/12
 * desc：
 * fixed time：
 */
public class MiServicesPermissionInterceptor implements IInterceptor {

    private static String INSURANCE = InsuranceActivity.class.getSimpleName();
    //办卡
    private static String CARD_BUSINESS = ShopCardListActivity.class.getSimpleName();
    //单笔收款
    private static String CASH_TRADE_BUSINESS = CashTradeActivity.class.getSimpleName();

    //刷卡
    private static String CARD_LIST_TRADE = UserCardListActivity.class.getSimpleName();
    private static String CARD_TRADE_BUSINESS = UserCardTradeBusinessActivity.class.getSimpleName();
    private static String SWIPE_CARD_BUSINESS = UserCardTradeActivity.class.getSimpleName();

    private int mReason;

    private Application mApp;

    private MiServicesPermissionInterceptor(Application app) {
        mApp = app;
    }

    public static IInterceptor init(Application app) {
        return new MiServicesPermissionInterceptor(app);
    }

    @Override
    public boolean intercept(@NotNull Class target) {
        String name = target.getSimpleName();
        int businessCode = getBusinessCode(name);
        boolean allow = Variable.getShop()._checkSupportBusiness(businessCode);
        if (!allow) {
            onIntercept();
        }
        return allow;
    }

    private int getBusinessCode(String name) {
        int code = 0;
        mReason = 0;
        if (name.equals(INSURANCE) || name.equals(CLAIM)
                || name.equals(VIOLATION) || name.equals(LOAN)
                || name.equals(SECOND_CAR) || name.equals(ANNUAL_CHECK)
                || name.equals(CREDIT_CARD) || name.equals(INSURANCE_GIFT_PACKAGE)
                || name.equals(METAL_PLATE)) {
            code = Shop.CHECK_PRODUCT_BUSINESS;
            mReason = R.string.str_permission_deny_value_service;
        } else if (name.equals(CARD_BUSINESS)) {
           ...
        return code;
    }

    private void onIntercept() {
        if (mReason != 0) {
            
        }
    }

}
