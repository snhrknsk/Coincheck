package trade.exec;

import org.apache.log4j.Logger;
import trade.manager.TaskManager;
import trade.util.CreateFileUtil;

import javax.annotation.processing.SupportedOptions;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Exec defined tasks in {@link TaskManager} regularly
 */
public class TaskWorker {

	private static Logger log = Logger.getLogger(TaskWorker.class);
	private Timer timer = null;

	/**
	 * Edit this constructor for exec trade logic<br>
	 * Add initial exec logic class to {@link TaskManager#getTradingTask()} to exec them.<br>
	 * After {@link #startTask(long)}, they are executed for constant interval.
	 */
	public TaskWorker(){
	}

	public void startTask(long interval){

		if (interval <= 0){
			log.error("Interval is invalid : " + interval);
			throw new IllegalArgumentException();
		}
		log.info("Start Trade. Plugin = " + TaskManager.getInstance().getTradingTask());

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				log.info("Exec tasks." + TaskManager.getInstance().getTradingTask());
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
		log.info("End Trade");
		postProcess();
	}

	public void postProcess(){

		// out put coin price history
		CreateFileUtil.createPriceHistoryCSV();
		CreateFileUtil.createTradeHistory();
		CreateFileUtil.createOpenOrder();

	}
}
