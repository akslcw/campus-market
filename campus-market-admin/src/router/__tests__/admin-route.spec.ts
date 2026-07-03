import { describe, expect, it } from "vitest";
import adminRoutes from "@/router/modules/admin";
import homeRoutes from "@/router/modules/home";

describe("administrator route group", () => {
  it("redirects /admin to the audit workspace", () => {
    expect(adminRoutes.path).toBe("/admin");
    expect(adminRoutes.redirect).toBe("/admin/audit");
  });

  it("redirects the admin entry route to the audit workspace instead of the hidden welcome page", () => {
    expect(homeRoutes.path).toBe("/");
    expect(homeRoutes.redirect).toBe("/admin/audit");
  });

  it("contains only the real audit and user management pages", () => {
    expect(adminRoutes.children?.map(route => route.path)).toEqual([
      "/admin/audit",
      "/admin/users"
    ]);
  });

  it("requires the uppercase ADMIN role", () => {
    expect(adminRoutes.meta?.roles).toEqual(["ADMIN"]);
    for (const route of adminRoutes.children ?? []) {
      expect(route.meta?.roles).toEqual(["ADMIN"]);
    }
  });
});
