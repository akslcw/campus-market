import { computed, type CSSProperties } from "vue";
import { useFullscreen } from "@vueuse/core";
import { isAllEmpty } from "@pureadmin/utils";
import Avatar from "@/assets/user.jpg";
import { getConfig } from "@/config";
import { useAppStoreHook } from "@/store/modules/app";
import { useUserStoreHook } from "@/store/modules/user";
import ExitFullscreen from "~icons/ri/fullscreen-exit-fill";
import Fullscreen from "~icons/ri/fullscreen-fill";

export function useNav() {
  const pureApp = useAppStoreHook();
  const { isFullscreen, toggle } = useFullscreen();
  const tooltipEffect = getConfig()?.TooltipEffect ?? "light";

  const getDivStyle = computed((): CSSProperties => ({
    width: "100%",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    overflow: "hidden"
  }));

  const userAvatar = computed(() =>
    isAllEmpty(useUserStoreHook().avatar)
      ? Avatar
      : useUserStoreHook().avatar
  );

  const username = computed(() =>
    isAllEmpty(useUserStoreHook().nickname)
      ? useUserStoreHook().username
      : useUserStoreHook().nickname
  );

  const avatarsStyle = computed(() =>
    username.value ? { marginRight: "10px" } : ""
  );

  const isCollapse = computed(() => !pureApp.getSidebarStatus);
  const device = computed(() => pureApp.getDevice);
  const layout = computed(() => "vertical");
  const title = computed(() => getConfig().Title);

  function logout() {
    useUserStoreHook().logOut();
  }

  function toggleSideBar() {
    pureApp.toggleSideBar();
  }

  function getLogo() {
    return new URL("/logo.svg", import.meta.url).href;
  }

  return {
    title,
    device,
    layout,
    logout,
    isFullscreen,
    Fullscreen,
    ExitFullscreen,
    toggle,
    getDivStyle,
    toggleSideBar,
    getLogo,
    isCollapse,
    pureApp,
    username,
    userAvatar,
    avatarsStyle,
    tooltipEffect
  };
}
