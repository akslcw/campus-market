import type { LoginInfo } from "@/api/user";

export type AdminSession = {
  avatar: string;
  username: string;
  nickname: string;
  roles: string[];
  permissions: string[];
  accessToken: string;
  refreshToken: string;
  expires: Date;
};

export function normalizeAdminLogin(user: LoginInfo): AdminSession {
  if (user.role !== "ADMIN") {
    throw new Error("仅管理员可登录后台");
  }

  return {
    avatar: "",
    username: user.username,
    nickname: user.nickname || user.username,
    roles: ["ADMIN"],
    permissions: ["*:*:*"],
    accessToken: user.token,
    refreshToken: "",
    expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
  };
}
