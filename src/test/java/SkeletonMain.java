import com.codingame.gameengine.runner.MultiplayerGameRunner;
import com.codingame.gameengine.runner.simulate.GameResult;

public class SkeletonMain {
    public static void main(String[] args) {

        // Uncomment this section and comment the other one to create a Solo Game
        /* Solo Game */
        // SoloGameRunner gameRunner = new SoloGameRunner();

        // Sets the player
        // gameRunner.setAgent(Player1.class);

        // Sets a test case
        // gameRunner.setTestCase("test1.json");

        /* Multiplayer Game */
        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();

        // Adds as many player as you need to test your game
        gameRunner.addAgent(Agent1.class);
        gameRunner.addAgent(Agent1.class);

        // Another way to add a player
        // gameRunner.addAgent("python3 /home/user/player.py");
        
        gameRunner.setLeagueLevel(1);

        // With server
//        gameRunner.start();
        
        // Without sever
        GameResult result = gameRunner.simulate();

        // Can be used to see the exceptions in the agent class
//      result.outputs.forEach((f, l) -> {
//      System.out.println(f);
//      for (String str : l) System.out.println(" "+str);
//      });
//      result.errors.forEach((f, l) -> {
//      System.out.println(f);
//      for (String str : l) System.out.println(" "+str);
//      });
        
        // Any game ending errors?
        result.tooltips.forEach(t -> System.out.println(t.turn+"#: "+t.text));
    }
}
