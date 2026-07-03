import { describe, expect, it, vi } from "vitest";

vi.mock("@/store/modules/user", () => ({
  useUserStoreHook: () => ({
    isRemembered: false,
    loginDay: 7
  })
}));

import { formatToken } from "@/utils/auth";
import { normalizeAdminLogin } from "@/utils/adminAuth";

describe("administrator authentication mapping", () => {
  it("sends the raw Sa-Token value", () => {
    expect(formatToken("token-value")).toBe("token-value");
  });

  it("normalizes a backend ADMIN login response", () => {
    expect(
      normalizeAdminLogin({
        token: "token-value",
        userId: 1,
        username: "admin",
        nickname: "系统管理员",
        role: "ADMIN"
      })
    ).toMatchObject({
      accessToken: "token-value",
      username: "admin",
      nickname: "系统管理员",
      roles: ["ADMIN"],
      permissions: ["*:*:*"]
    });
  });

  it("rejects a non-admin account", () => {
    expect(() =>
      normalizeAdminLogin({
        token: "user-token",
        userId: 2,
        username: "student",
        nickname: "学生",
        role: "USER"
      })
    ).toThrow("仅管理员可登录后台");
  });
});
