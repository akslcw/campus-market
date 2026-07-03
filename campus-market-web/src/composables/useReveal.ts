import { onMounted, onUnmounted } from 'vue'

/**
 * 滚动渐显动画 Composable
 * 自动为带有 .reveal 类的元素添加 Intersection Observer
 * 元素进入视口时自动添加 .revealed 类触发动画
 */
export function useReveal(options?: IntersectionObserverInit) {
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed')
            // 可选：动画触发后停止观察，避免重复触发
            observer?.unobserve(entry.target)
          }
        })
      },
      {
        root: null,
        rootMargin: '0px 0px -40px 0px',
        threshold: 0.1,
        ...options,
      }
    )

    // 观察所有 .reveal 元素
    document.querySelectorAll('.reveal').forEach((el) => {
      observer?.observe(el)
    })
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  /**
   * 手动刷新观察列表（用于动态加载内容后）
   */
  const isInViewport = (el: Element): boolean => {
    const rect = el.getBoundingClientRect()
    const viewHeight = window.innerHeight || document.documentElement.clientHeight
    const viewWidth = window.innerWidth || document.documentElement.clientWidth
    return (
      rect.top >= -rect.height * 0.2 &&
      rect.bottom <= viewHeight + rect.height * 0.2 &&
      rect.left >= -rect.width * 0.2 &&
      rect.right <= viewWidth + rect.width * 0.2
    )
  }

  const refresh = () => {
    document.querySelectorAll('.reveal:not(.revealed)').forEach((el) => {
      if (isInViewport(el)) {
        el.classList.add('revealed')
      } else {
        observer?.observe(el)
      }
    })
  }

  return { refresh }
}
