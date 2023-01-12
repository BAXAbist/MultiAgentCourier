import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;

public class YandexMan extends Agent {

    private final AID[] rovers = new AID[3];
    private String SQL;

    @Override
    protected void setup() {
        rovers[0] = new AID("rover1", AID.ISLOCALNAME);
        rovers[1] = new AID("rover2", AID.ISLOCALNAME);
        rovers[2] = new AID("rover3", AID.ISLOCALNAME);

        addBehaviour(new GetMsgFromRovers());
    }

    private class GetMsgFromRovers extends CyclicBehaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                String nameRov = msg.getSender().getLocalName();
                String typeBarrier = msg.getContent();
                SQL = String.format("insert into logs values('%s', 0, 0, '%s', getdate())",
                        nameRov,
                        typeBarrier);
            }

        }
    }

}
