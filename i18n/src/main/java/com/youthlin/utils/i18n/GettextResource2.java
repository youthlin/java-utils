package com.youthlin.utils.i18n;

import gnu.gettext.GettextResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 把父类<code>GettextResource</code>两个私有方法变成包范围.
 * <p>
 * 非公开类，包可见权限.
 * <p>
 * Created by lin on 2017-01-30-030.
 */
abstract class GettextResource2 extends GettextResource {
    static final String CONTEXT_GLUE = "\u0004";

    /**
     * Like gettext(catalog,msgid), except that it returns <CODE>null</CODE>
     * when no translation was found.
     */
    static String gettextnull(ResourceBundle catalog, String msgid) {
        try {
            if (catalog.containsKey(msgid))
                return (String) catalog.getObject(msgid);
            else
                return null;
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * Like ngettext(catalog,msgid,msgid_plural,n), except that it returns
     * <CODE>null</CODE> when no translation was found.
     */
    static String ngettextnull(ResourceBundle catalog, String msgid, long n) {
        // The reason why we use so many reflective API calls instead of letting
        // the GNU gettext generated ResourceBundles implement some interface,
        // is that we want the generated ResourceBundles to be completely
        // standalone, so that migration from the Sun approach to the GNU gettext
        // approach (without use of plurals) is as straightforward as possible.

        // ResourceBundle origCatalog = catalog;
        do {
            // Try catalog itself.
            if (verbose)
                System.out.println("ngettext on " + catalog);
            Method handleGetObjectMethod = null;
            Method getParentMethod = null;
            try {
                handleGetObjectMethod = catalog.getClass().getMethod("handleGetObject", String.class);
                getParentMethod = catalog.getClass().getMethod("getParent");
            } catch (NoSuchMethodException ignore) {
            } catch (SecurityException ignore) {
            }
            if (verbose)
                System.out.println("handleGetObject = " + (handleGetObjectMethod != null) + ", getParent = " + (getParentMethod != null));
            if (handleGetObjectMethod != null
                    && Modifier.isPublic(handleGetObjectMethod.getModifiers())
                    && getParentMethod != null) {
                // A GNU gettext created class.
                Method lookupMethod = null;
                Method pluralEvalMethod = null;
                try {
                    lookupMethod = catalog.getClass().getMethod("lookup", String.class);
                    pluralEvalMethod = catalog.getClass().getMethod("pluralEval", Long.TYPE);
                } catch (NoSuchMethodException ignore) {
                } catch (SecurityException ignore) {
                }
                if (verbose)
                    System.out.println("lookup = " + (lookupMethod != null) + ", pluralEval = " + (pluralEvalMethod != null));
                if (lookupMethod != null && pluralEvalMethod != null) {
                    // A GNU gettext created class with plural handling.
                    Object localValue = null;
                    try {
                        localValue = lookupMethod.invoke(catalog, msgid);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.getTargetException().printStackTrace();
                    }
                    if (localValue != null) {
                        if (verbose)
                            System.out.println("localValue = " + localValue);
                        if (localValue instanceof String)
                            // Found the value. It doesn't depend on n in this case.
                            return (String) localValue;
                        else {
                            String[] pluralforms = (String[]) localValue;
                            long i = 0;
                            try {
                                i = (Long) pluralEvalMethod.invoke(catalog, n);
                                if (!(i >= 0 && i < pluralforms.length))
                                    i = 0;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.getTargetException().printStackTrace();
                            }
                            return pluralforms[(int) i];
                        }
                    }
                } else {
                    // A GNU gettext created class without plural handling.
                    Object localValue = null;
                    try {
                        localValue = handleGetObjectMethod.invoke(catalog, msgid);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.getTargetException().printStackTrace();
                    }
                    if (localValue != null) {
                        // Found the value. It doesn't depend on n in this case.
                        if (verbose)
                            System.out.println("localValue = " + localValue);
                        return (String) localValue;
                    }
                }
                Object parentCatalog = catalog;
                try {
                    parentCatalog = getParentMethod.invoke(catalog);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.getTargetException().printStackTrace();
                }
                if (parentCatalog != catalog)
                    catalog = (ResourceBundle) parentCatalog;
                else
                    break;
            } else
                // Not a GNU gettext created class.
                break;
        } while (catalog != null);
        // The end of chain of GNU gettext ResourceBundles is reached.
        if (catalog != null) {
            // For a non-GNU ResourceBundle we cannot access 'parent' and
            // 'handleGetObject', so make a single call to catalog and all
            // its parent catalogs at once.
            Object value;
            try {
                value = catalog.getObject(msgid);
            } catch (MissingResourceException e) {
                value = null;
            }
            if (value != null)
                // Found the value. It doesn't depend on n in this case.
                return (String) value;
        }
        // Default: null.
        return null;
    }
}
