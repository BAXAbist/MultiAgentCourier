import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.Random;

public class Rover extends Agent{

    private final AID operator = new AID("operator", AID.ISLOCALNAME);
    private final AID[] rovers = new AID[3];
    private int me;
    private int Ox;
    private int Oy;

    private int[][] map = new int[10][10];

    private Random random = new Random();

    @Override
    protected void setup() {
        rovers[0] = new AID("rover1", AID.ISLOCALNAME);
        rovers[1] = new AID("rover2", AID.ISLOCALNAME);
        rovers[2] = new AID("rover3", AID.ISLOCALNAME);
        me = Arrays.asList(rovers).indexOf(super.getAID());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("control-recieve");
        sd.setName("JADE-control");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new Messaging(this,1000));
    }

    private class Messaging extends TickerBehaviour{

        public Messaging(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {

            move();
            //---------------------------------------------------------------------
            int isBarrier = random.nextInt(100);
            //System.out.println(isBarrier);
            if(isBarrier > 80) {
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for(int i = 0; i< rovers.length; i++)
                    if (i!=me)
                        cfp.addReceiver(rovers[i]);
                cfp.addReceiver(operator);
                cfp.setContent("pit:"+Ox+"-"+Oy);
                cfp.setConversationId("msge");
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(cfp);
                System.out.println("Сообщение отправлено мной "+rovers[me].getLocalName());

            }
            //---------------------------------------------------------------------
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                System.out.println("Я "+rovers[me].getLocalName()+" услышал от "+msg.getSender().getLocalName()+" сообщение: "+msg.getContent());
            }

        }

        private void move(){
                if(random.nextInt(100)>50)
                    if(random.nextInt(100)>50)
                        Ox++;
                    else
                        Ox--;
                else
                    if(random.nextInt(100)>50)
                        Oy++;
                    else
                        Oy--;

        }

    }
}
