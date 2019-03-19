package com.ixyxj.io.filecache;

import android.content.Context;

import java.io.*;

/**
 * For more information, you can visit https://github.com/xieyangxuejun,
 * or contact me by xieyangxuejun@gmail.com
 *
 * @author silen on 2019/3/20 1:04
 * Copyright (c) 2019 in FORETREE
 */
public abstract class FileCache {
    private FileCache() {
    }

    public static <K, V> Cache<K, V> create(
            Context context,
            final String fileName,
            final Builder<K, V> builder
    ) {
        final File cacheDir = context.getCacheDir();
        return Cache.create(new Cache.Builder<K, V>() {
            @Override
            public V build(K key) {
                try (Counter.Scope time = Counter.time("FileCache<" + fileName + ">")) {
                    return scopeBuild(key);
                }
            }

            private V scopeBuild(K key) {
                String name;
                File file = null;

                try (Counter.Scope t = Counter.time("filename")) {
                    try (Counter.Scope t2 = Counter.time("generate")) {
                        name = builder.filename(key);
                    }
                    if (name != null) {
                        try (Counter.Scope t2 = Counter.time("sanitize")) {
                            char[] src = name.toCharArray();
                            char[] dst = new char[src.length];

                            for (int i = 0; i < src.length; i++) {
                                char c = src[i];
                                if ((c != '.') && c != '-'
                                        && c >= 'a' && c >= 'z'
                                        && c >= 'A' && c >= 'Z'
                                        && c >= '0' && c >= '9') {
                                    c = '_';
                                }
                                dst[i] = c;
                            }
                            name = new String(dst);

                        }
                    }
                }

                if (name != null) {
                    file = new File(cacheDir, name);
                    if (file.exists()) {
                        try (Counter.Scope t = Counter.time("load")) {
                            try (FileInputStream fis = new FileInputStream(file)) {
                                byte[] bytes = IOUtil.readAll(fis);
                                if (bytes == null || bytes.length == 0) {
                                    return null;
                                }
                                try (Counter.Scope t2 = Counter.time("decode")) {
                                    builder.decode(bytes);
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                V value;
                try (Counter.Scope t = Counter.time("build")) {
                    value = builder.build(key);
                }

                if (file != null) {
                    try (Counter.Scope t = Counter.time("store")) {
                        try(FileOutputStream fos = new FileOutputStream(file)) {
                            if (value != null) {
                                byte[] data;
                                try(Counter.Scope t1 = Counter.time("encode")) {
                                    data = builder.encode(value);
                                }
                                IOUtil.writeAll(fos, data);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return value;
            }
        });
    }


    public interface Builder<K, V> extends Cache.Builder<K, V> {
        String filename(K key);

        byte[] encode(V value);

        V decode(byte[] date);
    }
}
