package trade.exec;

import org.json.JSONArray;
import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.AccountInfo;
import trade.manager.CoinManager;
import trade.manager.TradeManager;
import trade.util.CreateFileUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

public class TaskWorker {

	private List<ITradeLogic> taskList = new ArrayList<>();

	/**
	 * Edit this constructor for exec trade logic<br>
	 * Add new logic class to {@link #taskList} to exec them.<br>
	 * After {@link #startTask(long, long)}, they are executed for constant interval.
	 */
	public TaskWorker(){
		taskList.add(new CheckRate());
		taskList.add(new CheckTrade());
		taskList.add(new TradeSimple());
//		taskList.add(new TradeSimple());
	}

	public void startTask(long interval, long periodTerm){

		if (periodTerm < 0 || interval <= 0){
			System.out.println("period term is invalid : " + periodTerm);
			return;
		}
		System.out.println( LocalDateTime.now() + " Start Trade. Plugin = " + taskList);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println(LocalDateTime.now() + " Exec tasks.");
				for (ITradeLogic task:taskList ) {
					task.exec();
				}
			}
		}, interval, interval);

		try {
			Thread.sleep(periodTerm);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		System.out.println(LocalDateTime.now() + " End Trade.");
		postProcess();
	}

	private void postProcess(){

		// out put coin price history
		CreateFileUtil.createPriceHistoryCSV();
		CreateFileUtil.createTradeHistory();
		CreateFileUtil.createOpenOrder();

	}



}
