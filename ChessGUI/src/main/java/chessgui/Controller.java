package chessgui;

//INITIAL BOARD
/*a b c d e f g h
  0 0 0 0 0 0 0 0 7
  0 0 0 0 0 0 0 0 6
  0 0 0 0 0 0 0 0 5
  0 0 0 0 0 0 0 0 4
  0 0 0 0 0 0 0 0 3
  0 0 0 0 0 0 0 0 2
  0 0 0 0 0 0 0 0 1 */


import java.util.*;

public class Controller {
    private final int MAX=8, BLACK=0, WHITE=1;
    private final int EMPTY=-1;
    private final int [][] board=new int[MAX][MAX];

    //private int x,y;
    private int selX, selY, lastPiece=-1, castled;
    private boolean start,check;

    private int turn, nMove, cont, lastfen, passantX, passantY, currentFEN, halfmoves;
    private final List<String> fenHistory;
    private final List<List<String>> listOfFens;

    private final boolean[] stillCastle=new boolean[4]; //0) black-queenside,1) white-queenside,2) black-kingside ,3) white-kingside
    private String enpassantFEN="";


    public Controller(){

        setBoard(false);
        fenHistory=new ArrayList<>();
        listOfFens=new ArrayList<>();
        listOfFens.add(fenHistory);
        //fenHistory.add(getBoardFEN());
        start=false;
        castled=-1; //1 is white kingside, 0 is black kingside, 3 is white queenside, 2 is black queenside
        turn=WHITE;
        nMove=1;
        cont=1;
        passantX=-1; passantY=-1;
        check=false;
        lastfen=0;
        currentFEN=0;
        halfmoves=0;
        for(int i=0;i<4;i++)
            stillCastle[i]=true;
    }

    private void setBoard(boolean emptiness){

        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++){
                if(emptiness)
                    board[i][j]=EMPTY;
                else if(i==1 || i==6)
                    board[i][j]= i==1 ? j : 8+j; //pawns
                else if ((i==0 || i==7) && (j==0 || j==7))
                    board[i][j]= i==0 ? 24+j%2 : 26+j%2; //rooks
                else if ((i==0 || i==7) && (j==1 || j==6))
                    board[i][j]= i==0 ? 17-j%2 : 19-j%2; //knights
                else if ((i==0 || i==7) && (j==2 || j==5))
                    board[i][j]= i==0 ? 20+j%2 :22+j%2; //bishops
                else if ((i==0 || i==7) && j==3)
                    board[i][j]= i==0 ? 28 : 29; //queens
                else if ((i==0 || i==7) )
                    board[i][j]= i==0 ? 30 : 31;
                else
                    board[i][j]=EMPTY;
            }

    }
    public String printBoard(){
        //print board to console
        StringBuilder res= new StringBuilder("Current position: \n\n");
        for(int i=0;i<MAX;i++){
            for(int j=0;j<MAX;j++){

                res.append(board[i][j]);
                res.append((board[i][j] < 0) ? " " : "  ");
            }

            res.append("\n");
        }

        return res.toString();
    }

    public boolean move(int x, int y){

        int id=getID(selX, selY);
        int edge;
        if(getColor(id)!=turn)
            return false;
        if(getColor(id)==getColor(getID(x, y)) && getID(x,y)!=EMPTY)
            return false;

        if(x==selX && y==selY)
            return false;

        enpassantFEN="";

        if(!castle(id, x, y) && isPossible(x,y) && !isPinned(id, x,y) ){
            int temp=board[selX][selY];
            lastPiece=board[x][y];
            board[x][y]=temp;
            board[selX][selY]=EMPTY;
            switchTurn();
            nMove++;

            if(lastPiece!=-1 || id<=15)
                halfmoves=0;
            else
                halfmoves++;
            if(id<=15){
                //check en passant
                edge= (y==0) ? 0 : (y==7) ? 2 : 1;
                if(passantY!=-1 && passantX!=-1 && Math.abs(x-passantX)==1 && y==passantY){
                    //en passant has been done
                    lastPiece=board[passantX][passantY];
                    board[passantX][passantY]=EMPTY;
                }
                passantX=-1;
                passantY=-1;

                if(id<=7 && selX==1 && ((edge<=1 && board[x][y+1]<=15 && board[x][y+1]>7)  || (edge>=1 && board[x][y-1]<=15 && board[x][y-1]>7) ) ||
                    (id>7 && selX==6 && ((edge<=1 && board[x][y+1]<=7 && board[x][y+1]>0)  || (edge>=1 && board[x][y-1]<=7 && board[x][y-1]>0) ))){
                    passantX=x;
                    passantY=y;
                    enpassantFEN=(getColor(id)==WHITE) ? getBoardCoord(passantX+1, passantY) : getBoardCoord(passantX-1, passantY);
                    //System.out.println("en passant possible "+passantX+" "+passantY+enpassantFEN);
                }

            }
            if(id>=30){
                stillCastle[getColor(id)]=false;
                stillCastle[getColor(id)+2]=false;
            }
            if(id>=24 && id<=27)
                stillCastle[getColor(id)+2*(id%2)]=false;

            //System.out.println(printBoard());
            lastfen++;
            return true;
        }

        return castled >= 0;

    }

    private String getBoardCoord(int x, int y){
        char[] l = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        return ""+l[y]+(8-x);
    }
    private boolean isFree(int sX, int sY, int x, int y){
        //check if the path between src, dst is free of pieces ( for bishops, rooks, queens and pawns)

        int signX=(int)Math.signum(sX-x);
        int signY=(int)Math.signum(sY-y);
        int max=Math.max(Math.abs(sX-x), Math.abs(sY-y));


        for(int i=1;i<=max-1;i++){
            //System.out.println(i+" "+i*signX+" "+i*signY);
            if(board[x+i*signX][y+i*signY]!=-1)
                return false;
        }
        return true;
    }
    private boolean isPossible(int x, int y){
        return isPossible(selX, selY, x,y);
    }
    private boolean isPossible(int sX, int sY, int x, int y){
        int piece=getID(sX, sY);
        int color=getColor(piece);
        if(piece>=0 && piece<=15){
            //pawn
            if(color==BLACK){
                //System.out.println(turn+" "+passantX+" "+x+" "+passantY+ " "+y);
                if(x==passantX+1 &&  y==passantY){
                    return true;
                }
                if(y==sY && getID(x,y)!=-1 || (y!=sY && x==sX))
                    return false;

                return sX <= x && (sX != 1 || x <= 3 ) && (sX == 1 || x <= sX + 1) && (y < sY + 2 && y > sY - 2) && ((y == sY) || getID(x, y) != -1)
                        && (x != sX + 2 || (y == sY && getID(x, y) == -1)) && isFree(sX, sY, x, y);
            }else{
                if(x==passantX-1 &&  y==passantY){
                    return true;
                }
                if(y==sY && getID(x,y)!=-1 || (y!=sY && x==sX))
                    return false;
                return sX >= x && (sX != 6 || x >= 4) && (sX == 6 || x >= sX - 1) && (y < sY + 2 && y > sY - 2) && (y == sY || getID(x, y) != -1)
                        && (x != sX - 2 || (y == sY && getID(x, y) == -1)) && isFree(sX, sY, x, y);
            }

        }else if(piece<=19){
            //knights
            return Math.abs(x - sX) < 3 && Math.abs(x - sX) >= 1 && (Math.abs(x - sX) != 1 || Math.abs(y - sY) == 2) && (Math.abs(x - sX) != 2 || Math.abs(y - sY) == 1);
        }
        else if(piece<=23){
            //bishops
            return Math.abs(x - sX) == Math.abs(y - sY) && isFree(sX, sY, x, y);
        }
        else if(piece<=27){
            //rooks
            return (x == sX || y == sY) && isFree(sX, sY, x, y);
        }
        else if (piece<=29){
            //queens
            return ((x == sX || y == sY) || (Math.abs(x - sX) == Math.abs(y - sY))) && isFree(sX, sY, x, y);
        }else{
            //kings
            return Math.abs(x - sX) <= 1 && Math.abs(y - sY) <= 1;
        }
    }
    private boolean castle(int king, int x, int y){
        if(king!=30 && king!=31)
            return false;

        if(!((x==0 || x==7) && (y==2 || y==6)))
            return false;

        if(!stillCastle[getColor(king)+2-y%6])
            return false;

        int nX= king==30 ? 0 : 7;
        if(y==6){
            //kingside castle
            int rook=king==30 ? 25 : 27;
            if(getID(x, y)==-1 && x==nX && selX==nX && selY==4 && getID(nX,7)==rook && getID(nX, 6)==-1 && getID(nX, 5)==-1){
                castled=nX%2;
                board[nX][6]=king;
                board[nX][5]=rook;
                board[nX][7]=EMPTY;
                board[nX][4]=EMPTY;
                switchTurn();
                nMove++;
                stillCastle[getColor(king)]=false;
                stillCastle[getColor(king)+2]=false;
                lastfen++;
                halfmoves++;
                return true;
            }
        }else{
            //queenside castle
            int rook=king==30 ? 24 : 26;
            if(getID(x, y)==-1 && x==nX && selX==nX && selY==4 && getID(nX,0)==rook && getID(nX, 1)==-1 && getID(nX, 2)==-1 && getID(nX, 3)==-1){
                castled=nX%2+2;
                board[nX][2]=king;
                board[nX][3]=rook;
                board[nX][0]=EMPTY;
                board[nX][4]=EMPTY;
                switchTurn();
                stillCastle[getColor(king)]=false;
                stillCastle[getColor(king)+2]=false;
                nMove++;
                lastfen++;
                return true;
            }
        }


        return false;
    }
    public int getID(int x, int y){
        return board[x][y];
    }
    public void setSel(int x, int y){
        selX=x; selY=y;

    }

    public int switchStart(){
        //returns the piece id
        start= !start;
        castled=-1;
        return board[selX][selY];
    }
    public boolean getStart(){
        return start;
    }
    public int getSelX(){
        return selX;
    }
    public int getSelY(){
        return selY;
    }

    public int getColor(int id){
        //given a piece, it returns the color type (0 black, 1 white)
        if(id==-1)
            return EMPTY;
        List<Integer> black= new ArrayList<>(List.of(0,1,2,3,4,5,6,7,16,17,20,21,24,25,28,30));
        return black.contains(id) ? BLACK: WHITE;
    }

    public int getLastPieceID(){
        //check if there was a piece on the new position when moving
        return this.lastPiece;
    }

    public int hasCastled(){
        return castled;
    }

   public void switchTurn(){
        turn= (turn==WHITE) ? BLACK : WHITE;
   }

   public String printLastMove(int x, int y){
        String[] c={"a", "b", "c", "d", "e", "f", "g", "h", ""};
        String[] p={"p", "N", "B", "R", "Q", "K"};
        int coord, id=getID(x,y), move=listOfFens.get(currentFEN).size();
        String res, n=(move%2==0)? "    ": "\n";
        coord=8-x;
        String amb=c[ambiguityPGN(id, x, y)];


        if(castled>=0){
            res= castled<2 ? "O-O": "O-O-O";
        }else if(id<=15){
            //pawn
            if(lastPiece!=-1)
                res=c[selY]+"x"+c[y]+coord;
            else
                res=c[y]+coord;
        }else{
            String s = p[id / 4 - 3 + id / 30]+amb;
            if(lastPiece!=-1)
                res= s +"x"+c[y]+coord;
            else
                res= s +c[y]+coord;
        }
        if(check)
            res+="+";

        return move%2==0 ? (move/2)+": "+res+n : res+n;
   }

   private int[] getCoord(int id){
        int[]res=new int[2];
        res[0]=-1;
        res[1]=-1;
        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++){
                if(getID(i,j)==id){
                    res[0]=i; res[1]=j;
                    return res;
                }
            }
        return res;
   }
    private int ambiguityPGN(int id, int x, int y){
        //removes ambiguity for rooks and knights in case of 2 piece that could go on the same way
        if(id<16 || id>27 || (id>19 && id<24))
            return 8;
        int idAM=(id%2==0) ? id+1: id-1;
        int[] sXY=getCoord(idAM);
        if(sXY[0]==-1 && sXY[1]==-1)
            return 8;
        if(isPossible(sXY[0], sXY[1], x, y))
            return selY;
        return 8;
    }
   public void setBoardFEN(String fen) throws IllegalArgumentException{
       int token=0;
       HashMap<Character, Integer> val=new HashMap<>();
       val.put('p',0 );
       val.put('P',8);
       val.put('n', 16);
       val.put('N', 18);
       val.put('b',20);
       val.put('B',22);
       val.put('r',24);
       val.put('R',26);
       val.put('q', 28);
       val.put('Q', 29);
       val.put('k', 30);
       val.put('K', 31);

       StringTokenizer st=new StringTokenizer(fen);

       if(st.countTokens()!=6)
           throw new IllegalArgumentException("FEN does not contain 6 values!");
       while(st.hasMoreTokens()){
            if(token==0){

                String[] rows=st.nextToken().split("/");
                if(rows.length!=8)
                    throw new IllegalArgumentException("The board must be a 8x8!");
                for(int i=0;i< rows.length;i++){
                    int nJ=0;
                   for(int j=0;j<rows[i].length();j++){
                       if(Character.isDigit(rows[i].charAt(j))){
                           int m=Character.getNumericValue(rows[i].charAt(j));
                           for(int k=nJ;k<nJ+m;k++)
                               board[i][k]=-1;
                           nJ=nJ+m;
                       }
                       else{
                           Integer v=val.get(rows[i].charAt(j));
                           if(v==null)
                               throw new IllegalArgumentException("The board contains an unknown piece!");
                           board[i][nJ]=v;
                           val.put(rows[i].charAt(j),val.get(rows[i].charAt(j))+1 );
                           nJ++;
                       }

                   }
                }
                //break;
            }else{
                String s=st.nextToken();
                if(token==1){
                    //says the turn
                    turn=(s.equals("b")) ? BLACK : WHITE;
                }else if(token==2){
                    //says the castle ability
                    stillCastle[0]=s.contains("q");
                    stillCastle[1]=s.contains("Q");
                    stillCastle[2]=s.contains("k");
                    stillCastle[3]=s.contains("K");
                }

            }
            token++;

       }
        //adjust rooks for castling
        if(board[0][7]==24 && getCoord(25)[0]!=-1){
            int rx=getCoord(25)[0], ry=getCoord(25)[1];
            board[0][7]=25;
            board[rx][ry]=24;
        }
        if(board[7][0]==27){
            int rx=getCoord(26)[0], ry=getCoord(25)[1];
            board[7][0]=26;
            board[rx][ry]=27;
        }
   }
   public String getBoardFEN(){
        StringBuilder res= new StringBuilder();
        char[] val=new char[32];
        for(int i=0;i<32;i++){
            if(i<=15)
                val[i]= (i<=7)? 'p': 'P';
            else if(i<=19)
                val[i]=(i<=17)? 'n': 'N';
            else if(i<=23)
                val[i]=(i<=21)? 'b': 'B';
            else if(i<=27)
                val[i]=(i<=25)? 'r': 'R';
            else if(i<=29)
                val[i]=(i<=28)? 'q': 'Q';
            else
                val[i]= i==30 ? 'k' : 'K';
        }
        int n=0;
        for(int i=0;i<MAX;i++){
            for(int j=0;j<MAX;j++){
                while(j+n<MAX && board[i][j+n]==-1){
                    n++;
                }

                if(n!=0){
                    res.append(n);
                    j+=n-1;
                }
                else
                    res.append(val[board[i][j]]);

                n=0;
            }
            if(i<MAX-1)
                res.append("/");
        }
        res.append((turn == WHITE) ? " w" : " b");

        String temp=(stillCastle[3]) ? "K" : "-";
        temp+=((stillCastle[1])) ? "Q": '-';
        int c=temp.replace("-", "").length();
        if(c==2 || c==1)
            res.append(" ").append(temp.replace("-", ""));
        else
            res.append(" -");
        temp=(stillCastle[2]) ? "k" : "-";
        temp+=((stillCastle[0])) ? "q": '-';
        c=temp.replace("-", "").length();
        if(c==2 || c==1)
           res.append(temp.replace("-", ""));
        else
           res.append("-");

        if(!enpassantFEN.equals(""))
            res.append(" ").append(enpassantFEN);
        else
            res.append(" -");

        res.append(" ").append(halfmoves);

        res.append(" ").append(listOfFens.get(currentFEN).size()/2+1).append("\n");

        //save it on the fen list
        boolean find=false;
        if(lastfen<listOfFens.get(currentFEN).size()){
            //that's a different game possible

            for(List<String> l : listOfFens){
                if(l.size()>lastfen && l.get(lastfen).equals(res.toString())){
                    find=true;
                    break;
                }

            }
            if(!find){

                List<String> t= new ArrayList<>(listOfFens.get(currentFEN).subList(0,lastfen));
                t.add(res.toString());
                listOfFens.add(t);
                currentFEN++;

            }

        }else{
            //currentFEN--;
            if(!listOfFens.get(currentFEN).contains(res.toString()))
                listOfFens.get(currentFEN).add(res.toString());

        }
        //System.out.println("NEW LIST:\n"+listOfFens);
        return res.toString();
   }
   public int[] kingPos(int color){
        int king=30+color;
        int[] res=new int[2];
        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++){
                if(getID(i,j)==king){
                    res[0]=i; res[1]=j;
                    return res;
                }

            }
        return res;
   }
   public boolean isPinned(int id, int x, int y){
        //check if by moving the id piece on the x,y square, the king is exposed
       int ccolor=getColor(id);
       int tcolor;
       int[] cking=kingPos(ccolor);

       //temporary make the move to see if this is possible
       int temp=board[x][y];
       board[selX][selY]=EMPTY;
       board[x][y]=id;

       if(id>=30){
           //it's a king trying to move
           cking[0]=x;
           cking[1]=y;
       }

       for(int i=0;i<MAX;i++)
           for(int j=0;j<MAX;j++){
               tcolor=getColor(getID(i,j));
               if(tcolor!=ccolor && tcolor!=-1 && isPossible(i,j,cking[0], cking[1])){
                   //opposite color piece that could eat the king
                   //System.out.println("pinned by "+i+" "+j);
                   board[x][y]=temp;
                   board[selX][selY]=id;
                   return true; //it must be pinned
               }
           }
       board[x][y]=temp;
       board[selX][selY]=id;
       return false;
   }

   public boolean isCheck(){

        int color=turn==WHITE ? BLACK : WHITE;
        int[] cking=kingPos(turn);
        for(int i=0;i<MAX;i++)
            for(int j=0;j<MAX;j++){
                if(getColor(getID(i,j))==color && isPossible(i,j, cking[0], cking[1])){
                    check=true;
                    return true;
                }

            }
        return false;
   }
   public int getTurn(){return turn;}
   public boolean wasCheck(){
        return check;
   }
   public void setCheck(){
        check=!check;
   }

   public String getLastFEN(int pos){

        String res=listOfFens.get(currentFEN).get(lastfen+pos-1);
        if(pos==0){
            //i'm coming back, check if this position is on the main line too
            if(currentFEN!=0 && listOfFens.get(currentFEN-1).contains(res)){
                currentFEN--;
            }

        }
        //System.out.println(res+ " "+lastfen+" "+nMove);
        lastfen+= (pos==0) ? -1 : 1;
        return res;
   }
   public int getLast(){return lastfen;}

   public int getnMove(){return listOfFens.get(currentFEN).size();}
   public boolean addHistoryFEN(String fen){
        boolean cont=fenHistory.contains(fen);
        if(!cont){
            fenHistory.add(fen);
            lastfen++;
            nMove++;
        }
        return !cont;
    }

   public void clearHistoryFEN(){

        listOfFens.removeAll(listOfFens);
        fenHistory.clear();
        listOfFens.add(fenHistory);
        currentFEN=0;
        lastfen=0;
        nMove=1;
        cont=1;
        halfmoves=0;
        for(int i=0;i<4;i++)
            stillCastle[i]=true;
   }

   public String getFEN(){
        return listOfFens.get(currentFEN).get(listOfFens.get(currentFEN).size()-1);
   }


}
