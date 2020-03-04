package mc8max.tool.jsc.analyzer;

import java.io.IOException;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import mc8max.tool.jsc.Logger;

public class StackAnalyzer 
{
	private String fileName;
	private JavaClass javaClass;
	
	public StackAnalyzer(String fileName)
	{
		this.fileName = fileName;
	}
	
	private void init()
	{
		ClassParser parser = new ClassParser(fileName);
		try 
		{
			javaClass = parser.parse();
		} 
		catch (ClassFormatException | IOException e) 
		{
			Logger.error(e.getMessage());
			Logger.fail("Unable to parse input .class file.");
		}
	}

	public void compute()
	{
		init();
		for (Method method : javaClass.getMethods())
		{
			MethodAnalyzer mAnalyzer = new MethodAnalyzer(javaClass, method);
			mAnalyzer.compute();
			mAnalyzer.display();
		}
	}
}
