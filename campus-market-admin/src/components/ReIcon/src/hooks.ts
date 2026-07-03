import type { iconType } from "./types";
import { h, defineComponent, type Component } from "vue";
import { IconifyIconOnline, IconifyIconOffline } from "../index";

export function useRenderIcon(icon: any, attrs?: iconType): Component {
  if (typeof icon === "function" || typeof icon?.render === "function") {
    return attrs ? h(icon, { ...attrs }) : icon;
  }

  if (typeof icon === "object") {
    return defineComponent({
      name: "OfflineIcon",
      render() {
        return h(IconifyIconOffline, { icon, ...attrs });
      }
    });
  }

  return defineComponent({
    name: "Icon",
    render() {
      if (!icon) return;
      const IconifyIcon = icon.includes(":")
        ? IconifyIconOnline
        : IconifyIconOffline;
      return h(IconifyIcon, { icon, ...attrs });
    }
  });
}
