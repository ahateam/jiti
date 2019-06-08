package xhj.cn.start;

import io.vertx.core.Vertx;
import zyxhj.cms.controller.ContentController;
import zyxhj.core.controller.TagController;
import zyxhj.core.controller.TestController;
import zyxhj.core.controller.UserController;
import zyxhj.custom.controller.WxEventController;
import zyxhj.custom.controller.WxOAuth2Controller;
import zyxhj.jiti.controller.AssetController;
import zyxhj.jiti.controller.BankController;
import zyxhj.jiti.controller.DemonstrationController;
import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.controller.VoteController;
import zyxhj.movie.VideoController;
import zyxhj.utils.Singleton;
import zyxhj.utils.ZeroVerticle;
import zyxhj.utils.data.DataSourceUtils;

public class JITIVerticle extends ZeroVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new JITIVerticle());
	}

	public String name() {
		return "jiti";
	}

	public int port() {
		return 8080;
	}

	protected void init() throws Exception {

		DataSourceUtils.initDataSourceConfig();

		// initCtrl(ctrlMap, Singleton.ins(ServerController.class, "server", this));

		initCtrl(ctrlMap, Singleton.ins(ORGController.class, "org"));

		initCtrl(ctrlMap, Singleton.ins(VoteController.class, "vote"));

		initCtrl(ctrlMap, Singleton.ins(AssetController.class, "asset"));

		initCtrl(ctrlMap, Singleton.ins(TestController.class, "test"));

		initCtrl(ctrlMap, Singleton.ins(UserController.class, "user"));

		initCtrl(ctrlMap, Singleton.ins(TagController.class, "tag"));

		initCtrl(ctrlMap, Singleton.ins(WxEventController.class, "wx"));

		initCtrl(ctrlMap, Singleton.ins(WxOAuth2Controller.class, "wxOAuth"));

		initCtrl(ctrlMap, Singleton.ins(ContentController.class, "content"));

		initCtrl(ctrlMap, Singleton.ins(DemonstrationController.class, "demon"));

		initCtrl(ctrlMap, Singleton.ins(BankController.class, "bank"));

		initCtrl(ctrlMap, Singleton.ins(VideoController.class, "video"));

	}

}
