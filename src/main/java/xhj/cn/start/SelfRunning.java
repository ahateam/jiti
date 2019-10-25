package xhj.cn.start;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.vertx.core.AbstractVerticle;
import zyxhj.jiti.controller.VoteController;
import zyxhj.jiti.service.VoteService;

public class SelfRunning extends AbstractVerticle {

	public void start() {
		showDayTime();
	}

	private static VoteController voteService = new VoteController("node");

	public static void showDayTime() {

		Date defaultdate = new Date();

		Timer dTimer = new Timer();
		dTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					int row = voteService.VotoISOver();
					System.out.println("修改状态的投票数" + row);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, defaultdate, 5 * 60 * 1000);// 5分钟
	}
}
