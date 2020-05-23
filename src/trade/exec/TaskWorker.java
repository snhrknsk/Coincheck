package trade.exec;

import trade.manager.TaskManager;
import trade.util.CreateFileUtil;

import javax.annotation.processing.SupportedOptions;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Exec defined tasks in {@link TaskManager} regularly
 */
public class TaskWorker {

//	private List<ITradeLogic> taskList = new ArrayList<>();
	private Timer timer = null;

	public enum LOGIC_SET{
		TradeSimple{
			@Override
			public ITradeLogic createInstance() {
				return new TradeSimple();
			}
		},
		TradeTrend{
			@Override
			public ITradeLogic createInstance() {
				return new TradeTrend();
			}
		},
		;
		private ITradeLogic instance;
		private LOGIC_SET(){
		}
		public abstract ITradeLogic createInstance();
	}

	/**
	 * Edit this constructor for exec trade logic<br>
	 * Add initial exec logic class to {@link TaskManager#getTradingTask()} to exec them.<br>
	 * After {@link #startTask(long)}, they are executed for constant interval.
	 */
	public TaskWorker(){
	}

	public void startTask(long interval){

		if (interval <= 0){
			System.out.println("Interval is invalid : " + interval);
			throw new IllegalArgumentException();
		}
		System.out.println( LocalDateTime.now() + " Start Trade. Plugin = " + TaskManager.getInstance().getTradingTask());

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println(LocalDateTime.now() + " Exec tasks." + TaskManager.getInstance().getTradingTask());
				for (ITradeLogic task : TaskManager.getInstance().getTradingTask() ) {
					task.exec();
				}
			}
		}, interval, interval);
	}

	public void stopAllTask(){
		timer.cancel();
		for (ITradeLogic task : TaskManager.getInstance().getTradingTask() ) {
			task.stopTask();
		}
		System.out.println(LocalDateTime.now() + " End Trade.");
		postProcess();
	}



	public void postProcess(){

		// out put coin price history
		CreateFileUtil.createPriceHistoryCSV();
		CreateFileUtil.createTradeHistory();
		CreateFileUtil.createOpenOrder();

	}
}
