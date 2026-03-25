package blog.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils
{
    private IpUtils()
    {
    }

    public static String getClientIpAddress(HttpServletRequest request)
    {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIP = request.getHeader("X-Real-IP");
        String remoteAddr = request.getRemoteAddr();

        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
