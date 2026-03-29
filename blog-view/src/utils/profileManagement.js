export const formatManagementTime = (value) => {
  if (!value) return '--'

  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export const formatManagementTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  if (typeof tags === 'string') {
    return tags
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

export const getCommentTargetLabel = (comment) => {
  if (comment.blogId) {
    return comment.title ? `文章 · ${comment.title}` : `文章 #${comment.blogId}`
  }

  if (comment.page === 'friends') return '论坛页'
  if (comment.page?.startsWith('forum-post-')) {
    return `论坛帖子 #${comment.page.replace('forum-post-', '')}`
  }

  return comment.page || '站内页面'
}

export const parseManagementMomentImages = (imageValue, uploadBaseUrl) => {
  if (!imageValue) return []

  return String(imageValue)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => (item.startsWith('http') ? item : `${uploadBaseUrl}${item}`))
}
