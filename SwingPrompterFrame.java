import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class SwingPrompterFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	private List<JLabel> infoLabel = new ArrayList<JLabel>();
	private JLabel responseLabel = new JLabel();

	public SwingPrompterFrame(String s)
	{
		super (s);
		putResponseLabel(new JLabel());
	}

	public JLabel getResponseLabel()
	{
		return responseLabel;
	}

	public void putResponseLabel(JLabel responseLabel)
	{
		if (responseLabel != null)
		{	
			this.responseLabel = responseLabel;		
			this.getContentPane().add(responseLabel);
		}
	}

	public List<JLabel> getInfoLabels()
	{
		List<JLabel> infoLabel = new ArrayList<JLabel>();
		infoLabel.addAll (this.infoLabel);
		return infoLabel;
	}

	public void putInfoLabels(List<JLabel> infoLabel)
	{
		if (infoLabel != null)
		{
			this.infoLabel = new ArrayList<JLabel>();
			this.infoLabel.addAll(infoLabel);
			for (JLabel label : infoLabel)
			{	this.getContentPane().add(label);	}
		}
	}
}