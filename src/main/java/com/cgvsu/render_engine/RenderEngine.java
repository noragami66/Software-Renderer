package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.multiplyMatrix4ByVector3;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;

public class RenderEngine {

    private static boolean verticesVisible = false;

    private static final double POINT_SIZE = 5.0;

    private static int selectedVertexIndex = -1;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = model.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            Polygon polygon = model.polygons.get(polygonInd);
            final int nVerticesInPolygon = polygon.getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = model.vertices.get(polygon.getVertexIndices().get(vertexInPolygonInd));

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
            }
        }

        if (model.isVerticesVisible()) {
            renderVertices(graphicsContext, model, modelViewProjectionMatrix, width, height);
        }
    }

    public static void toggleVerticesVisibility(Model selectedModel) {
        if (selectedModel != null) {
            selectedModel.setVerticesVisible(!selectedModel.isVerticesVisible());
        }
    }


    public static void setSelectedVertexIndex(int index) {
        if (index >= 0) {
            selectedVertexIndex = index;
        } else {
            selectedVertexIndex = -1;
        }
    }

    public static void resetSelectedVertex() {
        selectedVertexIndex = -1;
    }

    private static void renderVertices(
            final GraphicsContext graphicsContext,
            final Model model,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height) {

        final int nVertices = model.vertices.size();

        for (int i = 0; i < nVertices; i++) {
            Vector3f vertex = model.vertices.get(i);

            javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

            Point2f screenPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);

            if (i == selectedVertexIndex) {
                graphicsContext.setFill(Color.RED);
            } else {
                graphicsContext.setFill(Color.BLUE);
            }

            graphicsContext.fillOval(screenPoint.x - POINT_SIZE / 2, screenPoint.y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
        }
    }

    public static void deleteSelectedVertex(Model model) {
        if (selectedVertexIndex != -1) {
            model.vertices.remove(selectedVertexIndex);

            for (Polygon polygon : model.polygons) {
                polygon.getVertexIndices().removeIf(index -> index == selectedVertexIndex);
                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                    if (polygon.getVertexIndices().get(i) > selectedVertexIndex) {
                        polygon.getVertexIndices().set(i, polygon.getVertexIndices().get(i) - 1);
                    }
                }
            }

            selectedVertexIndex = -1;
        }
    }
}
