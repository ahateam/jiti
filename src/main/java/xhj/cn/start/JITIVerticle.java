package xhj.cn.start;

import io.vertx.core.Vertx;
import zyxhj.core.controller.TestController;
import zyxhj.core.controller.UserController;
import zyxhj.flow.service.FlowService;
import zyxhj.flow.service.ProcessService;
import zyxhj.jiti.controller.ApprovalProcessController;
import zyxhj.jiti.controller.AssetController;
import zyxhj.jiti.controller.BankController;
import zyxhj.jiti.controller.DemonstrationController;
import zyxhj.jiti.controller.ExportTaskController;
import zyxhj.jiti.controller.ExternalController;
import zyxhj.jiti.controller.ImportController;
import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.controller.SingleCertificateTaskController;
import zyxhj.jiti.controller.VersionController;
import zyxhj.jiti.controller.VoteController;
import zyxhj.movie.VideoController;
import zyxhj.utils.Singleton;
import zyxhj.utils.ZeroVerticle;

public class JITIVerticle extends ZeroVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JITIVerticle());
		vertx.deployVerticle(SelfRunning.class.getName());
	}

	public String name() {
		return "jiti";
	}

	public int port() {
		return 8080;
	}

	protected void init() throws Exception {

		// initCtrl(ctrlMap, Singleton.ins(ServerController.class, "server", this));

		initCtrl(ctrlMap, Singleton.ins(ORGController.class, "org"));

		initCtrl(ctrlMap, Singleton.ins(VoteController.class, "vote"));

		initCtrl(ctrlMap, Singleton.ins(AssetController.class, "asset"));

		initCtrl(ctrlMap, Singleton.ins(TestController.class, "test"));

		initCtrl(ctrlMap, Singleton.ins(UserController.class, "user"));

		initCtrl(ctrlMap, Singleton.ins(DemonstrationController.class, "demon"));

		initCtrl(ctrlMap, Singleton.ins(BankController.class, "bank"));

		initCtrl(ctrlMap, Singleton.ins(VideoController.class, "video"));

		initCtrl(ctrlMap, Singleton.ins(ExternalController.class, "ext"));

		initCtrl(ctrlMap, Singleton.ins(ImportController.class, "imp"));
		
		initCtrl(ctrlMap, Singleton.ins(VersionController.class, "version"));
		
		initCtrl(ctrlMap, Singleton.ins(ExportTaskController.class, "export"));
		
		initCtrl(ctrlMap, Singleton.ins(SingleCertificateTaskController.class, "scft"));

		initCtrl(ctrlMap, Singleton.ins(ApprovalProcessController.class, "aProcess"));
		
		initCtrl(ctrlMap, Singleton.ins(ProcessService.class, "process"));

		initCtrl(ctrlMap, Singleton.ins(FlowService.class, "flow"));

	}

}
