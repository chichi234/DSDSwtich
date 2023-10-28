package com.reb.switchbt.db;

import android.content.Context;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;


/**
 * File description
 *
 * @author tonly
 * @version 1.0
 * @date 2019/6/26 10:56
 * @package_name com.tonly.recognizeBook.db
 * @project_name onLineReader
 * @history At 2019/6/26 10:56 created by tonly
 */
public class DBOpenHelper extends DaoMaster.DevOpenHelper {
    public DBOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        },  DeviceBondDao.class);
    }
}
