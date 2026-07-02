import { ElMessage } from 'element-plus'

const MAX_SIZE = 5 * 1024 * 1024 // 5MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/jpg']

/**
 * 校验图片文件：≤5MB 且为 jpg/png
 * @returns true if valid, false otherwise (已 toast 提示)
 */
export function validateImage(file: File): boolean {
  if (!ALLOWED_TYPES.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG 格式的图片')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning(`图片大小不能超过 ${MAX_SIZE / 1024 / 1024}MB`)
    return false
  }
  return true
}

export { ALLOWED_TYPES, MAX_SIZE }
