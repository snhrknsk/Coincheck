package trade.manager;

import trade.exec.*;

import java.util.*;

/**
 * Manage the executing trade tasks.
 */
public class TaskManager {

	private static volatile long TASK_ID = 0;

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
		private LOGIC_SET(){}
		public abstract ITradeLogic createInstance();
	}

	private static TaskManager instance = null ;
	private Map<String, ITradeLogic> tradingTaskMap = Collections.synchronizedMap(new TreeMap<>());

	private TaskManager() {
		//initial run logic
		tradingTaskMap.put("CheckRate", new CheckRate());
		tradingTaskMap.put("CheckTrade", new CheckTrade());
		tradingTaskMap.put("TradeSimple", new TradeSimple());
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
		tradingTaskMap.remove(cancelTaskName);
		System.out.println("Task Class Not Found in Task List : " + cancelTaskName);
		return false;
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
		tradingTaskMap.put(startTask + "#" + TASK_ID++, startTask);
		return startTask.getLogicName();
	}


}
