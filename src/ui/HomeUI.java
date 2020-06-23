package ui;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.exec.ITradeLogic;
import trade.exec.TaskWorker;
import trade.manager.CoinManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeUI extends JFrame implements ActionListener {

	private enum Action{
		startStop,update
	}

	private final long INTERVAL = 60000;
	private boolean isStarted = false;
	private JButton startButton = null;
	private JLabel currentPrice = null;
	private Timer timer = new Timer();

	private final TaskWorker taskWorker;
	private List<ITabComponent> tabInstanceList = new ArrayList<>();

	public HomeUI(){
		super();
		initialize();
		taskWorker = new TaskWorker();
	}

	private void initialize(){
		setSize(1000,600);
		setTitle("勝手に取引マン");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		JSONObject current = new JSONObject(CoinCheckClient.getCurrentPrice());
		String price = current.getString(PARAM_KEY.rate.name());
		CoinManager.getInstance().setCurrentRate(price);
		currentPrice = new JLabel("現在価格 : " + price);
		panel.add(currentPrice);
		startUpdatePrice();

		startButton = new JButton("開始");
		startButton.addActionListener(this);
		startButton.setActionCommand(Action.startStop.name());
		panel.add(startButton);

		JButton updateButton = new JButton("更新");
		updateButton.addActionListener(this);
		updateButton.setActionCommand(Action.update.name());
		panel.add(updateButton);
		add("North", panel);

		//Contents tab, Insert contents to tabInstanceList.
		JTabbedPane tabPane = new JTabbedPane();
		tabInstanceList.add(new HistoryDispUI());
		tabInstanceList.add(new ExecTradeMethodUI());
		for (ITabComponent component: tabInstanceList) {
			tabPane.addTab(component.getTabName(), component.createPanel());
		}
		add("Center", tabPane);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals(Action.startStop.name())) {
			startButton.setText(isStarted ? "開始" : "終了");
			isStarted = !isStarted;
			startStopTrade(isStarted);
		} else if (command.equals(Action.update.name())) {
			currentPrice.setText("現在価格 : " + String.valueOf(CoinManager.getInstance().getCurrentRate()));
			for (ITabComponent component: tabInstanceList) {
				component.updateComponent();
			}
		}
	}

	private void startStopTrade(boolean isStarted){
		if (isStarted){
			taskWorker.startTask(INTERVAL);
		} else {
			taskWorker.stopAllTask();
		}
	}

	private void startUpdatePrice() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				currentPrice.setText("現在価格 : " + String.valueOf(CoinManager.getInstance().getCurrentRate()));
				for (ITabComponent component: tabInstanceList) {
					component.updateByConstantInterval();
				}
			}
		}, INTERVAL, INTERVAL);
	}

}
