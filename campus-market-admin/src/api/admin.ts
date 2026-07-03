import { http } from "@/utils/http";
import type { Result } from "./user";

export type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
};

export type GoodsSummary = {
  id: number;
  title: string;
  price: number;
  originalPrice?: number;
  categoryId: number;
  categoryName: string;
  status: "ON_SALE" | "SOLD" | "OFF_SHELF";
  auditStatus: "PENDING" | "APPROVED" | "REJECTED";
  viewCount: number;
  coverImage?: string | null;
  sellerName: string;
  createdAt: string;
};

export type GoodsDetail = GoodsSummary & {
  description: string;
  rawDescription?: string;
  images: string[];
  auditRemark?: string | null;
  sellerId: number;
  sellerContact?: string | null;
  updatedAt: string;
};

export type PlatformUser = {
  id: number;
  username: string;
  nickname: string;
  avatar?: string | null;
  contact?: string | null;
  role: "USER" | "ADMIN";
  status: 0 | 1;
  createdAt: string;
  updatedAt?: string;
};

export const getPendingGoods = (params?: { page?: number; size?: number }) =>
  http.request<Result<PageResult<GoodsSummary>>>(
    "get",
    "/api/admin/goods/pending",
    { params }
  );

export const getGoodsDetail = (id: number) =>
  http.request<Result<GoodsDetail>>("get", `/api/goods/${id}`);

export const approveGoods = (id: number) =>
  http.request<Result<null>>("put", `/api/admin/goods/${id}/approve`);

export const rejectGoods = (id: number, reason: string) =>
  http.request<Result<null>>("put", `/api/admin/goods/${id}/reject`, {
    data: { reason }
  });

export const getUsers = (params?: { page?: number; size?: number }) =>
  http.request<Result<PageResult<PlatformUser>>>(
    "get",
    "/api/admin/users",
    { params }
  );

export const banUser = (id: number) =>
  http.request<Result<null>>("put", `/api/admin/users/${id}/ban`);

export const unbanUser = (id: number) =>
  http.request<Result<null>>("put", `/api/admin/users/${id}/unban`);
