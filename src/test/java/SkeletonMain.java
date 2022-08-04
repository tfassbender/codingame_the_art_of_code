import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class SkeletonMain {
	
	public static void main(String[] args) {
		/* Multiplayer Game */
		MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
		
		// Adds as many player as you need to test your game
		gameRunner.addAgent(Agent1.class, "Redstrike", "https://static.codingame.com/servlet/fileservlet?id=57293561455683&format=profile_avatar");
		gameRunner.addAgent(Agent1.class, "Tux4711", "https://static.codingame.com/servlet/fileservlet?id=82852165040194&format=profile_avatar");
		//gameRunner.addAgent("python3 ./src/test/java/player.py");
		
		// Another way to add a player
		// gameRunner.addAgent("python3 /home/user/player.py");
		
		gameRunner.setLeagueLevel(1);
		
		// With server
		gameRunner.start();
		
		// Without sever
		//        GameResult result = gameRunner.simulate();
		
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
		//        result.tooltips.forEach(t -> System.out.println(t.turn+"#: "+t.text));
	}
}
