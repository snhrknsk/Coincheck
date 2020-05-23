package ui;

import javax.swing.*;

public interface ITabComponent {

	public String getTabName();
	public JPanel createPanel();
	public void updateComponent();
}
