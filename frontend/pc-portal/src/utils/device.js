/**
 * 设备类型检测工具
 * 根据 User-Agent 判断当前是手机还是电脑，用于切换移动端/电脑端两套界面。
 */

export function isMobileDevice() {
  if (typeof navigator === 'undefined') return false
  const ua = navigator.userAgent || ''
  return /Android|iPhone|iPad|iPod|Mobile|Windows Phone|BlackBerry|Opera Mini|PlayBook/i.test(ua)
}

export function isDesktopDevice() {
  return !isMobileDevice()
}
