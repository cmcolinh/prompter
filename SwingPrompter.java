import jline.ConsoleReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.io.*;
import javax.accessibility.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;

public class SwingPrompter extends Prompter
{
	private ExecutorService executorService1;
	private Future<String> wrappedFuture;
	private String regex;
	private JFrame f;
	private JLabel l;
	private JPanel contentPane;
	private boolean started;
	private boolean canceled;
	private boolean completed;
	private Callable<String> call;

	private SwingPrompter(String[] prompt, String r)
	{
		regex = r;
		executorService1 = Executors.newSingleThreadExecutor();


		wrappedFuture = executorService1.submit(		(new Callable<String>()
		{
			public boolean justHitBackspace = false;
			public String minimumStart = "";
			public String returnString = "";
			boolean done;

			public String call()
			{
				call = this;

				done = false;
				started = true;

				f = new JFrame(prompt.length > 0 ? prompt[0] : "");

				contentPane = (JPanel) f.getContentPane();
				contentPane.setLayout(null);
				f.setVisible(true);

				f.setSize(640, 480);

				if (prompt.length > 1)
				{
					l = new JLabel(prompt[1], SwingConstants.CENTER);
					l.setBackground(Color.MAGENTA);
					//l.setSize(640, 100);
					//l.setText("abcd\n\nVocabulary Reading\n");
					l.setOpaque(true);
					l.setFont(new Font("Serif", Font.PLAIN, 48));
					l.setBounds(0,0,640,100);
					contentPane.add(l);
				}
				if (prompt.length > 2)
				{
					l = new JLabel(prompt[2], SwingConstants.CENTER);
					l.setBackground(Color.MAGENTA);
					//l.setSize(640, 100);
					//l.setText("abcd\n\nVocabulary Reading\n");
					l.setOpaque(true);
					l.setFont(new Font("Serif", Font.PLAIN, 48));
					l.setBounds(0,150,640,100);
					contentPane.add(l);
				}
				l = new JLabel("", SwingConstants.CENTER);
				//bg.setSize(500,100);
				l.setBackground(Color.GREEN);
				//bg.setText((new HiraganaKI().toString()+new HiraganaMA().toString()+new HiraganaGU().toString()+new HiraganaRE().toString()));
				l.setOpaque (true);
				l.setFont(new Font("Serif", Font.PLAIN, 48));
				l.setBounds(0,300,640,100);
				for (char ch : new char[]{'!', '\"', '#', '$', '&', '\'', '(', ')',
							'*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4',
							'5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
							'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
							'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
							'V', 'W', 'X', 'Y', 'Z', '[', '\\',']', '^', '_', '`',
							'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
							'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
							'w', 'x', 'y', 'z', '{', '|', '}', '~', ' '})
				{	l.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ch), "process key");	}
				l.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "process key");
				l.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "process key");
				l.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "process key");



				l.getActionMap().put("process key", new AbstractAction("a"){
					public void actionPerformed(ActionEvent e)
					{
						Object source = e.getSource();

						Thread t = new Thread(new Runnable() {
							@Override
							public void run()  {
								if (source instanceof JLabel)
								{
									try
									{
										if (e.getActionCommand().equals("\n"))
										{	processInput("" + ConsoleKeyPressPrompter.ENTER);	}
										else if ((((int)e.getActionCommand().charAt(0)) == 8) || (((int)e.getActionCommand().charAt(0)) == 127))
										{	processInput("" + ConsoleKeyPressPrompter.BACKSPACE);	}
										else
										{	processInput(e.getActionCommand());	}
									}
									catch (InterruptedException e)
									{}
								}
							}
						});
						t.start();
					}
				});

				contentPane.add(l);
				contentPane.repaint();

				while (nextLegalChars(regex, minimumStart).size() == 1)
				{	minimumStart += nextLegalChars(regex, minimumStart).get(0);	}

				if (! (minimumStart.equals("")))
				{
					for (int index = 0; index < minimumStart.length(); index++)
					{	putChar("" + minimumStart.charAt(index));		}
				}


				while (! done)
				{
					if (canceled)
					{	return null;	}
				}

				System.out.println(done);

				return returnString;
			}

			public void processInput(String input) throws InterruptedException
			{
					boolean doneProcessing = false;
					boolean inputProcessed = false;

					if (canceled)
					{	throw new InterruptedException();	}

					java.util.List<String> startChars = nextLegalChars(regex, returnString);

					java.util.List<String> nextCharList = nextLegalChars(
						regex, returnString);

					if (nextCharList.contains(input))
					{
						while (! doneProcessing)
						{
							nextCharList = nextLegalChars(
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
								if (inputProcessed)
								{	doneProcessing = true;	}
								else if ((input.equals("" +
									ConsoleKeyPressPrompter.ENTER)) ||
									(input.equals("" +
									ConsoleKeyPressPrompter.RETURN)))
								{
									//putChar("" +
									//	ConsoleKeyPressPrompter.ENTER);
									done = true;
									doneProcessing = true;
									inputProcessed = true;
								}
								else
								{
									putChar(input);
									inputProcessed = true;
								}
							}
						}
					}

				canceled = false;
			}


			public void putChar(String charToPut)
			{
				if (charToPut.equals("" + ConsoleKeyPressPrompter.BACKSPACE))
				{
					returnString=returnString.substring(
						0, returnString.length()-1);

					l.setText(returnString);

					justHitBackspace = true;
				}
				else if ((charToPut.equals(""+ConsoleKeyPressPrompter.ENTER))|
					(charToPut.equals(""+ConsoleKeyPressPrompter.RETURN)))
				{		}
				else
				{
					returnString += charToPut;
					l.setText(returnString);

					justHitBackspace = false;
				}
			}

			public java.util.List<String> nextLegalChars(String regex,
											   String responseSoFar)
			{
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(responseSoFar);

				java.util.List<String> legalChars = new java.util.ArrayList<String>();
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
		}));
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
	{	return new SwingPrompter(prompt.split("\n"), regex);	}

	public static void main (String[] args)
	{
		boolean done = false;
		//String s = "0|([1-9]\\d{0,1}0)|([1-9]\\d{0,2}(,\\d\\d\\d){0,2}(,\\d\\d0))";
		String s = "Sally|Sarah|Salacious\\sCrumb";
		//String s = "pass|(deploy\\s(((Endor|Yavin\\s4) from hand to table)|(asteroids from hand to (Tatooine|Coruscant))))|play from hand (Bith Shuffle|The Signal)|search Reserve Deck using game text of Hunt Down and Destroy the Jedi";
		//String s = "pass";
		//String s = "Sal|Sally";
		//String s = "(a){0,20}";
		//String s = "Jack\\*bot|Yahoo|Kkkkkkkb";
		java.util.List<String> l = new java.util.ArrayList<String>(); l.add(""+ConsoleKeyPressPrompter.ENTER);
		//Prompter a = ConsoleKeyPressPrompter.prompt("Press Something", l);
		//System.out.println("press enter");
		//while (! a.isDone())
		//{}
		Prompter p = SwingPrompter.prompt("Enter Score\nDo it now!\nVocabulary Reading", s);
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
