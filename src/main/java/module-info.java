open module brane.fx.mongo {
    requires java.sql;
    requires jdk.net;
    requires kotlin.stdlib.jdk8;
    requires kotlin.reflect;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.google.guice;
    requires slf4j.api;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires exposed.core;
    requires exposed.dao;
    requires javax.inject;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
}
