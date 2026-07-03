import { beforeEach, describe, expect, it, vi } from "vitest";

const request = vi.fn();

vi.mock("@/utils/http", () => ({
  http: { request }
}));

describe("real backend API contracts", () => {
  beforeEach(() => request.mockReset());

  it("uses the shared auth login endpoint", async () => {
    request.mockResolvedValue({ code: 200, data: null });
    const { getLogin } = await import("@/api/user");

    await getLogin({ username: "admin", password: "admin123" });

    expect(request).toHaveBeenCalledWith("post", "/api/auth/login", {
      data: { username: "admin", password: "admin123" }
    });
  });

  it("uses dedicated approve and reject endpoints", async () => {
    request.mockResolvedValue({ code: 200, data: null });
    const { approveGoods, rejectGoods } = await import("@/api/admin");

    await approveGoods(12);
    await rejectGoods(13, "图片与描述不符");

    expect(request).toHaveBeenNthCalledWith(
      1,
      "put",
      "/api/admin/goods/12/approve"
    );
    expect(request).toHaveBeenNthCalledWith(
      2,
      "put",
      "/api/admin/goods/13/reject",
      { data: { reason: "图片与描述不符" } }
    );
  });

  it("uses dedicated ban and unban endpoints", async () => {
    request.mockResolvedValue({ code: 200, data: null });
    const { banUser, unbanUser } = await import("@/api/admin");

    await banUser(21);
    await unbanUser(22);

    expect(request).toHaveBeenNthCalledWith(
      1,
      "put",
      "/api/admin/users/21/ban"
    );
    expect(request).toHaveBeenNthCalledWith(
      2,
      "put",
      "/api/admin/users/22/unban"
    );
  });
});
