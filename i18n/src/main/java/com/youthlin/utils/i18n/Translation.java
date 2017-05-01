package com.youthlin.utils.i18n;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import static com.youthlin.utils.i18n.GettextResource2.CONTEXT_GLUE;

/**
 * 翻译工具类.
 * <p>
 * Created by lin on 2017-01-30-030.
 * <p>
 * <code>__("str");</code><br>
 * <code>_x("str","context");</code><br>
 * <code>_n("single","plural",n);</code><br>
 * <code>_nx("single","plural",n,"context");</code><br>
 * <p>
 * <code>[main]$ xgettext -k__ -k_x:2c,1 -k_n:1,2 -k_nx:3c,1,2  -o resources/Message.pot java/pack/age/Clazz.java --from-code UTF-8</code>
 * <br><code>[main]$ msgfmt --java2 -d resources -r Message -l zh_CN resources\Message_zh_CN.po (--source生成 java 文件)</code>
 * <br><code>addResource("id", ResourceBundle.getBundle("Message"));</code>
 * <p>
 * 也可使用 Poedit 工具抽取待翻译字符串【复数编辑nplurals=2; plural=n == 1 ? 0 : 1;】
 *
 * @see <a href="http://youthlin.com/?p=1315">http://youthlin.com/20161315.html</a>
 */
@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused", "UnusedReturnValue"})
public class Translation {
    public static final ResourceBundle empty = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
            return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public String nextElement() {
                    return null;
                }
            };
        }
    };
    public static final String domain = Translation.class.getName();

    private static ResourceBundle dft = empty;
    private static Deque<Pair> resources = new LinkedList<Pair>();
    private static Set<String> domains = new HashSet<String>();
    private static Set<ResourceBundle> catalogs = new HashSet<ResourceBundle>();
    private static boolean verbose = false;

    private static ResourceBundle r = getBundle("Message");

    static {
        addResource(domain, r);
    }

    //region // add/remove

    /**
     * 注册一个翻译包.
     * <p>
     * 添加一个翻译资源包到队列头部, 翻译时将从队列头部搜索
     */
    public static boolean addResource(String domain, ResourceBundle rb) {
        notnull(domain, "domain");
        notnull(rb, "ResourceBundle");
        if (rb.equals(dft) || rb.equals(empty)) {
            if (verbose) System.err.println("dft/empty is no need to add.");
            return false;//dft 就没有必要添加了
        }
        Pair pair = new Pair(domain, rb);
        if (resources.contains(pair)) {
            return false;
        }
        resources.add(pair);
        domains.add(domain);
        catalogs.add(rb);
        return true;
    }

    public static boolean removeResource(String domain, ResourceBundle rb) {
        Pair p = new Pair(domain, rb);
        boolean hasName = false;
        boolean hasCatalog = false;
        if (resources.contains(p)) {
            resources.remove(p);
            for (Pair pair : resources) {
                if (pair.name.equals(domain)) {
                    hasName = true;
                }
                if (pair.catalog.equals(rb)) {
                    hasCatalog = true;
                }
            }
            if (!hasName) {
                domains.remove(domain);
            }
            if (!hasCatalog) {
                catalogs.remove(rb);
            }
            return true;
        }
        return false;
    }

    public static boolean removeResource(String domain) {
        if (domains.contains(domain)) {
            Iterator<Pair> iterator = resources.iterator();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.name.equals(domain)) {
                    catalogs.remove(p.catalog);
                    iterator.remove();
                }
            }
            domains.remove(domain);
            return true;
        }
        return false;
    }

    public static boolean removeResource(ResourceBundle rb) {
        if (catalogs.contains(rb)) {
            Iterator<Pair> iterator = resources.iterator();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.catalog.equals(rb)) {
                    domains.remove(p.name);
                    iterator.remove();
                }
            }
            catalogs.remove(rb);
            return true;
        }
        return false;
    }
    //endregion // add/remove

    //region // __
    public static String __(String msg) {
        for (Pair p : resources) {
            String s = GettextResource2.gettextnull(p.catalog, msg);
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.gettext(dft, msg);
    }

    public static String __(String msg, String domain) {
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.gettextnull(p.catalog, msg);
                if (s != null) {
                    return s;
                }
            }
        }
        return GettextResource2.gettext(dft, msg);
    }

    public static String __(String msg, ResourceBundle rb) {
        String s = GettextResource2.gettextnull(rb, msg);
        if (s != null) {
            return s;
        }
        return GettextResource2.gettext(rb, msg);
    }

    public static String __(String fmt, int unused, Object... params) {
        return MessageFormat.format(__(fmt), params);
    }

    public static String __(String fmt, String domain, Object... params) {
        return MessageFormat.format(__(fmt, domain), params);
    }

    public static String __(String fmt, ResourceBundle rb, Object... params) {
        return MessageFormat.format(__(fmt, rb), params);
    }
    //endregion // __

    //region // _x
    public static String _x(String msg, String ctx) {
        for (Pair p : resources) {
            String s = GettextResource2.gettextnull(p.catalog, ctx + CONTEXT_GLUE + msg);
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.pgettext(dft, ctx, msg);
    }

    public static String _x(String msg, String ctx, String domain) {
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.gettextnull(p.catalog, ctx + CONTEXT_GLUE + msg);
                if (s != null) {
                    return s;
                }
            }
        }
        return GettextResource2.pgettext(dft, ctx, msg);
    }

    public static String _x(String msg, String ctx, ResourceBundle rb) {
        String s = GettextResource2.gettextnull(rb, ctx + CONTEXT_GLUE + msg);
        if (s != null) {
            return s;
        }
        return GettextResource2.pgettext(rb, ctx, msg);
    }

    public static String _x(String fmt, String ctx, int unused, Object... param) {
        return MessageFormat.format(_x(fmt, ctx), param);
    }

    public static String _x(String fmt, String ctx, String domain, Object... param) {
        return MessageFormat.format(_x(fmt, ctx, domain), param);
    }

    public static String _x(String fmt, String ctx, ResourceBundle rb, Object... param) {
        return MessageFormat.format(_x(fmt, ctx, rb), param);
    }
    //endregion // _x

    //region // _n
    public static String _n(String msg, String msg_plural, long n) {
        for (Pair p : resources) {
            String s = GettextResource2.ngettextnull(p.catalog, msg, n);
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.ngettext(dft, msg, msg_plural, n);
    }

    public static String _n(String msg, String msg_plural, String domain, long n) {
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.ngettextnull(p.catalog, msg, n);
                if (s != null) {
                    return s;
                }
            }
        }
        return GettextResource2.ngettext(dft, msg, msg_plural, n);
    }

    public static String _n(String msg, String msg_plural, ResourceBundle rb, long n) {
        String s = GettextResource2.ngettextnull(rb, msg, n);
        if (s != null) {
            return s;
        }
        return GettextResource2.ngettext(dft, msg, msg_plural, n);
    }

    public static String _n(String msg, String msg_plural, long n, Object... param) {
        return MessageFormat.format(_n(msg, msg_plural, n), param);
    }

    public static String _n(String msg, String msg_plural, String domain, long n, Object... param) {
        return MessageFormat.format(_n(msg, msg_plural, domain, n), param);
    }

    public static String _n(String msg, String msg_plural, ResourceBundle rb, long n, Object... param) {
        return MessageFormat.format(_n(msg, msg_plural, n, rb), param);
    }
    //endregion // _n

    //region // _nx
    public static String _nx(String msg, String plural, String ctx, long n) {
        for (Pair p : resources) {
            String s = GettextResource2.ngettextnull(p.catalog, ctx + CONTEXT_GLUE + msg, n);
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.npgettext(dft, ctx, msg, plural, n);
    }

    public static String _nx(String msg, String plural, String ctx, String domain, long n) {
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.ngettextnull(p.catalog, ctx + CONTEXT_GLUE + msg, n);
                if (s != null) {
                    return s;
                }
            }
        }
        return GettextResource2.npgettext(dft, ctx, msg, plural, n);
    }

    public static String _nx(String msg, String plural, String ctx, ResourceBundle catalog, long n) {
        String s = GettextResource2.ngettextnull(catalog, ctx + CONTEXT_GLUE + msg, n);
        if (s != null) {
            return s;
        }
        return GettextResource2.npgettext(catalog, ctx, msg, plural, n);
    }

    public static String _nx(String msg, String plural, String ctx, long n, Object... param) {
        return MessageFormat.format(_nx(msg, plural, ctx, n), param);
    }

    public static String _nx(String msg, String plural, String ctx, String domain, long n, Object... param) {
        return MessageFormat.format(_nx(msg, plural, ctx, domain, n), param);
    }

    public static String _nx(String msg, String plural, String ctx, ResourceBundle catalog, long n, Object... param) {
        return MessageFormat.format(_nx(msg, plural, ctx, catalog, n), param);
    }
    //endregion  // _nx

    private static void notnull(Object o, String parameterName) {
        if (o == null) {
            throw new NullPointerException("The parameter: '" + parameterName + "' should be not null");
        }
    }

    public static ResourceBundle getDft() {
        return dft;
    }

    public static void setDft(ResourceBundle dft) {
        notnull(dft, "dft");
        Translation.dft = dft;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        Translation.verbose = verbose;
    }

    public static ResourceBundle getBundle(String baseName) {
        return getBundle(baseName, Locale.getDefault());
    }

    public static ResourceBundle getBundle(String baseName, Locale locale) {
        /*
         * ResourceBundle.getBundle(baseName, locale) 加载顺序：
         * baseName.locale.class -> baseName.dftLocale.class -> baseName.class
         */
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(baseName, locale);
        } catch (MissingResourceException e) {
            if (verbose)
                System.err.println("Can not find resources with locale " + locale + ". using default resources.");
        }
        if (bundle != null && bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
            return bundle;
        }
        return dft;
    }

    public static void main(String[] args) {
        System.out.println(domain);
        System.out.println("默认：------------------------");
        print();
        removeResource(domain);
        System.out.println("移除后：------------------------");
        print();
        r = getBundle("Message", Locale.FRENCH);
        addResource(domain, r);
        System.out.println("Fr：------------------------");
        print();
        removeResource(domain, r);
        r = getBundle("Message");
        addResource(domain, r);
        System.out.println("默认：------------------------");
        print();
    }

    private static void print() {
        System.out.println(__("Hello, World!"));
        System.out.println(__("Hello, {0}!", 0, "Lin"));
        System.out.println(__("Hello, {0}! Now is {1,date} {1,time}", 0, "World", new Date()));
        System.out.println(_x("Post", "a post"));
        System.out.println(_x("Post", "to post"));
        System.out.println(_n("One Comment", "{0} Comments", 1, 1));
        System.out.println(_n("One Comment", "{0} Comments", 3, 3));
        System.out.println(_nx("One Comment", "{0} Comments", "评论", 1, 1));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", 2, 2));
        //removeResource(r);
        System.out.println(_nx("One Comment", "{0} Comments", "注释", 2, 2));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", domain, 2, 2));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", r, 2, 2));

    }

    /*私有内部类. 用于添加至 Queue 检查重复.*/
    private static class Pair {
        final String name;
        final ResourceBundle catalog;

        private Pair(String name, ResourceBundle catalog) {
            notnull(name, "name");
            notnull(catalog, "catalog");
            this.name = name;
            this.catalog = catalog;
        }

        /**
         * 重写 equals 和 hashcode 用于 集合类
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return name.equals(pair.name) && catalog.equals(pair.catalog);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + catalog.hashCode();
            return result;
        }
    }
}
