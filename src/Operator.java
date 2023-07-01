import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;

public class Operator extends Agent {
  /**
   * type msg:
   * infb:y,x - выдать информацию по препятствиям на заданной точке
   * mov:y0,x0,y1,x1 - переместить агента на карте
   * start:y0,x0,y1,x1 - стартовые позиции для всех агентов и цели которые им нужно достичь
   * roll: y,x,roll - позиция на которую претендуют, а также результат броска
   * done - агент окончил работу
   */
  private int cntRovers;
  private ArrayList<String> isDone = new ArrayList<>();
  long start;
  long finish;
  private AID[] rovers;
  private HashMap<String,int[]> roversCurLoc = new HashMap<String,int[]>();
  private ArrayList<RollerBlock> roversBlocked = new ArrayList<>();
  private String res = "resources/";
  private World gui;

  private int abc;
  private int[][] startData;


  @Override
  protected void setup() {
    Object[] args = getArguments();
    cntRovers = Integer.parseInt(args[0].toString());
    rovers = new AID[cntRovers];
    startData = new int[cntRovers][4];

    for (int i = 0; i<cntRovers;i++) {
      String nameRover = Integer.toString(i);
      rovers[i] = new AID(nameRover, AID.ISLOCALNAME);
    }

    /*startData[0] = new int[]{1,1,1,10};//{1, 1, 26, 176};
    startData[1] = new int[]{1,11,1,2};//{1, 128, 67, 1};
    startData[2] = new int[]{91, 1, 92, 167};*/
    try {
      gui = new World(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    gui.showGui();
    //addBehaviour(new Starting());
    addBehaviour(new GetMsgFromRovers());


  }

  private class GetMsgFromRovers extends CyclicBehaviour {

    @Override
    public void onStart() {
      super.onStart();
      int[][] mapInt = gui.mapInt;
      try (FileWriter writer = new FileWriter(res + "map.txt", false)) {
        writer.write(mapInt.length + "," + mapInt[0].length + "\n");
        for (int[] i : mapInt) {
          for (int j : i) {
            writer.write(j + " ");
          }
          writer.append('\n');
        }
        writer.flush();
      } catch (IOException ex) {
        System.out.println(ex.getMessage());
      }

      Random rY = new Random();
      Random rX = new Random();
      for (int i = 0; i < startData.length;i++){
        int Oy = 0;
        int Ox = 0;
        while (mapInt[Oy][Ox] == 0){
          Oy = rY.nextInt(mapInt.length);
          Ox = rX.nextInt(mapInt[0].length);
        }
        startData[i][0] = Oy;
        startData[i][1] = Ox;

        Oy = 0;
        Ox = 0;
        while (mapInt[Oy][Ox] == 0){
          Oy = rY.nextInt(mapInt.length);
          Ox = rX.nextInt(mapInt[0].length);
        }
        startData[i][2] = Oy;
        startData[i][3] = Ox;
      }

      for (int i = 0; i < startData.length; i++) {
        gui.setAgent(startData[i][0], startData[i][1]);
        roversCurLoc.put(rovers[i].getLocalName(), new int[]{startData[i][0], startData[i][1]});
        gui.setStart(startData[i][2], startData[i][3]);
      }

      for (int i = 0; i < rovers.length; i++) {
        ACLMessage info = new ACLMessage(ACLMessage.INFORM);
        info.addReceiver(rovers[i]);
        info.setContent("start:" + startData[i][0] + "," + startData[i][1] +
                "," + startData[i][2] + "," + startData[i][3]);
        myAgent.send(info);
      }
      start = System.currentTimeMillis();
    }

    @Override
    public void action() {
      MessageTemplate mt = MessageTemplate.or(
              MessageTemplate.MatchPerformative(ACLMessage.INFORM)
              , MessageTemplate.MatchPerformative(ACLMessage.CFP));
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        String nameRov = msg.getSender().getLocalName();
        String typemsg = msg.getContent().split(":")[0];
        String data = msg.getContent().split(":")[1];

        if (typemsg.equals("infb")) {
          int x = Integer.parseInt(data.split(",")[1]);
          int y = Integer.parseInt(data.split(",")[0]);
          int res = gui.isBarrier(y, x) ? 1 : 0;

          ACLMessage info = new ACLMessage(ACLMessage.INFORM);
          info.addReceiver(msg.getSender());
          info.setContent("infb:" + res);
          myAgent.send(info);
        }
        if (typemsg.equals("mov")) {
          int y0 = Integer.parseInt(data.split(",")[0]);
          int x0 = Integer.parseInt(data.split(",")[1]);
          int y1 = Integer.parseInt(data.split(",")[2]);
          int x1 = Integer.parseInt(data.split(",")[3]);
          gui.clearAgentLastPlace(y0, x0);
          gui.setAgent(y1, x1);
          roversCurLoc.put(msg.getSender().getLocalName(), new int[]{y1,x1});
        }
        if (typemsg.equals("roll")) {
          RollerBlock rb = new RollerBlock();
          rb.rov = msg.getSender();
          rb.currOy = roversCurLoc.get(msg.getSender().getLocalName())[0];
          rb.currOx = roversCurLoc.get(msg.getSender().getLocalName())[1];
          rb.needOy = Integer.parseInt(data.split(",")[0]);
          rb.needOx = Integer.parseInt(data.split(",")[1]);
          rb.roll = Integer.parseInt(data.split(",")[2]);
          for (var blockrov: roversBlocked){
            if (blockrov.currOy == rb.needOy && blockrov.currOx == rb.needOx){
              boolean winCurr = (rb.roll > blockrov.roll)||
                (
                  (rb.roll == blockrov.roll)
                  &&(Integer.parseInt(rb.rov.getLocalName()) > Integer.parseInt(blockrov.rov.getLocalName()))
              );
              ACLMessage rollWin = new ACLMessage(ACLMessage.INFORM);
              rollWin.addReceiver(winCurr ? rb.rov : blockrov.rov);
              rollWin.setContent("roll:" + 1);
              myAgent.send(rollWin);

              ACLMessage rollLoss = new ACLMessage(ACLMessage.INFORM);
              rollLoss.addReceiver(!winCurr ? rb.rov : blockrov.rov);
              rollLoss.setContent("roll:" + 0);
              myAgent.send(rollLoss);
              roversBlocked.remove(blockrov);
              break;
            }
          }
          roversBlocked.add(rb);
        }
        if(typemsg.equals("done")){
          isDone.add("OK");
          if (isDone.size()==rovers.length) {
            finish = System.currentTimeMillis() - start;
            System.out.println(finish);
          }
        }
      }
    }
  }

  private class RollerBlock{
    public int currOy;
    public int currOx;
    public int needOy;
    public int needOx;
    public AID rov;
    public int roll;
  }
}
