package chessgui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application{

    private final int MAX = 8, NUMPIECE= 32, SSIZE=60, NIMG=12;
    private int lastX=-1, lastY=-1, llastX=-1, llastY=-1;
    private Controller ctr=new Controller();
    private Image black=new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/blue.jpg");
    private Image white= new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/white.png");
    private Image test=new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/test.png");
    private Image yellow=new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/yellow.jpg");
    private Image red=new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/red.png");
    private GridPane grid = new GridPane();
    private ImageView[][] board= new ImageView[MAX][MAX];
    private ImageView[] piece=new ImageView[NUMPIECE];
    private TextArea moves, debug, engineBestMove;
    private TextField engineDepth;
    private final Image[] imgs=new Image[NIMG];
    private StockFish stockfish;
    private Timer timer;
    private ToggleButton startEngine;
    private CheckBox inf;
    private TextField[] p;
    private UpwardProgress upwardProgress;
    private ProgressIndicator bar;
    @Override
    public void start(Stage stage) throws IOException{
        //Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        Label l1=new Label("Chess gui!");

        setImgs(); //set the array of imgs

        BorderPane root=new BorderPane();
        stockfish=new StockFish("D:\\Documenti\\Stockfish\\stockfish_15_win_x64_avx2\\stockfish_15_win_x64_avx2\\stockfish15.exe", ctr);

        root.setTop(l1);
        root.setCenter(setImgBoard());
        root.setBottom(setBottom());
        root.setRight(setRight());
        root.setLeft(setLeft());
        debug.setText("FEN:\n"+ctr.getBoardFEN());

        Scene scene= new Scene(root, 1150, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        stage.setOnCloseRequest(windowEvent -> {
            timer.cancel();
            stockfish.closeStockfish();
        });
        System.out.println(ctr.printBoard());
    }

    private void setImgs(){
        String[] name={"bPawn.png", "wPawn.png","bKnight.png","wKnight.png", "bBishop.png","wBishop.png","bRook.png","wRook.png","bQueen.png","wQueen.png","bKing.png","wKing.png" };
        for(int i=0;i<NIMG;i++)
            imgs[i]=new Image("file:C:/Users/tomma/IdeaProjects/ChessGUI/imgs/"+name[i]);
    }
    private HBox setImgBoard(){

        HBox center=new HBox();
        //set pieces
        for(int i=0;i<8;i++){
            //setting the pawn (black pawn has number from 0 to 7, white pawn from 8 to 15, black knights 16-17, white knights 18-19, black bishops 20-21, white bishops 22-23,
            // black rooks 24-25, white rooks 26-27
            // bQ 28 wQ 29, bK 30 wB 31
            piece[i]=new ImageView(imgs[0]);
            piece[8+i]=new ImageView(imgs[1]);
            if(i<2){
                //do knights
                piece[16+i]=new ImageView(imgs[2]);
                piece[18+i]=new ImageView(imgs[3]);
                //do bishops
                piece[20+i]=new ImageView(imgs[4]);
                piece[22+i]=new ImageView(imgs[5]);
                //do rooks
                piece[24+i]=new ImageView(imgs[6]);
                piece[26+i]=new ImageView(imgs[7]);
            }

        }
        //queens
        piece[28]=new ImageView(imgs[8]);
        piece[29]=new ImageView(imgs[9]);
        //kings
        piece[30]=new ImageView(imgs[10]);
        piece[31]=new ImageView(imgs[11]);

        //set board

        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++) {
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0))
                    board[i][j] = new ImageView(white);
                else
                    board[i][j] = new ImageView(black);

                board[i][j].setFitWidth(SSIZE);
                board[i][j].setFitHeight(SSIZE);
                GridPane.setRowIndex(board[i][j], i);
                GridPane.setColumnIndex(board[i][j], j);
                grid.getChildren().add(board[i][j]);
            }

        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++) {
                //add pieces
                int id=ctr.getID(i, j);
                if(id>=0){
                    piece[id].setFitWidth(SSIZE);
                    piece[id].setFitHeight(SSIZE);
                    GridPane.setRowIndex(piece[id], i);
                    GridPane.setColumnIndex(piece[id], j);
                    grid.getChildren().add(piece[id]);
                }

            }
        grid.setPadding(new Insets(0, 10, 0, 10));

        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++) {
                board[i][j].addEventHandler(MouseEvent.MOUSE_CLICKED, this::handlePieceMove);
                int id=ctr.getID(i, j);
                if(id>=0){
                    piece[id].addEventHandler(MouseEvent.MOUSE_CLICKED, this::handlePieceMove);
                }
            }
        upwardProgress=new UpwardProgress(15,SSIZE*MAX);
        bar=upwardProgress.getProgressBar();
        bar.setStyle(" -fx-base: skyblue; -fx-accent: gold;");
        bar.setProgress(0.5);

        center.getChildren().addAll(grid,upwardProgress.getProgressHolder());
        center.setPadding(new Insets(20, 10, 20, 20));
        return center;
    }

    private VBox setLeft(){
        VBox tot= new VBox();
        HBox params= new HBox();

        debug= new TextArea("");
        p=new TextField[2];
        inf= new CheckBox("infinite");
        startEngine=new ToggleButton("Start engine");
        p[0]=new TextField("0");
        p[1]=new TextField("0");

        engineBestMove= new TextArea();
        engineDepth= new TextField("ENGINE: \n");
        engineDepth.setEditable(false);
        engineBestMove.setEditable(false);

        params.getChildren().addAll(p[0],p[1], inf, startEngine);
        tot.setSpacing(15);
        tot.getChildren().addAll(params, engineDepth, engineBestMove, debug);
        startEngine.setOnAction(actionEvent -> {
            if(startEngine.isSelected()){
                try {
                    startEngine.setText("stop engine");
                    String fen=ctr.getFEN();
                    //System.out.println(fen);
                    stockfish.setPosition(fen);
                    stockfish.go(Integer.parseInt(p[0].getText()), Integer.parseInt(p[1].getText()), inf.isSelected());

                    timer=new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            try{
                                String res= stockfish.readCashed();
                                if (res != null) {
                                    engineDepth.setText(stockfish.getCurrentState(res)+"\n");
                                    engineBestMove.setText(stockfish.getBestMove(res)+"\n");

                                    bar.setProgress(stockfish.getScoreForBar());
                                }

                            }catch(IOException e){e.printStackTrace();}

                        }
                    }, 300, 300);

                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            else{
                startEngine.setText("Start engine");
                timer.cancel();
                try {
                    stockfish.stopSearch();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        return tot;
    }

    private HBox setBottom(){
        TextField t= new TextField("");
        Button b= new Button("set board (FEN)");
        Button clear = new Button("clear board");
        HBox tot= new HBox();

        t.setPrefWidth(500);
        tot.getChildren().addAll(t,b, clear);
        tot.setSpacing(20);
        tot.setPadding(new Insets(0,10,10,20));

        b.setOnAction(actionEvent -> {
            try{
                ctr.setBoardFEN(t.getText());
                setBoard();
                lastY=-1; lastX=-1;
                if(ctr.addHistoryFEN(t.getText()))
                    debug.setText(t.getText() + "\n");
            }catch(IllegalArgumentException e){
                ctr.clearHistoryFEN();
                ctr.setBoardFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                debug.setText("FEN:\n"+ctr.getBoardFEN()+"\n");
                moves.setText("Moves:\n");
                setBoard();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("FEN not valid!");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }


        });
        clear.setOnAction(actionEvent -> {
            ctr.clearHistoryFEN();
            ctr.setBoardFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            debug.setText("FEN:\n"+ctr.getBoardFEN()+"\n");
            moves.setText("Moves:\n");
            setBoard();
            try {
                if(startEngine.getText().contains("stop")){
                    stockfish.stopSearch();
                    stockfish.setPosition(ctr.getFEN());
                    stockfish.go(Integer.parseInt(p[0].getText()), Integer.parseInt(p[1].getText()), inf.isSelected());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return tot;
    }
    private VBox setRight(){
        Button left=new Button("<");
        Button right=new Button(">");
        HBox bottom=new HBox();
        HBox top=new HBox(10);
        VBox res= new VBox();



        bottom.getChildren().addAll(left, right);
        bottom.setPadding(new Insets(50,0,0,0));
        bottom.setSpacing(5);

        left.setOnAction(actionEvent -> {
            if (ctr.getLast() > 0 && ctr.getnMove()>1) {
                ctr.setBoardFEN(ctr.getLastFEN(0));
                setBoard();
            }

        });
        right.setOnAction(actionEvent -> {
            if (ctr.getLast() <ctr.getnMove()-1) {
                ctr.setBoardFEN(ctr.getLastFEN(2));
                setBoard();
            }

        });
        moves= new TextArea("Moves:\n");
        moves.setEditable(false);
        moves.setPrefHeight(SSIZE*MAX);
        moves.setPrefWidth(100);
        top.getChildren().addAll( moves);

        res.getChildren().addAll(top,bottom);

        res.setPadding(new Insets(20,30,0,10));
        return res;
    }


    private Image getImg(int id){

        if(id<=15)
            return id<=7 ? imgs[0] : imgs[1];
        else if(id<=19)
            return id<=17 ? imgs[2]: imgs[3];
        else if(id<=23)
            return id<=21 ? imgs[4] : imgs[5];
        else if (id<=27)
            return id<=25 ? imgs[6] : imgs[7];
        else if(id<=29)
            return id==28 ? imgs[8]: imgs[9];
        else
            return id==30 ? imgs[10] : imgs[11];
    }
    private void setBoard(){
        int id;
        //clear the previous board
        for(int i=0;i<NUMPIECE;i++){
            if(piece[i]!=null){
                piece[i].setVisible(false);
                piece[i]=null;
            }
        }

        //grid=null;

        //set the new board ( based on the controller)
        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++){
                board[i][j].setImage(whiteSquare(i,j) ? white : black);
                id=ctr.getID(i,j);
                if(id>=0){
                    piece[id]=new ImageView(getImg(id));
                    piece[id].setFitWidth(SSIZE);
                    piece[id].setFitHeight(SSIZE);
                    GridPane.setRowIndex(piece[id], i);
                    GridPane.setColumnIndex(piece[id], j);
                    grid.getChildren().add(piece[id]);
                    piece[id].addEventHandler(MouseEvent.MOUSE_CLICKED, this::handlePieceMove);
                }

            }
    }

    private boolean whiteSquare(int x, int y){
        return (x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0);
    }
    private void handlePieceMove(MouseEvent mouseEvent){

        Node src = (Node) mouseEvent.getSource();
        int x = GridPane.getRowIndex(src);
        int y = GridPane.getColumnIndex(src);
        int id, castled;
        boolean check=false,hasmoved=false;
        if (ctr.getStart()) {
            //nel ritorno da uno spostamento ( selX e selY sono validi)

            id=ctr.switchStart();
            if((hasmoved=ctr.move(x,y))){
                GridPane.setRowIndex(piece[id], x);
                GridPane.setColumnIndex(piece[id], y);
                check=ctr.isCheck();
                debug.setText(ctr.getBoardFEN());
                moves.appendText(ctr.printLastMove(x,y));
                /*board[x][y].setImage(yellow);

                if(lastX!=-1 && lastY!=-1)
                    board[lastX][lastY].setImage( whiteSquare(lastX, lastY) ? white : black);![](../../../../../../AppData/Local/Temp/ew3n1w3z.bmp)
                lastX=x;lastY=y;*/

            }

            board[ctr.getSelX()][ctr.getSelY()].setImage(whiteSquare(ctr.getSelX(), ctr.getSelY()) ? white : black);

            //check if a piece has been captured, in which case it must be hidden
            if(ctr.getLastPieceID()!=-1){
                piece[ctr.getLastPieceID()].setVisible(false);
                //board[x][y].setImage(yellow);
                lastX=x;lastY=y;
            }


            //check if it's a castle
            castled=ctr.hasCastled();
            if(castled>=0){
                int king= (castled%2==0) ? 30: 31;
                int rook= (castled%2==0) ? 25-(castled/2) : 26+castled%3;
                int nK = castled>1 ? 2: 6;
                int nR= castled>1 ? 3: 5;
                GridPane.setColumnIndex(piece[king], nK);
                GridPane.setColumnIndex(piece[rook], nR);
            }

            if(check){
                int[] k=ctr.kingPos(ctr.getTurn());
                board[k[0]][k[1]].setImage(red);
                ctr.setCheck();
            }else if(!ctr.wasCheck()){
                int[] k=ctr.kingPos((ctr.getTurn()+1)%2);
                if((k[0] % 2 == 0 && k[1] % 2 == 0) || (k[0] % 2 != 0 && k[1] % 2 != 0))
                    board[k[0]][k[1]].setImage(white);
                else
                    board[k[0]][k[1]].setImage(black);
            }

            try{
                if(hasmoved && startEngine!=null && startEngine.getText().contains("stop")){
                    stockfish.stopSearch();
                    stockfish.setPosition(ctr.getFEN());
                    //System.out.println(ctr.getFEN());
                    stockfish.go(Integer.parseInt(p[0].getText()), Integer.parseInt(p[1].getText()), inf.isSelected());

                }

            }catch(IOException e){ e.printStackTrace();}

        } else {
            //imposto la pos di partenza
            if((ctr.getID(x,y)!=-1) && ctr.getColor(ctr.getID(x,y))==ctr.getTurn()){
                board[x][y].setImage(yellow);
                ctr.switchStart();
                ctr.setSel(x, y);
                llastX=x; llastY=y;
            }

        }
    }

    public static void main(String[] args) {
        launch();

    }

}
