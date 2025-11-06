package oop.tanregister.register;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import oop.tanregister.model.Product;
import oop.tanregister.register.ProductData;

public class AdminMenuController implements Initializable {

    @FXML
    private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> productID;
    @FXML private TableColumn<Product, String> productName;
    @FXML private TableColumn<Product, String> productType;
    @FXML private TableColumn<Product, Integer> ProductStock;
    @FXML private TableColumn<Product, Double> ProductPrice;
    @FXML private TableColumn<Product, java.util.Date> ProductDate;

    @FXML
    private TextField IDField;
    @FXML
    private TextField ProductNameField;
    @FXML
    private ComboBox<String> TypeField;
    @FXML
    private TextField StockField;
    @FXML
    private TextField Price;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TypeField.getItems().addAll("Mockups", "Mugs", "Books");

        productID.setCellValueFactory(new PropertyValueFactory<>("id"));
        productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        productType.setCellValueFactory(new PropertyValueFactory<>("type"));
        ProductStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        ProductPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        ProductDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadProducts();
    }


    @FXML
    private void handleAdd(ActionEvent event) {
        String id = IDField.getText().trim();
        String name = ProductNameField.getText().trim();
        String type = (TypeField.getValue() != null) ? TypeField.getValue() : "";
        String stockText = StockField.getText().trim();
        String priceText = Price.getText().trim();

        if (id.isEmpty() || name.isEmpty() || type.isEmpty() ||
                stockText.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all fields.");
            return;
        }

        int stock;
        double price;
        try {
            stock = Integer.parseInt(stockText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Stock and Price must be numeric values.");
            return;
        }

        try {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setType(type);
            product.setStock(stock);
            product.setPrice(price);

            ProductData.insert(product);
            loadProducts();

            showAlert(Alert.AlertType.INFORMATION, "Product Added",
                    String.format("Product '%s' added successfully!", name));

            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add product: " + e.getMessage());
        }
    }

    private void clearForm() {
        IDField.clear();
        ProductNameField.clear();
        if (TypeField != null) TypeField.getSelectionModel().clearSelection();
        StockField.clear();
        Price.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadProducts() {
        List<Product> products = ProductData.findAll();
        ObservableList<Product> productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);
    }

}
