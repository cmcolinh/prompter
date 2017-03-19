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
	private SwingPrompterFrame f;
	private JLabel l;
	private JPanel contentPane;
	private boolean started;
	private boolean canceled;
	private boolean completed;
	private Callable<String> call;

	private SwingPrompter(String[] prompt, String r, SwingPrompterFrame spFrame)
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

				if (spFrame == null)
				{
					f = new SwingPrompterFrame(prompt.length > 0 ? prompt[0] : "");
					contentPane = (JPanel) f.getContentPane();
					contentPane.setLayout(null);
					java.util.List<JLabel> promptLabel = new ArrayList<JLabel>();

					int x = 0;
					int y = 0;
					int WIDTH = 640;
					int HEIGHT = 100;
					int FONT_SIZE = 48;

					for (int index = 1; index < prompt.length; index++)
					{
						l = new JLabel(prompt[index].split("\t")[0], SwingConstants.CENTER);
						try
						{
							l.setBackground(prompt[index].split("\t").length > 1 ?
								((Color)(Class.forName("java.awt.Color").getDeclaredField(
								prompt[index].split("\t")[1].toLowerCase())).get(null)) : Color.WHITE);
						}
						catch(Exception e){}
						l.setOpaque(true);
						l.setFont(new Font("Serif", Font.PLAIN, FONT_SIZE));
						l.setBounds(x,y,WIDTH,HEIGHT);
						contentPane.add(l);
						promptLabel.add(l);
						y = y + HEIGHT;
					}
					f.putInfoLabels(promptLabel);

					l = new JLabel("", SwingConstants.CENTER);
					l.setBackground(Color.WHITE);
					l.setOpaque (true);
					l.setFont(new Font("Serif", Font.PLAIN, 48));
					l.setBounds(x,y,WIDTH,HEIGHT);
					y = y + HEIGHT;
					f.putResponseLabel(l);

					f.setVisible(true);

					f.setSize(WIDTH, y + 50);
				}
				else
				{
					f = spFrame;
					contentPane = ((JPanel)f.getContentPane());

					//retitle the PrompterFrame
					if (prompt.length > 0)
					{	f.setTitle(prompt[0]);		}

					Iterator<JLabel> i = f.getInfoLabels().iterator();
					//iterate through the arguments and the info labels
					for (int index = 1; index < prompt.length; index++)
					{
						if (i.hasNext())
						{
							JLabel next = i.next();
							if (next != null)
							{
								next.setText(prompt[index].split("\t")[0]);
								if (prompt[index].split("\t").length > 1)
								{
									try
									{
										l.setBackground(prompt[index].split("\t").length > 1 ?
											((Color)(Class.forName("java.awt.Color").getDeclaredField(
											prompt[index].split("\t")[1].toLowerCase())).get(null)) : Color.WHITE);
									}
									catch(Exception e){}
								}
							}
						}
					}
				}

				f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				f.addWindowListener(new WindowAdapter(){
					public void windowClosing(WindowEvent e)
					{
						JFrame frame = (JFrame) e.getSource();

						cancel(true);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					}
				});

				l = f.getResponseLabel();

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


					System.out.print("");
				}

				l.getActionMap().remove("process key");

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
					returnString = l.getText();

					justHitBackspace = true;
				}
				else if ((charToPut.equals(""+ConsoleKeyPressPrompter.ENTER))|
					(charToPut.equals(""+ConsoleKeyPressPrompter.RETURN)))
				{	}
				else
				{
					returnString += charToPut;
					l.setText(returnString);
					returnString = l.getText();

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
				{
					canceled = true;
					if (l != null)	{l.getActionMap().remove("process key");}
				}
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
	{	return new SwingPrompter(prompt.split("\n"), regex, null);	}

	public static SwingPrompter prompt(String prompt, String regex, SwingPrompterFrame frame)
	{	return new SwingPrompter(prompt.split("\n"), regex, frame);	}

	public SwingPrompterFrame getFrame()
	{	return f;	}

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
		Prompter p = SwingPrompter.prompt("Enter Score\nDo it now!\tyellow\nVocabulary Reading\tyellow\nPlease\tyellow", s);
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

		SwingPrompterFrame f = ((SwingPrompter)p).getFrame();
		JLabel pan = new JLabel("", SwingConstants.CENTER){
			public void setText(String t)
			{
				t = t.replace("ta", "" + ((char)12383)).replace("ti", "" + (char)12385);
				super.setText(t);
			}
		};

		pan.setBackground(Color.green);
		pan.setOpaque (true);
		pan.setFont(new Font("Serif", Font.PLAIN, 48));
		pan.setBounds(0,400,640,100);

		pan.setVisible(true);

		f.setSize(640,550);
		//f.getResponseLabel().setVisible(false);
		f.putResponseLabel(pan);
		f.getContentPane().add(pan);

		p = SwingPrompter.prompt("Enter Score\nDo it now!\tyellow\nVocabulary Reading\tyellow\nIf you would\tyellow", "(" + ((char)12383) + "|" + ((char)12385) + "){0,20}(ta|ti|)", f);
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
