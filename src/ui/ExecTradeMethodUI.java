package ui;

import javax.swing.*;

public class ExecTradeMethodUI implements ITabComponent{
	private final String title = "手法";

	@Override
	public String getTabName() {
		return title;
	}

	@Override
	public JPanel createPanel() {
		return new JPanel();
	}

	@Override
	public void updateComponent() {

	}
}
