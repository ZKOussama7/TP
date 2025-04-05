import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

class INTERFACE_STATE{
    public int st=0;
    public boolean ap=true;

    INTERFACE_STATE(){
        st=0;
        ap=true;
    }
    INTERFACE_STATE(int st,boolean ap){
        this.st=st;
        this.ap=ap;
    }
}

public class App extends Application {
    AtomicReference<INTERFACE_STATE> state=new AtomicReference<INTERFACE_STATE>(new INTERFACE_STATE());

    VBox main_stack;
    VBox loading_vbox;
    VBox start_vbox;
    Scene pScene;
    TextArea r_text,t_text;
    String Received = "";
    String Sent = "";

    String address = "127.0.0.1";
    int port = 7890;

    Thread getloop;
    Socket con_Socket;
    BufferedReader con_in;
    PrintStream con_out;

    public void sendkey(String k){
        if(k.equals("\r")) k="\\n";
        if(!k.replaceAll("[\\p{C}]", "").isEmpty())
            con_out.println("0"+k);
        else{
            // String[] lines = this.r_text.getText().split("\n");
            // con_out.println("1"+lines[lines.length-1]);
            String txt = this.r_text.getText();
            int lidx = txt.lastIndexOf('\n');
            // System.out.println("lidx = "+Integer.toString(lidx));
            // if(lidx == txt.length())
            //     con_out.println("1");
            // else
                con_out.println("1"+txt.substring(lidx+1));
        }
    }

    public void startGettingkeys(){
        this.getloop = new Thread(()->{
            try {
                String str;
                while(!Thread.currentThread().isInterrupted()){
                    if(App.this.state.get().st==2){
                        // System.err.println("gotten to 2 -1 : "+Boolean.toString(App.this.state.get().ap));
                        if(!App.this.state.get().ap){
                            // System.err.println("gotten to 2");
                            App.this.state.set(new INTERFACE_STATE(2,true));;
                            App.this.pScene.setRoot(main_stack);
                            // App.this.r_text.requestFocus();
                            continue;
                        }
                        if(App.this.con_Socket.isClosed()){
                            App.this.state.set(new INTERFACE_STATE(0,false));
                            continue;
                        }
                        str=App.this.con_in.readLine();
                        // System.out.println("Received:"+str);
                        if(str.equals("0\\n"))str="0\n";
                        // App.this.t_text.appendText(str);
                        //--------------------------------------------------------
                        if(str.isEmpty()) continue;

                        char type = str.charAt(0);
                        str=str.substring(1);

                        if((type!='0' && type!='1')) continue;
                        
                        if(type=='0'){
                            App.this.t_text.appendText(str);
                        }
                        else if(type=='1'){
                            String[] trs = App.this.t_text.getText().split("\n");
                            trs[trs.length-1]=str;
                            App.this.t_text.setText(String.join("\n", trs));
                        }
                        //---------------------------------------------------
                        continue;
                    }else if(App.this.state.get().st==1 && !App.this.state.get().ap){
                        App.this.state.set(new INTERFACE_STATE(1,true));
                        App.this.pScene.setRoot(loading_vbox);
                        new Thread(()->{
                            try {
                                App.this.con_Socket = new Socket(address,port);
                                // App.this.t_text.insertText(0,"Connected Client: "+con_Socket.getInetAddress().toString()+"\n");
                                App.this.con_in=new BufferedReader(new InputStreamReader(con_Socket.getInputStream()));
                                App.this.con_out=new PrintStream(con_Socket.getOutputStream());
                                App.this.state.set(new INTERFACE_STATE(2,false));
                                // System.err.println("set to 2"+Integer.toString(App.this.state));
                                return;
                            } catch (Exception exp) {
                                System.out.print("!e!");                
                                return;
                            }
                        }).start();
                        continue;
                    }
                    else if(App.this.state.get().st==0 && !App.this.state.get().ap){
                        App.this.state.set(new INTERFACE_STATE(0,true));;
                        App.this.pScene.setRoot(start_vbox);
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.print("!e!");
                e.printStackTrace();
            }
        });
        getloop.start();
    }

    public void start(Stage pStage) {
        // Starting Page:
        Button start_btn = new Button("START");
        start_btn.getStyleClass().add("btn");
        start_btn.setOnMouseClicked(e -> {
            this.state.set(new INTERFACE_STATE(1,false));
            e.consume();
        });
        pStage.setOnCloseRequest(_->{
            try{
                this.getloop.interrupt();
                App.this.con_Socket.close();
            } catch (Exception exp) {
                System.out.print("!e!");  
                exp.printStackTrace();              
            }
        });

        Label start_label = new Label(
                "After starting the Server twin app and ensuring that it finished initialising, Click the START to connect to the server");
        start_label.setWrapText(true);
        start_label.setTextAlignment(TextAlignment.CENTER);
        start_label.getStyleClass().add("label");
        TextField addrip = new TextField(this.address + ":" + Integer.toString(this.port));
        // addrip.getStyleClass().add("textfl");
        addrip.setMaxHeight(100);
        addrip.setAlignment(Pos.CENTER);

        start_vbox = new VBox(20, start_label, start_btn, addrip);
        start_vbox.setAlignment(Pos.CENTER);

        loading_vbox = new VBox(new Label("Waiting for the Server to accept the Connection"));
        loading_vbox.setAlignment(Pos.CENTER);

        // Main Interface:
        // Button send_btn = new Button("Send");
        // Button continue_btn = new Button("Continuous");
        // send_btn.getStyleClass().add("btn");
        // continue_btn.getStyleClass().add("btn");

        // Main_btn.setOnMouseClicked(null);
        // HBox main_btns = new HBox(20, continue_btn, send_btn);
        // main_btns.setAlignment(Pos.CENTER_RIGHT);

        Label t_label = new Label("This is where all the text written by the server in real time :");
        Label r_label = new Label("This is What you write and is sent to the server in real time :");
        t_label.setWrapText(true);
        t_label.setTextAlignment(TextAlignment.LEFT);
        t_label.getStyleClass().add("label");

        r_label.setWrapText(true);
        r_label.setTextAlignment(TextAlignment.LEFT);
        r_label.getStyleClass().add("label");

        t_text = new TextArea();
        t_text.getStyleClass().add("text");
        t_text.setEditable(false);

        r_text = new TextArea();
        r_text.getStyleClass().add("text");
        r_text.setMaxHeight(200);

        this.r_text.setOnKeyTyped(e->{
            sendkey(e.getCharacter());
        });
        this.startGettingkeys();

        Separator sep_line = new Separator(Orientation.HORIZONTAL);

        main_stack = new VBox(1, t_label, t_text, sep_line, r_label, r_text);
        main_stack.setAlignment(Pos.CENTER);
        // VBox.setMargin(sep_line, new Insets(3));

        pScene = new Scene(start_vbox, 600, 400);
        pScene.getStylesheets().add("Style.css");

        pStage.setTitle("Client Side Interface");
        pStage.setMinWidth(800);
        pStage.setMinHeight(600);
        pStage.setScene(pScene);
        pStage.show();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Launching Client Side Interface...");
        launch(args);
        System.out.println("Closing Client Side Interface...");
    }
}