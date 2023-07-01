import jade.core.MainContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Main {

  private static int cntRovers = 20;

  public static void main(String[] args) {

    Runtime rt = Runtime.instance();


    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.MAIN_PORT, "10098");
    p.setParameter(Profile.GUI, "true");
    ContainerController cc = rt.createMainContainer(p);
    args[0] = Integer.toString(cntRovers);
    try {
      AgentController agentOp = cc.createNewAgent("operator", "Operator", args);
      agentOp.start();
      for (int i = 0; i < cntRovers; i++) {
        String nameRov = Integer.toString(i);
        AgentController agent = cc.createNewAgent(nameRov, "Rover", args);
        agent.start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
