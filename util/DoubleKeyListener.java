/*
 * from the RBSE Nova Search (Polaris) Plugin
 */
package util;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Allow a user to type in a number, and only a number
 * 
 */
public class DoubleKeyListener extends KeyAdapter
{
	private TextField m_textField;
	
	/**
	 * Create a new key handler
	 * @param textField Textfield to watch keystrokes of
	 */
	public DoubleKeyListener ( TextField textField )
	{
		m_textField = textField;
	}
	
	/**
	 * Key has been pressed
	 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed ( KeyEvent e )
	{
//		System.out.println("DoubleKeyListener heard something!");
		
		// Allow this character if it makes
		// a parseable number
		try
		{
			String number = m_textField.getText();
			
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
			
//			System.out.println("number="+number);
			
			// Allow negatives
/*			if ( number.length() == 1 && number.charAt ( 0 ) == '-' )
			{
				return;
			}
 */			
			// DIS-Allow negatives
			if (e.getKeyChar() == '-' || e.getKeyChar() == 'd' || e.getKeyChar() == 'f' ||
										 e.getKeyChar() == 'D' || e.getKeyChar() == 'F') {
				throw new NumberFormatException();
			}
 
			// Add the new character to the current text
			if ( m_textField.getCaretPosition() == number.length () )
			{
				number = number + e.getKeyChar ();
			}
			else
			{
				// Insert the character at the cursor location
				number = number.substring ( 0, m_textField.getCaretPosition() ) +
							e.getKeyChar () +
							number.substring ( m_textField.getCaretPosition() );
			}
			
			// If the number is valid, then when we try to 
			// instantiate a Double, it won't throw an exception
			// However, the number is invalid, it will throw an
			// exception which can be caught and handled
			new Double ( number );
		}
		catch ( NumberFormatException nfe )
		{
			// The number was invalid
			// Don't keep the keystoke
			e.consume ();
		}
		
	}
}
