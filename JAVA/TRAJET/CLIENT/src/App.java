import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.geometry.*;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public Point scale(double scale) {
        return new Point(this.x * scale, this.y * scale);
    }

    public void set(double d) {
        this.x = d;
        this.y = d;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Point Other) {
        this.x = Other.x;
        this.y = Other.y;
    }

    public double mag() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public String toString() {
        return Double.toString(this.x) + "," + Double.toString(this.y);
    }

    static public Point parse(String pn) {
        String[] coords = pn.split(",");
        return new Point(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
    }

}


enum AnimStat {
    READY, RUNNING;
}


enum ActionType {
    STAND, PECK, DYING, DIED, WALK, HAPPY, ASLEEP, SLEEPING, CHICK, CELEB;
}


class Line {
    public Point s, e;

    Line(double xs, double ys, double xe, double ye) {
        this.s = new Point(xs, ys);
        this.e = new Point(xe, ye);
    }

    Line(Point s, Point e) {
        this.s = s;
        this.e = e;
    }

    public boolean iseq(Line Other) {
        return this.s.iseq(Other.s) && this.e.iseq(Other.e);
    }

    public Line add(Line Other) {
        return new Line(this.s.add(Other.s), this.e.add(Other.e));
    }

    public Line sub(Line Other) {
        return new Line(this.s.sub(Other.s), this.e.sub(Other.e));
    }

    public Line scale(double k) {
        return new Line(this.s.scale(k), this.e.scale(k));
    }

    public boolean intersects(Line Other) {
        double denom = (Other.e.y - Other.s.y) * (this.e.x - this.s.x)
                - (Other.e.x - Other.s.x) * (this.e.y - this.s.y);
        if (denom == 0.0)
            return false;

        double ua = ((Other.e.x - Other.s.x) * (this.s.y - Other.s.y)
                - (Other.e.y - Other.s.y) * (this.s.x - Other.s.x)) / denom;
        double ub = ((this.e.x - this.s.x) * (this.s.y - Other.s.y) - (this.e.y - this.s.y) * (this.s.x - Other.s.x))
                / denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            // return new Point((this.s.x + ua*(this.e.x - this.s.x)), (this.s.y +
            // ua*(this.e.y - this.s.y)));
            return true;
        }
        return false;
    }
}


class ZAnimation {
    private AnimStat state = AnimStat.READY;
    private ActionType action = ActionType.STAND;
    private int time = 5; // times 15ms for each redraw
    private int cr_time = 0;
    private int anim_time = 0;
    private Point st = new Point(0), ed = new Point(0);
    private HashMap<ActionType, Image[]> Image_list = new HashMap<ActionType, Image[]>();

    ZAnimation() {
        // initialize the image list with images to be used afterwards with draw_anim()
        for (ActionType act : ActionType.values()) {
            Image[] imgs = new Image[get_anime_time(act)];
            String rel_path = "Doesn't \0 exist";
            switch (act) {
            case STAND:
                rel_path = "Assets/C-st1.png";
                break;
            case CELEB:
                rel_path = "Assets/C-cb";
                break;
            case CHICK:
                rel_path = "Assets/Ck-st";
                break;
            case DIED:
                rel_path = "Assets/C-d8.png";
                break;
            case DYING:
                rel_path = "Assets/C-d";
                break;
            case HAPPY:
                rel_path = "Assets/C-st";
                break;
            case PECK:
                rel_path = "Assets/C-p";
                break;
            case SLEEPING:
                rel_path = "Assets/C-z";
                break;
            case ASLEEP:
                rel_path = "Assets/C-z1.png";
                break;
            case WALK:
                rel_path = "Assets/C-w";
                break;
            }

            for (int i = 1; i <= get_anime_time(act); i++) {
                if (act == ActionType.CELEB)
                    break;
                if (get_anime_time(act) == 1) {
                    // System.out.println("Path is : "+rel_path);
                    imgs[0] = new Image(rel_path);
                    break;
                }
                // System.out.println("Path is : "+rel_path + Integer.toString(i) + ".png");
                imgs[i - 1] = new Image(rel_path + Integer.toString(i) + ".png");
            }

            this.Image_list.put(act, imgs);
        }
    }

    private int get_anime_time() {
        return get_anime_time(this.action);
    }

    private int get_anime_time(ActionType act) {
        switch (act) {
        case STAND:
            return 1;
        case CELEB:
            return 16;
        case CHICK:
            return 8;
        case DIED:
            return 1;
        case DYING:
            return 8;
        case HAPPY:
            return 4;
        case PECK:
            return 4;
        case SLEEPING:
            return 8;
        case ASLEEP:
            return 1;
        case WALK:
            return 8;

        default:
            return 8;
        }
    }

    public void start_animation(ActionType action, int time, Point start, Point end) {
        this.cr_time = 0;
        this.anim_time = 0;
        this.action = action;
        this.st = start;
        this.ed = end;
        this.time = time;
        this.state = AnimStat.RUNNING;
    }

    public void stop_animation() {
        this.cr_time = 0;
        this.anim_time = 0;
        this.time = 5;
        this.st = new Point(0);
        this.ed = new Point(0);
        this.state = AnimStat.READY;
        if (this.action == ActionType.CHICK)
            this.action = ActionType.CHICK;
        else if (this.action == ActionType.DYING)
            this.action = ActionType.DIED;
        else
            this.action = ActionType.STAND;
    }

    public void update() {
        if (this.state == AnimStat.READY)
            return;

        if (cr_time < time)
            this.cr_time++;

        if (cr_time >= time) {
            this.cr_time = 0;
            if (this.anim_time < this.get_anime_time())
                this.anim_time++;

            if (this.anim_time >= this.get_anime_time())
                stop_animation();
        }
    }

    public AnimStat get_state() {
        return this.state;
    }

    public Image get_image() {
        if (this.state == AnimStat.READY)
            return this.Image_list.get(ActionType.STAND)[0];
        return this.Image_list.get(this.action)[this.anim_time];
    }

    public Point getPosition() {
        if (this.state == AnimStat.READY)
            return new Point(0);
        long part = anim_time * time + cr_time + 1;
        // System.out.print("Start = ("+this.st.toString()+") | End = ("+this.ed+")");
        // System.out.println("Sub = ("+this.st.toString()+") | Scale = ("+this.ed+")");
        // System.out.println("Action:" + this.action.toString() + "| Part =" +
        // Long.toString(part) + ", Total = "
        // + Long.toString((get_anime_time() * time)) + "| Pos is ("
        // + this.st.add(this.ed.sub(this.st).scale((part) / (get_anime_time() *
        // time))).toString() + ")");
        return this.st.add(this.ed.sub(this.st).scale(((double) part) / (double) (get_anime_time() * time)));
    }

    // it should control the mouvement and action animations giving the appropriate
    // image to paint, with the current position
    // given the initial position and the
}


class UpdateTimer extends TimerTask {
    ZGame zg;

    UpdateTimer(ZGame zg) {
        this.zg = zg;
    }

    public void run() {
        this.zg.redraw(true);
    }
}


class Network {
    private Socket soc;
    private ServerSocket sersoc;
    private int port = 7891;
    private String adr = "127.0.0.1";
    private BufferedReader in;
    private PrintStream out;
    private boolean initialized = false;
    private boolean client = false;
    private ZGame engine;
    private Thread rec_loop;

    Network() {
        this.client = true;
        try {

            this.soc = new Socket(this.adr, this.port);
            this.in = new BufferedReader(new InputStreamReader(this.soc.getInputStream()));
            this.out = new PrintStream(this.soc.getOutputStream());
            this.initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            this.initialized = false;
            return;
        }
    }

    Network(ZGame eng) {
        this.client = false;
        this.engine = eng;
        try {
            this.sersoc = new ServerSocket(this.port);
            this.soc = this.sersoc.accept();
            this.in = new BufferedReader(new InputStreamReader(this.soc.getInputStream()));
            this.out = new PrintStream(this.soc.getOutputStream());
            this.initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            this.initialized = false;
            return;
        }

        if (!this.initialized)
            return;

        this.start_receiving();

    }

    private void send_traj_tmp(Point tmp_traj) {
        String data = "1;";
        data += tmp_traj.toString() + ";";
        this.out.println(data);
    }

    private void send_traj(Vector<Point> traj_lines) {
        String data = "2;" + Integer.toString(traj_lines.size()) + ";";
        for (Point pt : traj_lines)
            data += pt.toString() + ";";
        this.out.println(data);
    }

    private void start_receiving() {
        if (!this.initialized || this.client)
            return;
        System.out.println("starting to listen...");
        this.rec_loop = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String req = this.in.readLine();
                    System.out.println("received req " + req);
                    String[] elms = req.split(";");
                    if (elms.length < 2)
                        continue;

                    int res = -1;

                    int command = Integer.parseInt(elms[0]);

                    if (command == 1) {
                        Point tmp = Point.parse(elms[1]);
                        res = this.engine.net_traj_tmp(tmp);
                    } else if (command == 2) {
                        int len = Integer.parseInt(elms[1]);
                        Vector<Point> trajs = new Vector<Point>(0);
                        for (int i = 0; i < len; i++) {
                            trajs.add(Point.parse(elms[i + 2]));
                        }
                        res = this.engine.net_traj(trajs);
                    } else {
                        res = -1;
                    }

                    System.out.println("Res is " + Integer.toString(res) + " sending it....");
                    this.out.println(res);

                } catch (Exception e) {
                    e.printStackTrace();
                    // return;
                }
            }
        });
        this.rec_loop.start();
    }

    public void stop_receiving() {
        this.rec_loop.interrupt();
    }

    public int req_verify_tmp(Point tmp) {
        if (!this.client || !this.initialized)
            return -1;
        send_traj_tmp(tmp);
        try {
            String res = this.in.readLine();
            return Integer.parseInt(res);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int req_verify_end(Vector<Point> trajs) {
        if (!this.client || !this.initialized)
            return -1;
        send_traj(trajs);
        try {
            String res = this.in.readLine();
            return Integer.parseInt(res);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    void desctruct() {
        try {
            if (this.client)
                this.soc.close();
            else {
                this.sersoc.close();
                this.soc.close();
                this.stop_receiving();
            }
            this.initialized = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


class ZGame {
    // it should be able to store the initial/ final position, the obstacles, (if i
    // have time)portals
    // it should return the ...
    public boolean client = false;

    private Point canva_center = new Point(-250, -115);
    private double canva_scale = 1;

    private Point Mouse_Press = new Point(-1);

    private boolean mouse_pressed = false;

    private int Current_wp = 0;

    private Vector<Point> traj_lines = new Vector<Point>(20, 40);
    private Vector<Point> rm_traj_lines = new Vector<Point>(20, 40);

    private Vector<Line> obstacles = new Vector<Line>(20, 40);
    private Point Chick = new Point(Math.floor(Math.random() * 200.0), Math.floor(Math.random() * 200.0));
    private boolean found_chick = false;

    private Point tmp_line = new Point(0);

    private Point canva_size = new Point(500, 230);

    private ZAnimation anim_engine = new ZAnimation();
    private ZAnimation Chick_anim_engine = new ZAnimation();
    private Timer update_thread = new Timer();
    private long last_redraw = System.currentTimeMillis();
    private AtomicBoolean isdrawing = new AtomicBoolean(false);

    private TimerTask redraw_task;
    private GraphicsContext gc;

    public Network net;

    ZGame(GraphicsContext gc, boolean client) {
        this.client = client;
        this.gc = gc;
        this.redraw_task = new UpdateTimer(this);
        if (this.client)
            this.net = new Network();
        else
            this.net = new Network(this);

        if (this.client) {
            this.obstacles.add(new Line(10, -10, 10, 90));
            this.obstacles.add(new Line(-10, 100, 100, 100));
            this.obstacles.add(new Line(30, -60, 200, -60));
        } else {
            this.obstacles.add(new Line(-70, -70, -70, 190));
            this.obstacles.add(new Line(-10, -60, -10, 100));
            this.obstacles.add(new Line(-30, 100, 200, 100));
        }

    }

    private void draw_canva(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 25, 84));
        gc.fillRect(0, 0, canva_size.x, canva_size.y);
    }

    private void draw_circle(GraphicsContext gc, double rad, Point Pos) {
        gc.fillOval(Pos.x - rad, Pos.y - rad, rad * 2, rad * 2);
    }

    private void draw_stored(GraphicsContext gc) {
        // TODO: add small circles to all the waypoints

        gc.setStroke(Color.rgb(255, 96, 96));
        gc.setFill(Color.rgb(255, 96, 96));
        gc.setLineWidth(5);
        gc.setLineDashes(1, 0);
        Point lasPoint = new Point(0).sub(this.canva_center);
        this.draw_circle(gc, 5, lasPoint);
        for (Point vec : this.traj_lines) {
            vec = vec.scale(this.canva_scale);
            gc.strokeLine(lasPoint.x, lasPoint.y, lasPoint.x + vec.x, lasPoint.y + vec.y);
            lasPoint = lasPoint.add(vec);
            this.draw_circle(gc, 5, lasPoint);
        }

        gc.setStroke(Color.WHITESMOKE);
        gc.setFill(Color.WHITESMOKE);
        gc.setLineWidth(10);
        gc.setLineDashes(1, 0);
        for (Line ob : this.obstacles) {
            gc.strokeLine(ob.s.sub(this.canva_center).scale(canva_scale).x,
                    ob.s.sub(this.canva_center).scale(canva_scale).y, ob.e.sub(this.canva_center).scale(canva_scale).x,
                    ob.e.sub(this.canva_center).scale(canva_scale).y);
        }
        // System.out.println("drawing");

    }

    private void draw_tmp(GraphicsContext gc) {
        if (!this.mouse_pressed && this.client)
            return;
        // System.out.println("drawing tmp");
        gc.setStroke(Color.rgb(228, 255, 96));
        gc.setLineWidth(3);
        gc.setLineDashes(15, 15);

        Point lasPoint = new Point(0).sub(this.canva_center);
        for (Point vec : this.traj_lines) {
            lasPoint = lasPoint.add(vec);
        }
        lasPoint.set(lasPoint.scale(this.canva_scale));

        boolean intrs = false;
        for (Line ob : this.obstacles) {
            if (new Line(lasPoint, lasPoint.add(tmp_line))
                    .intersects(ob.sub(new Line(this.canva_center, this.canva_center)).scale(this.canva_scale)))
                intrs = true;
        }
        int ser_intr = -1;
        if (this.client) {
            ser_intr = verify_tmp();
        }
        if (intrs || ser_intr == 0) {
            gc.setStroke(Color.BLACK);
        } else if (ser_intr == 2) {
            gc.setStroke(Color.GOLD);
        }

        gc.strokeLine(lasPoint.x, lasPoint.y, this.tmp_line.add(lasPoint).x, this.tmp_line.add(lasPoint).y);

    }

    private void draw_duck(GraphicsContext gc, Point pos, double scale) {
        // System.out.println("draw_duck "+ pos.toString());
        int size = (int) Math.floor(32.0 * scale);
        int sizex = (int) Math.floor(size * 0.5);// ,sizex2 = size-sizen;
        int sizey = (int) Math.floor(size * 0.7);// ,sizey2=size-sizep;
        gc.drawImage(this.anim_engine.get_image(), pos.x - sizex, pos.y - sizey, size, size);
    }
    
    private void draw_ch_duck(GraphicsContext gc, Point pos, double scale) {
        // System.out.println("draw_duck "+ pos.toString());
        int size = (int) Math.floor(32.0 * scale);
        int sizex = (int) Math.floor(size * 0.5);// ,sizex2 = size-sizen;
        int sizey = (int) Math.floor(size * 0.8);// ,sizey2=size-sizep;
        gc.drawImage(this.Chick_anim_engine.get_image(), pos.x - sizex, pos.y - sizey, size, size);
    }

    private void draw_anime(GraphicsContext gc, boolean regular) {
        if (this.anim_engine.get_state() == AnimStat.RUNNING) {
            // System.out.print("running " +
            // this.anim_engine.getPosition().sub(this.canva_center).scale(this.canva_scale).toString());

            if (regular)
                this.anim_engine.update();

            if (this.anim_engine.get_state() == AnimStat.READY) {
                draw_anime(gc, regular);
                return;
            }

            // System.out.println("|| " +
            // this.anim_engine.getPosition().sub(this.canva_center).scale(this.canva_scale).toString());
            this.draw_duck(gc, this.anim_engine.getPosition().sub(this.canva_center).scale(this.canva_scale), 4);
            return;
        }

        int step = this.Current_wp < this.traj_lines.size() ? 1 : (this.Current_wp > this.traj_lines.size() ? -1 : 0);
        // System.out.println("redraw..." + Integer.toString(step));

        Point cr_pos = new Point(0);
        Point ds_pos = new Point(0);
        for (int i = 0; i < this.Current_wp + 1; i++) {
            Point next;
            if (i < this.traj_lines.size())
                next = cr_pos.add(this.traj_lines.elementAt(i));
            else if (this.Current_wp < this.traj_lines.size() + this.rm_traj_lines.size()) {
                // System.out.println("element is: "
                // + Integer.toString(this.rm_traj_lines.size() - 1 - (i -
                // this.traj_lines.size())));
                next = cr_pos.add(
                        this.rm_traj_lines.elementAt(this.rm_traj_lines.size() - 1 - (i - this.traj_lines.size())));
            } else {
                next = cr_pos;
            }
            if (i < this.Current_wp) {
                cr_pos.set(next);
            }
            if (i + 1 == this.Current_wp + step) {
                ds_pos.set(next);
            }
        }

        if (step == 0) {
            if (this.found_chick)
                this.anim_engine.start_animation(ActionType.HAPPY, 5, cr_pos, ds_pos);
            else
                this.anim_engine.start_animation(Math.random() > 0.2 ? ActionType.STAND : ActionType.PECK, 5, cr_pos,
                        ds_pos);
        } else {
            this.anim_engine.start_animation(ActionType.WALK, 5, cr_pos, ds_pos);
            this.Current_wp += step;
        }

        // System.err.println("Redraw pos = " + cr_pos.toString() + " epos = "+
        // ds_pos.toString());
        this.draw_duck(gc, this.anim_engine.getPosition().sub(this.canva_center).scale(this.canva_scale), 4);
        return;
        // set the state of the chicken
        // independent of the other stuff
        // if we are not in the latest waypoint, and an animation is not running, start
        // a new animation

        // draw the chick after the chicken so that they are both in frame

    }

    private void draw_Ch_anime(GraphicsContext gc, boolean regular) {
        if (this.Chick_anim_engine.get_state() == AnimStat.RUNNING) {
            if (regular)
                this.Chick_anim_engine.update();

            if (this.Chick_anim_engine.get_state() == AnimStat.READY) {
                draw_Ch_anime(gc, regular);
                return;
            }

            this.draw_ch_duck(gc, this.Chick_anim_engine.getPosition().sub(this.canva_center).scale(this.canva_scale), 4);
            return;
        }

        this.Chick_anim_engine.start_animation(ActionType.CHICK, 5, this.Chick, this.Chick);
        this.draw_ch_duck(gc, this.Chick.sub(this.canva_center).scale(this.canva_scale), 4);
        return;
    }

    private int verify_tmp() {
        return this.net.req_verify_tmp(tmp_line);
    }

    private int verify_tmp(Point tmp) {
        return this.net.req_verify_tmp(tmp);
    }

    private int verify_end(Vector<Point> trajs) {
        return this.net.req_verify_end(trajs);
    }

    private int verify_end() {
        return this.net.req_verify_end(this.traj_lines);
    }

    public void redraw(boolean regular) {
        if (System.currentTimeMillis() - last_redraw < 25)
            return;
        if (isdrawing.getAndSet(true))
            return;
        this.draw_canva(this.gc);
        this.draw_stored(this.gc);
        this.draw_tmp(this.gc);
        this.draw_anime(this.gc, true);
        if (!this.client)
            this.draw_Ch_anime(this.gc, true);
        last_redraw = System.currentTimeMillis();
        isdrawing.set(false);
        // TODO: Might have to do this before drawing;
    }

    public void user_pres(Point pres) {
        this.mouse_pressed = true;
        this.Mouse_Press.set(pres);
    }

    public void user_drag(Point drag) {
        if (!this.mouse_pressed)
            return;
        this.tmp_line.set(drag.sub(this.Mouse_Press));
    }

    public void user_released(Point rels) {
        if (!this.mouse_pressed)
            return;
        this.tmp_line.set(0);

        Point lasPoint = new Point(0);
        for (Point vec : this.traj_lines) {
            lasPoint = lasPoint.add(vec);
        }
        Point curPoint = lasPoint.add(rels.sub(this.Mouse_Press).scale(1 / canva_scale));

        boolean intrs = false;
        for (Line ob : this.obstacles) {
            if (new Line(lasPoint, curPoint).intersects(ob))
                intrs = true;
        }

        Vector<Point> trajs = new Vector<Point>(this.traj_lines);
        trajs.add(rels.sub(this.Mouse_Press).scale(1 / canva_scale));

        int verf_end = this.verify_end(trajs);

        if (intrs || verf_end == 0) {
            this.mouse_pressed = false;
            return;
        }
        this.mouse_pressed = false;
        this.traj_lines.add(rels.sub(this.Mouse_Press).scale(1 / canva_scale));
        if (!intrs && verf_end == 2) {
            this.found_chick = true;
            Point pos = new Point(0);
            for (Point tr : this.traj_lines) {
                pos.set(pos.add(tr));
            }
            this.Chick.set(pos);
        }
    }

    public void user_cancel() {
        this.tmp_line.set(0);
        this.mouse_pressed = false;
    }

    public void user_key(KeyCode key) {
        System.out.print("read key: " + key.toString() + " Done Key: ");
        switch (key) {
        case KeyCode.ADD:
            this.canva_scale += 0.1;
            System.out.print("Add");
            break;
        case KeyCode.SUBTRACT:
            this.canva_scale -= 0.1;
            System.out.print("Sub");
            break;

        case KeyCode.I:
            this.canva_center.set(this.canva_center.add(new Point(0, 30)));
            System.out.print("I");
            break;
        case KeyCode.K:
            this.canva_center.set(this.canva_center.add(new Point(0, -30)));
            System.out.print("K");
            break;
        case KeyCode.J:
            this.canva_center.set(this.canva_center.add(new Point(30, 0)));
            System.out.print("J");
            break;
        case KeyCode.L:
            this.canva_center.set(this.canva_center.add(new Point(-30, 0)));
            System.out.print("L");
            break;

        case KeyCode.Z:
            this.user_undo();
            System.out.print("Z");
            break;
        case KeyCode.X:
            this.user_redo();
            System.out.print("X");
            break;

        default:
            break;
        }
    }

    public void user_undo() {
        if (traj_lines.size() == 0)
            return;
        // TODO: set the current_wp;
        rm_traj_lines.add(traj_lines.removeLast());
    }

    public void user_redo() {
        if (rm_traj_lines.size() == 0)
            return;
        // TODO: set the current_wp;
        traj_lines.add(rm_traj_lines.removeLast());
    }

    public void user_resize_x(double nx) {
        this.canva_size.x = nx;
        // System.out.println("cs=(" + canva_size.toString() + ")");
    }

    public void user_resize_y(double ny) {
        this.canva_size.y = ny;
    }

    public void start_update_thread() {
        this.update_thread.scheduleAtFixedRate(redraw_task, 0, 30);
    }

    public void stop_update_thread() {
        this.update_thread.cancel();
        this.update_thread.purge();
    }

    public int net_traj(Vector<Point> trajs) {
        Point lasPoint = new Point(0);
        boolean intrs = false;

        for (Point vec : trajs) {
            for (Line ob : this.obstacles) {
                if (new Line(lasPoint, lasPoint.add(vec)).intersects(ob))
                    intrs = true;
            }
            lasPoint = lasPoint.add(vec);
        }

        this.tmp_line.set(0);
        if (intrs) {
            return 0;
        }
        this.traj_lines.clear();
        this.traj_lines.addAll(trajs);
        if (lasPoint.sub(this.Chick).mag() < 10) {
            this.found_chick = true;
            ;
            return 2;
        }
        return 1;
    }

    public int net_traj_tmp(Point tmp) {
        Point lasPoint = new Point(0);
        boolean intrs = false;

        for (Point vec : this.traj_lines) {
            lasPoint = lasPoint.add(vec);
        }

        for (Line ob : this.obstacles) {
            if (new Line(lasPoint, lasPoint.add(tmp)).intersects(ob))
                intrs = true;
        }

        this.tmp_line = tmp;
        // this.redraw(false);
        if (intrs) {
            return 0;
        }
        if (lasPoint.add(tmp).sub(this.Chick).mag() < 10)
            return 2;
        return 1;
    }

}


public class App extends Application {

    // TODONE: image = ImageIO.read(getClass().getResource("/resources/icon.gif"));

    BorderPane main_stack;
    VBox start_vbox;
    Scene pScene;
    Canvas main_canvas;
    Point wnd_size = new Point(0);
    // Point canva_size = new Point(500, 230);

    ZGame engine;
    boolean client = true; // #CLIENT
    // TODONE; add a variable to hold the center of the viewport's coords, and the
    // scale, so that we can impliment the paning actions and zooming

    // Point MousePress = new Point(-1);
    // Point MouseDrag = new Point(-1);

    // Point duck_pos = new Point(40);
    // Vector<Point> Trajectory = new Vector<Point>(20, 40);
    // Line tmpLine = new Line(-1, -1, -1, -1);

    void unsused_redraw_canvas(boolean regular) {
        GraphicsContext gc = main_canvas.getGraphicsContext2D();

        // gc.setFill(Color.rgb(0, 25, 84));
        // gc.fillRect(0, 0, canva_size.x, canva_size.y);

        // gc.setFill(Color.rgb(194, 205, 242));
        // gc.fillText("width Height\n text" , 50, 50);
        // gc.fillText("width = " + Double.toString(wnd_size.x) + ";\nheight = " +
        // Double.toString(wnd_size.y)
        // + "\nCwidth = " + Double.toString(canva_size.x) + ";\nCheight = " +
        // Double.toString(canva_size.y)
        // + "\nMouseP.X = " + Double.toString(MousePress.x) + ";\nMouseP.Y = "
        // + Double.toString(MousePress.y) + "\nMouseD.x = " +
        // Double.toString(MouseDrag.x) + ";\nMouseD.Y = "
        // + Double.toString(MouseDrag.y), 40, 50);

        // this.engine.draw(gc,regular);

        // gc.setStroke(Color.rgb(255, 96, 96));
        // gc.setLineWidth(5);
        // gc.setLineDashes(1,0);

        // // Point lasPoint = duck_pos;
        // Point lasPoint = new Point(40,40);

        // for (Point vec : Trajectory) {
        // gc.strokeLine(lasPoint.x,lasPoint.y ,lasPoint.x+vec.x ,lasPoint.y+vec.y);
        // lasPoint=lasPoint.add(vec);
        // }

        // if(!this.tmpLine.iseq(new Line(-1, -1, -1, -1))){
        // gc.setStroke(Color.rgb(228, 255, 96));
        // gc.setLineWidth(3);
        // gc.setLineDashes(15,15);
        // gc.strokeLine(lasPoint.x,lasPoint.y ,this.tmpLine.e.x+lasPoint.x
        // ,this.tmpLine.e.y+lasPoint.y);
        // }
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

        // TODONE; add a duck or car image in the middle of the window which we will be
        // moving using the mouse and the keyboard
        // TODO; add a group?? of graphics to draw the trajectory falloed by the object
        // in the middle
        // TODO; maybe we can make it so that the group is always centered, if we draw
        // more the items get smaller to always fit the group,
        // and if we go to one side the position of the group will shift to compensate
        // making it in the middle again

        Label main_label = new Label(
                "After Drawing the Iteneraire, You can Click the Main btn to send it to the server:");

        main_canvas = new Canvas(500, 230);
        main_canvas.getStyleClass().add("canva");
        main_canvas.setOnMousePressed(e -> {
            // this.MousePress.x = e.getSceneX();
            // this.MousePress.y = e.getSceneY();
            if (e.getButton() == MouseButton.SECONDARY)
                this.engine.user_cancel();
            else if (e.getButton() == MouseButton.PRIMARY)
                this.engine.user_pres(new Point(e.getSceneX(), e.getSceneY()));
            e.consume();
            App.this.engine.redraw(false);
        });
        main_canvas.setOnMouseReleased(e -> {
            // Point vector = MouseDrag.sub(MousePress);
            // this.MousePress = new Point(-1);
            // this.MouseDrag = new Point(-1);
            // tmpLine = new Line(-1,-1,-1,-1);
            // this.Trajectory.add(vector);
            this.engine.user_released(new Point(e.getSceneX(), e.getSceneY()));
            e.consume();
            App.this.engine.redraw(false);
        });
        main_canvas.setOnMouseDragged(e -> {
            // Point vector = MouseDrag.sub(MousePress);
            // tmpLine.s.x=0;
            // tmpLine.s.y=0;
            // tmpLine.e.x=vector.x;
            // tmpLine.e.y=vector.y;
            // this.MouseDrag.y = e.getSceneY();
            // this.MouseDrag.x = e.getSceneX();
            this.engine.user_drag(new Point(e.getSceneX(), e.getSceneY()));
            e.consume();
            App.this.engine.redraw(false);
        });
        /***********************
         * main_canvas.setOnMouseClicked(e -> { if ((!this.MousePress.iseq(new
         * Point(-1))) && e.isSecondaryButtonDown()) { this.MousePress = new Point(-1);
         * this.MouseDrag = new Point(-1); tmpLine = new Line(-1,-1,-1,-1); } });
         ************************/

        this.engine = new ZGame(main_canvas.getGraphicsContext2D(), this.client);
        this.engine.start_update_thread();

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

        pScene.setOnKeyPressed(e -> {
            this.engine.user_key(e.getCode());
            this.engine.redraw(false);
        });

        pStage.setTitle(this.client ? "Client" : "Server" + " Side Interface");
        pStage.setMinWidth(602);
        pStage.setMinHeight(433);
        pStage.setScene(pScene);
        pStage.setOnCloseRequest(_ -> {
            this.engine.net.desctruct();
            ;
            this.engine.stop_update_thread();
        });
        pStage.show();
        double h, w;
        h = pStage.getHeight();
        w = pStage.getWidth();
        wnd_size = new Point(w, h);
        this.engine.redraw(false);
        pScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldw, Number neww) {
                wnd_size.x = neww.doubleValue();
                main_canvas.setWidth(wnd_size.x - 130);
                App.this.engine.user_resize_x(wnd_size.x - 130);
                App.this.engine.redraw(false);
            }
        });
        pScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldh, Number newh) {
                wnd_size.y = newh.doubleValue();
                main_canvas.setHeight(wnd_size.y - 180);
                App.this.engine.user_resize_y(wnd_size.y - 180);
                App.this.engine.redraw(false);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        App h = new App();
        System.out.println("Launching " + (h.client ? "Client" : "Server") + " Side Interface...");
        launch(args);
        System.out.println("Closing " + (h.client ? "Client" : "Server") + " Side Interface...");
    }
}