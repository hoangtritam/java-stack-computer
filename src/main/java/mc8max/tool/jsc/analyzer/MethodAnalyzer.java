package mc8max.tool.jsc.analyzer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IfInstruction;
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
	private InstructionHandle[] handles;
	private Map<Integer, Integer> positionMap;
	private Block[] blocks;
	
	public MethodAnalyzer(JavaClass clazz, Method method)
	{
		this.clazz = clazz;
		this.method = method;
	}
	
	private void init()
	{
		ConstantPoolGen cpg = new ConstantPoolGen(clazz.getConstantPool());
		MethodGen mg = new MethodGen(method, clazz.getClassName(), cpg);
		InstructionList iList = mg.getInstructionList();
		handles = iList.getInstructionHandles();
		positionMap = new TreeMap<Integer, Integer>();
		for (int i = 0, position = 0; i < handles.length; i++)
		{
			positionMap.put(position, i);
			position += handles[i].getInstruction().getLength();
		}
	}
	
	public void compute()
	{
		init();
		blocks = cutBlocks(markBlocks());
		connectBlocks();
	}
	
	public void display()
	{
		// Current state for debugging
		System.out.println("Method: " + method.getName());
		for (Block block : blocks)
		{
			System.out.println(block.toString());
			Block[] origins = block.getOrigins();
			if (origins.length > 0)
			{
				System.out.print("- origins: ");
				for (Block originBlock : origins)
				{
					System.out.print(originBlock.getStart());
					System.out.print("(");
					System.out.print(handles[originBlock.getStart()].getPosition());
					System.out.println(") ");
				}
			}
			
			System.out.println(" --- instruction");
			for (int i = block.getStart(); i <= block.getEnd(); i++)
			{
				System.out.println(handles[i].toString());
			}
			System.out.println(" ---");
			
			Block[] targets = block.getTargets();
			if (targets.length > 0)
			{
				
				System.out.print(" - targets: ");
				for (Block targetBlock : targets)
				{
					System.out.print(targetBlock.getStart());
					System.out.print("(");
					System.out.print(handles[targetBlock.getStart()].getPosition());
					System.out.println(") ");
				}
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}
	
	private Block[] cutBlocks(int[] marks)
	{
		Set<Integer> exceptionIndices = new HashSet<>();
		for (CodeException exception : method.getCode().getExceptionTable())
		{
			int handlerIndex = searchTarget(exception.getHandlerPC()); 
			exceptionIndices.add(handlerIndex);
		}
		
		Block[] blocks = new Block[marks.length - 1]; // remove the last mark
		for (int i = 0; i < marks.length - 1; i++)
		{
			final int start = marks[i];
			final int end = marks[i + 1] - 1;
			
			blocks[i] = new Block(start, end);
			if (exceptionIndices.contains(start))
			{
				blocks[i].setExceptionHandler(true);
			}
		}
		return blocks;
	}
	
	private void connectBlocks()
	{
		for (int i = 0; i < blocks.length; i++)
		{
			InstructionHandle handle = handles[blocks[i].getEnd()];
			Instruction ins = handle.getInstruction();
			if (ins instanceof BranchInstruction)
			{
				BranchInstruction branchIns = (BranchInstruction) ins;
				int defaultTarget = searchTarget(branchIns.getTarget().getPosition());
				Block block = searchBlock(blocks, defaultTarget);
				connect(blocks[i], block);
				
				if (ins instanceof Select)
				{
					for (InstructionHandle targetHandle : ((Select) ins).getTargets())
					{
						int target = searchTarget(targetHandle.getPosition());
						Block targetBlock = searchBlock(blocks, target);
						connect(blocks[i], targetBlock);
					}
				}
				else if (ins instanceof IfInstruction)
				{
					int target = blocks[i].getEnd() + 1;
					Block targetBlock = searchBlock(blocks, target);
					connect(blocks[i], targetBlock);
				}
			}
		}
	}
	
	private int[] markBlocks()
	{		
		Set<Integer> marks = new TreeSet<>();
		// first instruction is always a new block
		marks.add(0);
		
		// add OutOfBound index for easier cutting
		marks.add(handles.length);
		
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
				BranchInstruction branchIns = (BranchInstruction) ins;
				// the next instruction will start a new block
				marks.add(i + 1);
				
				// jump targets will start a new block
				// default target includes: target of Goto, If, JSR, default of switch / lookup
				int defaultTarget = searchTarget(branchIns.getTarget().getPosition());
				marks.add(defaultTarget);
				
				// add other targets of switch / lookup instructions
				if (ins instanceof Select)
				{
					for (InstructionHandle targetHandle : ((Select) ins).getTargets())
					{
						int target = searchTarget(targetHandle.getPosition());
						marks.add(target);
					}
				}
			}
		}

		// each handler is a new block
		for (CodeException exception : method.getCode().getExceptionTable())
		{
			int handlerIndex = searchTarget(exception.getHandlerPC()); 
			marks.add(handlerIndex);
		}
		return toArray(marks);
	}
	
	private int searchTarget(final int position)
	{
		if (positionMap.containsKey(position))
		{
			return positionMap.get(position);
		}
		Logger.fail("Unable to find instruction at PC " + position + " in method " + method.getName());
		return -1;
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
	
	private void connect(Block origin, Block target)
	{
		origin.addTarget(target);
		target.addOrigin(origin);
	}
	
	private Block searchBlock(Block[] blocks, int target)
	{
		for (Block block : blocks)
		{
			if (block.getStart() == target)
			{
				return block;
			}
		}
		return null;
	}
}
