module com.yusufatakanozmen.tankapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires com.almasb.fxgl.all;

    opens com.yusufatakanozmen.tankapp to javafx.fxml;
    exports com.yusufatakanozmen.tankapp;
}