package top.jplayer.audio.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator on 2018/5/5.
 * 登录Bean
 */
@Entity
public class LoginBean {
    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    public String name;
    @NotNull
    public String password;
    @Generated(hash = 1213769788)
    public LoginBean(Long _id, @NotNull String name, @NotNull String password) {
        this._id = _id;
        this.name = name;
        this.password = password;
    }
    @Generated(hash = 1112702939)
    public LoginBean() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
