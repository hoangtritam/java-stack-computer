package mc8max.tool.jsc;

public class Logger 
{
	private Logger()
	{
		// private, empty on purpose
	}
	
	public static void info(String msg)
	{
		System.out.println(msg);
	}
	
	public static void debug(String msg)
	{
		System.out.println(msg);
	}
	
	public static void error(String msg)
	{
		System.err.println(msg);
	}
	
	public static void fail(String msg)
	{
		System.err.println(msg);
		System.exit(-1);
	}
}
