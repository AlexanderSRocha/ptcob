package br.com.pentagrupo.pentalog.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.pentagrupo.pentalog.objects.BaseObj;

public class BaseEntity implements Parcelable {
    public static final Creator<BaseEntity> CREATOR = new Creator<BaseEntity>() {
        @Override
        public BaseEntity createFromParcel(Parcel in) {
            return new BaseEntity(in);
        }

        @Override
        public BaseEntity[] newArray(int size) {
            return new BaseEntity[size];
        }
    };
    public long _id;
    public String id;
    public String name;

    protected BaseEntity(Parcel in) {
        _id = in.readLong();
        id = in.readString();
        name = in.readString();
    }

    public BaseEntity() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(id);
        dest.writeString(name);
    }

    @Override
    public boolean equals(Object o) {
        BaseEntity mEntity = (BaseEntity) o;
        if(mEntity.id == id && !TextUtils.isEmpty(id))
            return true;
        else
            return false;
    }

    public Map<String, Object> getObjectMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        return result;
    }
}
