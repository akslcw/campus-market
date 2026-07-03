import vue from "@vitejs/plugin-vue";
import svgLoader from "vite-svg-loader";
import Icons from "unplugin-icons/vite";
import type { PluginOption } from "vite";
import tailwindcss from "@tailwindcss/vite";
import { configCompressPlugin } from "./compress";
import removeConsole from "vite-plugin-remove-console";

export function getPluginsList(
  VITE_COMPRESSION: ViteCompression
): PluginOption[] {
  return [
    tailwindcss(),
    vue(),
    svgLoader(),
    Icons({
      compiler: "vue3",
      scale: 1
    }),
    configCompressPlugin(VITE_COMPRESSION),
    removeConsole()
  ];
}
