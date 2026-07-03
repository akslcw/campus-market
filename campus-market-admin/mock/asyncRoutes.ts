// 模拟后端动态生成路由（管理员角色）
import { defineFakeRoute } from "vite-plugin-fake-server/client";

const goodsAuditRouter = {
  path: "/admin/goods-audit",
  name: "GoodsAudit",
  component: "admin/goods-audit/index",
  meta: {
    title: "待审核商品",
    icon: "ri:file-list-3-line",
    rank: 100,
    roles: ["admin"]
  }
};

const userManageRouter = {
  path: "/admin/users",
  name: "UserManage",
  component: "admin/users/index",
  meta: {
    title: "用户管理",
    icon: "ri:group-line",
    rank: 101,
    roles: ["admin"]
  }
};

const goodsManageRouter = {
  path: "/admin/goods-manage",
  name: "GoodsManage",
  component: "admin/goods-manage/index",
  meta: {
    title: "商品管理",
    icon: "ri:shopping-bag-3-line",
    rank: 102,
    roles: ["admin"]
  }
};

const systemSettingsRouter = {
  path: "/admin/settings",
  name: "SystemSettings",
  component: "admin/settings/index",
  meta: {
    title: "系统设置",
    icon: "ri:settings-3-line",
    rank: 103,
    roles: ["admin"]
  }
};

export default defineFakeRoute([
  {
    url: "/get-async-routes",
    method: "get",
    response: () => {
      return {
        success: true,
        data: [goodsAuditRouter, userManageRouter, goodsManageRouter, systemSettingsRouter]
      };
    }
  }
]);