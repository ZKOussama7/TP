import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.geometry.*;
// import javafx.scene.shape.*;
import javafx.scene.text.TextAlignment;

import java.util.Vector;

class Point {
    public double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Point(double d) {
        this.x = this.y = d;
    }

    public boolean iseq(Point other) {
        return (this.x == other.x && this.y == other.y);
    }

    public Point add(Point Other) {
        return new Point(this.x + Other.x, this.y + Other.y);
    }

    public Point sub(Point Other) {
        return new Point(this.x - Other.x, this.y - Other.y);
    }

}


enum LINE_TYPES {
    START, MID, END;
}


class Line {
    public Point s, e;
    public LINE_TYPES type = LINE_TYPES.MID;

    Line(double xs, double ys, double xe, double ye) {
        this.s = new Point(xs, xe);
        this.e = new Point(ys, ye);
    }

    Line(double xs, double ys, double xe, double ye, LINE_TYPES tp) {
        this.s = new Point(xs, xe);
        this.e = new Point(ys, ye);
        this.type = tp;
    }

    public boolean iseq(Line Other) {
        return this.s.iseq(Other.s) && this.e.iseq(Other.e);
    }
}


public class App extends Application {
    BorderPane main_stack;
    VBox start_vbox;
    Scene pScene;
    Canvas main_canvas;
    Point wnd_size = new Point(0);
    Point canva_size = new Point(500, 230);

    // TODO; add a variable to hold the center of the viewport's coords, and the
    // scale, so that we can impliment the paning actions and zooming
    
    Point canva_center = new Point(0);
    double canva_scale = 1;

    Point MousePress = new Point(-1);
    Point MouseDrag = new Point(-1);

    Point duck_pos = new Point(40);
    Vector<Point> Trajectory = new Vector<Point>(20, 40);
    Line tmpLine = new Line(-1, -1, -1, -1);

    void redraw_canvas() {
        main_canvas.setWidth(canva_size.x);
        main_canvas.setHeight(canva_size.y);
        GraphicsContext gc = main_canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(0, 25, 84));
        gc.fillRect(0, 0, canva_size.x, canva_size.y);
        
        // gc.setFill(Color.rgb(194, 205, 242));
        // gc.fillText("width Height\n text" , 50, 50);
        // gc.fillText("width     = " + Double.toString(wnd_size.x) + ";\nheight    = " + Double.toString(wnd_size.y)
        // + "\nCwidth    = " + Double.toString(canva_size.x) + ";\nCheight   = " + Double.toString(canva_size.y)
        // + "\nMouseP.X    = " + Double.toString(MousePress.x) + ";\nMouseP.Y   = "
        // + Double.toString(MousePress.y) + "\nMouseD.x    = " + Double.toString(MouseDrag.x) + ";\nMouseD.Y   = "
        // + Double.toString(MouseDrag.y), 40, 50);
        
        gc.setStroke(Color.rgb(255, 96, 96));
        gc.setLineWidth(5);
        gc.setLineDashes(1,0);

        // Point lasPoint = duck_pos;
        Point lasPoint = new Point(40,40);

        for (Point vec : Trajectory) {
            gc.strokeLine(lasPoint.x,lasPoint.y ,lasPoint.x+vec.x ,lasPoint.y+vec.y);
            lasPoint=lasPoint.add(vec);
        }
        
        if(!this.tmpLine.iseq(new Line(-1, -1, -1, -1))){
            gc.setStroke(Color.rgb(228, 255, 96));
            gc.setLineWidth(3);
            gc.setLineDashes(15,15);
            gc.strokeLine(lasPoint.x,lasPoint.y ,this.tmpLine.e.x+lasPoint.x ,this.tmpLine.e.y+lasPoint.y);
        }
    }

    public void start(Stage pStage) {
        // Starting Page:
        Button start_btn = new Button("START");
        start_btn.getStyleClass().add("btn");
        start_btn.setOnMouseClicked(e -> {
            this.pScene.setRoot(main_stack);
            e.consume();
        });
        Label start_label = new Label(
                "After starting the Server twin app and ensuring that it finished initialising, Click the START to connect to the server");
        start_label.setWrapText(true);
        start_label.setTextAlignment(TextAlignment.CENTER);
        start_label.getStyleClass().add("label");
        start_vbox = new VBox(20, start_label, start_btn);
        start_vbox.setAlignment(Pos.CENTER);

        // Main Interface:
        Button send_btn = new Button("Send");
        Button continue_btn = new Button("Continuous");
        send_btn.getStyleClass().add("btn");
        continue_btn.getStyleClass().add("btn");

        // Main_btn.setOnMouseClicked(null);
        HBox main_btns = new HBox(20, continue_btn, send_btn);
        main_btns.setAlignment(Pos.CENTER_RIGHT);

        // TODO; add a duck or car image in the middle of the window which we will be
        // moving using the mouse and the keyboard
        // TODO; add a group?? of graphics to draw the trajectory falloed by the object
        // in the middle
        // TODO; maybe we can make it so that the group is always centered, if we draw
        // more the items get smaller to always fit the group,
        // and if we go to one side the position of the group will shift to compensate
        // making it in the middle again

        Label main_label = new Label(
                "After Drawing the Iteneraire, You can Click the Main btn to send it to the server:");

        main_canvas = new Canvas(canva_size.x, canva_size.y);
        main_canvas.getStyleClass().add("canva");
        main_canvas.setOnMousePressed(e -> {
            this.MousePress.x = e.getSceneX();
            this.MousePress.y = e.getSceneY();
            e.consume();
            App.this.redraw_canvas();
        });
        main_canvas.setOnMouseReleased(e -> {
            Point vector = MouseDrag.sub(MousePress);
            this.MousePress = new Point(-1);
            this.MouseDrag = new Point(-1);
            tmpLine = new Line(-1,-1,-1,-1);
            this.Trajectory.add(vector);

            e.consume();
            App.this.redraw_canvas();
        });
        main_canvas.setOnMouseDragged(e -> {
            Point vector = MouseDrag.sub(MousePress);
            tmpLine.s.x=0;
            tmpLine.s.y=0;
            tmpLine.e.x=vector.x;
            tmpLine.e.y=vector.y;
            this.MouseDrag.y = e.getSceneY();
            this.MouseDrag.x = e.getSceneX();
            App.this.redraw_canvas();
        });
        main_canvas.setOnMouseClicked(e -> {
            if ((!this.MousePress.iseq(new Point(-1))) && e.isSecondaryButtonDown()) {
                this.MousePress = new Point(-1);
                this.MouseDrag = new Point(-1);
                tmpLine = new Line(-1,-1,-1,-1);
            }
        });
        // main_canvas.setonmou
        // this.redraw_canvas();

        main_label.setWrapText(true);
        main_label.setTextAlignment(TextAlignment.LEFT);
        main_label.getStyleClass().add("label");
        main_stack = new BorderPane();
        main_stack.setTop(main_label);
        main_stack.setCenter(main_canvas);
        main_stack.setBottom(main_btns);

        BorderPane.setMargin(main_btns, new Insets(7));

        // main_stack.set class *

        pScene = new Scene(start_vbox, 600, 400);
        pScene.getStylesheets().add("Style.css");

        pStage.setTitle("Client Side Interface");
        pStage.setMinWidth(602);
        pStage.setMinHeight(433);
        pStage.setScene(pScene);
        pStage.show();
        double h, w;
        h = pStage.getHeight();
        w = pStage.getWidth();
        wnd_size = new Point(w, h);
        this.redraw_canvas();
        pScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldw, Number neww) {
                wnd_size.x = neww.doubleValue();
                canva_size.x = neww.doubleValue() - 130;
                App.this.redraw_canvas();
            }
        });
        pScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldh, Number newh) {
                wnd_size.y = newh.doubleValue();
                canva_size.y = newh.doubleValue() - 180;
                App.this.redraw_canvas();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Launching Client Side Interface...");
        launch(args);
        System.out.println("Closing Client Side Interface...");
    }
}