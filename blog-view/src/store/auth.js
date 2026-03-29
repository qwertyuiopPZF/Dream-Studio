import { defineStore } from "pinia";
import { ref } from "vue";
import { clearUserProfileStorage } from "@/utils/userProfile";
import { useUserStore } from "@/store/user";

const LOGIN_SESSION_STORAGE_KEY = "loginSessionId";

const createLoginSessionId = () => `login-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

export const useAuthStore = defineStore("auth", () => {
  // 从 localStorage 初始化，防止刷新页面 Token 丢失
  const accessToken = ref(localStorage.getItem("accessToken") || "");
  const refreshToken = ref(localStorage.getItem("refreshToken") || "");
  const cachedLoginSessionId = localStorage.getItem(LOGIN_SESSION_STORAGE_KEY) || "";
  const loginSessionId = ref(cachedLoginSessionId || (accessToken.value ? createLoginSessionId() : ""));

  if (loginSessionId.value) {
    localStorage.setItem(LOGIN_SESSION_STORAGE_KEY, loginSessionId.value);
  }

  // 登录成功，保存 Token
  function setTokens(access, refresh, options = {}) {
    const { markLogin = true } = options;

    accessToken.value = access;
    refreshToken.value = refresh;
    localStorage.setItem("accessToken", access);
    localStorage.setItem("refreshToken", refresh);

    if (markLogin) {
      loginSessionId.value = createLoginSessionId();
      localStorage.setItem(LOGIN_SESSION_STORAGE_KEY, loginSessionId.value);
    } else if (!loginSessionId.value && access) {
      loginSessionId.value = localStorage.getItem(LOGIN_SESSION_STORAGE_KEY) || createLoginSessionId();
      localStorage.setItem(LOGIN_SESSION_STORAGE_KEY, loginSessionId.value);
    }
  }

  // 退出登录，清理 Token
  function logout(options = {}) {
    const userStore = useUserStore();
    const { redirectToLogin = true } = options;

    accessToken.value = "";
    refreshToken.value = "";
    loginSessionId.value = "";
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem(LOGIN_SESSION_STORAGE_KEY);
    clearUserProfileStorage();
    userStore.clearProfile();

    if (redirectToLogin && typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.assign("/login");
    }
  }

  return { accessToken, refreshToken, loginSessionId, setTokens, logout };
});
