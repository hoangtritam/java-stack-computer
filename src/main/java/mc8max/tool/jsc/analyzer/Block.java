package mc8max.tool.jsc.analyzer;

import java.util.Set;
import java.util.TreeSet;

public class Block implements Comparable<Block>
{
	private final int start;
	private final int end; // inclusive
	Set<Block> origins = new TreeSet<>();
	Set<Block> targets = new TreeSet<>();;
	private boolean isExceptionHandler;
	
	public Block(int start, int end)
	{
		this.start = start;
		this.end = end;
		this.isExceptionHandler = false;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public boolean isExceptionHandler()
	{
		return isExceptionHandler;
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
	
	public Block[] getOrigins()
	{
		return origins.toArray(new Block[origins.size()]);
	}
	
	public Block[] getTargets()
	{
		return targets.toArray(new Block[targets.size()]);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Block: [").append(start).append(", ").append(end).append("]");
		if (isExceptionHandler)
		{
			sb.append(" exception handler");
		}
		return sb.toString();
	}
	
	void addOrigin(Block origin)
	{
		if (!origins.contains(origin))
		{
			origins.add(origin);
		}
	}
	
	void addTarget(Block target)
	{
		if (!targets.contains(target))
		{
			targets.add(target);
		}
	}
	
	void setExceptionHandler(boolean isExceptionHandler)
	{
		this.isExceptionHandler = isExceptionHandler;
	}

	@Override
	public int compareTo(Block o) {
		return this.start - o.start;
	}
}
