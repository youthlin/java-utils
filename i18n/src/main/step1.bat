xgettext -k__ -k_x:2c,1 -k_n:1,2 -k_nx:3c,1,2  -o resources/Message.pot java/com/youthlin/utils/i18n/Translation.java --from-code=UTF-8
copy resources\Message.pot resources\Message_en.po
copy resources\Message.pot resources\Message_zh_CN.po
