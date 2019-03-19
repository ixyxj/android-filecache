package com.ixyxj.io.filecache;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * For more information, you can visit https://github.com/xieyangxuejun,
 * or contact me by xieyangxuejun@gmail.com
 *
 * @author silen on 2019/3/20 2:04
 * Copyright (c) 2019 in FORETREE
 */
public class IOUtil {
    private IOUtil() {
    }

    public static byte[] readAll(InputStream is) throws IOException {
        try (Counter.Scope t = Counter.time("readAll")) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                for (int n; (n = is.read(buf)) != -1; ) {
                    os.write(buf, 0, n);
                }
                return os.toByteArray();
            }
        }
    }

    public static void writeAll(OutputStream os, byte[] data) throws IOException {
        if (data == null) return;
        try(Counter.Scope t = Counter.time("writeAll")) {
            os.write(data, 0, data.length);
            os.flush();
        }
    }

    public static byte[] encode(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] out = parcel.marshall();
        parcel.recycle();
        return out;
    }

    public static <T> T decode(Parcelable.Creator<T> creator, byte[] data) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(data, 0, data.length);
        parcel.setDataPosition(0);//必须设置
        T t = creator.createFromParcel(parcel);
        parcel.recycle();
        return t;
    }
}
