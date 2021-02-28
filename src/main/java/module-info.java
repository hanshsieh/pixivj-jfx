module com.github.hanshsieh.pixivjjfx {
    requires javafx.controls;
    requires javafx.web;
    requires pixivj;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;
    requires urlbuilder;
    requires org.apache.commons.lang3;
    exports com.github.hanshsieh.pixivjjfx;
    exports com.github.hanshsieh.pixivjjfx.pkce;

}