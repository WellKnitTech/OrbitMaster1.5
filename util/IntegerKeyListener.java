/*
 * from the RBSE Nova Search (Polaris) Plugin
 */
package util;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Only allow integer values
 * 
 */
public class IntegerKeyListener extends KeyAdapter
{
	// Internal
	private TextField m_textField;
	
	/**
	 * Only allow integer values
	 * 
	 * @param textField the textfield to watch
	 */
	public IntegerKeyListener ( TextField textField )
	{
		m_textField = textField;
	}
	
	/**
	 * Key was pressed, handle it
	 * 
	 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed ( KeyEvent e )
	{
		
		// Keep characters that create a parseable integer
		String number = m_textField.getText () + e.getKeyChar();
		try
		{
			// Acceptable keys
			switch ( e.getKeyCode() )
			{
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
				{
					return;
				}
			}
			
			// Parseable, if this fails, then the number
			// is not an integer
			new Integer ( number );
		}
		catch ( NumberFormatException nfe )
		{
			// Don't keep this character
			e.consume ();
		}
		
	}
}
