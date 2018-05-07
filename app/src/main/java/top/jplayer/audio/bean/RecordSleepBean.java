package top.jplayer.audio.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Obl on 2018/5/7.
 * top.jplayer.audio.bean
 * call me : jplayer_top@163.com
 * github : https://github.com/oblivion0001
 */
@Entity
public class RecordSleepBean implements Parcelable{
    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    public String day;
    @NotNull
    public String startTime;
    @NotNull
    public String endTime;
    @NotNull
    public String sleepTime;
    @NotNull
    public String sleepSnoring;
    @NotNull
    public String account;
    @Generated(hash = 902071034)
    public RecordSleepBean(Long _id, @NotNull String day, @NotNull String startTime,
            @NotNull String endTime, @NotNull String sleepTime,
            @NotNull String sleepSnoring, @NotNull String account) {
        this._id = _id;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sleepTime = sleepTime;
        this.sleepSnoring = sleepSnoring;
        this.account = account;
    }
    @Generated(hash = 423924899)
    public RecordSleepBean() {
    }

    protected RecordSleepBean(Parcel in) {
        if (in.readByte() == 0) {
            _id = null;
        } else {
            _id = in.readLong();
        }
        day = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        sleepTime = in.readString();
        sleepSnoring = in.readString();
        account = in.readString();
    }

    public static final Creator<RecordSleepBean> CREATOR = new Creator<RecordSleepBean>() {
        @Override
        public RecordSleepBean createFromParcel(Parcel in) {
            return new RecordSleepBean(in);
        }

        @Override
        public RecordSleepBean[] newArray(int size) {
            return new RecordSleepBean[size];
        }
    };

    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getDay() {
        return this.day;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public String getStartTime() {
        return this.startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return this.endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getSleepTime() {
        return this.sleepTime;
    }
    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }
    public String getSleepSnoring() {
        return this.sleepSnoring;
    }
    public void setSleepSnoring(String sleepSnoring) {
        this.sleepSnoring = sleepSnoring;
    }
    public String getAccount() {
        return this.account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "RecordSleepBean{" +
                "_id=" + _id +
                ", day='" + day + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", sleepTime='" + sleepTime + '\'' +
                ", sleepSnoring='" + sleepSnoring + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (_id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_id);
        }
        dest.writeString(day);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(sleepTime);
        dest.writeString(sleepSnoring);
        dest.writeString(account);
    }
}
