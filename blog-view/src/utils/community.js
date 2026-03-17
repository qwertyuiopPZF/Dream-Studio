const COMMUNITY_KEYS = {
  forumPosts: 'community_forum_posts',
  forumComments: 'community_forum_comments',
  forumReports: 'community_forum_reports',
  notifications: 'community_notifications',
}

const readList = (key) => {
  try {
    const value = localStorage.getItem(key)
    return value ? JSON.parse(value) : []
  } catch (error) {
    console.error(`读取 ${key} 失败`, error)
    return []
  }
}

const writeList = (key, list) => {
  localStorage.setItem(key, JSON.stringify(list))
}

const upsertById = (list, record) => {
  const nextList = [...list]
  const index = nextList.findIndex((item) => item.id === record.id)
  if (index >= 0) {
    nextList[index] = { ...nextList[index], ...record }
  } else {
    nextList.unshift(record)
  }
  return nextList
}

export const getCommunityForumPosts = () => readList(COMMUNITY_KEYS.forumPosts)

export const saveCommunityForumPost = (post) => {
  const nextList = upsertById(getCommunityForumPosts(), post)
  writeList(COMMUNITY_KEYS.forumPosts, nextList)
  return nextList
}

export const getCommunityComments = () => readList(COMMUNITY_KEYS.forumComments)

export const saveCommunityComment = (comment) => {
  const currentList = getCommunityComments()
  currentList.unshift({ id: Date.now(), ...comment })
  writeList(COMMUNITY_KEYS.forumComments, currentList)
  return currentList
}

export const getCommunityReports = () => readList(COMMUNITY_KEYS.forumReports)

export const saveCommunityReport = (report) => {
  const currentList = getCommunityReports()
  const reportRecord = {
    id: `report-${Date.now()}`,
    status: 'pending',
    createTime: new Date().toISOString(),
    ...report,
  }
  currentList.unshift(reportRecord)
  writeList(COMMUNITY_KEYS.forumReports, currentList)
  return reportRecord
}

export const getCommunityNotifications = () => readList(COMMUNITY_KEYS.notifications)

export const pushCommunityNotification = (notification) => {
  const currentList = getCommunityNotifications()
  currentList.unshift({
    id: `notification-${Date.now()}`,
    read: false,
    createTime: new Date().toISOString(),
    ...notification,
  })
  writeList(COMMUNITY_KEYS.notifications, currentList)
  return currentList
}

export const markNotificationAsRead = (notificationId) => {
  const currentList = getCommunityNotifications().map((item) =>
    item.id === notificationId ? { ...item, read: true } : item,
  )
  writeList(COMMUNITY_KEYS.notifications, currentList)
  return currentList
}
