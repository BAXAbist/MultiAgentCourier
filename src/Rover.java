import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.*;
import java.util.*;

public class Rover extends Agent {

  private int cntRovers;
  private final AID operator = new AID("operator", AID.ISLOCALNAME);
  private AID[] rovers;
  private int me, Ox, Oy;
  private Node nodeNow, goal, end;
  Node u;
  private Stack<Node> path = new Stack<Node>();
  private Node[][] map;


  @Override
  protected void setup() {
    Object[] args = getArguments();
    cntRovers = Integer.parseInt(args[0].toString());
    rovers = new AID[cntRovers];
    for (int i = 0; i < cntRovers; i++) {
      String nameRover = Integer.toString(i);
      rovers[i] = new AID(nameRover, AID.ISLOCALNAME);
    }
    me = Arrays.asList(rovers).indexOf(super.getAID());

    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("control-recieve");
    sd.setName("JADE-control");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }
    addBehaviour(new Messaging(this, 100));
  }

  private class Messaging extends TickerBehaviour {

    boolean isbarrier = false;
    boolean isblocked = false;
    public Messaging(Agent a, long period) {
      super(a, period);
    }

    @Override
    public void onStart() {
      super.onStart();
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = myAgent.receive(mt);
      if (msg == null)
        msg = blockingReceive(mt);
      try (Reader reader = new FileReader("resources/map.txt")) {
        BufferedReader bfreader = new BufferedReader(reader);
        String dump = bfreader.readLine();
        int hieghtm = Integer.parseInt(dump.split(",")[0]);
        int widthm = Integer.parseInt(dump.split(",")[1]);
        map = new Node[hieghtm][widthm];
        for (int i = 0; i < map.length; i++) {
          dump = bfreader.readLine();
          String[] dd = dump.split(" ");
          for (int j = 0; j < map[0].length; j++) {
            if (dd[j].equals("1"))
              map[i][j] = new Node(i, j);
          }
        }

        for (int i = 0; i < map.length; i++) {
          for (int j = 0; j < map[0].length; j++) {
            if (map[i][j] != null) {
              if (map[i + 1][j] != null)
                map[i][j].neighbors.add(map[i + 1][j]);
              if (map[i][j + 1] != null)
                map[i][j].neighbors.add(map[i][j + 1]);
              if (map[i - 1][j] != null)
                map[i][j].neighbors.add(map[i - 1][j]);
              if (map[i][j - 1] != null)
                map[i][j].neighbors.add(map[i][j - 1]);
            }
          }
        }
      } catch (IOException ex) {
        System.out.println(ex.getMessage());
      }
      String data = msg.getContent().split(":")[1];
      int startY = Integer.parseInt(data.split(",")[0]);
      int startX = Integer.parseInt(data.split(",")[1]);
      int goalY = Integer.parseInt(data.split(",")[2]);
      int goalX = Integer.parseInt(data.split(",")[3]);
      nodeNow = map[startY][startX];
      goal = map[goalY][goalX];
      end = Node.aStar(nodeNow, goal);
      System.out.println(end.Oy+" "+end.Ox);
      while (end.parent != null) {
        path.add(end);
        end = end.parent;
      }
      u = nodeNow;
      //System.out.println(path.size());
      try {
        Thread.sleep(3000);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected void onTick() {
      if (u.equals(nodeNow))
        u = path.pop();
      isbarrier = isBarrier(u.Oy, u.Ox);
      if (isbarrier) {
        try {Thread.sleep(10);} catch (Exception e) {throw new RuntimeException(e);}
        if(isblocked){
          boolean res = roll(u.Oy, u.Ox);
          if (res)
            try {Thread.sleep(10);}catch (Exception e) {throw new RuntimeException(e);}
          else {
            map[u.Oy][u.Ox].isReachable = false;
            for (int i = 0; i<map.length;i++)
              for (int j = 0; j<map[0].length;j++)
                if (map[i][j] != null)
                  map[i][j].parent = null;
            end = Node.aStar(nodeNow, goal);
            map[u.Oy][u.Ox].isReachable = true;
            //System.out.println(end.Oy+" "+end.Ox);
            path.clear();
            while (end.parent != null) {
              path.add(end);
              end = end.parent;
            }
            u = path.peek();
          }
        }else
          isblocked = true;
      } else {
        isblocked = false;
        move(nodeNow.Oy, nodeNow.Ox, u.Oy, u.Ox);
        nodeNow = u;
        if (path.empty()) {
          doneWork();
          doDelete();
        }
      }

      //---------------------------------------------------------------------

            /*ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for(int i = 0; i< rovers.length; i++)
                if (i!=me)
                    cfp.addReceiver(rovers[i]);
            cfp.addReceiver(operator);
            cfp.setContent("pit:"+Ox+"-"+Oy);
            cfp.setConversationId("msge");
            cfp.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(cfp);*/
      //System.out.println("Сообщение отправлено мной "+rovers[me].getLocalName()+" : "+cfp.getContent());


      //---------------------------------------------------------------------
            /*MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                System.out.println("Я "+rovers[me].getLocalName()+" услышал от "+msg.getSender().getLocalName()+" сообщение: "+msg.getContent());
            }*/

    }

    private void move(int y0, int x0, int y1, int x1) {
      ACLMessage info = new ACLMessage(ACLMessage.INFORM);
      info.addReceiver(operator);
      info.setContent("mov:" + y0 + "," + x0 + "," + y1 + "," + x1);
      myAgent.send(info);
    }

    private boolean isBarrier(int y, int x) {
      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
      cfp.addReceiver(operator);
      cfp.setContent("infb:" + y + "," + x);
      myAgent.send(cfp);
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = blockingReceive(mt, 100);;
      String res = "1";
      if (msg != null)
        res = msg.getContent().split(":")[1];

      return res.equals("1");
    }

    private boolean roll(int y, int x){
      Random r = new Random();
      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
      cfp.addReceiver(operator);
      cfp.setContent("roll:" + y + "," + x + "," + r.nextInt(2));
      myAgent.send(cfp);
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = blockingReceive(mt, 100);
      boolean res = false;
      if (msg != null) {
        res = msg.getContent().split(":")[1].equals("1");
      }
      return res;
    }

    private void doneWork(){
      ACLMessage info = new ACLMessage(ACLMessage.INFORM);
      info.addReceiver(operator);
      info.setContent("done:1");
      myAgent.send(info);
    }
  }
}
