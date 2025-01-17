package com.cgvsu;

import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.LightSource;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.File;
import java.util.*;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;

import static com.cgvsu.SceneTools.*;
import static com.cgvsu.render_engine.RenderEngine.*;

public class GuiController {
    final private float TRANSLATION = 0.5F;

    @FXML
    private VBox textureListVBox;

    @FXML
    private VBox lightSourcesVBox;

    @FXML
    private ComboBox<String> textureComboBox;

    @FXML
    private TextField xCoordTextField;
    @FXML
    private TextField yCoordTextField;
    @FXML
    private TextField zCoordTextField;

    @FXML
    private TextField xCoordTextFieldLigtning;
    @FXML
    private TextField yCoordTextFieldLigtning;
    @FXML
    private TextField zCoordTextFieldLigtning;

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
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox sidePanel;

    @FXML
    private ToggleButton togglePolygonMesh;


    @FXML
    private ToggleButton toggleUseLighting;

    @FXML
    private CheckMenuItem menuPolygonMesh;


    @FXML
    private CheckMenuItem menuUseLighting;

    @FXML
    private ListView<String> vertexListView;

    @FXML
    private ListView<Model> modelListView;

    @FXML
    private ToggleButton toggleVertices;

    @FXML
    private ListView<Camera> camerasListView;

    @FXML
    private ListView<LightSource> lightningListView;

    @FXML
    private RadioButton textureRadioButton;

    @FXML
    private RadioButton colorRadioButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private VBox sideBox;

    @FXML
    private TitledPane selectedTitledPane;

    @FXML
    private TitledPane editingTitledPane;

    @FXML
    private TitledPane modelsTitledPane;

    @FXML
    private TitledPane camerasTitledPane;

    @FXML
    private TitledPane textureTitledPane;

    @FXML
    private TitledPane lightningTitledPane;

    @FXML
    private ScrollPane sideBoxScrollPane;

    private final Set<TitledPane> openedTitledPanes = new HashSet<>();

    private boolean isDarkTheme = true;

    private Camera mainCamera = new Camera(
            new Vector3f(0, 0, 200),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 200);

    //init
    //INIT
    //штше
    //ШТШЕ

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        Timeline timeline = new Timeline();
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

        bindToggleAndMenuItem(toggleUseLighting, menuUseLighting);

        togglePolygonMesh.setOnAction(event -> handlePolygonMesh());
        menuPolygonMesh.setOnAction(event -> handlePolygonMeshMenuItem());

        toggleUseLighting.setOnAction(event -> handleUseLighting());
        menuUseLighting.setOnAction(event -> handleUseLightingMenuItem());

        colorPicker.setValue(Color.BLACK);

        colorRadioButton.setSelected(true);
        colorPicker.setVisible(true);
        textureComboBox.setVisible(false);
        textureComboBox.getItems().add("default");

        textureRadioButton.setOnAction(e -> {
            if (!textureRadioButton.isSelected()) {
                textureRadioButton.setSelected(true);
            }
            handleRadioButtonChange(textureRadioButton);
        });

        colorRadioButton.setOnAction(e -> {
            if (!colorRadioButton.isSelected()) {
                colorRadioButton.setSelected(true);
            }
            handleRadioButtonChange(colorRadioButton);
        });

        textureComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if ("default".equals(   newValue)) {
                    showMessage("Текстура по умолчанию выбрана"); // опасная заглушка
                    textureRadioButton.setSelected(false);
                    colorRadioButton.setSelected(true);
                    colorPicker.setVisible(true);
                    textureComboBox.setVisible(false);
                    selectedModel.setUsingColor(true);
                    selectedModel.setUsingTexture(false);
                } else {
                    chooseTexture(newValue);
                    applyTextureOrColor();
                }
            }
        });


        colorPicker.setOnAction(e -> applyTextureOrColor());
        vertexListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vertexListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                handleVertexSelection();
                event.consume();
            }
        });
        toggleVertices.setOnAction(event -> handleToggleVerticesAction());

        cameras = FXCollections.observableArrayList();
        camerasListView.setItems((ObservableList<Camera>) cameras);

        camerasListView.setCellFactory(e -> {
            ListCell<Camera> cell = new ListCell<>() {
                @Override
                protected void updateItem(Camera camera, boolean empty) {
                    super.updateItem(camera, empty);
                    if (empty || camera == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText("Camera (" + camera.getPosition().x + ", " + camera.getPosition().y + ", " + camera.getPosition().z + ")");

                        ContextMenu contextMenu = new ContextMenu();

                        MenuItem editItem = new MenuItem("Edit");
                        editItem.setOnAction(e -> {
                            if (selectedCamera != null) {
                                openEditDialog(camera, isDarkTheme);
                            } else {
                                showMessage("Сначала выберите камеру (ЛКМ) перед редактированием.");
                            }
                        });

                        MenuItem deleteItem = new MenuItem("Delete");
                        deleteItem.setOnAction(e -> SceneTools.removeCamera(camera));

                        if (isDarkTheme) {
                            contextMenu.setStyle("");
                            editItem.setStyle("");
                            deleteItem.setStyle("");
                            contextMenu.setStyle("");
                            contextMenu.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

                            editItem.setStyle("-fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                            deleteItem.setStyle("-fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                        }
                        else {
                            contextMenu.setStyle("");
                            editItem.setStyle("");
                            deleteItem.setStyle("");
                            contextMenu.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                            editItem.setStyle("-fx-background-color: #c0c0c0; -fx-text-fill: black;");
                            deleteItem.setStyle("-fx-background-color: #c0c0c0; -fx-text-fill: black;");
                        }

                        contextMenu.getItems().addAll(editItem, deleteItem);
                        setContextMenu(contextMenu);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !cell.isEmpty()) {
                    selectedCamera = cell.getItem();
                    mainCamera = new Camera(selectedCamera); // Обсудить с вторым участником
                    System.out.println("Выбрана камера: " + selectedCamera.getPosition());
                }
            });

            return cell;
        });

        lightSources = FXCollections.observableArrayList();
        lightningListView.setItems((ObservableList<LightSource>) lightSources);

        lightningListView.setCellFactory(e -> {
            ListCell<LightSource> cell = new ListCell<>() {
                @Override
                protected void updateItem(LightSource lightSource, boolean empty) {
                    super.updateItem(lightSource, empty);
                    if (empty || lightSource == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText("Light Source (" + lightSource.getPosition().getX() + ", " + lightSource.getPosition().getY() + ", " + lightSource.getPosition().getZ() + ")");

                        ContextMenu contextMenu = new ContextMenu();

                        MenuItem editItem = new MenuItem("Edit");
                        editItem.setOnAction(e -> {
                            if (selectedCamera != null) {
//                                openEditDialog(lightSource, isDarkTheme);
                            } else {
                                showMessage("Сначала выберите источник света (ЛКМ) перед редактированием.");
                            }
                        });

                        MenuItem deleteItem = new MenuItem("Delete");
                        deleteItem.setOnAction(e -> SceneTools.removeLightSource(lightSource));

                        if (isDarkTheme) {
                            contextMenu.setStyle("");
                            editItem.setStyle("");
                            deleteItem.setStyle("");
                            contextMenu.setStyle("");
                            contextMenu.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

                            editItem.setStyle("-fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                            deleteItem.setStyle("-fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                        }
                        else {
                            contextMenu.setStyle("");
                            editItem.setStyle("");
                            deleteItem.setStyle("");
                            contextMenu.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                            editItem.setStyle("-fx-background-color: #c0c0c0; -fx-text-fill: black;");
                            deleteItem.setStyle("-fx-background-color: #c0c0c0; -fx-text-fill: black;");
                        }

                        contextMenu.getItems().addAll(editItem, deleteItem);
                        setContextMenu(contextMenu);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !cell.isEmpty()) {
                    selectedLightSource = cell.getItem();
                    System.out.println("Выбрана камера: " + selectedLightSource.getPosition());
                }
            });

            return cell;
        });

        modelListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);

                if (empty || model == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(model.getName());

                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> {
                        SceneTools.deleteModel(model);
                        modelListView.getItems().remove(model);
                        slidePanelOut();
                        vertexListView.getItems().clear();
                        resetSelectedVertices();
                        updateButtonState();
                    });

                    if (isDarkTheme) {
                        contextMenu.setStyle("");
                        deleteItem.setStyle("");
                        contextMenu.setStyle("");
                        contextMenu.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                        deleteItem.setStyle("-fx-text-fill: white; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
                    } else {
                        contextMenu.setStyle("");
                        deleteItem.setStyle("");
                        contextMenu.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        deleteItem.setStyle("-fx-background-color: #c0c0c0; -fx-text-fill: black;");
                    }

                    contextMenu.getItems().add(deleteItem);

                    setContextMenu(contextMenu);

                    setOnMouseClicked(event -> {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            if (selectedModel == model) {
                                selectedModel = null;
                                updateButtonState();
                                slidePanelOut();
                                vertexListView.getItems().clear();
                                resetSelectedVertices();
                            } else {
                                selectedModel = model;
                                updateButtonState();
                                slidePanelIn();
                                updateVertexList();
                                resetSelectedVertices();
                            }
                        }
                    });
                }
            }
        });
    }
    /*   <----------------------------БЛОК ВЫНОСА----------------------->   */
    public void setSelectionEditingPane() {
            selectedTitledPane = editingTitledPane;
            System.out.println("Selected TitledPane: " + editingTitledPane.getText());
            handleMouseDraggedEditingPane();
    }

    public void setSelectionModelsPane() {
        selectedTitledPane = modelsTitledPane;
        System.out.println("Selected TitledPane: " + modelsTitledPane.getText());
        handleMouseDraggedEditingPane();
    }

    public void setSelectionCamerasPane() {
        selectedTitledPane = camerasTitledPane;
        System.out.println("Selected TitledPane: " + camerasTitledPane.getText());
        handleMouseDraggedEditingPane();
    }

    public void setSelectionTexturesPane() {
        selectedTitledPane = textureTitledPane;
        System.out.println("Selected TitledPane: " + textureTitledPane.getText());
        handleMouseDraggedEditingPane();
    }

    public void setSelectionLightningPane() {
        selectedTitledPane = lightningTitledPane;
        System.out.println("Selected TitledPane: " + lightningTitledPane.getText());
        handleMouseDraggedEditingPane();
    }

        public void handleMouseDraggedEditingPane() {
        if (selectedTitledPane != null) {
            createNewWindowWithTitledPane(selectedTitledPane);
        } else {
            System.out.println("No TitledPane is selected!");
        }
    }

    private void slideOutSideBoxScrollPane() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), sideBoxScrollPane);
        slideOut.setToX(sideBoxScrollPane.getWidth());
        slideOut.setOnFinished(event -> sideBoxScrollPane.setVisible(false));
        slideOut.play();
    }

    private void slideInSideBoxScrollPane() {
        sideBoxScrollPane.setVisible(true);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), sideBoxScrollPane);
        slideIn.setToX(0);
        slideIn.play();
    }



    private void createNewWindowWithTitledPane(TitledPane selectedTitledPane) {
        if (openedTitledPanes.contains(selectedTitledPane)) {
            return;
        }
        openedTitledPanes.add(selectedTitledPane);
        Stage editingWindow = new Stage();
        editingWindow.initStyle(StageStyle.TRANSPARENT);
        int originalIndex = sideBox.getChildren().indexOf(selectedTitledPane);
        sideBox.getChildren().remove(selectedTitledPane);
        if (sideBox.getChildren().isEmpty()) {
            slideOutSideBoxScrollPane();
//            objectBorderPane.setMinWidth(canvas.getWidth());
        }
        HBox container = new HBox();
        Button returnButton = new Button("Back");
        returnButton.setMinWidth(75);
        selectedTitledPane.setExpanded(true);
        container.getChildren().addAll(returnButton, selectedTitledPane);
        Scene scene = new Scene(container);
        if (!isDarkTheme){
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/LightTheme.css")).toExternalForm());
        }else{
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/DarkTheme.css")).toExternalForm());
        }
        scene.getRoot().setStyle("-fx-background-color: transparent;");
        scene.getRoot().setStyle("-fx-background-color: #1d1d1d;");
        returnButton.setStyle("-fx-background-color: #46463c; -fx-text-fill: white;");
        editingWindow.setScene(scene);

        double width = selectedTitledPane.getMaxWidth() + 75;
        double height = selectedTitledPane.getMaxHeight();

        editingWindow.setWidth(width);
        editingWindow.setHeight(height);
        returnButton.setOnAction(event -> {
            container.getChildren().remove(returnButton);
            if (originalIndex >= 0 && originalIndex < sideBox.getChildren().size()) {
                sideBox.getChildren().add(originalIndex, selectedTitledPane);
            } else {
                sideBox.getChildren().add(selectedTitledPane);
            }
            if (!sideBox.getChildren().isEmpty()) {
                slideInSideBoxScrollPane();
//                objectBorderPane.setMinWidth(Region.USE_COMPUTED_SIZE);
            }

            selectedTitledPane.setVisible(true);
            editingWindow.close();

            openedTitledPanes.remove(selectedTitledPane);
        });

        final double[] mouseX = {0};
        final double[] mouseY = {0};
        scene.setOnMousePressed(event -> {
            mouseX[0] = event.getScreenX() - editingWindow.getX();
            mouseY[0] = event.getScreenY() - editingWindow.getY();
        });

        scene.setOnMouseDragged(event -> {
            editingWindow.setX(event.getScreenX() - mouseX[0]);
            editingWindow.setY(event.getScreenY() - mouseY[0]);
        });

        editingWindow.setOnCloseRequest(event -> {
            container.getChildren().remove(returnButton);
            if (originalIndex >= 0 && originalIndex < sideBox.getChildren().size()) {
                sideBox.getChildren().add(originalIndex, selectedTitledPane);
            } else {
                sideBox.getChildren().add(selectedTitledPane);
            }
            if (!sideBox.getChildren().isEmpty()) {
                slideInSideBoxScrollPane();
//                objectBorderPane.setMinWidth(Region.USE_COMPUTED_SIZE);
            }


            selectedTitledPane.setVisible(true);

            openedTitledPanes.remove(selectedTitledPane);
        });
        editingWindow.show();
    }
    /*   <----------------------------БЛОК ВЫНОСА----------------------->   */


    /*   <----------------------------DRAG AND DROP----------------------->   */
    @FXML
    private BorderPane objectBorderPane;

    public void handleDragOverObj(DragEvent event) {
        if (event.getGestureSource() != objectBorderPane && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    public void handleDragEnteredObj(DragEvent event) {
        if (event.getGestureSource() != objectBorderPane && event.getDragboard().hasFiles()) {
            objectBorderPane.setStyle("-fx-background-color: #46463c; -fx-opacity: 0.25");
            applyBackgroundAndBlurEffect(true);
        }
        event.consume();
    }

    private void applyBackgroundAndBlurEffect(boolean apply) {
        Timeline timeline = new Timeline();

        GaussianBlur blur = new GaussianBlur();
        KeyFrame blurKeyFrame = new KeyFrame(Duration.millis(150),
                new javafx.animation.KeyValue(blur.radiusProperty(), apply ? 15 : 0));
            if (apply) {
                objectBorderPane.setOpacity(0);
            } else {
                objectBorderPane.setOpacity(0.25);
            }
            KeyFrame opacityKeyFrame = new KeyFrame(Duration.millis(150),
                    new javafx.animation.KeyValue(objectBorderPane.opacityProperty(), apply ? 0.25 : 0));
            timeline.getKeyFrames().add(opacityKeyFrame);

        timeline.getKeyFrames().add(blurKeyFrame);

        canvas.setEffect(blur);

        timeline.play();
    }

    public void handleDragExitedObj(DragEvent event) {
        applyBackgroundAndBlurEffect(false);
        event.consume();
    }

    public void handleDropObj(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            for (File file : files) {
                if (file.getName().endsWith(".obj")) {
                    loadModelFromFile(file);
                    Model model = SceneTools.addModel(file);
                    modelListView.getItems().add(model);
                    if (model != null) {
                        success = true;
                    } else {
                        showMessage("Не удалось загрузить модель: " + file.getName());
                    }
                } else {
                    showMessage("Недопустимый файл: " + file.getName());
                }
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }
    /*   <----------------------------DRAG AND DROP----------------------->   */


    /*   <----------------------------БЛОК С ВЕРШИНАМИ----------------------->   */

    private void renderScene() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        mainCamera.setAspectRatio((float) (canvasWidth / canvasHeight));

        for (Model model : models) {
            RenderEngine.render(gc,mainCamera, model, (int) canvasWidth, (int) canvasHeight);
        }
    }

    public void handleToggleVerticesAction() {
        if (selectedModel == null) {
            showMessage("Модель не выбрана!");
            toggleVertices.setSelected(false);
            return;
        }

        RenderEngine.toggleVerticesVisibility(selectedModel);
        renderScene();
    }

    private void updateButtonState() {
        if (selectedModel == null) {
            toggleVertices.setSelected(false);
            toggleUseLighting.setSelected(false);
            togglePolygonMesh.setSelected(false);
        } else {
            toggleVertices.setSelected(selectedModel.isVerticesVisible());
            toggleUseLighting.setSelected(selectedModel.isLightingEnabled());
            togglePolygonMesh.setSelected(selectedModel.isPolygonMeshEnabled());
            if (selectedModel.isUsingTexture()) {
                textureRadioButton.setSelected(true);
                textureComboBox.setVisible(true);
                textureComboBox.getSelectionModel().select(selectedModel.getTexture());
                colorRadioButton.setSelected(false);
                colorPicker.setVisible(false);
            } else {
                textureRadioButton.setSelected(false);
                textureComboBox.setVisible(false);
                colorRadioButton.setSelected(true);
                colorPicker.setVisible(true);
                colorPicker.setValue(selectedModel.getColor());
            }
        }
    }

    @FXML
    private void handleDeleteVertices() {
        if (selectedModel == null) {
            showMessage("Модель не выбрана!");
            return;
        }
        deleteSelectedVertices(selectedModel);
        updateVertexList();
        renderScene();
    }

    @FXML
    private void handleVertexSelection() {
        ObservableList<Integer> selectedIndices = vertexListView.getSelectionModel().getSelectedIndices();

        for (Integer index : selectedIndices) {
            if (selectedVertexIndices.contains(index)) {
                selectedVertexIndices.remove(index);
            } else {
                selectedVertexIndices.add(index);
            }
        }
        updateVertexList();
    }

    private void updateVertexList() {
        vertexListView.getItems().clear();

        if (selectedModel != null) {
            for (int i = 0; i < selectedModel.vertices.size(); i++) {
                com.cgvsu.math.vector.Vector3f vertex = selectedModel.vertices.get(i);
                vertexListView.getItems().add("Vertex " + i + ": (" + vertex.getX() + ", " + vertex.getY() + ", " + vertex.getZ() + ")");
            }
        }

        vertexListView.setCellFactory(param -> new VertexListCell(isDarkTheme));
    }

    public class VertexListCell extends ListCell<String> {
        private final CheckBox checkBox;
        private final Text vertexText;
        private boolean isDarkTheme;

        public VertexListCell(boolean isDarkTheme) {
            this.isDarkTheme = isDarkTheme;
            HBox hbox = new HBox(10);
            vertexText = new Text();
            checkBox = new CheckBox();
            hbox.getChildren().addAll(vertexText, checkBox);
            setGraphic(hbox);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                vertexText.setText(item);
                vertexText.setFill(isDarkTheme ? Color.WHITE : Color.BLACK);

                int index = getIndex();

                checkBox.setSelected(selectedVertexIndices.contains(index));
                checkBox.setOnAction(event -> toggleSelection(index));
                vertexText.setOnMouseClicked(event -> toggleSelection(index));

                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.getChildren().add(checkBox);
                hbox.getChildren().add(vertexText);
                hbox.setAlignment(Pos.CENTER_LEFT);

                setGraphic(hbox);
            }
        }

        private void toggleSelection(int index) {
            if (selectedVertexIndices.contains(index)) {
                selectedVertexIndices.remove(Integer.valueOf(index));
            } else {
                selectedVertexIndices.add(index);
            }

            updateVertexList();
        }
    }

    /*   <----------------------------БЛОК С ВЕРШИНАМИ----------------------->   */



    /*   <----------------------------БЛОК ОБРАБОТКИ----------------------->   */
    private void bindToggleAndMenuItem(ToggleButton toggleButton, CheckMenuItem checkMenuItem) {
        toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> checkMenuItem.setSelected(newVal));
        checkMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> toggleButton.setSelected(newVal));
    }

    private void handlePolygonMesh() {
        if (selectedModel != null) {
            if (togglePolygonMesh.isSelected()) {
                showMessage("Полигональная сетка применена к: " + selectedModel.getName());
                selectedModel.setPolygonMeshEnabled(true);
            } else {
                showMessage("Удалена сетка с модели: " + selectedModel.getName());
                selectedModel.setPolygonMeshEnabled(false);
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            togglePolygonMesh.setSelected(false);
        }
    }

    private void handlePolygonMeshMenuItem() {
        if (selectedModel != null) {
            if (menuPolygonMesh.isSelected()) {
                showMessage("Полигональная сетка применена к: " + selectedModel.getName());
                selectedModel.setPolygonMeshEnabled(true);
            } else {
                showMessage("Удалена сетка с модели: " + selectedModel.getName());
                selectedModel.setPolygonMeshEnabled(false);
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            menuPolygonMesh.setSelected(false);
        }
    }

    private void handleUseLighting() {
        if (selectedModel != null) {
            if (toggleUseLighting.isSelected()) {
                showMessage("Освещение применено к модели: " + selectedModel.getName());
                selectedModel.setLightingEnabled(true);
            } else {
                showMessage("Удалено освещение для модели: " + selectedModel.getName());
                selectedModel.setLightingEnabled(false);
            }
        } else {
            showMessage("Возникла ошибка, выберете модель, чтобы применить к ней изменения");
            toggleUseLighting.setSelected(false);
        }
    }

    private void handleUseLightingMenuItem() {
        if (selectedModel != null) {
            if (menuUseLighting.isSelected()) {
                showMessage("Освещение применено к модели: " + selectedModel.getName());
                selectedModel.setLightingEnabled(true);
            } else {
                showMessage("Удалено освещение для модели: " + selectedModel.getName());
                selectedModel.setLightingEnabled(false);
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
        if (isDarkTheme){
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/showBox.css")).toExternalForm());
        } else {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/showBoxLight.css")).toExternalForm());
        }
        errorStage.setScene(scene);

        errorStage.show();
    }
    /*   <----------------------------БЛОК СООБЩЕНИЕ----------------------->   */

    /*   <----------------------------БЛОК TRS----------------------->   */

    private void slidePanelIn() {
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePanel);
        slideIn.setToX(200);
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
            SceneTools.applyTransform(selectedModel, x, y, z);
            showMessage("Трансформация: " + selectedModel.getName()
                    + " с x = " + x + " y = " + y + " z = " + z);
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
            SceneTools.applyRotate(selectedModel, x, y, z);
            showMessage("Поворот: " + selectedModel.getName()
                    + " с  x = " + x + " y = " + y + " z = " + z);
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
            SceneTools.applyScale(selectedModel, x, y, z);
            showMessage("Изменение масштаба: " + selectedModel.getName()
                    + " с x = " + x + " y = " + y + " z = " + z);
        } catch (NumberFormatException e) {
            showMessage("Возникла ошибка: укажите числовые значения для масштабирования модели");
        }
    }
    /*   <----------------------------БЛОК TRS----------------------->   */

    /*   <----------------------------БЛОК ТЕМЫ----------------------->   */
    private final List<String> originalStylesheets = new ArrayList<>();

    @FXML
    public void switchToLightTheme() {
        isDarkTheme = !isDarkTheme;
        if (originalStylesheets.isEmpty()) {
            originalStylesheets.addAll(anchorPane.getStylesheets());
        }

        clearStyles(anchorPane);
        anchorPane.getStylesheets().clear();
        anchorPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/LightTheme.css")).toExternalForm());
        updateVertexList();
    }

    private void clearStyles(Parent parent) {
        parent.getStylesheets().clear();

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Parent) {
                clearStyles((Parent) node);
            }

            if (!node.styleProperty().isBound()) {
                node.setStyle("");
            }
        }
    }

    @FXML
    public void switchToDarkTheme() {
        isDarkTheme = true;
        clearStyles(anchorPane);
        anchorPane.getStylesheets().clear();
        anchorPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/DarkTheme.css")).toExternalForm());
        updateVertexList();
    }
    /*   <----------------------------БЛОК ТЕМЫ----------------------->   */

    /*   <----------------------------БЛОК СВЕТА----------------------->   */

    @FXML
    private ColorPicker colorPickerLightning;

    @FXML
    private void onAddLightSourceClick() {
        try {
            float x = Float.parseFloat(xCoordTextFieldLigtning.getText());
            float y = Float.parseFloat(yCoordTextFieldLigtning.getText());
            float z = Float.parseFloat(zCoordTextFieldLigtning.getText());
//            Color color = colorPickerLightning.getValue();

            LightSource newLightSource = SceneTools.createLightSource(x, y, z);

            lightningListView.getItems().add(newLightSource);
            lightningListView.getSelectionModel().select(newLightSource);
        } catch (NumberFormatException e) {
            showMessage("Убедитесь, что параметры для координат источника света корректны");
        }
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


    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */

    private void openEditDialog(Camera camera, boolean isDarkTheme) {
        if (camera == null) return;

        Stage editStage = new Stage();
        editStage.initStyle(StageStyle.TRANSPARENT);

        editStage.setTitle("Edit Camera");
        editStage.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label positionLabel = new Label("Camera Position:");
        TextField xField = new TextField(String.valueOf(camera.getPosition().x));
        xField.setPromptText("X Coordinate");
        TextField yField = new TextField(String.valueOf(camera.getPosition().y));
        yField.setPromptText("Y Coordinate");
        TextField zField = new TextField(String.valueOf(camera.getPosition().z));
        zField.setPromptText("Z Coordinate");

        Label targetLabel = new Label("Camera Target:");
        TextField targetXField = new TextField(String.valueOf(camera.getTarget().x));
        targetXField.setPromptText("Target X Coordinate");
        TextField targetYField = new TextField(String.valueOf(camera.getTarget().y));
        targetYField.setPromptText("Target Y Coordinate");
        TextField targetZField = new TextField(String.valueOf(camera.getTarget().z));
        targetZField.setPromptText("Target Z Coordinate");

        Label fovLabel = new Label("Field of View:");
        Slider fovSlider = new Slider(0.1, 180, camera.getFov());
        fovSlider.setShowTickLabels(true);
        fovSlider.setShowTickMarks(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        layout.getChildren().addAll(
                new Label("Edit Camera Parameters"),
                positionLabel,
                new Label("X:"), xField,
                new Label("Y:"), yField,
                new Label("Z:"), zField,
                targetLabel,
                new Label("Target X:"), targetXField,
                new Label("Target Y:"), targetYField,
                new Label("Target Z:"), targetZField,
                fovLabel, fovSlider,
                buttonBox
        );

        Scene scene = new Scene(layout, 350, 600);
        if (isDarkTheme) {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/DarkTheme.css")).toExternalForm());
        } else {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/cgvsu/fxml/LightTheme.css")).toExternalForm());
        }
        editStage.setScene(scene);

        saveButton.setOnAction(e -> {
            try {
                float x = Float.parseFloat(xField.getText());
                float y = Float.parseFloat(yField.getText());
                float z = Float.parseFloat(zField.getText());

                float targetX = Float.parseFloat(targetXField.getText());
                float targetY = Float.parseFloat(targetYField.getText());
                float targetZ = Float.parseFloat(targetZField.getText());

                float fov = (float) fovSlider.getValue();

                SceneTools.setCamera(camera, x, y, z, targetX, targetY, targetZ, fov);

                camerasListView.refresh();

                editStage.close();
            } catch (NumberFormatException ex) {
                showMessage("Ошибка: некорректные значения в полях");
            }
        });

        cancelButton.setOnAction(e -> editStage.close());

        editStage.showAndWait();
    }

    @FXML
    private void onAddCameraClick() {
        try {
            float x = Float.parseFloat(xCoordTextField.getText());
            float y = Float.parseFloat(yCoordTextField.getText());
            float z = Float.parseFloat(zCoordTextField.getText());

            Camera newCamera = SceneTools.createCamera(x, y, z);

            camerasListView.getItems().add(newCamera);
            camerasListView.getSelectionModel().select(newCamera);
        } catch (NumberFormatException e) {
            showMessage("Произошла ошибка, пожалуйста убедитесь, что параметры для координат камеры корректны");
        }
    }

    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */

    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */
    @FXML
    private void onAddTextureClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg)", "*.png", "*.jpg"));
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog(textureComboBox.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String textureName = SceneTools.addTexture(file.getName());
            textureComboBox.getItems().add(textureName);

            HBox textureRow = createTextureRow(textureName, file.getName(),isDarkTheme);
            textureListVBox.getChildren().add(textureRow);

        } catch (IllegalArgumentException e) {
            showMessage("Ошибка: " + e.getMessage());
        }
    }

    private HBox createTextureRow(String textureName, String texturePath, boolean isDarkTheme) {
        HBox textureRow = new HBox(15);
        textureRow.setAlignment(Pos.CENTER_LEFT);

        Label textureLabel = new Label(textureName);
        if (isDarkTheme) {
            textureLabel.setStyle("-fx-text-fill: white;");
        } else {
            textureLabel.setStyle("-fx-text-fill: black;");
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            textureComboBox.getItems().remove(textureName);
            textureListVBox.getChildren().remove(textureRow);
            if (selectedTexture.equals(texturePath)) {
                selectedTexture = null;
            }
            System.out.println("Вы удалили текстуру: " + textureName);
        });

        buttonBox.getChildren().addAll(deleteButton);

        textureRow.getChildren().addAll(textureLabel, buttonBox);

        return textureRow;
    }

    @FXML
    private void handleRadioButtonChange(RadioButton selectedRadioButton) {
        if (selectedRadioButton == textureRadioButton) {
            colorRadioButton.setSelected(false);

            colorPicker.setVisible(false);
            textureComboBox.setVisible(true);
        } else if (selectedRadioButton == colorRadioButton) {
            textureRadioButton.setSelected(false);

            colorPicker.setVisible(true);
            textureComboBox.setVisible(false);
        }
    }

    @FXML
    private void applyTextureOrColor() {
        if (selectedModel == null) {
            showMessage("Ошибка: необходимо выбрать модель.");
            return;
        }

        if (textureRadioButton.isSelected()) {
            if (selectedTexture != null) {
                SceneTools.setTexture();
                showMessage("Применена текстура: " + selectedTexture + " к модели " + selectedModel.getName());
            } else {
                showMessage("Ошибка: необходимо выбрать текстуру.");
                textureComboBox.getSelectionModel().clearSelection();
            }
        } else if (colorRadioButton.isSelected()) {
            selectedColor = colorPicker.getValue();
            SceneTools.setColor();
            showMessage("Применен цвет: " + selectedColor + " к модели " + selectedModel.getName());
        } else {
            showMessage("Ошибка: необходимо выбрать текстуру или цвет.");
        }
    }
    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */

    /*   <----------------------------БЛОК МОДЕЛИ----------------------->   */
    @FXML
    private void onAddModelClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog(modelListView.getScene().getWindow());
        Model model = SceneTools.addModel(file);

        if (model != null) {
            modelListView.getItems().add(model);
        }
    }


    @FXML
    private void onSaveModelMenuItemClick() {
        if (selectedModel == null) {
            showMessage("Выберите модель");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ObjWriter.write(selectedModel, file.getAbsolutePath());
            showMessage("Модель успешно сохранена: " + file.getAbsolutePath());
        } catch (IOException exception) {
            showMessage("Не удалось сохранить файл: " + exception.getMessage());
        }
    }
/*   <----------------------------БЛОК МОДЕЛИ----------------------->   */

    @FXML
    public void handleCameraForward() {
        mainCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward() {
        mainCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft() {
        mainCamera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight() {
        mainCamera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp() {
        mainCamera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown() {
        mainCamera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}