package com.reb.switchbt.db;

import android.content.Context;


public class DBManager {
    private final static String dbName = "book";
    private static DBManager mInstance;
    private DaoSession mDaoSession;
//    private Context context;

    private DBManager(Context context) {
//        this.context = context;
        DaoMaster.DevOpenHelper openHelper = new DBOpenHelper(context, dbName);
        DaoMaster mDaoMaster = new DaoMaster(openHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager(context);
                }
            }
        }
    }

    /**
     * 获取单例引用
     */
    public static DBManager getInstance() {
        return mInstance;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DeviceBondDao getDeviceBondDao() {
        return mDaoSession.getDeviceBondDao();
    }
}