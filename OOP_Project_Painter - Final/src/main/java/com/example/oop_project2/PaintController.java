package com.example.oop_project2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

// these are all the necessary imports so that i can save a file
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

// linked list for Data Structure
import java.util.LinkedList;

// random is specifically for the graphite brush
import java.util.Random;



public class PaintController {
    @FXML // these are all the buttons for the brush so that you can draw on it
    private Button brushButton;
    private Button roundBrushButton;
    private Button squareBrushButton;
    private Button graphiteBrushButton;
    private Button rectangularBrushButton;
    private Button circleBrushButton;

    private ContextMenu brushSelectionMenu;

    @FXML
    private Button saveButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Slider brushSizeSlider;
    
    @FXML
    private Canvas canvas;

    private GraphicsContext graphicsContext;
    private Brush currentBrush;

    private double lastX;
    private double lastY;

    private Tool selectedTool;

    private enum Tool {
        BRUSH, ERASER
    }


    private abstract class Brush { // create an abstract brush class so that i don't need to write the attributes for the other brush types
        protected double size;
        protected Color color;
        protected LinkedList<Stroke> strokes; // i used a linked list for the strokes

        public Brush(double size, Color color) {
            this.size = size;
            this.color = color;
            this.strokes = new LinkedList<>();
        }

        public abstract void startDrawing(double x, double y);

        public abstract void draw(double x, double y);

        public abstract void stopDrawing();

        public LinkedList<Stroke> getStrokes()
        {
            return strokes;
        }
    }
    private class Stroke {
        private LinkedList<Point> points;

        public Stroke() {
            this.points = new LinkedList<>();
        }

        public LinkedList<Point> getPoints() {

            return points;
        }

        public void addPoint(double x, double y) {

            points.add(new Point(x, y));
        }
    }
    private class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {

            return x;
        }

        public double getY() {

            return y;
        }

        public double distanceTo(Point other) {
            double deltaX = other.getX() - x;
            double deltaY = other.getY() - y;
            return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        }

        public double angleTo(Point other) {
            double deltaX = other.getX() - x;
            double deltaY = other.getY() - y;
            return Math.atan2(deltaY, deltaX);
        }
    }

    private class RoundBrush extends Brush {
        public RoundBrush(double size, Color color) {

            super(size, color);
        }

        @Override
        public void startDrawing(double x, double y) {
            graphicsContext.setFill(color);
            graphicsContext.fillOval(x, y, size, size);
        }

        @Override
        public void draw(double x, double y)
        {
            graphicsContext.fillOval(x, y, size, size);
        }

        @Override
        public void stopDrawing() {
            // No additional action needed
        }
    }

    private class SquareBrush extends Brush {
        public SquareBrush(double size, Color color) {

            super(size, color);
        }

        @Override
        public void startDrawing(double x, double y) {
            graphicsContext.setFill(color);
            graphicsContext.fillRect(x, y, size, size);
        }

        @Override
        public void draw(double x, double y)
        {
            graphicsContext.fillRect(x, y, size, size);
        }

        @Override
        public void stopDrawing() {
            // No additional action needed
        }
    }
    private class GraphiteBrush extends Brush {
        private Random random;

        public GraphiteBrush(double size, Color color) {
            super(size, color);
            this.random = new Random();
        }

        @Override
        public void startDrawing(double x, double y) {

            drawGraphiteBrush(x, y);
        }

        @Override
        public void draw(double x, double y) {

            drawGraphiteBrush(x, y);
        }

        @Override
        public void stopDrawing() {
            // No additional action needed
        }

        private void drawGraphiteBrush(double x, double y) {
            double radius = size / 2;
            double area = Math.PI * radius * radius;

            int numParticles = (int) (area / 4); // Adjust the density of particles as needed

            graphicsContext.setFill(color);

            for (int i = 0; i < numParticles; i++) {
                double offsetX = random.nextDouble() * size - radius;
                double offsetY = random.nextDouble() * size - radius;

                graphicsContext.fillOval(x + offsetX, y + offsetY, 1, 1);
            }
        }
    }

    private class CircleBrush extends Brush {
        private double centerX;
        private double centerY;
        private double radius;

        public CircleBrush(double size, Color color) {
            super(size, color);
        }

        @Override
        public void startDrawing(double x, double y) {
            centerX = x;
            centerY = y;
            radius = 0;
            drawCircle();
        }

        @Override
        public void draw(double x, double y) {
            double deltaX = x - centerX;
            double deltaY = y - centerY;
            radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            drawCircle();
        }

        @Override
        public void stopDrawing() {
            drawCircle();
        }

        private void drawCircle() {
            graphicsContext.setFill(color);
            graphicsContext.setStroke(color);
            graphicsContext.setLineWidth(size);
            graphicsContext.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }

    private class RectangularBrush extends Brush {
        private double startX;
        private double startY;
        private double endX;
        private double endY;

        public RectangularBrush(double size, Color color) {
            super(size, color);
        }

        @Override
        public void startDrawing(double x, double y) {
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }

        @Override
        public void draw(double x, double y) {
            endX = x;
            endY = y;
            drawRectangle();
        }

        @Override
        public void stopDrawing() {
            drawRectangle();
        }
        private void drawRectangle() {
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);
            double upperLeftX = Math.min(startX, endX);
            double upperLeftY = Math.min(startY, endY);

            graphicsContext.setFill(color);
            graphicsContext.fillRect(upperLeftX, upperLeftY, width, height);
        }
    }

    @FXML
    private void initialize() {
        graphicsContext = canvas.getGraphicsContext2D();
        brushSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentBrush != null) {
                currentBrush.size = newValue.doubleValue();
            }
        });
        saveButton.setOnAction(this::handleSaveButtonAction);
        colorPicker.setOnAction(this::handleColorPickerAction);
        brushButton.setOnAction(this::handleBrushButtonAction);
        canvas.setOnMousePressed(this::handleCanvasMousePressed);
        canvas.setOnMouseDragged(this::handleCanvasMouseDragged);
        canvas.setOnMouseReleased(this::handleCanvasMouseReleased);

        // Set default brush to RoundBrush
        currentBrush = new RoundBrush(brushSizeSlider.getValue(), colorPicker.getValue());
        selectedTool = Tool.BRUSH;

        // Create brush selection buttons
        brushSelectionMenu = new ContextMenu();
        roundBrushButton = createBrushSelectionButton("Round Brush", RoundBrush.class);
        squareBrushButton = createBrushSelectionButton("Square Brush", SquareBrush.class);
        graphiteBrushButton = createBrushSelectionButton("Graphite Brush", GraphiteBrush.class);
        rectangularBrushButton = createBrushSelectionButton("Rectangular Brush", RectangularBrush.class);
        circleBrushButton = createBrushSelectionButton("Circle Brush", CircleBrush.class);
    }
    private Button createBrushSelectionButton(String buttonText, Class<? extends Brush> brushClass) {
        Button button = new Button(buttonText);
        button.setOnAction(event -> {
            try {
                currentBrush = brushClass.getDeclaredConstructor(double.class, Color.class)
                        .newInstance(brushSizeSlider.getValue(), colorPicker.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return button;
    }


    @FXML
    private void handleBrushButtonAction(ActionEvent event) {
        // Create a dialog to select the brush type
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Brush Selection");
        alert.setHeaderText("Select a Brush");

        ButtonType roundBrushButton = new ButtonType("Round Brush");
        ButtonType squareBrushButton = new ButtonType("Square Brush");
        ButtonType graphiteBrushButton = new ButtonType("Graphite Brush");
        ButtonType rectangularBrushButton = new ButtonType("Rectangular Brush");
        ButtonType circleBrushButton = new ButtonType("Circle Brush");
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(
                roundBrushButton,
                squareBrushButton,
                graphiteBrushButton,
                rectangularBrushButton,
                circleBrushButton,
                cancelButtonType
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == roundBrushButton) {
                currentBrush = new RoundBrush(brushSizeSlider.getValue(), colorPicker.getValue());
            } else if (result.get() == squareBrushButton) {
                currentBrush = new SquareBrush(brushSizeSlider.getValue(), colorPicker.getValue());
            } else if (result.get() == graphiteBrushButton) {
                currentBrush = new GraphiteBrush(brushSizeSlider.getValue(), colorPicker.getValue());
            } else if (result.get() == rectangularBrushButton) {
                currentBrush = new RectangularBrush(brushSizeSlider.getValue(), colorPicker.getValue());
            } else if (result.get() == circleBrushButton) {
                currentBrush = new CircleBrush(brushSizeSlider.getValue(), colorPicker.getValue());
            }
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        FileChooser file_chooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG Files (*.png)", "*.png");
        file_chooser.getExtensionFilters().add(extFilter);
        File file = file_chooser.showSaveDialog(null);;
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage(1080, 790);
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
                System.out.println("Error!");
            }
        }

    }

    @FXML
    private void handleColorPickerAction(ActionEvent event) {
        if (currentBrush != null) {
            currentBrush.color = colorPicker.getValue();
        }
    }
    @FXML
    private void handleCanvasMousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();

        if (currentBrush != null) {
            currentBrush.startDrawing(lastX, lastY);
        }
    }
    @FXML
    private void handleCanvasMouseDragged(MouseEvent event) {
        double currentX = event.getX();
        double currentY = event.getY();

        if (currentBrush != null) {
            drawLine(lastX, lastY, currentX, currentY);
        }

        lastX = currentX;
        lastY = currentY;
    }

    // this is the code for making the lines more smoother, before this code was implemented the lines wouldn't keep up with the speed of mah hand
    private void drawLine(double startX, double startY, double endX, double endY) {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double stepX = deltaX / distance;
        double stepY = deltaY / distance;

        double x = startX;
        double y = startY;

        while (distance > 0) {
            if (currentBrush != null) {
                currentBrush.draw(x, y);
            }

            x += stepX;
            y += stepY;
            distance--;
        }
    }

    @FXML
    private void handleCanvasMouseReleased(MouseEvent event) {
        if (currentBrush != null) {
            currentBrush.stopDrawing();
        }
    }
    @FXML
    private void handleEraserButtonAction(ActionEvent event) {
        selectedTool = Tool.ERASER;
        canvas.setOnMousePressed(this::startErasing);
        canvas.setOnMouseDragged(this::erase);
        canvas.setOnMouseReleased(this::stopErasing);
    }
    @FXML
    private void erase(MouseEvent event) {
        double currentX = event.getX();
        double currentY = event.getY();

        graphicsContext.clearRect(currentX, currentY, brushSizeSlider.getValue(), brushSizeSlider.getValue());

        lastX = currentX;
        lastY = currentY;
    }
    private void stopErasing(MouseEvent event) {
        canvas.setOnMousePressed(this::handleCanvasMousePressed);
        canvas.setOnMouseDragged(this::handleCanvasMouseDragged);
        canvas.setOnMouseReleased(this::handleCanvasMouseReleased);
    }
    private void startErasing(MouseEvent event) {
        if (selectedTool == Tool.ERASER) {
            double currentX = event.getX();
            double currentY = event.getY();

            graphicsContext.clearRect(currentX, currentY, brushSizeSlider.getValue(), brushSizeSlider.getValue());

            lastX = currentX;
            lastY = currentY;
        }
    }
    @FXML
    private void handleClearCanvasAction(ActionEvent event) {

        clearCanvas();
    }

    private void clearCanvas() {

        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

}



