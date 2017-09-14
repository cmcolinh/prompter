import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class CommandLinePrompter extends Prompter
{
	private ExecutorService executorService1;
	private Future<String> wrappedFuture;
	private String regex;
	private boolean started;
	private boolean canceled;
	private boolean completed;

	private CommandLinePrompter(String r)
	{
		regex = r;
		executorService1 = Executors.newSingleThreadExecutor();

		wrappedFuture = executorService1.submit(new Callable<String>(){

			public boolean justHitBackspace = false;
			public String minimumStart = "";
			public String returnString = "";


			public String call()
			{
				boolean done = false;
				started = true;

				try{
					if (canceled)
					{	throw new InterruptedException();	}

					while (nextLegalChars(regex, minimumStart).size() == 1)
					{	minimumStart += nextLegalChars(regex, minimumStart).get(0);	}

					done = false;
					while (! done){
						if (canceled)
						{	throw new InterruptedException();	}

						List<String> nextCharList = nextLegalChars(
							regex, returnString);

						if ((nextCharList.size() == 1) ||
							((nextCharList.size() == 2) &&
							(nextCharList.contains("" +
							ConsoleKeyPressPrompter.BACKSPACE))))
						{
							if (justHitBackspace)
							{
								if (!(returnString.equals(minimumStart)))
								{
									putChar("" +
										ConsoleKeyPressPrompter.BACKSPACE);
								}
								else
								{	justHitBackspace = false;		}
							}
							else
							{
								putChar(nextCharList.get(0).equals(
									"" + ConsoleKeyPressPrompter.BACKSPACE) ?
									"" + nextCharList.get(1) :
									"" + nextCharList.get(0));
							}
						}
						else
						{
							Prompter c =
							  ConsoleKeyPressPrompter.prompt("",nextCharList);
							while (! c.isDone())
							{
								if (canceled)
								{
									c.cancel(true);
									throw new InterruptedException();
								}
							}
							if ((c.get().equals("" +
								ConsoleKeyPressPrompter.ENTER)) ||
								(c.get().equals("" +
								ConsoleKeyPressPrompter.RETURN)))
							{
								putChar("" +
									ConsoleKeyPressPrompter.ENTER);
								done = true;
							}
							else
							{	putChar(c.get());	}
						}

					}
				}
				catch (InterruptedException e)
				{	return null;					}
				catch (Exception e)
				{	throw new RuntimeException();	}
				canceled = false;
				return returnString;
			}
			public void putChar(String charToPut)
			{
				if (charToPut.equals("" + ConsoleKeyPressPrompter.BACKSPACE))
				{
					System.out.print(ConsoleKeyPressPrompter.BACKSPACE);
					System.out.print(" ");
					System.out.print(ConsoleKeyPressPrompter.BACKSPACE);

					returnString=returnString.substring(
						0, returnString.length()-1);

					justHitBackspace = true;
				}
				else if ((charToPut.equals(""+ConsoleKeyPressPrompter.ENTER))|
					(charToPut.equals(""+ConsoleKeyPressPrompter.RETURN)))
				{	System.out.print("\n");		}
				else
				{
					System.out.print(charToPut);
					returnString += charToPut;

					justHitBackspace = false;
				}
			}

			public List<String> nextLegalChars(String regex,
											   String responseSoFar)
			{
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(responseSoFar);

				List<String> legalChars = new ArrayList<String>();
				if (! (responseSoFar.equals(minimumStart)))
				{	legalChars.add("" + ConsoleKeyPressPrompter.BACKSPACE);	}

				char[] chars = {'!', '\"', '#', '$', '&', '\'', '(', ')',
					'*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4',
					'5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
					'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
					'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
					'V', 'W', 'X', 'Y', 'Z', '[', '\\',']', '^', '_', '`',
					'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
					'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
					'w', 'x', 'y', 'z', '{', '|', '}', '~', ' '};

				if (m.matches())
				{
					legalChars.add("" + ConsoleKeyPressPrompter.ENTER);
					legalChars.add("" + ConsoleKeyPressPrompter.RETURN);
				}
				for (int i = 0; i < chars.length; i++)
				{
					m = p.matcher(responseSoFar + chars[i]);
					if (m.matches() || m.hitEnd())
					{	//System.out.println(chars[i]);
						legalChars.add("" + chars[i]);	}
				}
				return legalChars;
			}
		});
		executorService1.shutdown();
	}

	public boolean cancel(boolean mayInterruptIfRunning)
	{
		try
		{
			if ((! started) || mayInterruptIfRunning);
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
		if (canceled)
		{	throw new InterruptedException();	}
		return wrappedFuture.get(timeout, unit);
	}
	public boolean isCancelled()
	{	return wrappedFuture.isCancelled();				}
	public boolean isDone()
	{	return wrappedFuture.isDone();					}

	public static Prompter prompt(String prompt, String regex)
	{	return new CommandLinePrompter(regex);			}

	public static void main (String[] args)
	{
		//String s = "0|([1-9]\\d{0,1}0)|([1-9]\\d{0,2}(,\\d\\d\\d){0,2}(,\\d\\d0))";
		String s = "Sally|Sarah|Salacious Crumb";
		//String s = "pass|(deploy\\s(((Endor|Yavin\\s4) from hand to table)|(asteroids from hand to (Tatooine|Coruscant))))|play from hand (Bith Shuffle|The Signal)|search Reserve Deck using game text of Hunt Down and Destroy the Jedi";
		//String s = "pass";
		//String s = "Sal|Sally";
		//String s = "Jack\\*bot|Yahoo|Kkkkkkkb";
		List<String> l = new ArrayList<String>(); l.add(""+ConsoleKeyPressPrompter.ENTER);
		Prompter a = ConsoleKeyPressPrompter.prompt("Press Something", l);
		System.out.println("press enter");
		while (! a.isDone())
		{}
		Prompter p = CommandLinePrompter.prompt("enter score", s);
		//String r = "";
		System.out.println("Your move");
		while (! p.isDone())
		{}
		try
		{
			System.out.println(p.get());
		}
		catch (InterruptedException e)
		{	System.out.println("interrupted!");	}
		catch (Exception e){e.printStackTrace();}
	}
}
