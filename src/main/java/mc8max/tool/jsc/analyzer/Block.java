package mc8max.tool.jsc.analyzer;

import java.util.List;

public class Block 
{
	private final int start;
	private final int end; // inclusive
	private List<Block> origins;
	private List<Block> targets;
	
	public Block(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof Block)
		{
			Block other = (Block) o;
			return this.start == other.start && this.end == other.end;
		}
		return false;
	}
	
	public int hashCode()
	{
		return start;
	}
}
