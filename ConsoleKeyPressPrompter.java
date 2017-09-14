import jline.console.ConsoleReader;
import java.util.*;
import java.util.concurrent.*;
//import java.io.*;

public class ConsoleKeyPressPrompter extends Prompter/*<Character>*/
{
	public static final char CTRL_A = (char) 1;
	public static final char CTRL_B = (char) 2;
	public static final char LEFT_ARROW = (char) 2;
	public static final char CTRL_C = (char) 3;
	public static final char CTRL_D = (char) 4;
	public static final char CTRL_E = (char) 5;
	public static final char CTRL_F = (char) 6;
	public static final char RIGHT_ARROW = (char) 6;
	public static final char CTRL_G = (char) 7;
	public static final char CTRL_H = (char) 8;
	public static final char BACKSPACE = (char) 8;
	public static final char CTRL_I = (char) 9;
	public static final char CTRL_J = (char) 10;
	public static final char RETURN = (char) 10;
	public static final char CTRL_K = (char) 11;
	public static final char CTRL_L = (char) 12;
	public static final char CTRL_M = (char) 13;
	public static final char ENTER  = (char) 13;
	public static final char CTRL_N = (char) 14;
	public static final char DOWN_ARROW = (char) 14;
	public static final char CTRL_O = (char) 15;
	public static final char CTRL_P = (char) 16;
	public static final char UP_ARROW = (char) 16;
	public static final char CTRL_Q = (char) 17;
	public static final char CTRL_R = (char) 18;
	public static final char CTRL_S = (char) 19;
	public static final char CTRL_T = (char) 20;
	public static final char CTRL_U = (char) 21;
	public static final char CTRL_V = (char) 22;
	public static final char CTRL_W = (char) 23;
	public static final char CTRL_X = (char) 24;
	public static final char CTRL_Y = (char) 25;
	public static final char CTRL_Z = (char) 26;
	public static final char ESCAPE = (char) 27;
	public static final char SPACE  = (char) 32;
	public static final char DELETE = (char) 127;

	private char[] legalChar = {CTRL_A, CTRL_B, CTRL_C, CTRL_D, CTRL_E,CTRL_F,
		CTRL_G, CTRL_H, CTRL_I, CTRL_J, CTRL_K, CTRL_L, CTRL_M,CTRL_N,CTRL_O,
		CTRL_P, CTRL_Q, CTRL_R, CTRL_S, CTRL_T, CTRL_U, CTRL_V,CTRL_W,CTRL_X,
		CTRL_Y, CTRL_Z, ESCAPE, SPACE, '!', '\"', '#', '$', '&', '\'', '(',
		')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D',
		'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\',']', '^', '_', '`',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|',
		'}', '~', DELETE};

	private ExecutorService executorService1;
	private Future<String>/*<Character>*/ wrappedFuture;
	private List<String>/*<Character>*/ validResponse;
	private ConsoleReader reader;
	private boolean started;
	private boolean canceled;
	private boolean completed;

	private ConsoleKeyPressPrompter(List<String> validResponse)
	{
		this.validResponse = new ArrayList<String>(validResponse);
		executorService1 = Executors.newSingleThreadExecutor();


		final List<String>/*<Character>*/ vr = this.validResponse;
		wrappedFuture = executorService1.submit(new Callable<String>(){
			public String/*Character*/ call()
			{
				boolean done = false;
				started = true;
				char typedChar = ' ';
				try
				{
					reader = new ConsoleReader();
					while(! done)
					{
						if (canceled)
						{	throw new InterruptedException();	}
						typedChar = (char) reader.readCharacter(legalChar);
						//System.out.println((int) typedChar);
						if (vr.contains("" + typedChar))
						{
							completed = true;
							break;
							//reader.clearScreen();
						}
					}
				}
				catch(InterruptedException e)
				{	return null;					}
				catch(Exception e)
				{	throw new RuntimeException();	}
				canceled = false;
				return "" + typedChar;/*return typedChar;*/
			}
		});
		executorService1.shutdown();
	}

	public boolean cancel(boolean mayInterruptIfRunning)
	{
		try
		{
			if ((! started) || mayInterruptIfRunning)
			{
				if (! completed)
				{	canceled = true;	}
			}
			return wrappedFuture.cancel(mayInterruptIfRunning);
		}
		catch(NullPointerException e){}
		return false;
	}
	public String get() throws InterruptedException, ExecutionException
	{
		if (canceled)
		{	throw new InterruptedException();	}
		return wrappedFuture.get();
	}
	public String get(long timeout, TimeUnit unit) throws
		InterruptedException, ExecutionException, TimeoutException
	{
		if(canceled)
		{	throw new InterruptedException();	}
		return wrappedFuture.get(timeout, unit);
	}
	public boolean isCancelled()
	{	return wrappedFuture.isCancelled();				}
	public boolean isDone()
	{	return wrappedFuture.isDone();					}

	public static Prompter prompt (String prompt, List<String> options)
	{
		String next;
		List<String> charList = new ArrayList<String>();

		Iterator<String> i = options.iterator();
		while(i.hasNext())
		{
			next=i.next();
			if (next.length() == 1)
			{	charList.add(next);	}
		}

		return new ConsoleKeyPressPrompter(charList);
	}


	public static void main (String[] args)
	{
		List<String> s = new ArrayList<String>(); s.add("" + CTRL_H); s.add("" + CTRL_R); s.add("1"); s.add("2"); s.add("3"); s.add("4"); s.add("5");
		Prompter p = ConsoleKeyPressPrompter.prompt("what's up?", s);
		//String r = "";
		System.out.println("Choose a race\n1: Human\n2: Dwarf\n3: Elf\n4: Halfling\n5: Gnome");
		while (! p.isDone())
		{	}
		try
		{
			System.out.println(p.get());
			String a = "aaaaa";
			a = a + ((char)8);
			System.out.println(a.length());
		}
		catch (InterruptedException e)
		{	System.out.println("interrupted!");	}
		catch (Exception e){}
	}
}