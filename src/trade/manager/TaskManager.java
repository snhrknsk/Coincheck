package trade.manager;

import org.apache.log4j.Logger;
import trade.exec.*;
import ui.ITabComponent;

import java.util.*;

/**
 * Manage the executing trade tasks.
 */
public class TaskManager {

	private static volatile long TASK_ID = 0;
	private static Logger log = Logger.getLogger(TaskManager.class);
	long start = 0;

	private Timer dumpTimer = new Timer();

	public enum LOGIC_SET{
		TradeSimple{
			@Override
			public ITradeLogic createInstance() {
				return new TradeSimple();
			}
		},
		TradeTechnicalIndex{
			@Override
			public ITradeLogic createInstance() {
				return TradeTechIndex.getInstance();
			}
		},
		TradeOriginal{
			@Override
			public ITradeLogic createInstance() {
				return new TradeOriginal();
			}
		},
		;
		private ITradeLogic instance;
		private LOGIC_SET(){}
		public abstract ITradeLogic createInstance();
	}

	private static TaskManager instance = null ;
	private Map<String, ITradeLogic> tradingTaskMap = Collections.synchronizedMap(new TreeMap<>());

	private TaskManager() {
		//initial run logic
		tradingTaskMap.put("CheckRate", new CheckRate());
		tradingTaskMap.put("CheckTrade", new CheckTrade());
		ITradeLogic logic = LOGIC_SET.TradeSimple.createInstance();
		tradingTaskMap.put(logic.toString(), logic);
	}

	public synchronized static TaskManager getInstance(){
		if (instance == null){
			instance = new TaskManager();
		}
		return instance;
	}

	public synchronized List<ITradeLogic> getTradingTask(){
		List<ITradeLogic> taskList = new ArrayList<>();
		for (ITradeLogic logic: tradingTaskMap.values() ) {
			taskList.add(logic);
		}
		return taskList;
	}

	/**
	 * return task name and its task instance
	 */
	public synchronized Map<String, ITradeLogic> getTradingTaskMap(){
		return tradingTaskMap;
	}

	/**
	 * Remove specified task from task list.
	 */
	public synchronized boolean stopTask(String cancelTaskName) {
		if (tradingTaskMap.containsKey(cancelTaskName)) {
			log.info("Stop the task and delete the current exec logic: " + cancelTaskName);
			tradingTaskMap.get(cancelTaskName).stopTask();
			tradingTaskMap.remove(cancelTaskName);
		} else {
			log.info("Task Class Not Found in Task List : " + cancelTaskName);
			return false;
		}
		return true;
	}

	/**
	 * Restart Task with new params
	 */
	public synchronized boolean resetTask(String restartTaskName, List<String> params){
		// TODO: implement this method(Make task manager or in this class)
		throw new UnsupportedOperationException();
	}

	/**
	 * Start specified task.
	 * @return new task name(class name + No[incremented])
	 */
	public synchronized String startNewTask(ITradeLogic startTask) {
		log.info("Add new Task : " + startTask);
		tradingTaskMap.put(startTask.toString() , startTask);
		return startTask.getLogicName();
	}
}
