import { defineStore } from "pinia";
import { ref } from "vue";
import { clearUserProfileStorage } from "@/utils/userProfile";
import { useUserStore } from "@/store/user";

export const useAuthStore = defineStore("auth", () => {
  // 从 localStorage 初始化，防止刷新页面 Token 丢失
  const accessToken = ref(localStorage.getItem("accessToken") || "");
  const refreshToken = ref(localStorage.getItem("refreshToken") || "");

  // 登录成功，保存 Token
  function setTokens(access, refresh) {
    accessToken.value = access;
    refreshToken.value = refresh;
    localStorage.setItem("accessToken", access);
    localStorage.setItem("refreshToken", refresh);
  }

  // 退出登录，清理 Token
  function logout(options = {}) {
    const userStore = useUserStore();
    const { redirectToLogin = true } = options;

    accessToken.value = "";
    refreshToken.value = "";
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    clearUserProfileStorage();
    userStore.clearProfile();

    if (redirectToLogin && typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.assign("/login");
    }
  }

  return { accessToken, refreshToken, setTokens, logout };
});
