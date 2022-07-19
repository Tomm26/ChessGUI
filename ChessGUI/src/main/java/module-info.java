module com.example.chessgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.chessgui to javafx.fxml;
    exports chessgui;
}