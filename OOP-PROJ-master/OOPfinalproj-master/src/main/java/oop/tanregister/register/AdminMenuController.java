package oop.tanregister.register;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import oop.tanregister.model.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Optional;

public class AdminMenuController implements Initializable {

    public Button customerButton;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> productID;
    @FXML private TableColumn<Product, String> productName;
    @FXML private TableColumn<Product, String> productType;
    @FXML private TableColumn<Product, Integer> ProductStock;
    @FXML private TableColumn<Product, Double> ProductPrice;
    @FXML private TableColumn<Product, Date> ProductDate;

    @FXML private TextField IDField;
    @FXML private TextField ProductNameField;
    @FXML private ComboBox<String> TypeField;
    @FXML private TextField StockField;
    @FXML private TextField Price;
    @FXML private Button UpdateButton;
    @FXML private Button ClearButton;
    @FXML private Button DeleteButton;
    @FXML private Button importButton;
    @FXML private Label SignOut;
    @FXML private ImageView productImageView;

    private byte[] currentImageBytes;

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

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                IDField.setText(newSelection.getId());
                ProductNameField.setText(newSelection.getName());
                TypeField.setValue(newSelection.getType());
                StockField.setText(String.valueOf(newSelection.getStock()));
                Price.setText(String.valueOf(newSelection.getPrice()));
                currentImageBytes = newSelection.getImageBytes();

                if (currentImageBytes != null && currentImageBytes.length > 0) {
                    productImageView.setImage(new Image(new java.io.ByteArrayInputStream(currentImageBytes)));
                } else {
                    productImageView.setImage(null);
                }
            }
        });
    }

    private void loadProducts() {
        List<Product> products = ProductData.findAll();
        ObservableList<Product> productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);
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
            product.setDate(new Date());
            product.setImageBytes(currentImageBytes);

            ProductData.insert(product);
            loadProducts();
            clearForm();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }

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

        try {
            int stock = Integer.parseInt(stockText);
            double price = Double.parseDouble(priceText);

            selectedProduct.setId(id);
            selectedProduct.setName(name);
            selectedProduct.setType(type);
            selectedProduct.setStock(stock);
            selectedProduct.setPrice(price);
            selectedProduct.setDate(new Date());
            selectedProduct.setImageBytes(currentImageBytes);

            ProductData.update(selectedProduct);
            loadProducts();

            showAlert(Alert.AlertType.INFORMATION, "Updated", "Product updated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void handleDelete(ActionEvent event) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a product to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete " + selectedProduct.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ProductData.delete(selectedProduct.getId());
            loadProducts();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Product deleted successfully!");
        }
    }

    @FXML
    public void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] imageBytes = new byte[(int) file.length()];
                fis.read(imageBytes);
                fis.close();

                currentImageBytes = imageBytes;
                productImageView.setImage(new Image(file.toURI().toString()));

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not read image file.");
            }
        }
    }

    @FXML
    public void handleClear(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        IDField.clear();
        ProductNameField.clear();
        TypeField.getSelectionModel().clearSelection();
        StockField.clear();
        Price.clear();
        productImageView.setImage(null);
        currentImageBytes = null;
    }

    public void handleSignOut(MouseEvent mouseEvent) throws Exception {
        Stage currentStage = (Stage) SignOut.getScene().getWindow();
        currentStage.close();
        Login loginApp = new Login();
        Stage stage = new Stage();
        loginApp.start(stage);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
