package mc8max.tool.jsc;

import mc8max.tool.jsc.analyzer.StackAnalyzer;

public class App 
{	
    public static void main(String[] args) 
    {
    	if (args.length < 1)
		{
			Logger.fail("Missing input file.");
		}
    	
    	StackAnalyzer analyzer = new StackAnalyzer(args[0]);
    	analyzer.compute();
    }
}
