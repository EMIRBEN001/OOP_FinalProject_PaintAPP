module com.example.oop_project2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;


    opens com.example.oop_project2 to javafx.fxml;
    exports com.example.oop_project2;
    exports javafx.embed.swing;
    opens javafx.embed.swing to javafx.fxml;
}