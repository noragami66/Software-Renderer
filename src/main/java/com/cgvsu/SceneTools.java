package com.cgvsu;

import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.LightSource;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SceneTools {
    public static List<Model> models = new ArrayList<>();
    public static List<Camera> cameras = new ArrayList<>();
    public static List<LightSource> lightSources = new ArrayList<>();
    public static List<String> Textures = new ArrayList<>();

    public static String selectedTexture;
    public static Model selectedModel;
    public static Camera selectedCamera;
    public static LightSource selectedLightSource = null;
    public static Color selectedColor;

    public static Button previousLightSourceSelectButton = null;

    /*   <----------------------------БЛОК МОДЕЛИ----------------------->   */
    public static Model deleteModel(Model model) {
        if (model != null && models != null) {
            models.remove(model);
            if (selectedModel == model) {
                selectedModel = null;
            }
        }
        return selectedModel;
    }

    public static Model addModel(File file) {
        if (file == null) {
            return null;
        }

        Model model = loadModelFromFile(file);
        if (model != null) {
            model.setName(file.getName());
            models.add(model);
            return model;
        } else {
            System.err.println("Не удалось загрузить модель из файла: " + file.getName());
            return null;
        }
    }

    public static Model loadModelFromFile(File file) {
        Path fileName = Path.of(file.getAbsolutePath());
        try {
            String fileContent = Files.readString(fileName);
            return ObjReader.read(fileContent);
        } catch (IOException exception) {
            System.err.println("Не удалось загрузить модель: " + exception.getMessage());
            return null;
        }
    }
    /*   <----------------------------БЛОК МОДЕЛИ----------------------->   */

    /*   <----------------------------БЛОК СВЕТА----------------------->   */
    public static LightSource createLightSource(float x, float y, float z) {
        return new LightSource("Light", new com.cgvsu.math.Vector3f(x, y, z));
    }
    /*   <----------------------------БЛОК СВЕТА----------------------->   */


    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */
    public static void setTexture() {
        if (selectedModel != null && selectedTexture != null) {
            selectedModel.setUsingTexture(true);
            selectedModel.setUsingColor(false);
            selectedModel.setTexture(selectedTexture);
        }
    }

    public static void setColor() {
        if (selectedModel != null && selectedColor != null) {
            selectedModel.setUsingTexture(false);
            selectedModel.setUsingColor(true);
            selectedModel.setColor(selectedColor);
        }
    }

    public static void chooseTexture(String textureName) {
        if (Textures.contains(textureName)) {
            selectedTexture = textureName;
            System.out.println("Выбрана текстура: " + selectedTexture);
        } else {
            throw new IllegalArgumentException("Текстура не найдена: " + textureName);
        }
    }

    public static String addTexture(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            throw new IllegalArgumentException("Путь к текстуре не может быть пустым.");
        }

        String textureName = texturePath.substring(texturePath.lastIndexOf("/") + 1);
        Textures.add(texturePath);
        System.out.println("Текстура успешно добавлена: " + textureName);

        return textureName;
    }
    /*   <----------------------------БЛОК ТЕКСТУРЫ----------------------->   */

    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */
    public static void setCamera(Camera camera, float x, float y, float z, float targetX, float targetY, float targetZ, float fov) {
        camera.setPosition(new Vector3f(x, y, z));
        camera.setTarget(new Vector3f(targetX, targetY, targetZ));
        camera.setFov(fov);

        System.out.println("Камера обновлена: позиция = (" + x + ", " + y + ", " + z + "), цель = (" + targetX + ", " + targetY + ", " + targetZ + "), FOV = " + fov);
    }


    public static Camera createCamera(float x, float y, float z) {
        return new Camera(new Vector3f(x, y, z), new Vector3f(0, 0, 0), 1.0F, 1, 0.01F, 100);
    }

    public static void removeCamera(Camera camera) {
        if (camera == null) return;

        cameras.remove(camera);
        if (selectedCamera == camera) {
            selectedCamera = null;
        }
        System.out.println("Удалена камера: " + camera.getPosition());
    }
    /*   <----------------------------БЛОК КАМЕРЫ----------------------->   */

    /*   <----------------------------БЛОК TRS----------------------->   */
    public static void applyTransform(Object selectedModel, double x, double y, double z) {
        if (selectedModel != null) {
            System.out.println("Трансформация успешно применена к модели: " + selectedModel.toString()
                    + " с следующими значениями x = " + x + " y = " + y + " z = " + z);
        } else {
            throw new IllegalStateException("Ошибка: Модель не выбрана. Пожалуйста, выберите модель.");
        }
    }

    public static void applyRotate(Object selectedModel, double x, double y, double z) {
        if (selectedModel != null) {
            System.out.println("Поворот успешно применен к модели: " + selectedModel.toString()
                    + " с следующими значениями x = " + x + " y = " + y + " z = " + z);
        } else {
            throw new IllegalStateException("Ошибка: Модель не выбрана. Пожалуйста, выберите модель.");
        }
    }

    public static void applyScale(Object selectedModel, double x, double y, double z) {
        if (selectedModel != null) {
            System.out.println("Изменение масштаба успешно применено к модели: " + selectedModel.toString()
                    + " с следующими значениями x = " + x + " y = " + y + " z = " + z);
        } else {
            throw new IllegalStateException("Ошибка: Модель не выбрана. Пожалуйста, выберите модель.");
        }
    }
    /*   <----------------------------БЛОК TRS----------------------->   */
}