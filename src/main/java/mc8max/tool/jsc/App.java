package mc8max.tool.jsc;

import java.io.File;

import mc8max.tool.jsc.analyzer.StackAnalyzer;

public class App 
{	
    public static void main(String[] args) 
    {
    	if (args.length < 1)
		{
			Logger.fail("Missing input file.");
		}
    	
    	File file = new File(args[0]);
    	StackAnalyzer analyzer = new StackAnalyzer(file);
    	analyzer.compute();
    	analyzer.display();
    }
}
