package deadlymobs;


/**
* A mod which using the Digging Mobs AI API to make the default MC mobs dig
* 
* @author	Clinton Alexander
* @version	0.3.1
*/
public class mod_DiggingMobs {
	public static boolean zombiesDig;
	public static boolean creepersDig;
	public static boolean spidersDig;
	public static boolean mobsSprint;
	public static int  mobSprintSpeed;//("Sprint multiplier", 2, 10);
	
	public mod_DiggingMobs() {
	}
	
	public String Version() {
		return "v0.4";
	}
}