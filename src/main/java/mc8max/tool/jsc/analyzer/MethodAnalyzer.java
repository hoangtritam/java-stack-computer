package mc8max.tool.jsc.analyzer;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;

import mc8max.tool.jsc.Logger;

public class MethodAnalyzer 
{
	private JavaClass clazz;
	private Method method;
	private InstructionList iList;
	
	public MethodAnalyzer(JavaClass clazz, Method method)
	{
		this.clazz = clazz;
		this.method = method;
		ConstantPoolGen cpg = new ConstantPoolGen(clazz.getConstantPool());
		MethodGen mg = new MethodGen(method, clazz.getClassName(), cpg);
		iList = mg.getInstructionList();
	}
	
	public void compute()
	{
		
	}
	
	public void display()
	{
		// Current state for debugging
		System.out.println("Method: " + method.getName());
		
		Iterator<InstructionHandle> iterator = iList.iterator();
		while(iterator.hasNext())
		{
			InstructionHandle handle = iterator.next();
			System.out.println(handle.toString());
		}
		
		System.out.println();
		System.out.println();
	}
	
	private Block[] cutBlocks(int[] marks)
	{
		Block[] blocks = new Block[marks.length - 1]; // remove the last mark
		for (int i = 0; i < marks.length - 1; i++)
		{
			blocks[i] = new Block(marks[i], marks[i + 1]);
		}
		return blocks;
	}
	
	private int[] markBlocks()
	{
		InstructionHandle[] handles = iList.getInstructionHandles();
		
		Set<Integer> marks = new TreeSet<>();
		// first instruction is always a new block
		marks.add(0);
		
		// add OutOfBound index for easier cutting
		marks.add(iList.getLength());
		
		for (int i = 0; i < handles.length; i++)
		{
			InstructionHandle handle = handles[i];
			Instruction ins = handle.getInstruction();
			if (ins instanceof RET || ins instanceof ReturnInstruction || ins instanceof ATHROW)
			{
				// the next instruction will start a new block
				marks.add(i + 1);
			}
			if (ins instanceof BranchInstruction)
			{
				// the next instruction will start a new block
				marks.add(i + 1);
				
				// jump targets will start a new block
				// default target includes: target of Goto, If, JSR, default of switch / lookup
				int defaultTarget = searchTarget(handles, i, getOffset(handle, ((BranchInstruction) ins).getTarget()));
				marks.add(defaultTarget);
				
				// add other targets of switch / lookup instructions
				if (ins instanceof Select)
				{
					for (InstructionHandle targetHandle : ((Select) ins).getTargets())
					{
						int target = searchTarget(handles, i, getOffset(handle, targetHandle));
						marks.add(target);
					}
				}
			}
		}
		return toArray(marks);
	}
	
	private static int getOffset(InstructionHandle origin, InstructionHandle target)
	{
		return origin.getPosition() - target.getPosition();
	}
	
	private int searchTarget(InstructionHandle[] handles, final int index, int offset)
	{
		int current = index;
		if (offset > 0)
		{
			while(offset > 0)
			{
				current++;
				offset -= handles[current].getInstruction().getLength();
			}
			if (current >= handles.length || offset < 0)
			{
				Logger.fail("Unable to search target of instruction " + index + " in method " + method.getName());
			}
			return current;
		}
		else if (offset < 0)
		{
			while (offset < 0)
			{
				current--;
				offset += handles[current].getInstruction().getLength();
			}
			if (current < 0 || offset > 0)
			{
				Logger.fail("Unable to search target of instruction " + index + " in method " + method.getName());
			}
		}
		return current;
	}
	
	private static int[] toArray(Set<Integer> set)
	{
		int [] ar = new int[set.size()];
		int i = 0; 
		for (int integer : set)
		{
			ar[i++] = integer;
		}
		return ar;
	}
}
