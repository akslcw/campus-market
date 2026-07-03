// 模拟后台管理接口
import { defineFakeRoute } from "vite-plugin-fake-server/client";

// 模拟待审核商品数据
const mockPendingGoods = [
  {
    id: 1,
    title: "九成新 iPhone 14 Pro 256GB",
    description: "去年买的，一直带壳使用，无划痕无维修，电池健康度92%",
    price: 5800,
    categoryName: "数码电子",
    username: "zhangsan",
    nickname: "张三",
    images: ["https://picsum.photos/200/200?random=1"],
    createdAt: "2026-06-18 14:30:00",
    status: 0
  },
  {
    id: 2,
    title: "考研数学全套资料",
    description: "2025版李永乐考研数学全套，仅看了前两章，几乎全新",
    price: 80,
    categoryName: "书籍教材",
    username: "lisi",
    nickname: "李四",
    images: ["https://picsum.photos/200/200?random=2"],
    createdAt: "2026-06-19 09:15:00",
    status: 0
  },
  {
    id: 3,
    title: "宿舍用小冰箱 50L",
    description: "毕业急出，50L小冰箱，制冷正常，适合宿舍使用",
    price: 200,
    categoryName: "生活电器",
    username: "wangwu",
    nickname: "王五",
    images: ["https://picsum.photos/200/200?random=3"],
    createdAt: "2026-06-19 16:45:00",
    status: 0
  },
  {
    id: 4,
    title: "机械键盘 Cherry 青轴",
    description: "Cherry MX 青轴，87键，RGB背光，用了半年",
    price: 150,
    categoryName: "数码电子",
    username: "zhaoliu",
    nickname: "赵六",
    images: ["https://picsum.photos/200/200?random=4"],
    createdAt: "2026-06-20 08:00:00",
    status: 0
  },
  {
    id: 5,
    title: "二手自行车 山地车",
    description: "捷安特ATX660，24速，车辆状况良好，送车锁",
    price: 450,
    categoryName: "运动户外",
    username: "sunqi",
    nickname: "孙七",
    images: ["https://picsum.photos/200/200?random=5"],
    createdAt: "2026-06-20 10:20:00",
    status: 0
  }
];

// 模拟用户数据
const mockUsers = [
  { id: 1, username: "admin", nickname: "管理员", avatar: "https://avatars.githubusercontent.com/u/44761321", email: "admin@campus.com", phone: "13800000001", status: 0, role: "admin", createdAt: "2026-06-01 08:00:00" },
  { id: 2, username: "zhangsan", nickname: "张三", avatar: "https://picsum.photos/100/100?random=10", email: "zhangsan@campus.com", phone: "13800000002", status: 0, role: "user", createdAt: "2026-06-10 14:30:00" },
  { id: 3, username: "lisi", nickname: "李四", avatar: "https://picsum.photos/100/100?random=11", email: "lisi@campus.com", phone: "13800000003", status: 0, role: "user", createdAt: "2026-06-12 09:15:00" },
  { id: 4, username: "wangwu", nickname: "王五", avatar: "https://picsum.photos/100/100?random=12", email: "wangwu@campus.com", phone: "13800000004", status: 1, role: "user", createdAt: "2026-06-14 11:00:00" },
  { id: 5, username: "zhaoliu", nickname: "赵六", avatar: "https://picsum.photos/100/100?random=13", email: "zhaoliu@campus.com", phone: "13800000005", status: 0, role: "user", createdAt: "2026-06-15 16:20:00" },
  { id: 6, username: "sunqi", nickname: "孙七", avatar: "https://picsum.photos/100/100?random=14", email: "sunqi@campus.com", phone: "13800000006", status: 0, role: "user", createdAt: "2026-06-16 10:45:00" },
  { id: 7, username: "zhouba", nickname: "周八", avatar: "https://picsum.photos/100/100?random=15", email: "zhouba@campus.com", phone: "13800000007", status: 1, role: "user", createdAt: "2026-06-17 13:10:00" },
  { id: 8, username: "wujiu", nickname: "吴九", avatar: "https://picsum.photos/100/100?random=16", email: "wujiu@campus.com", phone: "13800000008", status: 0, role: "user", createdAt: "2026-06-18 08:30:00" }
];

// 模拟已通过审核的商品
const mockApprovedGoods = [
  { id: 101, title: "iPad Air 5 M1芯片 64GB", description: "半年前购入，考研看网课用，现已上岸", price: 3200, categoryName: "数码电子", username: "xiaoqiang", nickname: "小强", images: ["https://picsum.photos/200/200?random=20"], createdAt: "2026-06-08 10:00:00", status: 1 },
  { id: 102, title: "大学英语四级真题集", description: "2025版华研外语四级真题，做过两套", price: 25, categoryName: "书籍教材", username: "xiaohong", nickname: "小红", images: ["https://picsum.photos/200/200?random=21"], createdAt: "2026-06-11 15:30:00", status: 1 },
  { id: 103, title: "床上折叠桌 带风扇", description: "宿舍神器，可折叠带USB风扇", price: 45, categoryName: "生活用品", username: "xiaoming", nickname: "小明", images: ["https://picsum.photos/200/200?random=22"], createdAt: "2026-06-13 19:00:00", status: 1 },
  { id: 104, title: "罗技 G502 游戏鼠标", description: "用了三个月，换了无线版故出", price: 180, categoryName: "数码电子", username: "dawang", nickname: "大王", images: ["https://picsum.photos/200/200?random=23"], createdAt: "2026-06-15 12:00:00", status: 1 }
];

// 模拟管理员信息
const mockAdmin = {
  id: 1,
  username: "admin",
  nickname: "管理员",
  avatar: "https://avatars.githubusercontent.com/u/44761321",
  email: "admin@campus.com",
  phone: "13800000001",
  role: "admin",
  password: "admin123"
};

// 模拟系统配置
const mockSystemConfig = {
  siteName: "校园二手交易平台",
  siteDescription: "专为大学生打造的校园二手交易平台，安全、便捷、实惠",
  announcement: "欢迎使用校园二手交易平台！请文明交易，谨防诈骗。如遇问题请联系管理员。",
  autoAudit: false,
  maxGoodsPerUser: 10,
  logo: "https://picsum.photos/100/100?random=99"
};

// 模拟已拒绝的商品
const mockRejectedGoods = [
  { id: 201, title: "高仿LV包 99新", description: "朋友送的，只用过一次", price: 200, categoryName: "服饰箱包", username: "xiaoli", nickname: "小李", images: ["https://picsum.photos/200/200?random=30"], createdAt: "2026-06-16 09:00:00", status: 2 },
  { id: 202, title: "代写论文 包过", description: "各专业均可，保证通过", price: 500, categoryName: "其他", username: "laowang", nickname: "老王", images: ["https://picsum.photos/200/200?random=31"], createdAt: "2026-06-17 22:00:00", status: 2 }
];

export default defineFakeRoute([
  // ---- 商品审核 ----
  {
    url: "/api/admin/goods/pending",
    method: "get",
    response: ({ query }) => {
      const page = parseInt(query.page) || 1;
      const size = parseInt(query.size) || 10;
      const start = (page - 1) * size;
      const end = start + size;
      return {
        code: 200,
        msg: "查询成功",
        data: {
          records: mockPendingGoods.slice(start, end),
          total: mockPendingGoods.length
        }
      };
    }
  },
  {
    url: "/api/admin/goods/audit",
    method: "post",
    response: ({ body }) => {
      const { goodsId, status, reason } = body;
      const idx = mockPendingGoods.findIndex(g => g.id === goodsId);
      if (idx !== -1) {
        const goods = mockPendingGoods[idx];
        goods.status = status;
        if (status === 2) {
          return {
            code: 200,
            msg: `商品「${goods.title}」已拒绝，原因：${reason || "未填写"}`,
            data: null
          };
        } else {
          return {
            code: 200,
            msg: `商品「${goods.title}」审核通过，已上架`,
            data: null
          };
        }
      }
      return {
        code: 404,
        msg: "商品不存在",
        data: null
      };
    }
  },

  // ---- 用户管理 ----
  {
    url: "/api/admin/users",
    method: "get",
    response: ({ query }) => {
      const page = parseInt(query.page) || 1;
      const size = parseInt(query.size) || 10;
      const status = query.status !== undefined && query.status !== "" ? parseInt(query.status) : undefined;
      let filtered = mockUsers;
      if (status !== undefined && !isNaN(status)) {
        filtered = mockUsers.filter(u => u.status === status);
      }
      const start = (page - 1) * size;
      const end = start + size;
      return {
        code: 200,
        msg: "查询成功",
        data: {
          records: filtered.slice(start, end),
          total: filtered.length
        }
      };
    }
  },
  {
    url: "/api/admin/users/ban",
    method: "post",
    response: ({ body }) => {
      const { userId, status } = body;
      const user = mockUsers.find(u => u.id === userId);
      if (user) {
        user.status = status;
        const action = status === 1 ? "封禁" : "解封";
        return {
          code: 200,
          msg: `用户「${user.nickname}」已${action}`,
          data: null
        };
      }
      return {
        code: 404,
        msg: "用户不存在",
        data: null
      };
    }
  },

  // ---- 商品管理 ----
  {
    url: "/api/admin/goods/list",
    method: "get",
    response: ({ query }) => {
      const page = parseInt(query.page) || 1;
      const size = parseInt(query.size) || 10;
      const status = query.status !== undefined && query.status !== "" ? parseInt(query.status) : undefined;
      const keyword = query.keyword || "";
      // 合并待审核 + 已审核数据
      const allGoods = [...mockPendingGoods, ...mockApprovedGoods, ...mockRejectedGoods];
      let filtered = allGoods;
      if (status !== undefined && !isNaN(status)) {
        filtered = filtered.filter(g => g.status === status);
      }
      if (keyword) {
        const kw = keyword.toLowerCase();
        filtered = filtered.filter(
          g => g.title.toLowerCase().includes(kw) ||
               g.username.toLowerCase().includes(kw) ||
               g.nickname.toLowerCase().includes(kw) ||
               g.categoryName.toLowerCase().includes(kw)
        );
      }
      const start = (page - 1) * size;
      const end = start + size;
      return {
        code: 200,
        msg: "查询成功",
        data: {
          records: filtered.slice(start, end),
          total: filtered.length
        }
      };
    }
  },
  {
    url: "/api/admin/goods/take-down",
    method: "post",
    response: ({ body }) => {
      const { goodsId } = body;
      const all = [...mockPendingGoods, ...mockApprovedGoods, ...mockRejectedGoods];
      const goods = all.find(g => g.id === goodsId);
      if (goods) {
        return {
          code: 200,
          msg: `商品「${goods.title}」已下架`,
          data: null
        };
      }
      return {
        code: 404,
        msg: "商品不存在",
        data: null
      };
    }
  },

  // ---- 系统设置 ----
  {
    url: "/api/admin/settings/profile",
    method: "put",
    response: ({ body }) => {
      if (body.nickname !== undefined) mockAdmin.nickname = body.nickname;
      if (body.email !== undefined) mockAdmin.email = body.email;
      if (body.phone !== undefined) mockAdmin.phone = body.phone;
      return {
        code: 200,
        msg: "信息更新成功",
        data: {
          id: mockAdmin.id,
          username: mockAdmin.username,
          nickname: mockAdmin.nickname,
          avatar: mockAdmin.avatar,
          email: mockAdmin.email,
          phone: mockAdmin.phone,
          role: mockAdmin.role
        }
      };
    }
  },
  {
    url: "/api/admin/settings/password",
    method: "put",
    response: ({ body }) => {
      const { oldPassword, newPassword } = body;
      if (oldPassword !== mockAdmin.password) {
        return { code: 400, msg: "原密码错误", data: null };
      }
      if (newPassword.length < 6) {
        return { code: 400, msg: "新密码长度至少6位", data: null };
      }
      mockAdmin.password = newPassword;
      return { code: 200, msg: "密码修改成功", data: null };
    }
  },
  {
    url: "/api/admin/settings/avatar",
    method: "post",
    response: () => {
      // 模拟上传，返回随机头像
      const newAvatar = `https://picsum.photos/200/200?random=${Date.now()}`;
      mockAdmin.avatar = newAvatar;
      return { code: 200, msg: "头像上传成功", data: { url: newAvatar } };
    }
  },
  {
    url: "/api/admin/settings/config",
    method: "get",
    response: () => {
      return { code: 200, msg: "查询成功", data: mockSystemConfig };
    }
  },
  {
    url: "/api/admin/settings/config",
    method: "put",
    response: ({ body }) => {
      if (body.siteName !== undefined) mockSystemConfig.siteName = body.siteName;
      if (body.siteDescription !== undefined) mockSystemConfig.siteDescription = body.siteDescription;
      if (body.announcement !== undefined) mockSystemConfig.announcement = body.announcement;
      if (body.autoAudit !== undefined) mockSystemConfig.autoAudit = body.autoAudit;
      if (body.maxGoodsPerUser !== undefined) mockSystemConfig.maxGoodsPerUser = body.maxGoodsPerUser;
      if (body.logo !== undefined) mockSystemConfig.logo = body.logo;
      return { code: 200, msg: "系统配置更新成功", data: null };
    }
  }
]);