const express = require("express");
const axios = require("axios");
const cors = require("cors");
const morgan = require("morgan");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const PORT = process.env.PORT || 3001;
const BACKEND_BASE = process.env.BACKEND_BASE || "http://localhost:8080";

const likesFile = path.join(__dirname, "..", "data", "likes.json");

function ensureLikesFile() {
  try {
    const dir = path.dirname(likesFile);
    fs.mkdirSync(dir, { recursive: true });
    if (!fs.existsSync(likesFile)) {
      fs.writeFileSync(likesFile, JSON.stringify({ articleLikes: {} }, null, 2));
    }
  } catch (err) {
    console.error("初始化点赞存储文件失败", err);
  }
}

function loadLikes() {
  ensureLikesFile();
  try {
    const raw = fs.readFileSync(likesFile, "utf-8");
    return JSON.parse(raw || "{}");
  } catch (err) {
    console.error("读取点赞存储失败，使用空对象", err);
    return { articleLikes: {} };
  }
}

function saveLikes(data) {
  ensureLikesFile();
  fs.writeFileSync(likesFile, JSON.stringify(data, null, 2));
}

let likeStore = loadLikes();

function decodeUserFromAuth(headerValue) {
  if (!headerValue) return null;
  const parts = headerValue.split(" ");
  if (parts.length !== 2) return null;
  const token = parts[1];
  const segments = token.split(".");
  if (segments.length < 2) return null;
  try {
    const payload = JSON.parse(Buffer.from(segments[1], "base64").toString("utf-8"));
    const userId = payload.id || payload.userId || payload.sub || payload.username;
    const username = payload.username || payload.sub || payload.id;
    return { userId, username };
  } catch {
    return null;
  }
}

function makeResponse(code, message, data) {
  return {
    code,
    message,
    data,
    timestamp: Date.now(),
    requestId: crypto.randomUUID()
  };
}

function errorResponse(res, code, message, data = null) {
  return res.status(code === 200 ? 400 : code).json(makeResponse(code, message, data));
}

function mapArticleToHeadline(article, currentUserId) {
  const likesEntry = likeStore.articleLikes?.[article.id] || { users: [] };
  const likeUsers = new Set(likesEntry.users || []);
  const liked = currentUserId ? likeUsers.has(String(currentUserId)) : false;
  return {
    id: article.id,
    title: article.title,
    summary: article.summary,
    cover: article.coverImage,
    publishTime: article.publishTime,
    author: {
      id: article.authorId,
      nickname: article.authorNickname,
      avatar: article.authorAvatar
    },
    stats: {
      likes: likeUsers.size,
      comments: null, // 需要时可在详情接口补全
      views: article.viewCount,
      liked
    }
  };
}

const app = express();
app.use(cors());
app.use(express.json());
app.use(morgan("dev"));

app.get("/health", (req, res) => {
  res.json(makeResponse(200, "ok", { service: "headlines-bff" }));
});

app.get("/api/headlines", async (req, res) => {
  const { page = 1, pageSize = 10 } = req.query;
  const auth = decodeUserFromAuth(req.headers.authorization);
  try {
    const resp = await axios.get(`${BACKEND_BASE}/api/articles`, {
      params: { page, size: pageSize },
      headers: { Authorization: req.headers.authorization || "" }
    });
    if (resp.data.code !== 1) {
      return errorResponse(res, 502, resp.data.msg || "后端文章接口失败");
    }
    const payload = resp.data.data || {};
    const articles = Array.isArray(payload.data) ? payload.data : [];
    const sorted = [...articles].sort((a, b) => {
      const t1 = a.publishTime || a.createTime || 0;
      const t2 = b.publishTime || b.createTime || 0;
      return t2 > t1 ? 1 : -1;
    });
    const list = sorted.map((a) => mapArticleToHeadline(a, auth?.userId));
    const pagination = payload.pagination || {};
    return res.json(makeResponse(200, "success", { list, pagination }));
  } catch (err) {
    console.error("获取头条列表失败", err.message);
    return errorResponse(res, 500, "获取头条列表失败");
  }
});

app.get("/api/headlines/:id", async (req, res) => {
  const { id } = req.params;
  const includeComments = req.query.includeComments === "true";
  const auth = decodeUserFromAuth(req.headers.authorization);
  try {
    const articleResp = await axios.get(`${BACKEND_BASE}/api/articles/${id}`, {
      headers: { Authorization: req.headers.authorization || "" }
    });
    if (articleResp.data.code !== 1) {
      return errorResponse(res, 404, articleResp.data.msg || "文章不存在");
    }
    const article = articleResp.data.data;
    const headline = mapArticleToHeadline(article, auth?.userId);
    let comments = undefined;
    if (includeComments) {
      const commentResp = await axios.get(`${BACKEND_BASE}/api/comments`, {
        params: { blogId: id },
        headers: { Authorization: req.headers.authorization || "" }
      });
      if (commentResp.data.code === 1) {
        comments = commentResp.data.data || [];
        headline.stats.comments = Array.isArray(comments) ? comments.length : 0;
      }
    }
    return res.json(makeResponse(200, "success", { ...headline, comments }));
  } catch (err) {
    console.error("获取头条详情失败", err.message);
    return errorResponse(res, 500, "获取头条详情失败");
  }
});

app.post("/api/headlines/:id/like", (req, res) => {
  const { id } = req.params;
  const { action = "like" } = req.body || {};
  const auth = decodeUserFromAuth(req.headers.authorization);
  if (!auth?.userId) {
    return errorResponse(res, 401, "请先登录再点赞");
  }
  likeStore.articleLikes = likeStore.articleLikes || {};
  const entry = likeStore.articleLikes[id] || { users: [] };
  const userSet = new Set(entry.users || []);
  if (action === "unlike") {
    userSet.delete(String(auth.userId));
  } else {
    userSet.add(String(auth.userId));
  }
  likeStore.articleLikes[id] = { users: Array.from(userSet) };
  saveLikes(likeStore);
  const liked = userSet.has(String(auth.userId));
  const likes = userSet.size;
  return res.json(makeResponse(200, "success", { likes, liked }));
});

app.get("/api/headlines/:id/comments", async (req, res) => {
  const { id } = req.params;
  try {
    const commentResp = await axios.get(`${BACKEND_BASE}/api/comments`, {
      params: { blogId: id },
      headers: { Authorization: req.headers.authorization || "" }
    });
    if (commentResp.data.code !== 1) {
      return errorResponse(res, 502, commentResp.data.msg || "获取评论失败");
    }
    return res.json(makeResponse(200, "success", { list: commentResp.data.data || [] }));
  } catch (err) {
    console.error("获取评论失败", err.message);
    return errorResponse(res, 500, "获取评论失败");
  }
});

app.post("/api/headlines/:id/comments", async (req, res) => {
  const { id } = req.params;
  const { content } = req.body || {};
  if (!content || !String(content).trim()) {
    return errorResponse(res, 422, "评论内容不能为空");
  }
  try {
    const body = { content: String(content).trim(), blogId: Number(id), page: "blog" };
    const resp = await axios.post(`${BACKEND_BASE}/api/comments/comment`, body, {
      headers: {
        Authorization: req.headers.authorization || "",
        "Content-Type": "application/json"
      }
    });
    if (resp.data.code !== 1) {
      return errorResponse(res, 502, resp.data.msg || "提交评论失败");
    }
    return res.json(makeResponse(200, "success", null));
  } catch (err) {
    console.error("提交评论失败", err.message);
    return errorResponse(res, 500, "提交评论失败");
  }
});

app.listen(PORT, () => {
  console.log(`Headlines BFF is running on http://localhost:${PORT}`);
});
