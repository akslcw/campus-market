import { http } from "@/utils/http";

export type Result<T = unknown> = {
  code: number;
  msg: string;
  data: T;
};

export type LoginParams = {
  username: string;
  password: string;
};

export type LoginInfo = {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  role: "USER" | "ADMIN";
};

export const getLogin = (data: LoginParams) =>
  http.request<Result<LoginInfo>>("post", "/api/auth/login", { data });
