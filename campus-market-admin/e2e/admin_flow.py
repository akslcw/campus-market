from __future__ import annotations

import json
import time
from pathlib import Path

from playwright.sync_api import Page, Playwright, sync_playwright


ROOT = Path(__file__).resolve().parents[1]
SHOT_DIR = ROOT / "shots"
CUSTOMER_BASE = "http://127.0.0.1:8888"
ADMIN_BASE = "http://127.0.0.1:8848"
SERVER_BASE = "http://127.0.0.1:8080"
IMAGE_PATH = Path("E:/code/campus-market-web/shots/pic-1.png")


def chromium_path() -> str:
    candidates = [
        Path(
            "C:/Users/29327/AppData/Local/ms-playwright/"
            "chromium-1223/chrome-win64/chrome.exe"
        ),
        Path("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return str(candidate)
    raise RuntimeError("No Chromium-compatible browser executable found")


def select_option(page: Page, index: int, option_index: int) -> None:
    select = page.locator(".pub-form .el-select:visible").nth(index)
    select.click()
    page.locator(
        ".el-select-dropdown:visible .el-select-dropdown__item:visible"
    ).first.wait_for(state="visible")
    for _ in range(option_index + 1):
        page.keyboard.press("ArrowDown")
    page.keyboard.press("Enter")


def register_and_login(page: Page, username: str, password: str) -> None:
    page.goto(f"{CUSTOMER_BASE}/register", wait_until="domcontentloaded")
    form = page.locator(".auth-form")
    form.wait_for(state="visible")
    inputs = form.locator("input")
    inputs.nth(0).fill(username)
    inputs.nth(1).fill(f"联调用户{username[-4:]}")
    form.locator("input[type=password]").nth(0).fill(password)
    form.locator("input[type=password]").nth(1).fill(password)
    form.locator(".auth-submit").click()
    page.wait_for_url("**/login")
    form = page.locator(".auth-form")
    form.locator("input").nth(0).fill(username)
    form.locator("input[type=password]").fill(password)
    form.locator(".auth-submit").click()
    page.wait_for_url("**/home")


def fill_publish(page: Page, title: str, description: str) -> None:
    page.goto(f"{CUSTOMER_BASE}/publish", wait_until="domcontentloaded")
    page.locator(".pub-form").wait_for(state="visible")
    page.locator(".pub-form input").nth(0).fill(title)
    select_option(page, 0, 0)
    select_option(page, 1, 1)
    page.locator(".pub-form input[type=number]").nth(0).fill("188")
    page.locator(".pub-form input[type=number]").nth(1).fill("399")
    page.locator(".pub-form textarea").fill(description)
    upload = page.locator(".img-add input[type=file]")
    upload.set_input_files(str(IMAGE_PATH))
    page.locator(".img-cell").first.wait_for(state="visible")


def publish_with_ai(page: Page, title: str) -> tuple[int, list[str]]:
    fill_publish(
        page,
        title,
        "九成新，功能正常，换新闲置，校内当面交易，配件齐全。",
    )
    with page.expect_response(
        lambda response: "/api/ai/optimize" in response.url
        and response.request.method == "POST",
        timeout=30000,
    ):
        page.locator(".ai-btn").click()
    page.locator(".ai-btn").wait_for(state="visible")
    page.screenshot(path=str(SHOT_DIR / "01-customer-publish.png"), full_page=True)
    with page.expect_response(
        lambda response: response.url.endswith("/api/goods")
        and response.request.method == "POST",
        timeout=30000,
    ) as publish_info:
        page.locator(".submit-btn").click()
    payload = publish_info.value.json()["data"]
    page.locator(".el-message-box").wait_for(state="visible")
    page.locator(".el-message-box__btns button").last.click()
    page.wait_for_url("**/profile")
    return int(payload["id"]), payload["images"]


def admin_login(page: Page, username: str, password: str) -> None:
    page.goto(f"{ADMIN_BASE}/#/login", wait_until="networkidle")
    inputs = page.locator(".el-input__inner:visible")
    inputs.nth(0).fill(username)
    inputs.nth(1).fill(password)
    page.locator("button.el-button--primary:visible").click()


def wait_admin_ready(page: Page) -> None:
    page.wait_for_url("**/#/admin/audit", timeout=20000)
    page.locator("h1").filter(has_text="商品审核台").wait_for(state="visible")
    page.locator(".el-loading-mask").wait_for(state="hidden", timeout=20000)
    page.wait_for_timeout(500)


def find_row(page: Page, text: str):
    row = page.locator(".el-table__body-wrapper tbody tr").filter(has_text=text)
    row.first.wait_for(state="visible", timeout=20000)
    return row.first


def run_flow(playwright: Playwright) -> dict:
    browser = playwright.chromium.launch(
        headless=True, executable_path=chromium_path()
    )
    customer = browser.new_page(viewport={"width": 460, "height": 900})
    admin = browser.new_page(viewport={"width": 1440, "height": 1000})
    for page, name in ((customer, "customer"), (admin, "admin")):
        page.set_default_timeout(20000)
        page.on(
            "pageerror",
            lambda error, source=name: print(f"{source}:pageerror:{error}"),
        )

    suffix = str(int(time.time() * 1000))[-8:]
    username = f"joint{suffix}"
    password = "Test1234"
    approved_title = f"三端联调相机 {suffix}"
    rejected_title = f"图片待补充商品 {suffix}"
    reject_reason = "商品图片信息不足，请补充实物细节图"

    register_and_login(customer, username, password)
    approved_id, images = publish_with_ai(customer, approved_title)
    customer_token = customer.evaluate("localStorage.getItem('token')")

    admin_login(admin, username, password)
    admin.wait_for_timeout(1200)
    assert "/#/login" in admin.url, "normal user entered the admin frontend"
    assert admin.get_by_text("仅管理员可登录后台").is_visible()

    admin_login(admin, "admin", "admin123")
    wait_admin_ready(admin)
    admin.screenshot(path=str(SHOT_DIR / "02-admin-audit.png"), full_page=True)

    approved_row = find_row(admin, approved_title)
    with admin.expect_response(
        lambda response: response.url.endswith(
            f"/api/admin/goods/{approved_id}/approve"
        )
        and response.request.method == "PUT"
    ):
        approved_row.get_by_role("button", name="通过", exact=True).click()
        admin.locator(".el-message-box").wait_for(state="visible")
        admin.locator(".el-message-box__btns button").last.click()
    approved_row.wait_for(state="detached")
    admin.screenshot(
        path=str(SHOT_DIR / "03-admin-approved.png"), full_page=True
    )

    customer.goto(f"{CUSTOMER_BASE}/home", wait_until="domcontentloaded")
    customer.get_by_text(approved_title, exact=True).wait_for(state="visible")
    customer.screenshot(
        path=str(SHOT_DIR / "04-customer-approved-home.png"), full_page=True
    )

    publish_response = customer.request.post(
        f"{SERVER_BASE}/api/goods",
        headers={"Authorization": customer_token},
        data={
            "title": rejected_title,
            "description": "图片较少，商品功能正常，校内交易。",
            "price": 66,
            "originalPrice": 100,
            "categoryId": 1,
            "categoryName": "数码电子",
            "images": images,
        },
    )
    assert publish_response.ok, publish_response.text()
    rejected_id = int(publish_response.json()["data"]["id"])

    with admin.expect_response(
        lambda response: "/api/admin/goods/pending" in response.url
        and response.request.method == "GET"
    ):
        admin.get_by_role("button", name="刷新", exact=True).click()
    admin.locator(".el-loading-mask").wait_for(state="hidden")
    rejected_row = find_row(admin, rejected_title)
    rejected_row.get_by_role("button", name="拒绝", exact=True).click()
    admin.locator(".el-dialog").wait_for(state="visible")
    admin.locator(".el-dialog textarea").fill(reject_reason)
    admin.wait_for_timeout(400)
    admin.screenshot(
        path=str(SHOT_DIR / "05-admin-reject-dialog.png"), full_page=True
    )
    with admin.expect_response(
        lambda response: response.url.endswith(
            f"/api/admin/goods/{rejected_id}/reject"
        )
        and response.request.method == "PUT"
    ):
        admin.locator(".el-dialog__footer button").last.click()
    rejected_row.wait_for(state="detached")

    customer.goto(f"{CUSTOMER_BASE}/profile", wait_until="domcontentloaded")
    customer.get_by_text(rejected_title, exact=True).wait_for(state="visible")
    assert customer.get_by_text("已拒绝", exact=True).is_visible()

    forbidden = customer.request.get(
        f"{SERVER_BASE}/api/admin/users",
        headers={"Authorization": customer_token},
    )
    assert forbidden.status == 403, f"expected 403, got {forbidden.status}"

    admin.goto(f"{ADMIN_BASE}/#/admin/users", wait_until="networkidle")
    admin.locator("h1").filter(has_text="用户管理").wait_for(state="visible")
    admin.locator(".el-loading-mask").wait_for(state="hidden")
    user_row = find_row(admin, username)
    admin.screenshot(path=str(SHOT_DIR / "06-admin-users.png"), full_page=True)
    with admin.expect_response(
        lambda response: response.url.endswith("/ban")
        and response.request.method == "PUT"
    ):
        user_row.get_by_role("button", name="封禁", exact=True).click()
        admin.locator(".el-message-box").wait_for(state="visible")
        admin.locator(".el-message-box__btns button").last.click()
    admin.locator(".el-loading-mask").wait_for(state="hidden")
    admin.locator(".el-message-box").wait_for(state="hidden")
    user_row = find_row(admin, username)
    user_row.get_by_text("已封禁", exact=True).wait_for(state="visible")
    admin.wait_for_timeout(500)
    admin.screenshot(
        path=str(SHOT_DIR / "07-admin-user-banned.png"), full_page=True
    )

    banned_login = customer.request.post(
        f"{SERVER_BASE}/api/auth/login",
        data={"username": username, "password": password},
    ).json()
    assert banned_login["code"] == 1004, banned_login

    with admin.expect_response(
        lambda response: response.url.endswith("/unban")
        and response.request.method == "PUT"
    ):
        user_row.get_by_role("button", name="解封", exact=True).click()
        admin.locator(".el-message-box").wait_for(state="visible")
        admin.locator(".el-message-box__btns button").last.click()
    admin.locator(".el-loading-mask").wait_for(state="hidden")
    user_row = find_row(admin, username)
    user_row.get_by_text("正常", exact=True).wait_for(state="visible")

    restored_login = customer.request.post(
        f"{SERVER_BASE}/api/auth/login",
        data={"username": username, "password": password},
    ).json()
    assert restored_login["code"] == 200, restored_login

    browser.close()
    return {
        "ok": True,
        "username": username,
        "approvedGoodsId": approved_id,
        "rejectedGoodsId": rejected_id,
        "customerAdminStatus": forbidden.status,
        "bannedLoginCode": banned_login["code"],
        "restoredLoginCode": restored_login["code"],
    }


def main() -> None:
    SHOT_DIR.mkdir(parents=True, exist_ok=True)
    with sync_playwright() as playwright:
        result = run_flow(playwright)
    (SHOT_DIR / "admin-flow-result.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
