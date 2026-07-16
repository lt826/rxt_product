import request from './index'

/**
 * 通用图片上传（发布商品、收购需求等场景）
 * @param {File} file 图片文件
 * @returns {Promise} Result<{ code, message, data:url }>
 */
export function uploadImageAPI(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/v1/upload/image',
    method: 'post',
    data: formData,
    timeout: 30000
  })
}
