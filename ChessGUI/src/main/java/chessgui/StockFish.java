package chessgui;

import java.io.*;

public class StockFish{

    private final int MAXCASHED=50;
    private OutputStream ops;
    private InputStream is;
    private Process process;
    private BufferedWriter wr;
    private BufferedReader br;

    private Controller ctr;

    private double score;
    private int depth;
    private String bestMove;
    //private List<String> bestMoves;
    //private List<Double> bestScore;

    public StockFish(String path, Controller ctr) throws IOException {
        process = new ProcessBuilder(path).start();
        is=process.getInputStream();
        ops= process.getOutputStream();
        wr= new BufferedWriter(new OutputStreamWriter(ops));
        br= new BufferedReader(new InputStreamReader(is));
        score=0;
        depth=0;
        this.ctr=ctr;
       // bestMoves=new ArrayList<>();
       // bestScore=new ArrayList<>();
    }

    public void write(String par) throws IOException {
        wr.write(par);
        wr.flush();
        //wr.close();
    }

    public String read() throws IOException {
        return br.readLine();
    }

    public void setPosition(String fen) throws IOException {

        write("position fen "+fen+"\n");
    }
    public void go(int depth, int movetime, boolean infinite) throws IOException {
        String param="";
        if(depth>0)
            param+="depth "+depth;
        if(movetime>0)
            param+="movetime "+movetime;
        if(infinite)
            param="infinite";
        write("go "+param+"\n");
    }

    public void stopSearch() throws IOException {
        write("stop\n");
    }

    public double getScore(String res){
        assert res != null;
        if(!res.contains(" cp "))
            return score;
        String s=res.substring(res.indexOf(" cp ")+4).split(" ")[0];
        return Double.parseDouble(s)/100;
    }

    public int getDepth(String res){
        assert res != null;
        if(!res.contains(" depth "))
            return depth;
        String s= res.substring(res.indexOf(" depth ")+7).split(" ")[0];
        return Integer.parseInt(s);
    }

    public String getBestMove(String res){
        if(res==null)
            return bestMove;
        if(!res.contains(" pv "))
            return bestMove;
        bestMove=res.substring(res.indexOf(" pv ")+4);
        return bestMove;
    }

    public String getCurrentState(String res) throws IOException{
        if(res!=null){
            score=getScore(res);
            depth=getDepth(res);

          /*  if(bestScore.size()==0){
                bestScore.add(s);
                bestMoves.add(getBestMove(res));
            }else if(bestScore.get(bestScore.size()-1)<=s){
                bestScore.add(s);
                bestMoves.add(getBestMove(res));
            }*/

        }
        //System.out.println(res);
        return score+" depth: "+depth;
    }

    public String getHistoryOfPos(){
        StringBuilder res= new StringBuilder();
        /*if(bestScore.size()==0)
            return "";
        for(int i=0;i<bestScore.size();i++)
            res.append(bestScore.get(i)).append(": ").append(bestMoves.get(i)).append("\n");*/
        return res.toString();
    }
    public void closeStockfish(){
        if(process.isAlive())
            process.destroy();
    }

    public String readCashed() throws IOException {
        String res="";
        for(int i=0;i<MAXCASHED;i++){
            res=read();
            if(res!=null){
                getCurrentState(res);
                getDepth(res);
                getBestMove(res);
            }

        }

        return res;
    }

    public double getScoreForBar(){

        if(0.5-score*0.1<0 && ctr.getTurn()==0)
            return 0.05;
        return (ctr.getTurn()==0) ? 0.5-score*0.1 : 0.5+score*0.1;
    }
}
