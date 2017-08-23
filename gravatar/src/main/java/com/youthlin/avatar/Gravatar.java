package com.youthlin.avatar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({ "WeakerAccess", "unused" })
public final class Gravatar {
    public static final String PREFIX = "https://www.gravatar.com/avatar/";
    private String prefix = PREFIX;//URL 前缀
    private String md5 = null;//电子邮件地址 md5
    private String extension = null;//后缀，如 .jpg
    private String defaultUrl = null;//默认头像地址
    private String forceDefault = null;//即使在 Gravatar 设置了也强制使用默认头像
    private int size = 0;//图片大小，正方形边长

    public enum DefaultType {
        /**
         * 当没有这个头像时返回 404 状态码
         * <p>
         * https://www.gravatar.com/avatar/7158b0cc5dae9d7527b171166a9b7gr4?d=404
         */
        NOT_FOUND("404"),
        /**
         * 神秘人 mystery-man
         */
        MM("mm"),
        /**
         * 抽象几何图形
         */
        IDENTICON("identicon"),
        /**
         * 小怪物
         */
        MONSTERID("monsterid"),
        /**
         * 随机卡通面孔
         */
        WAVATAR("wavatar"),
        /**
         * 8位街机风格像素图
         */
        RETRO("retro"),
        /**
         * 空白图 透明背景
         */
        BLANK("blank");

        private String desc;

        DefaultType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public static final Set<String> ALL_TYPE;

        static {
            Set<String> aSet = new HashSet<String>(7);
            DefaultType[] defaultTypes = DefaultType.values();
            for (DefaultType t : defaultTypes) {
                aSet.add(t.desc);
            }
            ALL_TYPE = Collections.unmodifiableSet(aSet);
        }
    }

    private Gravatar() {
    }

    public static Gravatar withEmail(String email) {
        return withHash(HashUtil.md5(email));
    }

    public static Gravatar withHash(String hash) {
        return new Gravatar().md5(hash);
    }

    private Gravatar md5(String hash) {
        md5 = hash;
        return this;
    }

    public Gravatar prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Gravatar extension(String extension) {
        if (extension.startsWith(".")) {
            this.extension = extension;
        }
        return this;
    }

    public Gravatar size(int size) {
        if (size > 0) {
            this.size = size;
        }
        return this;
    }

    public Gravatar defaults(String url) {
        try {
            defaultUrl = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            throw new AssertionError(ignore);
        }
        return this;
    }

    public Gravatar defaults(DefaultType type) {
        if (type != null) {
            defaultUrl = type.getDesc();
        }
        return this;
    }

    public Gravatar defaultsForce() {
        forceDefault = "f=y";
        return this;
    }

    public Gravatar defaultsType(String type) {
        if (DefaultType.ALL_TYPE.contains(type)) {
            defaultUrl = type;
        }
        return this;
    }

    public String getUrl() {
        StringBuilder sb = new StringBuilder(prefix).append(md5);
        if (extension != null) {
            sb.append(extension);
        }
        boolean hasAppend = false;
        if (size > 0) {
            sb.append("?s=").append(size);
            hasAppend = true;
        }
        if (defaultUrl != null) {
            if (!hasAppend) {
                sb.append("?d=").append(defaultUrl);
            } else {
                sb.append("&d=").append(defaultUrl);
            }
            hasAppend = true;
        }
        if (forceDefault != null) {
            if (hasAppend) {
                sb.append("&").append(forceDefault);
            } else {
                sb.append("?").append(forceDefault);
            }
        }
        return sb.toString();
    }

    public static String getUrlWithEmail(String email) {
        return PREFIX + HashUtil.md5(email);
    }

    public static String getUrlWithHash(String md5) {
        return PREFIX + md5;
    }

    public static void main(String[] args) {
        String url = Gravatar.withEmail("youthlin.chen@qunar.com")
                .extension(".jpg")
                .defaults(DefaultType.MONSTERID)//随机小怪物
                .size(80)
                .getUrl();
        //https://www.gravatar.com/avatar/7158b0cc5dae9d7527b171166a9b7d74.jpg?s=80&d=monsterid
        System.out.println(url);
        System.out.printf(Gravatar.getUrlWithEmail("youthlin.chen@qunar.com"));
    }
}
