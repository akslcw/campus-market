const Layout = () => import("@/layout/index.vue");

export default {
  path: "/admin",
  name: "Admin",
  component: Layout,
  redirect: "/admin/audit",
  meta: {
    icon: "ri:archive-drawer-line",
    title: "管理工作台",
    rank: 1,
    roles: ["ADMIN"]
  },
  children: [
    {
      path: "/admin/audit",
      name: "AdminAudit",
      component: () => import("@/views/admin/goods-audit/index.vue"),
      meta: {
        icon: "ri:file-search-line",
        title: "商品审核",
        roles: ["ADMIN"]
      }
    },
    {
      path: "/admin/users",
      name: "AdminUsers",
      component: () => import("@/views/admin/users/index.vue"),
      meta: {
        icon: "ri:group-line",
        title: "用户管理",
        roles: ["ADMIN"]
      }
    }
  ]
} satisfies RouteConfigsTable;
