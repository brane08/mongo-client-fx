open module brane.fx.mongo {
    requires java.sql;
    requires kotlin.stdlib.jdk8;
    requires kotlin.reflect;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.google.guice;
    requires exposed.core;
    requires exposed.dao;
    requires slf4j.api;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
}
