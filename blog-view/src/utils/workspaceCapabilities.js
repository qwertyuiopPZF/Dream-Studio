export const WORKSPACE_CAPABILITIES = {
  PROFILE_OVERVIEW_VIEW: 'profile.overview.view',
  PROFILE_NOTIFICATIONS_VIEW: 'profile.notifications.view',
  PROFILE_ACCOUNT_EDIT: 'profile.account.edit',
  ARTICLE_MANAGE_OWN: 'article.manage.own',
  ARTICLE_MANAGE_ALL: 'article.manage.all',
  ARTICLE_WRITE: 'article.write',
  MOMENT_MANAGE_OWN: 'moment.manage.own',
  MOMENT_MANAGE_ALL: 'moment.manage.all',
  MOMENT_WRITE: 'moment.write',
  FORUM_POST_WRITE: 'forum.post.write',
  FORUM_POST_MANAGE_OWN: 'forum.post.manage.own',
  FORUM_POST_MODERATE: 'forum.post.moderate',
  COMMENT_MODERATE: 'comment.moderate',
  TAXONOMY_MANAGE: 'taxonomy.manage',
  USER_MANAGE: 'user.manage',
  SITE_MANAGE: 'site.manage',
  DASHBOARD_VIEW: 'dashboard.view',
  REPORT_REVIEW: 'report.review',
}

const USER_CAPABILITIES = [
  WORKSPACE_CAPABILITIES.PROFILE_OVERVIEW_VIEW,
  WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
  WORKSPACE_CAPABILITIES.PROFILE_ACCOUNT_EDIT,
  WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_OWN,
  WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
  WORKSPACE_CAPABILITIES.MOMENT_MANAGE_OWN,
  WORKSPACE_CAPABILITIES.MOMENT_WRITE,
  WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
  WORKSPACE_CAPABILITIES.FORUM_POST_MANAGE_OWN,
]

const ADMIN_CAPABILITIES = [
  ...USER_CAPABILITIES,
  WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_ALL,
  WORKSPACE_CAPABILITIES.MOMENT_MANAGE_ALL,
  WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE,
  WORKSPACE_CAPABILITIES.COMMENT_MODERATE,
  WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
  WORKSPACE_CAPABILITIES.USER_MANAGE,
  WORKSPACE_CAPABILITIES.SITE_MANAGE,
  WORKSPACE_CAPABILITIES.DASHBOARD_VIEW,
  WORKSPACE_CAPABILITIES.REPORT_REVIEW,
]

const ROLE_CAPABILITY_MAP = {
  USER: USER_CAPABILITIES,
  ADMIN: ADMIN_CAPABILITIES,
}

export const resolveCapabilitiesByRole = (role = '') => {
  const normalizedRole = String(role || '').toUpperCase()
  return [...(ROLE_CAPABILITY_MAP[normalizedRole] || [])]
}

export const hasRequiredCapability = (capabilities = [], requiredCapability) => {
  if (!requiredCapability) return true

  const capabilitySet = capabilities instanceof Set ? capabilities : new Set(capabilities)
  const requiredList = Array.isArray(requiredCapability) ? requiredCapability : [requiredCapability]

  return requiredList.every((item) => capabilitySet.has(item))
}
