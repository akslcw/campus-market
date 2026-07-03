// 模拟后端登录接口，适配 /api/user/login 路径
import { defineFakeRoute } from "vite-plugin-fake-server/client";

export default defineFakeRoute([
  {
    url: "/api/user/login",
    method: "post",
    response: ({ body }) => {
      if (body.username === "admin") {
        return {
          code: 200,
          msg: "登录成功",
          data: {
            id: 1,
            avatar: "https://avatars.githubusercontent.com/u/44761321",
            username: "admin",
            nickname: "管理员",
            role: "admin",
            accessToken: "eyJhbGciOiJIUzUxMiJ9.admin",
            refreshToken: "eyJhbGciOiJIUzUxMiJ9.adminRefresh",
            expires: "2030/10/30 00:00:00"
          }
        };
      } else {
        return {
          code: 200,
          msg: "登录成功",
          data: {
            id: 2,
            avatar: "https://avatars.githubusercontent.com/u/52823142",
            username: "common",
            nickname: "普通用户",
            role: "user",
            accessToken: "eyJhbGciOiJIUzUxMiJ9.common",
            refreshToken: "eyJhbGciOiJIUzUxMiJ9.commonRefresh",
            expires: "2030/10/30 00:00:00"
          }
        };
      }
    }
  },
  {
    url: "/api/user/refresh-token",
    method: "post",
    response: () => {
      return {
        code: 200,
        msg: "刷新成功",
        data: {
          accessToken: "eyJhbGciOiJIUzUxMiJ9.newToken",
          refreshToken: "eyJhbGciOiJIUzUxMiJ9.newRefresh",
          expires: "2030/10/30 00:00:00"
        }
      };
    }
  }
]);