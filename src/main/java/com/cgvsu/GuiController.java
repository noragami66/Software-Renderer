package com.cgvsu;

import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.LightSource;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;


import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {
    private final List<Model> models = new ArrayList<>();

    String modelName;

    private final List<LightSource> lightSources = new ArrayList<>();
    private LightSource selectedLightSource = null;
    private Button previousLightSourceSelectButton = null;

    final private float TRANSLATION = 0.5F;

    @FXML
    private VBox modelListVBox;

    @FXML
    private VBox camerasVBox;

    @FXML
    private ComboBox<String> textureComboBox;

    @FXML
    private VBox lightSourcesVBox;

    private List<Camera> cameras = new ArrayList<>();
    private Camera selectedCamera;

    @FXML
    private TextField xCoordTextField;
    @FXML
    private TextField yCoordTextField;
    @FXML
    private TextField zCoordTextField;

    @FXML
    private TextField xScaleCoords;
    @FXML
    private TextField yScaleCoords;
    @FXML
    private TextField zScaleCoords;

    @FXML
    private TextField xTransformCoords;
    @FXML
    private TextField yTransformCoords;
    @FXML
    private TextField zTransformCoords;

    @FXML
    private TextField xRotateCoords;
    @FXML
    private TextField yRotateCoords;
    @FXML
    private TextField zRotateCoords;


    @FXML
    private Pane topMenuPane;

    @FXML
    private MenuBar topMenuBar;

    private TitledPane modelsTitledPane;

    private TitledPane camerasTitledPane;

    @FXML
    AnchorPane anchorPane;

    private Button previousCameraSelectButton = null;
    private Button previousModelSelectButton = null;

    @FXML
    private Canvas canvas;

    @FXML
    private Canvas mainCanvas;

    @FXML
    private VBox sidePanel;

    @FXML
    private Slider sliderFOV;
    @FXML
    private Slider sliderAR;

    @FXML
    private ToggleButton togglePolygonMesh;

    @FXML
    private ToggleButton toggleUseTexture;

    @FXML
    private ToggleButton toggleUseLighting;

    @FXML
    private CheckMenuItem menuPolygonMesh;

    @FXML
    private CheckMenuItem menuUseTexture;

    @FXML
    private CheckMenuItem menuUseLighting;

    private Button previousEditButton = null;

    private Button previousTextureSelectButton;

    @FXML
    private ListView<String> vertexListView;

    @FXML
    private HBox hboxFOV;

    @FXML
    private HBox hboxAR;

    private Model selectedModel;

    private Matrix4f modelViewProjectionMatrix;
    @FXML
    private ToggleButton toggleVertices;

    @FXML
    private RadioButton textureRadioButton;
    @FXML
    private RadioButton colorRadioButton;
    @FXML
    private ColorPicker colorPicker;

    private String selectedTexture;

    @FXML
    private VBox textureListVBox;

    @FXML
    private MenuItem lightThemeMenuItem;

    @FXML
    private MenuItem darkThemeMenuItem;

    private Model mesh = null;

    private Camera mainCamera = new Camera(
            new Vector3f(0, 00, 200),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 200);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            mainCamera.setAspectRatio((float) (width / height));

            for (Model model : models) {
                RenderEngine.render(canvas.getGraphicsContext2D(), mainCamera, model, (int) width, (int) height);
            }
        });


        timeline.getKeyFrames().add(frame);
        timeline.play();


        bindToggleAndMenuItem(togglePolygonMesh, menuPolygonMesh);

        bindToggleAndMenuItem(toggleUseTexture, menuUseTexture);

        bindToggleAndMenuItem(toggleUseLighting, menuUseLighting);

        togglePolygonMesh.setOnAction(event -> handlePolygonMesh());
        menuPolygonMesh.setOnAction(event -> handlePolygonMeshMenuItem());

        toggleUseTexture.setOnAction(event -> handleUseTexture());
        menuUseTexture.setOnAction(event -> handleUseTextureMenuItem());

        toggleUseLighting.setOnAction(event -> handleUseLighting());
        menuUseLighting.setOnAction(event -> handleUseLightingMenuItem());

        textureRadioButton.setOnAction(e -> handleRadioButtonChange(textureRadioButton));
        colorRadioButton.setOnAction(e -> handleRadioButtonChange(colorRadioButton));

        vertexListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleVertexSelection());

        toggleVertices.setOnAction(event -> handleToggleVerticesAction());
    }

    public void handleToggleVerticesAction() {
        if (selectedModel == null) {
            showMessage("Модель не выбрана!");
            toggleVertices.setSelected(false);
            return;
        }

        if (selectedCamera == null) {
            showMessage("Камера не выбрана!");
            toggleVertices.setSelected(false);
            return;
        }

        RenderEngine.toggleVerticesVisibility(selectedModel);
        renderScene();
    }

    private void updateButtonState() {
        if (selectedModel != null) {
            if (selectedModel.isVerticesVisible()){
                toggleVertices.setSelected(true);
            }else {
                toggleVertices.setSelected(false);
            }
        }
    }

    private void updateVertexList() {
        vertexListView.getItems().clear();

        if (selectedModel != null) {
            for (int i = 0; i < selectedModel.vertices.size(); i++) {
                com.cgvsu.math.Vector3f vertex = selectedModel.vertices.get(i);
                vertexListView.getItems().add("Vertex " + i + ": (" + vertex.getX() + ", " + vertex.getY() + ", " + vertex.getZ() + ")");
            }
        }
    }

    @FXML
    private void handleVertexSelection() {
        int selectedIndex = vertexListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex >= 0 && selectedModel != null) {
            RenderEngine.setSelectedVertexIndex(selectedIndex);
            renderScene();
        }
    }

    private void renderScene() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        selectedCamera.setAspectRatio((float) (canvasWidth / canvasHeight));

        for (Model model : models) {
            RenderEngine.render(gc,selectedCamera, model, (int) canvasWidth, (int) canvasHeight);
        }
    }

    @FXML
    private void handleDeleteVertex() {
        if (selectedModel != null) {
            RenderEngine.deleteSelectedVertex(selectedModel);
            updateVertexList();
            renderScene();
        }
    }


    /*   <----------------------------БЛОК ОБРАБОТКИ----------------------->   */
    private void bindToggleAndMenuItem(ToggleButton toggleButton, CheckMenuItem checkMenuItem) {
        toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> checkMenuItem.setSelected(newVal));
        checkMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> toggleButton.setSelected(newVal));
    }


    private void handlePolygonMesh() {
        if (selectedModel != null) {
            if (togglePolygonMesh.isSelected()) {
                showMessage("Полигональная сетка применена к: " + selectedModel.toString());
            } else {
                showMessage("Удалена сетка с модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            togglePolygonMesh.setSelected(false);
        }
    }

    private void handlePolygonMeshMenuItem() {
        if (selectedModel != null) {
            if (menuPolygonMesh.isSelected()) {
                showMessage("Полигональная сетка применена к: " + selectedModel.toString());
            } else {
                showMessage("Удалена сетка с модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            menuPolygonMesh.setSelected(false);
        }
    }

    private void handleUseTexture() {
        if (selectedModel != null) {
            if (toggleUseTexture.isSelected()) {
                showMessage("Применена текстура к модели: " + selectedModel.toString());
            } else {
                showMessage("Удалена текстура модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            toggleUseTexture.setSelected(false);
        }
    }

    private void handleUseTextureMenuItem() {
        if (selectedModel != null) {
            if (menuUseTexture.isSelected()) {
                showMessage("Применена текстура к модели: " + selectedModel.toString());
            } else {
                showMessage("Удалена текстура модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            menuUseTexture.setSelected(false);
        }
    }

    private void handleUseLighting() {
        if (selectedModel != null) {
            if (toggleUseLighting.isSelected()) {
                showMessage("Освещение применено к модели: " + selectedModel.toString());
            } else {
                showMessage("Удалено освещение для модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            toggleUseLighting.setSelected(false);
        }
    }

    private void handleUseLightingMenuItem() {
        if (selectedModel != null) {
            if (menuUseLighting.isSelected()) {
                showMessage("Освещение применено к модели: " + selectedModel.toString());
            } else {
                showMessage("Удалено освещение для модели: " + selectedModel.toString());
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            menuUseLighting.setSelected(false);
        }
    }
    /*   <----------------------------БЛОК ОБРАБОТКИ----------------------->   */



    /*   <----------------------------БЛОК СООБЩЕНИЕ----------------------->   */
    private void showMessage(String message) {
        Stage errorStage = new Stage();
        errorStage.initStyle(StageStyle.TRANSPARENT);

        Text errorText = new Text(message);
        errorText.getStyleClass().add("error-text");

        TextArea errorTextArea = new TextArea(message);
        errorTextArea.setWrapText(true);
        errorTextArea.setEditable(false);
        errorTextArea.getStyleClass().add("error-textarea");

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> errorStage.close());
        closeButton.getStyleClass().add("close-button");

        StackPane root = new StackPane();
        root.getChildren().addAll(errorText, closeButton);
        root.getStyleClass().add("error-window");

        StackPane.setAlignment(errorTextArea, Pos.CENTER);
        StackPane.setAlignment(closeButton, Pos.BOTTOM_CENTER);

        Scene scene = new Scene(root, 800, 100);
        scene.getStylesheets().add(getClass().getResource("/com/cgvsu/fxml/showBox.css").toExternalForm());
        errorStage.setScene(scene);

        errorStage.show();
    }
    /*   <----------------------------БЛОК СООБЩЕНИЕ----------------------->   */

    /*   <----------------------------БЛОК TRS----------------------->   */

    private void slidePanelIn() {
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePanel);
        slideIn.setToX(180);
        slideIn.play();
    }

    private void slidePanelOut() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), sidePanel);
        slideOut.setToX(0);
        slideOut.play();
    }

    @FXML
    private void onApplyTransformClick() {
        try {
            float x = Float.parseFloat(xTransformCoords.getText());
            float y = Float.parseFloat(yTransformCoords.getText());
            float z = Float.parseFloat(zTransformCoords.getText());
            applyTransform(x, y, z);
        } catch (NumberFormatException e) {
            showMessage("Возникла ошибка: укажите числовые значения для переноса модели");
        }
    }

    @FXML
    private void onApplyRotateClick() {
        try {
            float x = Float.parseFloat(xRotateCoords.getText());
            float y = Float.parseFloat(yRotateCoords.getText());
            float z = Float.parseFloat(zRotateCoords.getText());
            applyRotate(x, y, z);
        } catch (NumberFormatException e) {
            showMessage("Возникла ошибка: укажите числовые значения для поворота модели в пространстве");
        }
    }

    @FXML
    private void onApplyScaleClick() {
        try {
            float x = Float.parseFloat(xScaleCoords.getText());
            float y = Float.parseFloat(yScaleCoords.getText());
            float z = Float.parseFloat(zScaleCoords.getText());
            applyScale(x, y, z);
        } catch (NumberFormatException e) {
            showMessage("Возникла ошибка: укажите числовые значения для масштабирования модели");
        }
    }

    public void applyTransform(double x, double y, double z) {
        if (selectedModel != null) {
            showMessage("Трансформация успешно применена к модели: " + selectedModel.toString() + " с следующими значениями" + " x = " + x + " y = " + y + " z = " + z);
            System.out.println("Трансформация успешно применена к модели: " + selectedModel.toString() + " с следующими значениями" + " x = " + x + " y = " + y + " z = " + z);
        }else {
            showMessage("Возникла ошибка, пожалуйста нажмите кнопку 'Выбрать' рядом с нужной моделью в меню справа");
        }
    }

    public void applyRotate(double x, double y, double z) {
        if (selectedModel != null) {
            showMessage("Поворот успешно применен к модели: " + selectedModel.toString() + " с следующими значениями" + " x = " + x + " y = " + y + " z = " + z);
            System.out.println("Rotate model: " + selectedModel.toString() + " with params" + " x = " + x + " y = " + y + "z = " + z);
        }else {
            showMessage("Возникла ошибка, пожалуйста нажмите кнопку 'Выбрать' рядом с нужной моделью в меню справа");
        }
    }

    public void applyScale(double x, double y, double z) {
        if (selectedModel != null) {
            showMessage("Изменение масштаба успешно применено к модели: " + selectedModel.toString() + " с следующими значениями" + " x = " + x + " y = " + y + " z = " + z);
            System.out.println("Изменение масштаба успешно применено к модели: " + selectedModel.toString() + " с следующими значениями" + " x = " + x + " y = " + y + " z = " + z);
        } else {
            showMessage("Возникла ошибка, пожалуйста нажмите кнопку 'Выбрать' рядом с нужной моделью в меню справа");
        }
    }

    /*   <----------------------------БЛОК TRS----------------------->   */


    /*   <----------------------------БЛОК СВЕТА----------------------->   */
    @FXML
    private void onAddLightSourceClick(ActionEvent event) {
        LightSource newLight = new LightSource("Light " + lightSources.size(), new com.cgvsu.math.Vector3f(0, 10, 0)
        );
        lightSources.add(newLight);

        if (selectedLightSource != null) {
            updateSelectedLightSource(selectedLightSource, false);
        }

        selectedLightSource = newLight;

        HBox lightRow = createLightSourceRow("Light " + lightSources.size(), newLight);
        lightSourcesVBox.getChildren().add(lightRow);

        System.out.println("Источник освещения успешно добавлен: " + newLight.getName());
    }



    private HBox createLightSourceRow(String lightName, LightSource lightSource) {
        HBox lightRow = new HBox(15);
        lightRow.setAlignment(Pos.CENTER_LEFT);

        Label lightLabel = new Label(lightName);
        lightLabel.setStyle("-fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> {
            if (previousLightSourceSelectButton != null) {
                previousLightSourceSelectButton.setStyle("");
            }

            if (selectedLightSource != null) {
                updateSelectedLightSource(selectedLightSource, false);
            }

            selectedLightSource = lightSource;
            updateSelectedLightSource(selectedLightSource, true);

            System.out.println("Выбран источник света: " + lightName);
            selectButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f; ");

            previousLightSourceSelectButton = selectButton;
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            lightSources.remove(lightSource);
            lightSourcesVBox.getChildren().remove(lightRow);
            if (selectedLightSource == lightSource) {
                selectedLightSource = null;
            }
            updateSelectedLightSource(selectedLightSource, selectedLightSource != null);
            System.out.println("Вы удалили источник света: " + lightName);
        });

        buttonBox.getChildren().addAll(selectButton, deleteButton);

        lightRow.getChildren().addAll(lightLabel, buttonBox);

        return lightRow;
    }

    private void updateSelectedLightSource(LightSource lightSource, boolean isSelected) {
        for (int i = 0; i < lightSourcesVBox.getChildren().size(); i++) {
            HBox lightRow = (HBox) lightSourcesVBox.getChildren().get(i);
            LightSource rowLightSource = lightSources.get(i);

            if (rowLightSource == lightSource) {
                if (isSelected) {
                    lightRow.setStyle("-fx-background-color: #1f1f1f;");
                } else {
                    lightRow.setStyle("");
                }
            }
        }
    }
    /*   <----------------------------БЛОК СВЕТА----------------------->   */


    /*   <----------------------------БЛОК ТЕМЫ----------------------->   */
    @FXML
    public void switchToLightTheme() {
        topMenuBar.getStylesheets().clear();
        topMenuBar.getStylesheets().add(getClass().getResource("/com/cgvsu/fxml/lightTheme.css").toExternalForm());
        topMenuPane.setStyle("-fx-background-color: gray;");
        topMenuBar.setStyle("-fx-background-color: gray;");
    }

    @FXML
    public void switchToDarkTheme() {
        topMenuBar.getStylesheets().clear();
        topMenuBar.getStylesheets().add(getClass().getResource("/com/cgvsu/fxml/menuStyleDark.css").toExternalForm());
        topMenuPane.setStyle("-fx-background-color: #1D1D1D;");
        topMenuBar.setStyle("-fx-background-color: #1D1D1D;");
    }
    /*   <----------------------------БЛОК ТЕМЫ----------------------->   */



    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */
    @FXML
    private void onAddCameraClick(ActionEvent event) {
        try {
            float x = Float.parseFloat(xCoordTextField.getText());
            float y = Float.parseFloat(yCoordTextField.getText());
            float z = Float.parseFloat(zCoordTextField.getText());

            Camera newCamera = new Camera(new Vector3f(x, y, z), new Vector3f(0, 0, 0), 1.0F, 1, 0.01F, 100);

            cameras.add(newCamera);

            if (selectedCamera != null) {
                updateSelectedCamera(selectedCamera, false);
            }

            selectedCamera = newCamera;

            HBox cameraRow = createCameraRow("Camera " + cameras.size(), newCamera);
            camerasVBox.getChildren().add(cameraRow);

            System.out.println("Создана камера с координатами: " + x + ", " + y + ", " + z);
        } catch (NumberFormatException e) {
            showMessage("Произошла ошибка, пожалуйста убедитесь, что параметры для координат камеры корректны");
        }
    }

    private HBox createCameraRow(String cameraName, Camera camera) {
        HBox cameraRow = new HBox(15);
        cameraRow.setAlignment(Pos.CENTER_LEFT);

        Label cameraLabel = new Label(cameraName);
        cameraLabel.setStyle("-fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> {
            if (previousEditButton != null && previousEditButton.getStyle().contains("-fx-background-color: #46463c")) {
                showMessage("Изменение параметров запрещено, так как активен режим редактирования.");
                return;
            }

            xCoordTextField.setText(String.valueOf(camera.getPosition().x));
            yCoordTextField.setText(String.valueOf(camera.getPosition().y));
            zCoordTextField.setText(String.valueOf(camera.getPosition().z));

            sliderFOV.setValue(camera.getFov());
            sliderAR.setValue(camera.getAspectRatio());

            if (previousCameraSelectButton != null) {
                previousCameraSelectButton.setStyle("");
            }

            if (selectedCamera != null) {
                updateSelectedCamera(selectedCamera, false);
            }

            selectedCamera = camera;
            mainCamera = selectedCamera;
            updateSelectedCamera(selectedCamera, true);

            selectButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f;");
            previousCameraSelectButton = selectButton;

            System.out.println("Выбрана камера: " + cameraName);
        });

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            if (previousEditButton != null && previousEditButton != editButton) {
                previousEditButton.setStyle("");
            }

            editButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f;");
            previousEditButton = editButton;

            xCoordTextField.setText(String.valueOf(camera.getPosition().x));
            yCoordTextField.setText(String.valueOf(camera.getPosition().y));
            zCoordTextField.setText(String.valueOf(camera.getPosition().z));
            sliderFOV.setValue(camera.getFov());
            sliderAR.setValue(camera.getAspectRatio());

            hboxAR.setVisible(true);
            hboxFOV.setVisible(true);
            xCoordTextField.setVisible(true);
            yCoordTextField.setVisible(true);
            zCoordTextField.setVisible(true);
        });

        Button saveButton = new Button("Save");
        hboxFOV.getChildren().add(saveButton);

        saveButton.setOnAction(saveEvent -> {
            try {
                saveButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f;");

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), event -> {
                    saveButton.setStyle("-fx-background-color: #1f1f1f; -fx-text-fill: #46463c;");
                }));
                timeline.play();

                float x = Float.parseFloat(xCoordTextField.getText());
                float y = Float.parseFloat(yCoordTextField.getText());
                float z = Float.parseFloat(zCoordTextField.getText());
                float fov = (float) sliderFOV.getValue();
                float aspectRatio = (float) sliderAR.getValue();

                camera.getPosition().set(x, y, z);
                camera.setFov(fov);
                camera.setAspectRatio(aspectRatio);

                hboxAR.setVisible(false);
                hboxFOV.setVisible(false);

                System.out.println("Сохранены изменения для камеры: " + cameraName);
                System.out.println("Координаты: " + camera.getPosition());
                System.out.println("FOV: " + camera.getFov());
                System.out.println("Aspect Ratio: " + camera.getAspectRatio());

                editButton.setStyle("");
                previousEditButton = null;
            } catch (NumberFormatException ex) {
                showMessage("Ошибка: Проверьте корректность введенных данных!");
            }
        });

        Button deleteButton = new Button("Del");
        deleteButton.setOnAction(e -> {
            deleteButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), event -> {
                deleteButton.setStyle("-fx-background-color: #1f1f1f; -fx-text-fill: #46463c;");
            }));
            timeline.play();

            if (previousEditButton != null && previousEditButton.getStyle().contains("-fx-background-color: #46463c")) {
                showMessage("Изменение параметров запрещено, так как активен режим редактирования.");
                return;
            }

            cameras.remove(camera);
            camerasVBox.getChildren().remove(cameraRow);
            if (selectedCamera == camera) {
                selectedCamera = null;
            }
            updateSelectedCamera(selectedCamera, selectedCamera != null);
        });

        buttonBox.getChildren().addAll(selectButton, editButton, saveButton, deleteButton);
        cameraRow.getChildren().addAll(cameraLabel, buttonBox);

        return cameraRow;
    }



    private void updateSelectedCamera(Camera camera, boolean isSelected) {
        for (int i = 0; i < camerasVBox.getChildren().size(); i++) {
            HBox cameraRow = (HBox) camerasVBox.getChildren().get(i);
            Camera rowCamera = cameras.get(i);

            if (rowCamera == camera) {
                if (isSelected) {
                    cameraRow.setStyle("-fx-background-color: #1f1f1f;");
                } else {
                    cameraRow.setStyle("");
                }
            }
        }
    }
    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */

    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */
    @FXML
    private void onAddTextureClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg)", "*.png", "*.jpg"));
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog((Stage) textureComboBox.getScene().getWindow());
        if (file == null) {
            return;
        }

        String texturePath = file.getAbsolutePath();
        String textureName = file.getName();

        // Добавление текстуры в ComboBox
        textureComboBox.getItems().add(textureName);
        textureComboBox.setValue(textureName);

        // Создание строки для отображения текстуры в списке
        HBox textureRow = createTextureRow(textureName, texturePath);
        textureListVBox.getChildren().add(textureRow);

        System.out.println("Текстура успешно загружена: " + textureName);
    }

    private HBox createTextureRow(String textureName, String texturePath) {
        HBox textureRow = new HBox(15);
        textureRow.setAlignment(Pos.CENTER_LEFT);

        Label textureLabel = new Label(textureName);
        textureLabel.setStyle("-fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        // Кнопка для выбора текстуры
        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> {
            // Если текстура уже выбрана, сбрасываем выбор
            if (previousTextureSelectButton != null) {
                previousTextureSelectButton.setStyle("");
            }

            selectedTexture = texturePath; // Обновляем выбранную текстуру
            updateSelectedTexture(texturePath, true); // Отображаем выбранную текстуру

            System.out.println("Выбрана текстура: " + textureName);
            selectButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f; "); // Стиль для выбранной текстуры

            previousTextureSelectButton = selectButton;
        });

        // Кнопка для удаления текстуры
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            textureComboBox.getItems().remove(textureName);
            textureListVBox.getChildren().remove(textureRow);
            if (selectedTexture.equals(texturePath)) {
                selectedTexture = null; // Сбрасываем выбранную текстуру, если она была удалена
            }
            updateSelectedTexture(selectedTexture, selectedTexture != null); // Обновляем отображение выбранной текстуры
            System.out.println("Вы удалили текстуру: " + textureName);
        });

        buttonBox.getChildren().addAll(selectButton, deleteButton);

        textureRow.getChildren().addAll(textureLabel, buttonBox);

        return textureRow;
    }

    // Метод для обновления отображения выбранной текстуры
    private void updateSelectedTexture(String texturePath, boolean isSelected) {
        for (int i = 0; i < textureListVBox.getChildren().size(); i++) {
            HBox textureRow = (HBox) textureListVBox.getChildren().get(i);
            String rowTexturePath = textureComboBox.getItems().get(i);

            if (rowTexturePath.equals(texturePath)) {
                if (isSelected) {
                    textureRow.setStyle("-fx-background-color: #1f1f1f;"); // Выделяем строку выбранной текстуры
                } else {
                    textureRow.setStyle(""); // Убираем выделение
                }
            } else {
                textureRow.setStyle(""); // Убираем выделение для других текстур
            }
        }
    }


    @FXML
    private void handleRadioButtonChange(RadioButton selectedRadioButton) {
        if (selectedRadioButton == textureRadioButton) {
            colorRadioButton.setSelected(false);
        }
        else if (selectedRadioButton == colorRadioButton) {
            textureRadioButton.setSelected(false);
        }
    }


    @FXML
    private void applyTextureOrColor() {
        if (textureRadioButton.isSelected()) {
            String selectedTexture = textureComboBox.getValue();
            if (selectedTexture != null) {
                showMessage("Применена текстура: " + selectedTexture);
            } else {
                showMessage("Возникла ошибка: необходимо выбрать текстуру");
            }
        } else if (colorRadioButton.isSelected()) {
            Color selectedColor = colorPicker.getValue();
            showMessage("Применен цвет: " + selectedColor);
        } else {
            showMessage("Вы должны выбрать режим текстуры или цвета");
        }
    }
    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */

    /*   <----------------------------БЛОК МОДЕЛИ----------------------->   */
    @FXML
    private void onAddModelClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) modelListVBox.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());
        String modelName = file.getName();

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);

            models.add(model);
            selectedModel = model;
            mesh = model;

            if (selectedModel != null) {
                updateSelected(selectedModel, false);
            }

            HBox modelRow = createModelRow(modelName, model);
            modelListVBox.getChildren().add(modelRow);

            System.out.println("Успешно загружена модель: " + modelName);
        } catch (IOException exception) {
            showMessage("Не удалось загрузить модель");
        }
    }


    private HBox createModelRow(String modelName, Model model) {
        HBox modelRow = new HBox(15);
        modelRow.setAlignment(Pos.CENTER_LEFT);

        Label modelLabel = new Label(modelName);
        modelLabel.setStyle("-fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(e -> {
            if (selectedModel == model) {
                updateSelected(selectedModel, false);
                selectedModel = null;
                selectButton.setStyle("");
                slidePanelOut();
                RenderEngine.resetSelectedVertex();
                updateVertexList();
            } else {
                if (previousModelSelectButton != null) {
                    previousModelSelectButton.setStyle("");
                    RenderEngine.resetSelectedVertex();
                }

                if (selectedModel != null) {
                    updateSelected(selectedModel, false);
                }

                selectedModel = model;
                updateSelected(selectedModel, true);
                System.out.println("Selected model: " + modelName);

                selectButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: #1f1f1f; ");
                previousModelSelectButton = selectButton;
                updateButtonState();
                slidePanelIn();
                updateVertexList();
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            models.remove(model);
            modelListVBox.getChildren().remove(modelRow);
            if (selectedModel == model) {
                selectedModel = null;
                slidePanelOut();
            }
            updateSelected(selectedModel, selectedModel != null);
            vertexListView.getItems().clear();
            toggleVertices.setSelected(false);
        });

        buttonBox.getChildren().addAll(selectButton, deleteButton);
        modelRow.getChildren().addAll(modelLabel, buttonBox);

        return modelRow;
    }


    private void updateSelected(Model model, boolean isSelected) {
        for (int i = 0; i < modelListVBox.getChildren().size(); i++) {
            HBox modelRow = (HBox) modelListVBox.getChildren().get(i);
            Model rowModel = models.get(i);

            if (rowModel == model) {
                if (isSelected) {
                    modelRow.setStyle("-fx-background-color: #1f1f1f;");
                } else {
                    modelRow.setStyle("");
                }
            } else {
                modelRow.setStyle("");
            }
        }
    }

    private void applyTexture(String modelName) {
        if (selectedModel != null) {
            System.out.println("Applying texture to: " + modelName);
            // !!!СДЕЛАТЬ ЛОГИКУ НАЛОЖЕНИЯ ТЕКСТУРЫ!!!
        } else {
            System.out.println("No model selected for texture application.");
        }
    }

    private void deleteTexture(String modelName) {
        if (selectedModel != null) {
            System.out.println("Deleting texture of: " + modelName);
            // !!!СДЕЛАТЬ ЛОГИКУ УДАЛЕНИЯ ТЕКСТУРЫ!!!
        } else {
            System.out.println("No texture to delete");
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        if (selectedModel == null) {
            System.out.println("No model selected to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ObjWriter.write(selectedModel, file.getAbsolutePath());
            System.out.println("Модель успешно сохранена: " + file.getAbsolutePath());
        } catch (IOException exception) {
            showMessage("Не удалось сохранить файл: " + exception.getMessage());
        }
    }
/*   <----------------------------БЛОК МОДЕЛИ----------------------->   */

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        mainCamera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}