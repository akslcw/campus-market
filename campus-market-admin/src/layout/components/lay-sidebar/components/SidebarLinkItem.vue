<script setup lang="ts">
import { computed } from "vue";
import { isUrl } from "@pureadmin/utils";
import { menuType } from "@/layout/types";

const props = defineProps<{
  to: menuType | string;
}>();

const isExternalLink = computed(() => isUrl(typeof props.to === "string" ? props.to : props.to.name));
const getLinkProps = (item: menuType | string) => {
  if (isExternalLink.value) {
    return {
      href: typeof item === "string" ? item : item.name,
      target: "_blank",
      rel: "noopener"
    };
  }
  return {
    to: item
  };
};
</script>

<template>
  <component
    :is="isExternalLink ? 'a' : 'router-link'"
    v-bind="getLinkProps(to)"
  >
    <slot />
  </component>
</template>
