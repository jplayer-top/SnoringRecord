package top.jplayer.audio.utils;

import android.content.Context;
import android.util.Log;

import com.ping.greendao.gen.RecordSleepBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import top.jplayer.audio.bean.RecordSleepBean;

/**
 * Created by Administrator on 2018/5/5.
 * 注册工具类
 */

public class RecordDaoUtil {
    private static final String TAG = RecordDaoUtil.class.getSimpleName();
    private DaoManager mManager;

    public RecordDaoUtil(Context context){
        mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成bean记录的插入，如果表未创建，先创建bean表
     * @param bean
     * @return
     */
    public boolean insertbean(RecordSleepBean bean){
        boolean flag = false;
        flag = mManager.getDaoSession().getRecordSleepBeanDao().insert(bean) != -1;
        Log.i(TAG, "insert LoginBean :" + flag + "-->" + bean.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     * @param beanList
     * @return
     */
    public boolean insertMultbean(final List<RecordSleepBean> beanList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (RecordSleepBean bean : beanList) {
                        mManager.getDaoSession().insertOrReplace(bean);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改一条数据
     * @param bean
     * @return
     */
    public boolean updatebean(RecordSleepBean bean){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(bean);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param bean
     * @return
     */
    public boolean deletebean(RecordSleepBean bean){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(bean);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     * @return
     */
    public boolean deleteAll(){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(RecordSleepBean.class);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     * @return
     */
    public List<RecordSleepBean> queryAllbean(){
        return mManager.getDaoSession().loadAll(RecordSleepBean.class);
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public RecordSleepBean querybeanById(long key){
        return mManager.getDaoSession().load(RecordSleepBean.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<RecordSleepBean> querybeanByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(RecordSleepBean.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     * @return
     */
    public List<RecordSleepBean> querybeanByQueryBuilder(long id){
        QueryBuilder<RecordSleepBean> queryBuilder = mManager.getDaoSession().queryBuilder(RecordSleepBean.class);
        return queryBuilder.where(RecordSleepBeanDao.Properties._id.eq(id)).list();
    }
}
