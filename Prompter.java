import java.util.concurrent.*;
import java.util.*;

public abstract class Prompter/*<E>*/ implements Future<String>
{
	public abstract boolean cancel(boolean mayInterruptIfRunning);

	public abstract String/*E*/ get() throws InterruptedException, ExecutionException;

	public abstract String/*E*/ get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

	public abstract boolean isCancelled();

	public abstract boolean isDone();

	public static Prompter/*<?>*/ prompt(String prompt, List<String> options) throws UnsupportedOperationException, IllegalArgumentException
	{	throw new UnsupportedOperationException();		}

	public static Prompter/*<?>*/ prompt(String prompt, String regex) throws UnsupportedOperationException, IllegalArgumentException
	{	throw new UnsupportedOperationException();		}
}