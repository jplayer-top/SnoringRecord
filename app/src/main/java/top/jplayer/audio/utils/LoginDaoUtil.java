package top.jplayer.audio.utils;

import android.content.Context;
import android.util.Log;

import com.ping.greendao.gen.LoginBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import top.jplayer.audio.bean.LoginBean;

/**
 * Created by Administrator on 2018/5/5.
 * 注册工具类
 */

public class LoginDaoUtil {
    private static final String TAG = LoginDaoUtil.class.getSimpleName();
    private DaoManager mManager;

    public LoginDaoUtil(Context context){
        mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成loginBean记录的插入，如果表未创建，先创建loginBean表
     * @param loginBean
     * @return
     */
    public boolean insertloginBean(LoginBean loginBean){
        boolean flag = false;
        flag = mManager.getDaoSession().getLoginBeanDao().insert(loginBean) != -1;
        Log.i(TAG, "insert LoginBean :" + flag + "-->" + loginBean.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     * @param loginBeanList
     * @return
     */
    public boolean insertMultloginBean(final List<LoginBean> loginBeanList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (LoginBean loginBean : loginBeanList) {
                        mManager.getDaoSession().insertOrReplace(loginBean);
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
     * @param loginBean
     * @return
     */
    public boolean updateloginBean(LoginBean loginBean){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(loginBean);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param loginBean
     * @return
     */
    public boolean deleteloginBean(LoginBean loginBean){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(loginBean);
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
            mManager.getDaoSession().deleteAll(LoginBean.class);
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
    public List<LoginBean> queryAllloginBean(){
        return mManager.getDaoSession().loadAll(LoginBean.class);
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public LoginBean queryloginBeanById(long key){
        return mManager.getDaoSession().load(LoginBean.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<LoginBean> queryloginBeanByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(LoginBean.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     * @return
     */
    public List<LoginBean> queryloginBeanByQueryBuilder(long id){
        QueryBuilder<LoginBean> queryBuilder = mManager.getDaoSession().queryBuilder(LoginBean.class);
        return queryBuilder.where(LoginBeanDao.Properties._id.eq(id)).list();
    }
}
