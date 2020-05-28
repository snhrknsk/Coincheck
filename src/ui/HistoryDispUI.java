package ui;

import trade.manager.CoinManager;
import trade.manager.TradeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS;

public class HistoryDispUI implements ITabComponent{
	private final String tableName = "取引";
	private DefaultTableModel tradeHistoryTableModel = null;
	private DefaultTableModel priceHistoryTableModel = null;
	private DefaultTableModel currentOrderTableModel = null;

	@Override
	public String getTabName() {
		return tableName;
	}

	@Override
	public JPanel createPanel() {
		JPanel historyPanel = new JPanel();
		historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));

		//first line table(trade history and price history)
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//trade history panel
		JPanel tradeHistoryPanel = new JPanel();
		tradeHistoryPanel.setLayout(new BoxLayout(tradeHistoryPanel, BoxLayout.Y_AXIS));
		tradeHistoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel tradeHistory = new JLabel("取引履歴");
		tradeHistory.setAlignmentX(Component.CENTER_ALIGNMENT);
		tradeHistoryPanel.add(tradeHistory);
		String[] columnTradeHistoryNames = {"ID","取引ID", "日時", "売買", "レート", "量", "アルゴリズム"};
		tradeHistoryTableModel = new DefaultTableModel(columnTradeHistoryNames, 0) {
			@Override public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable tradeHistoryTable = new JTable(tradeHistoryTableModel);
		tradeHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPanel = new JScrollPane(tradeHistoryTable);
		tradeHistoryPanel.add(scrollPanel);
		panel.add(tradeHistoryPanel);
		//price history panel
		JPanel priceHistoryPanel = new JPanel();
		priceHistoryPanel.setLayout(new BoxLayout(priceHistoryPanel, BoxLayout.Y_AXIS));
		priceHistoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel priceHistory = new JLabel("価格履歴");
		priceHistory.setAlignmentX(Component.CENTER_ALIGNMENT);
		priceHistoryPanel.add(priceHistory);
		String[] columnPriceHistoryNames = {"日時", "レート"};
		priceHistoryTableModel = new DefaultTableModel(columnPriceHistoryNames, 0) {
			@Override public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable priceHistoryTable = new JTable(priceHistoryTableModel);
		JScrollPane scrollPanel2 = new JScrollPane(priceHistoryTable);
		priceHistoryPanel.add(scrollPanel2);
		panel.add(priceHistoryPanel);
		historyPanel.add(panel);

		//second line table (order table)
		JPanel panelSecond = new JPanel();
		panelSecond.setLayout(new BoxLayout(panelSecond, BoxLayout.X_AXIS));
		//order panel
		JPanel orderPanel = new JPanel();
		orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
		orderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel order = new JLabel("現在注文");
		order.setAlignmentX(Component.CENTER_ALIGNMENT);
		orderPanel.add(order);
		String[] columnCurrentOrderNames = {"ID", "日時", "売買", "レート", "量", "アルゴリズム"};
		currentOrderTableModel = new DefaultTableModel(columnCurrentOrderNames, 0) {
			@Override public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable currentOrderTable = new JTable(currentOrderTableModel);
		currentOrderTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPanel3 = new JScrollPane(currentOrderTable);
		orderPanel.add(scrollPanel3);
		panelSecond.add(orderPanel);

		historyPanel.add(panelSecond);
		return historyPanel;
	}

	@Override
	public void updateByConstantInterval() {
		updateComponent();
	}

	@Override
	public void updateComponent() {
		updateTradeHistory();
		updatePriceHistory();
		updateCurrentOrder();
	}

	/**
	 * Update Trade History
	 */
	private void updateTradeHistory(){
		tradeHistoryTableModel.setNumRows(0);
		List<TradeManager.TradedOrderEntity> tradedEntity = TradeManager.getInstance().getCompletedTradeList();
		for (ListIterator<TradeManager.TradedOrderEntity> it = tradedEntity.listIterator(tradedEntity.size()); it.hasPrevious() ;){
			TradeManager.TradedOrderEntity entity = it.previous();
			String buySell = entity.isBuyOrder() ? "買い" : "売り" ;
			tradeHistoryTableModel.addRow(new String[]{entity.getOrderId(), entity.getTradeId(), entity.getDate(), buySell, String.valueOf(entity.getRate()), String.valueOf(entity.getAmount()), entity.getLogic()});
		}
	}

	/**
	 * Update Price History
	 */
	private void updatePriceHistory(){
		priceHistoryTableModel.setNumRows(0);
		List<CoinManager.PriceEntity> history = CoinManager.getInstance().getPriceHistory();
		int i = 0;
		for (ListIterator<CoinManager.PriceEntity> it = history.listIterator(history.size()); it.hasPrevious() && i < 50; i ++) {
			CoinManager.PriceEntity entity = it.previous();
			priceHistoryTableModel.addRow(new String[]{entity.date, entity.rate});
		}
	}

	/**
	 * Update Current Orders
	 */
	private void updateCurrentOrder(){
		currentOrderTableModel.setNumRows(0);
		Map<String, TradeManager.TradeEntity> orderMap = TradeManager.getInstance().getAllOrder();
		for (Map.Entry<String, TradeManager.TradeEntity> element: orderMap.entrySet()) {
			TradeManager.TradeEntity entity = element.getValue();
			String buySell = entity.isBuyOrder() ? "買い" : "売り" ;
			currentOrderTableModel.addRow(new String[]{element.getKey(), entity.getDate(), buySell, String.valueOf(entity.getRate()), String.valueOf(entity.getAmount()), entity.getLogic()});
		}
	}

}
