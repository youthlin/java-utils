# i18n
Gettext / i18n / l10n / Java  
WordPress 风格翻译



0. 工具下载： https://mlocati.github.io/articles/gettext-iconv-windows.html
1. 使用下方命令提取待翻译字符串，或使用 Poedit 等工具提取  
   >`xgettext -k__ -k_x:2c,1 -k_n:1,2 -k_nx:4c,1,2 -o resources\Message.pot java\com\youthlin\utils\i18n\Translation.java --from-code UTF-8`
2. 手动编辑 pot 文件翻译为目标语言另存为 po 文件，或使用 Poedit 等工具
3. 使用下方命令生成 Java 资源文件(不是 properties 资源文件而是 class 资源文件) --source 选项可以只生成 java 文件 不生成 class 文件  
   >`msgfmt --java2 -d resources -r Message -l zh_CN resources\Message_zh_CN.po`
4. 在 Java 代码中通过 <code>ResourceBundle.getBundle("Message");</code> 注册翻译资源包


>See Also http://youthlin.com/?p=1315
