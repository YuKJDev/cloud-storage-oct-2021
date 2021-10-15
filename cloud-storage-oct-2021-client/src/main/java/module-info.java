module cloudstorageoct2021client{
        requires javafx.fxml;
        requires javafx.controls;
        requires javafx.graphics;

        requires org.controlsfx.controls;
        requires com.dlsc.formsfx;

        opens com.geekbrains.io to javafx.fxml;
        exports com.geekbrains.io;

        }