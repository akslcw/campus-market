/**
 * 轻量级动态组件库
 * 提供各种低负载动画效果，不影响页面性能
 */

import { ref, onMounted, onUnmounted } from 'vue'
import type { Ref } from 'vue'

// ========== 滚动视差效果 ==========
export function useParallax() {
  const scrollY = ref(0)
  const parallaxElements = ref<HTMLElement[]>([])

  const handleScroll = () => {
    scrollY.value = window.scrollY

    parallaxElements.value.forEach((el) => {
      const speed = parseFloat(el.dataset.speed || '0.5')
      const yPos = -(scrollY.value * speed)
      el.style.transform = `translateY(${yPos}px)`
    })
  }

  const registerElement = (el: HTMLElement, speed: number = 0.5) => {
    el.dataset.speed = speed.toString()
    parallaxElements.value.push(el)
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    scrollY,
    registerElement
  }
}

// ========== 渐显入场动画 ==========
export function useFadeIn() {
  const observer = ref<IntersectionObserver | null>(null)

  const observeElements = (elements: HTMLElement[], threshold: number = 0.1) => {
    observer.value = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('fade-in-visible')
            // 动画完成后停止观察
            setTimeout(() => {
              observer.value?.unobserve(entry.target)
            }, 1000)
          }
        })
      },
      {
        threshold,
        rootMargin: '0px 0px -50px 0px'
      }
    )

    elements.forEach((el) => observer.value?.observe(el))
  }

  onUnmounted(() => {
    observer.value?.disconnect()
  })

  return {
    observeElements
  }
}

// ========== 悬浮动效 ==========
export function useHoverEffect() {
  const addHoverEffect = (element: HTMLElement, options: {
    scale?: number
    translateY?: number
    shadow?: boolean
    duration?: number
  } = {}) => {
    const {
      scale = 1.05,
      translateY = -4,
      shadow = true,
      duration = 300
    } = options

    element.style.transition = `transform ${duration}ms cubic-bezier(0.22, 1, 0.36, 1),
                              box-shadow ${duration}ms cubic-bezier(0.22, 1, 0.36, 1)`

    element.addEventListener('mouseenter', () => {
      element.style.transform = `translateY(${translateY}px) scale(${scale})`
      if (shadow) {
        element.style.boxShadow = '0 12px 32px rgba(0, 0, 0, 0.15)'
      }
    })

    element.addEventListener('mouseleave', () => {
      element.style.transform = 'translateY(0) scale(1)'
      if (shadow) {
        element.style.boxShadow = ''
      }
    })
  }

  return {
    addHoverEffect
  }
}

// ========== 微交互反馈 ==========
export function useMicroInteraction() {
  const createRipple = (event: MouseEvent, element: HTMLElement) => {
    const circle = document.createElement('span')
    const diameter = Math.max(element.clientWidth, element.clientHeight)
    const radius = diameter / 2

    const rect = element.getBoundingClientRect()
    circle.style.width = circle.style.height = `${diameter}px`
    circle.style.left = `${event.clientX - rect.left - radius}px`
    circle.style.top = `${event.clientY - rect.top - radius}px`
    circle.classList.add('ripple-effect')

    const ripple = element.getElementsByClassName('ripple-effect')[0]
    if (ripple) {
      ripple.remove()
    }

    element.appendChild(circle)
  }

  const addClickFeedback = (element: HTMLElement, callback?: () => void) => {
    element.addEventListener('click', (e) => {
      createRipple(e, element)
      element.style.transform = 'scale(0.95)'
      setTimeout(() => {
        element.style.transform = ''
        callback?.()
      }, 150)
    })
  }

  return {
    createRipple,
    addClickFeedback
  }
}

// ========== 滚动触发动画 ==========
export function useScrollTrigger() {
  const triggers = ref<Map<HTMLElement, (() => void)>>(new Map())

  const handleScroll = () => {
    const scrollY = window.scrollY
    const windowHeight = window.innerHeight

    triggers.value.forEach((callback, element) => {
      const rect = element.getBoundingClientRect()
      const elementTop = rect.top + scrollY
      const triggerPoint = elementTop - windowHeight * 0.8

      if (scrollY >= triggerPoint) {
        callback()
        triggers.value.delete(element)
      }
    })
  }

  const registerTrigger = (element: HTMLElement, callback: () => void) => {
    triggers.value.set(element, callback)
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    registerTrigger
  }
}

// ========== 数字滚动动画 ==========
export function useNumberAnimation() {
  const animateNumber = (
    element: HTMLElement,
    target: number,
    duration: number = 1000,
    formatFn?: (num: number) => string
  ) => {
    const start = 0
    const startTime = performance.now()

    const updateNumber = (currentTime: number) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)

      // 使用缓动函数
      const easeOutQuart = 1 - Math.pow(1 - progress, 4)
      const current = Math.floor(start + (target - start) * easeOutQuart)

      element.textContent = formatFn ? formatFn(current) : current.toString()

      if (progress < 1) {
        requestAnimationFrame(updateNumber)
      }
    }

    requestAnimationFrame(updateNumber)
  }

  return {
    animateNumber
  }
}

// ========== 卡片3D倾斜效果 ==========
export function useCardTilt() {
  const addTiltEffect = (element: HTMLElement, intensity: number = 10) => {
    element.style.transformStyle = 'preserve-3d'
    element.style.transition = 'transform 0.1s ease-out'

    element.addEventListener('mousemove', (e) => {
      const rect = element.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      const centerX = rect.width / 2
      const centerY = rect.height / 2

      const rotateX = ((y - centerY) / centerY) * -intensity
      const rotateY = ((x - centerX) / centerX) * intensity

      element.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`
    })

    element.addEventListener('mouseleave', () => {
      element.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale3d(1, 1, 1)'
    })
  }

  return {
    addTiltEffect
  }
}

// ========== 加载动画 ==========
export function useLoadingAnimation() {
  const createSkeletonLoader = (element: HTMLElement, lines: number = 3) => {
    element.innerHTML = ''
    element.classList.add('skeleton-loader')

    for (let i = 0; i < lines; i++) {
      const line = document.createElement('div')
      line.className = 'skeleton-line'
      line.style.width = `${Math.random() * 40 + 60}%`
      element.appendChild(line)
    }
  }

  const removeSkeletonLoader = (element: HTMLElement) => {
    element.classList.remove('skeleton-loader')
    const skeletonLines = element.querySelectorAll('.skeleton-line')
    skeletonLines.forEach(line => line.remove())
  }

  return {
    createSkeletonLoader,
    removeSkeletonLoader
  }
}

// ========== 磁性按钮效果 ==========
export function useMagneticButton() {
  const addMagneticEffect = (element: HTMLElement, strength: number = 0.5) => {
    element.style.transition = 'transform 0.3s cubic-bezier(0.22, 1, 0.36, 1)'

    element.addEventListener('mousemove', (e) => {
      const rect = element.getBoundingClientRect()
      const x = e.clientX - rect.left - rect.width / 2
      const y = e.clientY - rect.top - rect.height / 2

      element.style.transform = `translate(${x * strength}px, ${y * strength}px)`
    })

    element.addEventListener('mouseleave', () => {
      element.style.transform = 'translate(0, 0)'
    })
  }

  return {
    addMagneticEffect
  }
}